package com.example.edutrackapp.cms.feature.auth.presentation

import android.app.Activity
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.core.common.Resource
import com.example.edutrackapp.cms.feature.auth.data.AuthRepository
import com.example.edutrackapp.cms.feature.auth.data.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val firebaseAuth: FirebaseAuth          // needed for sendOtp (not async)
) : ViewModel() {

    // ─── UI States ────────────────────────────────────────────────────────────

    private val _loginState = mutableStateOf<AuthState>(AuthState.Idle)
    val loginState: State<AuthState> = _loginState

    /** Holds the verificationId returned by Firebase after OTP is sent.
     *  Passed to verifyOtp() when the user submits the code. */
    private val _verificationId = mutableStateOf("")
    val verificationId: State<String> = _verificationId

    /** Tracks whether a resend is allowed (after 60 s cooldown) */
    private val _resendAllowed = mutableStateOf(false)
    val resendAllowed: State<Boolean> = _resendAllowed

    /**
     * Countdown in seconds shown on the OTP screen.
     * Driven entirely from the ViewModel so resend and the displayed value
     * are always in sync, even after a resend.
     */
    private val _secondsLeft = mutableStateOf(60)
    val secondsLeft: State<Int> = _secondsLeft

    // ─── Session Check ────────────────────────────────────────────────────────

    fun isLoggedIn(): Boolean = repository.getCurrentUser() != null

    // ─── Email & Password ─────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        if (!validateEmail(email) || !validatePassword(password)) return

        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            _loginState.value = when (val result = repository.login(email, password)) {
                is Resource.Success -> AuthState.Success(result.data!!)
                is Resource.Error   -> AuthState.Error(result.message ?: "Unknown error")
                else                -> AuthState.Loading   // Resource.Loading is unused here
            }
        }
    }

    // ─── Google Sign-In ───────────────────────────────────────────────────────

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            _loginState.value = when (val result = repository.loginWithGoogle(idToken)) {
                is Resource.Success -> AuthState.Success(result.data!!)
                is Resource.Error   -> AuthState.Error(result.message ?: "Google login failed")
                else                -> AuthState.Loading
            }
        }
    }

    // ─── Phone OTP — Step 1: Send OTP ────────────────────────────────────────

    /**
     * Triggers Firebase to send an OTP SMS to [phoneNumber].
     * phoneNumber must include country code, e.g. "+919876543210"
     *
     * On success         → state becomes AuthState.OtpSent
     * On failure         → state becomes AuthState.Error
     * On auto-retrieve   → signs in immediately, state becomes AuthState.Success
     */
    fun sendOtp(phoneNumber: String, activity: Activity) {
        if (!validatePhone(phoneNumber)) return

        _loginState.value = AuthState.Loading
        _resendAllowed.value = false
        startCountdown()                    // reset + start the shared countdown

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // ── Auto-retrieval / instant verification ─────────────────────────
            // Firebase has already signed the user in at this point.
            // We must NOT call signInWithCredential again — just fetch the role.
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch {
                    _loginState.value = AuthState.Loading
                    try {
                        // Firebase signs in automatically; await it cleanly.
                        firebaseAuth.signInWithCredential(credential).await()
                        // Now only fetch the Firestore profile — no second sign-in.
                        _loginState.value = when (val result = repository.fetchCurrentUserRole()) {
                            is Resource.Success -> AuthState.Success(result.data!!)
                            is Resource.Error   -> AuthState.Error(result.message ?: "Error")
                            else                -> AuthState.Loading
                        }
                    } catch (e: Exception) {
                        _loginState.value = AuthState.Error(
                            e.message ?: "Auto-verification failed"
                        )
                    }
                }
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                _resendAllowed.value = true
                _loginState.value = AuthState.Error(
                    when {
                        e.message?.contains("quota") == true ->
                            "SMS quota exceeded. Try again later."
                        e.message?.contains("invalid") == true ->
                            "Invalid phone number format."
                        else -> e.message ?: "Failed to send OTP."
                    }
                )
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _verificationId.value = verificationId
                _loginState.value = AuthState.OtpSent
                // Countdown is already running from startCountdown() above.
            }
        }

        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
        )
    }

    // ─── Phone OTP — Step 2: Verify OTP ──────────────────────────────────────

    fun verifyOtp(otp: String) {
        if (otp.length != 6) {
            _loginState.value = AuthState.Error("OTP must be 6 digits.")
            return
        }
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            _loginState.value = when (
                val result = repository.verifyOtp(_verificationId.value, otp)
            ) {
                is Resource.Success -> AuthState.Success(result.data!!)
                is Resource.Error   -> AuthState.Error(result.message ?: "Verification failed")
                else                -> AuthState.Loading
            }
        }
    }

    // ─── Countdown timer ──────────────────────────────────────────────────────

    /**
     * Resets the countdown to 60 s and counts down to 0,
     * then flips [resendAllowed] to true.
     * Safe to call multiple times (each call cancels the previous coroutine
     * because it runs in viewModelScope which is cancelled on logout/reset).
     */
    private fun startCountdown() {
        viewModelScope.launch {
            _secondsLeft.value = 60
            _resendAllowed.value = false
            for (i in 60 downTo 0) {
                _secondsLeft.value = i
                if (i > 0) kotlinx.coroutines.delay(1_000L)
            }
            _resendAllowed.value = true
        }
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    fun logout() {
        repository.logout()
        _loginState.value = AuthState.Idle
        _verificationId.value = ""
        _resendAllowed.value = false
        _secondsLeft.value = 60
    }

    // ─── Reset (e.g. when user navigates back) ────────────────────────────────

    fun resetState() {
        _loginState.value = AuthState.Idle
    }

    // ─── Input Validation ─────────────────────────────────────────────────────

    private fun validateEmail(email: String): Boolean {
        return if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.value = AuthState.Error("Please enter a valid email address.")
            false
        } else true
    }

    private fun validatePassword(password: String): Boolean {
        return if (password.length < 6) {
            _loginState.value = AuthState.Error("Password must be at least 6 characters.")
            false
        } else true
    }

    private fun validatePhone(phone: String): Boolean {
        return if (!phone.startsWith("+") || phone.length < 10) {
            _loginState.value = AuthState.Error(
                "Enter phone with country code e.g. +919876543210"
            )
            false
        } else true
    }
}

// ─── Auth UI States ───────────────────────────────────────────────────────────

sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()                        // OTP sent, show OTP screen
    data class Success(val user: UserModel) : AuthState()
    data class Error(val message: String)  : AuthState()
}
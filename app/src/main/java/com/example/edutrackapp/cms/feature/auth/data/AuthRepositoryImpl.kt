package com.example.edutrackapp.cms.feature.auth.data

import com.example.edutrackapp.cms.core.common.Resource
import com.example.edutrackapp.cms.feature.auth.data.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    // ─── Email & Password ─────────────────────────────────────────────────────
    override suspend fun login(email: String, password: String): Resource<UserModel> {
        return try {
            firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()
            fetchUserRole()
        } catch (e: Exception) {
            Resource.Error(
                when {
                    e.message?.contains("no user record") == true ->
                        "No account found with this email."
                    e.message?.contains("password is invalid") == true ->
                        "Incorrect password. Please try again."
                    e.message?.contains("network") == true ->
                        "Network error. Check your internet connection."
                    else -> e.message ?: "Login failed. Please try again."
                }
            )
        }
    }

    // ─── Google Sign-In ───────────────────────────────────────────────────────
    override suspend fun loginWithGoogle(idToken: String): Resource<UserModel> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth
                .signInWithCredential(credential)
                .await()
            fetchUserRole()
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Google Sign-In failed. Please try again.")
        }
    }

    // ─── Phone OTP Verification ───────────────────────────────────────────────
    override suspend fun verifyOtp(
        verificationId: String,
        otp: String
    ): Resource<UserModel> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            firebaseAuth
                .signInWithCredential(credential)
                .await()
            fetchUserRole()
        } catch (e: Exception) {
            Resource.Error(
                when {
                    e.message?.contains("invalid") == true ->
                        "Invalid OTP. Please check and try again."
                    e.message?.contains("expired") == true ->
                        "OTP has expired. Please request a new one."
                    else -> e.message ?: "OTP verification failed."
                }
            )
        }
    }

    // ─── Fetch role for already-signed-in user (auto-verification path) ───────
    /**
     * Called after Firebase auto-verifies the phone credential in
     * onVerificationCompleted — at that point Firebase has already signed the
     * user in, so we only need to read the Firestore profile.
     */
    override suspend fun fetchCurrentUserRole(): Resource<UserModel> = fetchUserRole()

    // ─── Fetch Role from Firestore ────────────────────────────────────────────
    /**
     * Called after any successful Firebase sign-in.
     * Reads the /users/{uid} document to get the user's role and profile.
     */
    private suspend fun fetchUserRole(): Resource<UserModel> {
        val uid = firebaseAuth.currentUser?.uid
            ?: return Resource.Error("Authentication succeeded but user ID is missing.")

        return try {
            val document = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()

            if (!document.exists()) {
                // User signed in via Firebase but has no Firestore profile yet.
                // This can happen if admin hasn't created the user record.
                firebaseAuth.signOut()
                return Resource.Error(
                    "Your account is not registered in the system. Please contact admin."
                )
            }

            val user = document.toObject(UserModel::class.java)
                ?: return Resource.Error("Failed to read user profile. Please contact admin.")

            // Attach uid (not stored as a field in Firestore, comes from document ID)
            Resource.Success(user.copy(uid = uid))

        } catch (e: Exception) {
            Resource.Error("Failed to fetch user profile: ${e.message}")
        }
    }

    // ─── Session Helpers ──────────────────────────────────────────────────────
    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    override fun logout() = firebaseAuth.signOut()
}
package com.example.edutrackapp.cms.feature.auth.data

import com.example.edutrackapp.cms.core.common.Resource
import com.example.edutrackapp.cms.feature.auth.data.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    /** Sign in with email and password */
    suspend fun login(email: String, password: String): Resource<UserModel>

    /** Sign in with a Google ID token obtained from Credential Manager */
    suspend fun loginWithGoogle(idToken: String): Resource<UserModel>

    /** Verify a phone OTP using the verificationId returned by Firebase */
    suspend fun verifyOtp(verificationId: String, otp: String): Resource<UserModel>

    /**
     * Fetches the Firestore role/profile for whoever is currently signed in to Firebase.
     * Used after auto-verification (onVerificationCompleted) where Firebase signs the
     * user in directly — bypassing verifyOtp — so we only need the Firestore fetch.
     */
    suspend fun fetchCurrentUserRole(): Resource<UserModel>

    /** Returns the currently signed-in Firebase user, or null if not signed in */
    fun getCurrentUser(): FirebaseUser?

    /** Signs the current user out of Firebase */
    fun logout()
}
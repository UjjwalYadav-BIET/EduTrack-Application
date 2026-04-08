package com.example.edutrackapp.cms.feature.auth.data.model

import com.google.firebase.Timestamp

/**
 * Mirrors the Firestore document at /users/{uid}
 *
 * Firestore structure:
 * users/
 *   {uid}/
 *     name         : String
 *     email        : String
 *     phone        : String
 *     role         : "teacher" | "student" | "admin"
 *     department   : String
 *     employeeId   : String   (teachers only)
 *     enrollmentId : String   (students only)
 *     subject      : String   (teachers only)
 *     createdAt    : Timestamp
 */
data class UserModel(
    val uid          : String    = "",
    val name         : String    = "",
    val email        : String    = "",
    val phone        : String    = "",
    val role         : String    = "",       // "teacher" | "student" | "admin"
    val department   : String    = "",
    val employeeId   : String    = "",       // teachers only
    val enrollmentId : String    = "",       // students only
    val subject      : String    = "",       // teachers only
    val createdAt    : Timestamp? = null
)
package com.example.edutrackapp.cms.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val password: String, // <--- ADD THIS LINE
    val role: String      // "TEACHER", "STUDENT", "ADMIN"
)
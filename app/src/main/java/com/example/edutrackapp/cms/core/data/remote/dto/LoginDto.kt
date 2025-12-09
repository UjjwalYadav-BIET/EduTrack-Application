package com.example.edutrackapp.cms.core.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class LoginResponseDto(
    val token: String,
    val userId: String,
    val name: String,
    val role: String // "STUDENT", "TEACHER", "ADMIN"
)
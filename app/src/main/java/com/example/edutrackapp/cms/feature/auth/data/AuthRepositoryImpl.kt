package com.example.edutrackapp.cms.feature.auth.data

import com.example.edutrackapp.cms.core.common.Resource
import com.example.edutrackapp.cms.core.data.remote.dto.LoginResponseDto
import kotlinx.coroutines.delay
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor() {

    // Simulating an API call
    suspend fun login(email: String, password: String): Resource<LoginResponseDto> {
        delay(1500) // Simulate network delay (1.5 seconds)

        return if (email.isNotEmpty() && password.isNotEmpty()) {
            val role = when {
                email.contains("teacher") -> "TEACHER"
                email.contains("admin") -> "ADMIN"
                else -> "STUDENT"
            }

            Resource.Success(
                LoginResponseDto(
                    token = "mock_token_123",
                    userId = "user_001",
                    name = "Test User",
                    role = role
                )
            )
        } else {
            Resource.Error("Email and Password cannot be empty")
        }
    }
}
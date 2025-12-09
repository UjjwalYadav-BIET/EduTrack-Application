package com.example.edutrackapp.cms.feature.auth.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.core.common.Resource
import com.example.edutrackapp.cms.feature.auth.data.AuthRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepositoryImpl
) : ViewModel() {

    private val _loginState = mutableStateOf<AuthState>(AuthState.Idle)
    val loginState: State<AuthState> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading

            val result = repository.login(email, password)

            _loginState.value = when (result) {
                is Resource.Success -> AuthState.Success(result.data!!.role)
                is Resource.Error -> AuthState.Error(result.message ?: "Unknown Error")
                is Resource.Loading -> AuthState.Loading
            }
        }
    }
}

// Simple State class for the UI
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
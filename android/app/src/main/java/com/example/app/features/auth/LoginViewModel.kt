package com.example.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.auth.AuthResult
import com.example.app.data.auth.AuthService
import com.example.app.data.auth.LoginRequest
import com.example.app.data.auth.MockAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

class LoginViewModel(
    private val authService: AuthService = MockAuthService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val normalizedEmail = email.trim()

        if (normalizedEmail.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Completá email y contraseña")
            }
            return
        }

        if (!normalizedEmail.contains("@")) {
            _uiState.update {
                it.copy(errorMessage = "Ingresá un email válido")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            val result = try {
                authService.login(
                    LoginRequest(
                        email = normalizedEmail,
                        password = password
                    )
                )
            } catch (exception: Exception) {
                AuthResult.Failure("No se pudo contactar al servicio")
            }

            _uiState.update {
                when (result) {
                    AuthResult.Success -> it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        errorMessage = null
                    )

                    is AuthResult.Failure -> it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}
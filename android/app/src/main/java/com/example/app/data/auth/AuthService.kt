package com.example.app.data.auth

data class LoginRequest(
    val email: String,
    val password: String
)

sealed interface AuthResult {
    data object Success : AuthResult

    data class Failure(
        val message: String
    ) : AuthResult
}

interface AuthService {
    suspend fun login(request: LoginRequest): AuthResult
}
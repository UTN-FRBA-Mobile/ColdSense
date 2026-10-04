package com.example.app.data.auth

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

//Implementacion mockeada para no tener que andar levantando otro servicio. La idea es mas adelante ir con un servicio posta.
class MockAuthService : AuthService {

    override suspend fun login(request: LoginRequest): AuthResult {
        // Simulamos el tiempo de respuesta de un servidor.
        delay(500.milliseconds)

        val validEmail = "demo@coldsense.com"
        val validPassword = "123456"

        return if (
            request.email.trim() == validEmail && // Le saco los espacios y comparo
            request.password == validPassword
        ) {
            AuthResult.Success
        } else {
            AuthResult.Failure("El email o la contraseña son incorrectos")
        }
    }
}
package ar.edu.utn.coldsense.data.auth

import androidx.annotation.StringRes

data class AuthCredentials(
    val email: String,
    val password: String
)

sealed interface AuthResult {
    data object Success : AuthResult

    data class Failure(
        @StringRes val message: Int
    ) : AuthResult
}

/** Error de login con un mensaje para mostrar (recurso de strings.xml). */
class AuthError(@StringRes val mensaje: Int) : Exception()

interface AuthService {
    val isSignedIn: Boolean

    suspend fun login(credentials: AuthCredentials): AuthResult
    suspend fun register(credentials: AuthCredentials): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signOut()
}

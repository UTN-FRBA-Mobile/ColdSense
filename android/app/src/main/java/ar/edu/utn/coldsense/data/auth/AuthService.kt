package ar.edu.utn.coldsense.data.auth

data class AuthCredentials(
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
    val isSignedIn: Boolean

    suspend fun login(credentials: AuthCredentials): AuthResult
    suspend fun register(credentials: AuthCredentials): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signOut()
}

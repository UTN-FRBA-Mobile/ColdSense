package ar.edu.utn.coldsense.features.auth

import android.util.Patterns
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ar.edu.utn.coldsense.data.auth.AuthCredentials
import ar.edu.utn.coldsense.data.auth.AuthResult
import ar.edu.utn.coldsense.data.auth.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

class LoginViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(isAuthenticated = authService.isSignedIn)
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        submit(email, password, isRegistration = false)
    }

    fun register(email: String, password: String) {
        submit(email, password, isRegistration = true)
    }

    fun signInWithGoogle(getIdToken: suspend () -> String?) {
        executeAuth {
            val idToken = getIdToken() ?: return@executeAuth null
            authService.signInWithGoogle(idToken)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun submit(email: String, password: String, isRegistration: Boolean) {
        if (_uiState.value.isLoading) return

        val normalizedEmail = email.trim()
        val validationError = when {
            normalizedEmail.isBlank() || password.isBlank() ->
                "Completá email y contraseña"
            !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() ->
                "Ingresá un email válido"
            isRegistration && password.length < 6 ->
                "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        executeAuth {
            val credentials = AuthCredentials(
                email = normalizedEmail,
                password = password
            )
            if (isRegistration) {
                authService.register(credentials)
            } else {
                authService.login(credentials)
            }
        }
    }

    private fun executeAuth(action: suspend () -> AuthResult?) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = try {
                action()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: GetCredentialException) {
                AuthResult.Failure("No se pudo iniciar sesión con Google. Intentá de nuevo.")
            } catch (exception: IllegalStateException) {
                AuthResult.Failure(exception.message ?: "No se pudo iniciar sesión con Google.")
            } catch (exception: Exception) {
                AuthResult.Failure("No se pudo completar la operación. Intentá de nuevo.")
            }

            _uiState.update {
                when (result) {
                    null -> it.copy(isLoading = false, errorMessage = null)
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

class LoginViewModelFactory(
    private val authService: AuthService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
        }
        return LoginViewModel(authService) as T
    }
}

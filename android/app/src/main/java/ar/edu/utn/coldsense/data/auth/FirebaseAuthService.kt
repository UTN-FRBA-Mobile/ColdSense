package ar.edu.utn.coldsense.data.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.annotation.StringRes
import ar.edu.utn.coldsense.R

class FirebaseAuthService(context: Context) : AuthService {

    private val appContext = context.applicationContext
    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(appContext)
    }

    // FirebaseApp is supplied by google-services.json when the project is configured.
    // Keeping this nullable lets the app show a setup error instead of crashing beforehand.
    private val firebaseAuth: FirebaseAuth? by lazy {
        FirebaseApp.getApps(appContext)
            .firstOrNull()
            ?.let { app -> FirebaseAuth.getInstance(app) }
    }

    override val isSignedIn: Boolean
        get() = firebaseAuth?.currentUser != null

    override suspend fun login(credentials: AuthCredentials): AuthResult =
        runAuthAction { auth ->
            auth.signInWithEmailAndPassword(credentials.email, credentials.password)
        }

    override suspend fun register(credentials: AuthCredentials): AuthResult =
        runAuthAction { auth ->
            auth.createUserWithEmailAndPassword(credentials.email, credentials.password)
        }

    override suspend fun signInWithGoogle(idToken: String): AuthResult =
        runAuthAction { auth ->
            auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
        }

    override suspend fun signOut() {
        firebaseAuth?.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: ClearCredentialException) {
            // Firebase is already signed out; credential-provider cleanup is best effort.
        }
    }

    private suspend fun runAuthAction(
        action: (FirebaseAuth) -> Task<*>
    ): AuthResult {
        val auth = firebaseAuth
            ?: return AuthResult.Failure(R.string.login_error_firebase_sin_configurar)

        return try {
            action(auth).awaitCompletion()
            AuthResult.Success
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AuthResult.Failure(exception.toUserMessage())
        }
    }

    @StringRes
    private fun Exception.toUserMessage(): Int {
        val authErrorCode = (this as? FirebaseAuthException)?.errorCode
        return when {
            this is FirebaseNetworkException -> R.string.login_error_sin_conexion
            authErrorCode == "ERROR_INVALID_EMAIL" -> R.string.login_error_email_invalido
            authErrorCode == "ERROR_EMAIL_ALREADY_IN_USE" ->
                R.string.login_error_email_en_uso
            authErrorCode == "ERROR_WEAK_PASSWORD" ->
                R.string.login_error_contrasena_corta
            authErrorCode == "ERROR_USER_NOT_FOUND" ||
                authErrorCode == "ERROR_WRONG_PASSWORD" ||
                authErrorCode == "ERROR_INVALID_CREDENTIAL" ->
                R.string.login_error_credenciales
            authErrorCode == "ERROR_TOO_MANY_REQUESTS" ->
                R.string.login_error_demasiados_intentos
            authErrorCode == "ERROR_OPERATION_NOT_ALLOWED" ->
                R.string.login_error_metodo_deshabilitado
            else -> R.string.login_error_generico
        }
    }
}

private suspend fun Task<*>.awaitCompletion() {
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { completedTask ->
            if (!continuation.isActive) return@addOnCompleteListener

            if (completedTask.isSuccessful) {
                continuation.resume(Unit)
            } else {
                continuation.resumeWithException(
                    completedTask.exception
                        ?: IllegalStateException("Firebase Auth falló sin informar un error")
                )
            }
        }
    }
}

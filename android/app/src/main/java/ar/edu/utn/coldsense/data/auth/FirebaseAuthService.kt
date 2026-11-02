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
            ?: return AuthResult.Failure(
                "Firebase no está configurado. Agregá google-services.json al módulo Android."
            )

        return try {
            action(auth).awaitCompletion()
            AuthResult.Success
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AuthResult.Failure(exception.toUserMessage())
        }
    }

    private fun Exception.toUserMessage(): String {
        val authErrorCode = (this as? FirebaseAuthException)?.errorCode
        return when {
            this is FirebaseNetworkException -> "No se pudo conectar. Revisá tu conexión."
            authErrorCode == "ERROR_INVALID_EMAIL" -> "Ingresá un email válido"
            authErrorCode == "ERROR_EMAIL_ALREADY_IN_USE" ->
                "Ya existe una cuenta con ese email"
            authErrorCode == "ERROR_WEAK_PASSWORD" ->
                "La contraseña debe tener al menos 6 caracteres"
            authErrorCode == "ERROR_USER_NOT_FOUND" ||
                authErrorCode == "ERROR_WRONG_PASSWORD" ||
                authErrorCode == "ERROR_INVALID_CREDENTIAL" ->
                "El email o la contraseña son incorrectos"
            authErrorCode == "ERROR_TOO_MANY_REQUESTS" ->
                "Hubo demasiados intentos. Probá de nuevo más tarde."
            authErrorCode == "ERROR_OPERATION_NOT_ALLOWED" ->
                "Habilitá este método de acceso en Firebase."
            else -> "No se pudo completar la operación. Intentá de nuevo."
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

package ar.edu.utn.coldsense.data.auth

import android.content.Context
import androidx.core.content.edit
import ar.edu.utn.coldsense.BuildConfig

/** Usuario mock para probar la app sin cuenta de Firebase. Solo existe en los builds debug. */
const val DEMO_EMAIL = "demo@coldsense.com"
const val DEMO_PASSWORD = "demo1234"

fun esUsuarioDemo(credentials: AuthCredentials): Boolean =
    credentials.email.equals(DEMO_EMAIL, ignoreCase = true) && credentials.password == DEMO_PASSWORD

/**
 * Servicio de login de la app. En debug se puede entrar con el usuario mock;
 * en release es directamente el de Firebase.
 */
fun crearAuthService(context: Context): AuthService {
    val firebase = FirebaseAuthService(context)
    return if (BuildConfig.DEBUG) DemoAuthService(context, firebase) else firebase
}

/**
 * Deja entrar con el usuario mock sin pasar por Firebase; cualquier otro usuario
 * va a Firebase como siempre. Sin usuario de Firebase la app llama al backend sin
 * token, y el backend lo acepta solo desde la misma PC (el emulador).
 */
class DemoAuthService(
    context: Context,
    private val firebase: AuthService
) : AuthService {

    private val prefs = context.applicationContext
        .getSharedPreferences("usuario_demo", Context.MODE_PRIVATE)

    private var demoActivo: Boolean
        get() = prefs.getBoolean(CLAVE_ACTIVO, false)
        set(value) = prefs.edit { putBoolean(CLAVE_ACTIVO, value) }

    override val isSignedIn: Boolean
        get() = demoActivo || firebase.isSignedIn

    override suspend fun login(credentials: AuthCredentials): AuthResult {
        if (esUsuarioDemo(credentials)) {
            demoActivo = true
            return AuthResult.Success
        }
        return firebase.login(credentials)
    }

    override suspend fun register(credentials: AuthCredentials): AuthResult =
        firebase.register(credentials)

    override suspend fun signInWithGoogle(idToken: String): AuthResult =
        firebase.signInWithGoogle(idToken)

    override suspend fun signOut() {
        demoActivo = false
        firebase.signOut()
    }

    private companion object {
        const val CLAVE_ACTIVO = "activo"
    }
}

package ar.edu.utn.coldsense.features.auth

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import ar.edu.utn.coldsense.data.auth.FirebaseAuthService
import ar.edu.utn.coldsense.features.home.HomeScreen

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val context = LocalContext.current
        val appContext = context.applicationContext
        val credentialManager = remember(context) {
            CredentialManager.create(context)
        }
        val factory = remember(appContext) {
            LoginViewModelFactory(FirebaseAuthService(appContext))
        }
        val viewModel: LoginViewModel = viewModel(factory = factory)
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(state.isAuthenticated) {
            if (state.isAuthenticated) {
                navigator?.replace(HomeScreen())
            }
        }

        LoginContent(
            state = state,
            onLogin = viewModel::login,
            onRegister = viewModel::register,
            onGoogleSignIn = {
                viewModel.signInWithGoogle {
                    requestGoogleIdToken(context, credentialManager)
                }
            },
            onClearError = viewModel::clearError
        )
    }
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onClearError: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isRegistering by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ColdSense",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = if (isRegistering) "Creá una cuenta para continuar" else "Monitoreá el estado de tus heladeras",
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            supportingText = if (isRegistering) {
                { Text("Usá al menos 6 caracteres") }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Button(
            onClick = {
                if (isRegistering) onRegister(email, password) else onLogin(email, password)
            },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(4.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (isRegistering) "Crear cuenta" else "Iniciar sesión")
            }
        }

        Text(
            text = "o",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        OutlinedButton(
            onClick = onGoogleSignIn,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Continuar con Google")
        }

        TextButton(
            onClick = {
                isRegistering = !isRegistering
                onClearError()
            },
            enabled = !state.isLoading,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                if (isRegistering) "¿Ya tenés cuenta? Iniciá sesión"
                else "¿No tenés cuenta? Registrate"
            )
        }
    }
}

private suspend fun requestGoogleIdToken(
    context: Context,
    credentialManager: CredentialManager
): String? {
    val clientIdResource = context.resources.getIdentifier(
        "default_web_client_id",
        "string",
        context.packageName
    )
    if (clientIdResource == 0) {
        throw IllegalStateException(
            "Falta el cliente OAuth web. Descargá el google-services.json actualizado de Firebase."
        )
    }

    // Flujo del botón "Continuar con Google": muestra el selector de cuentas
    // y, si el celular no tiene ninguna, ofrece agregar una
    val googleIdOption = GetSignInWithGoogleOption.Builder(context.getString(clientIdResource))
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val credential = credentialManager.getCredential(context, request).credential
        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            throw IllegalStateException("Google devolvió una credencial incompatible.")
        }
    } catch (_: GetCredentialCancellationException) {
        null
    }
}

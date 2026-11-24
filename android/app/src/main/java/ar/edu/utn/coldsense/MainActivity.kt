package ar.edu.utn.coldsense

import android.Manifest
import android.graphics.Color
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import ar.edu.utn.coldsense.data.preferencias.PreferenciasTema
import ar.edu.utn.coldsense.features.auth.LoginScreen
import ar.edu.utn.coldsense.notifications.crearCanalAlertas
import ar.edu.utn.coldsense.ui.theme.AppTheme
import android.content.Context
import ar.edu.utn.coldsense.data.preferencias.PreferenciasIdioma

class MainActivity : ComponentActivity() {

    // Si rechaza alguno, se vuelve a pedir al abrir la app
    private val pedirPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    // Android 12 o anterior: el idioma elegido en la app se aplica acá (en 13+ lo hace Android)
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(PreferenciasIdioma.aplicar(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Tema elegido por el usuario (claro, oscuro o el del celular)
        PreferenciasTema.init(this)

        // El canal tiene que existir antes de que llegue la primera alerta
        crearCanalAlertas(this)

        if (savedInstanceState == null) {
            val faltantes = permisosNecesarios().filter {
                checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }
            if (faltantes.isNotEmpty()) {
                pedirPermisos.launch(faltantes.toTypedArray())
            }
        }

        setContent {
            val modoTema by PreferenciasTema.modo.collectAsState()
            val darkTheme = modoTema.esOscuro(isSystemInDarkTheme())

            // Los íconos de la barra de estado siguen al tema de la app, no al del celular
            LaunchedEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                )
            }

            AppTheme(darkTheme = darkTheme) {
                // Fondo y color de texto del tema para todas las pantallas
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigator(LoginScreen())
                }
            }
        }
    }

    private fun permisosNecesarios(): List<String> = buildList {
        // Android 17+: el backend corre en la red local (10.0.2.2 desde el emulador o la IP de la PC)
        // y sin este permiso las conexiones a la red local se bloquean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            add(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }
        // Android 13+: para mostrar las alertas (push notifications)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

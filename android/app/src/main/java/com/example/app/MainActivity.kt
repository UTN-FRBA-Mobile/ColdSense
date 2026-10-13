package com.example.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import cafe.adriel.voyager.navigator.Navigator
import com.example.app.data.auth.SesionStorage
import com.example.app.features.auth.LoginScreen
import com.example.app.features.home.HomeScreen
import com.example.app.notifications.crearCanalAlertas
import com.example.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    // Si rechaza alguno, se vuelve a pedir al abrir la app
    private val pedirPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

        // Si ya inició sesión antes (y no la cerró), arranca directo en el Home
        SesionStorage.init(this)
        val primeraPantalla = if (SesionStorage.haySesion) HomeScreen() else LoginScreen()

        setContent {
            AppTheme {
                Navigator(primeraPantalla)
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

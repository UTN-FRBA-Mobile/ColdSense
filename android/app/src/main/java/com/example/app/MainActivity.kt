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
import com.example.app.features.auth.LoginScreen
import com.example.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    // Si lo rechaza, las pantallas muestran el error de conexión y se vuelve a pedir al abrir la app
    private val pedirPermisoRedLocal = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Android 17+: el backend corre en la red local (10.0.2.2 desde el emulador o la IP de la PC)
        // y sin este permiso las conexiones a la red local se bloquean
        if (savedInstanceState == null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
            checkSelfPermission(Manifest.permission.ACCESS_LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED
        ) {
            pedirPermisoRedLocal.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }

        setContent {
            AppTheme {
                Navigator(LoginScreen())
            }
        }
    }
}

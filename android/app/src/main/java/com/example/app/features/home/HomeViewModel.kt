package com.example.app.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.auth.SesionStorage
import com.example.app.model.Heladera
import com.example.app.network.ApiClient
import com.example.app.network.HeladeraService
import com.example.app.network.NotificacionService
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val heladeraService: HeladeraService = ApiClient.heladeraService,
    private val notificacionService: NotificacionService = ApiClient.notificacionService
): ViewModel() {

    // Lista de heladeras
    private val _heladeras = MutableStateFlow<List<Heladera>>(emptyList())
    val heladeras = _heladeras.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Estado de error
    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    fun loadHeladeras() {

        // Actualizo el estado de carga para evitar múltiples solicitudes simultáneas
        if (isLoading.value)
            return
        _isLoading.value = true
        _isError.value = false

        viewModelScope.launch(Dispatchers.IO) {

            try {
                val response = heladeraService.getHeladeras()
                _heladeras.value = response

            } catch (e: Exception) {
                _isError.value = true
                Log.e("HomeViewModel", "Error al cargar los datos de heladeras: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Registra este celular para recibir las alertas (push notifications). Firebase avisa en
     * ColdSenseMessagingService.onRegistered y de ahí se manda al backend. Se repite cada vez
     * que se abre el Home porque el backend guarda los celulares en memoria y los pierde al reiniciarse.
     */
    fun registrarParaAlertas() {
        FirebaseMessaging.getInstance().register()
            .addOnFailureListener { e -> Log.e("HomeViewModel", "No se pudo registrar en FCM: ${e.message}", e) }
    }

    /**
     * Borra la sesión guardada y le pide al backend que no le mande más alertas a este
     * celular (al volver a iniciar sesión, el Home lo registra de nuevo).
     */
    fun cerrarSesion() {
        SesionStorage.cerrar()
        FirebaseInstallations.getInstance().id.addOnSuccessListener { fid ->
            viewModelScope.launch {
                try {
                    notificacionService.darDeBaja(fid)
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "No se pudo dar de baja el celular en el backend: ${e.message}", e)
                }
            }
        }
    }
}

private suspend fun mockedServiceGetHeladeras(): List<Heladera> {

    delay(3000)

    return listOf(
        Heladera(
            id = 1,
            nombre = "Heladera Cocina",
            temperaturaActual = 4.2,
            temperaturaMinima = 2.0,
            temperaturaMaxima = 6.0
        ),
        Heladera(
            id = 2,
            nombre = "Freezer 1",
            temperaturaActual = -16.5,
            temperaturaMinima = -20.0,
            temperaturaMaxima = -18.0
        ),
        Heladera(
            id = 3,
            nombre = "Freezer 2",
            temperaturaActual = -16.5,
            temperaturaMinima = -20.0,
            temperaturaMaxima = -18.0
        ),
        Heladera(
            id = 4,
            nombre = "Freezer 3",
            temperaturaActual = -16.5,
            temperaturaMinima = -20.0,
            temperaturaMaxima = -18.0
        ),
        Heladera(
            id = 5,
            nombre = "Heladera Laboratorio",
            temperaturaActual = null,
            temperaturaMinima = 2.0,
            temperaturaMaxima = 8.0
        )
    )
}
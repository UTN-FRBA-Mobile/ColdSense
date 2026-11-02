package ar.edu.utn.coldsense.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.coldsense.model.Heladera
import ar.edu.utn.coldsense.network.ApiClient
import ar.edu.utn.coldsense.network.HeladeraService
import ar.edu.utn.coldsense.network.NotificacionService
import com.google.android.gms.tasks.Tasks
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    // Evita pedir la lista dos veces al mismo tiempo (ej: al volver a la app y por una alerta)
    @Volatile
    private var cargando = false

    /**
     * Pide las heladeras. Se llama cada 3 segundos mientras se ve el Home y cuando llega una alerta.
     * El spinner se muestra solo si todavía no hay nada; si no, la lista se actualiza sin taparla.
     */
    fun loadHeladeras() {

        // Evito múltiples solicitudes simultáneas
        if (cargando)
            return
        cargando = true
        _isLoading.value = _heladeras.value.isEmpty()

        viewModelScope.launch(Dispatchers.IO) {

            try {
                val response = heladeraService.getHeladeras()
                _heladeras.value = response
                // El error se va solo cuando vuelve a responder (así no parpadea en cada intento)
                _isError.value = false

            } catch (e: Exception) {
                _isError.value = true
                Log.e("HomeViewModel", "Error al cargar los datos de heladeras: ${e.message}")
            } finally {
                _isLoading.value = false
                cargando = false
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

    /** Da de baja el dispositivo antes de cerrar la sesión de Firebase. */
    suspend fun cerrarSesion() {
        try {
            val fid = withContext(Dispatchers.IO) {
                Tasks.await(FirebaseInstallations.getInstance().id)
            }
            notificacionService.darDeBaja(fid)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "No se pudo dar de baja el celular en el backend: ${e.message}", e)
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

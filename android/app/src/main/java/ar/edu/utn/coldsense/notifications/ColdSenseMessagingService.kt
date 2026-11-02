package ar.edu.utn.coldsense.notifications

import android.util.Log
import ar.edu.utn.coldsense.model.RegistroRequest
import ar.edu.utn.coldsense.network.ApiClient
import ar.edu.utn.coldsense.network.NotificacionService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Recibe las push de Firebase Cloud Messaging. Con la app cerrada o en segundo plano
 * Android muestra la notificación solo; con la app abierta llega acá y la mostramos nosotros.
 */
class ColdSenseMessagingService : FirebaseMessagingService() {

    private val notificacionService: NotificacionService = ApiClient.notificacionService
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Firebase registró este celular (al abrir la app o al llamar a register()).
     * Le paso el ID de instalación al backend, que lo usa para mandarle las alertas.
     */
    override fun onRegistered(installationId: String) {
        scope.launch {
            try {
                notificacionService.registrarCelular(RegistroRequest(installationId))
            } catch (e: Exception) {
                // Sin conexión con el backend no hay alertas, pero el resto de la app sigue funcionando
                Log.e(TAG, "No se pudo registrar el celular en el backend: ${e.message}", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val heladeraId = message.data["heladeraId"]?.toLongOrNull()

        // La heladera cambió de estado: si hay una pantalla abierta, que se actualice
        CambiosHeladeras.avisar(heladeraId)

        val notificacion = message.notification ?: return

        mostrarAlerta(
            context = this,
            titulo = notificacion.title.orEmpty(),
            texto = notificacion.body.orEmpty(),
            heladeraId = heladeraId
        )
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ColdSenseMessaging"
    }
}

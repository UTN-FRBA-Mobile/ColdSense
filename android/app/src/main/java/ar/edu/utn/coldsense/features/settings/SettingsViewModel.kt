package ar.edu.utn.coldsense.features.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import ar.edu.utn.coldsense.network.ApiClient
import ar.edu.utn.coldsense.network.NotificacionService
import com.google.android.gms.tasks.Tasks
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val notificacionService: NotificacionService = ApiClient.notificacionService
) : ViewModel() {

    /** Da de baja el dispositivo antes de cerrar la sesión de Firebase. */
    suspend fun cerrarSesion() {
        try {
            val fid = withContext(Dispatchers.IO) {
                Tasks.await(FirebaseInstallations.getInstance().id)
            }
            notificacionService.darDeBaja(fid)
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "No se pudo dar de baja el celular en el backend: ${e.message}", e)
        }
    }
}

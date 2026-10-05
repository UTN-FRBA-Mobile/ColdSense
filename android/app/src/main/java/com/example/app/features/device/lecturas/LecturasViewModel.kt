package com.example.app.features.device.lecturas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.Heladera
import com.example.app.model.Lectura
import com.example.app.network.ApiClient
import com.example.app.network.HeladeraService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LecturasViewModel(
    private val heladeraService: HeladeraService = ApiClient.heladeraService
) : ViewModel() {

    // Heladera (se usa para el nombre y el rango configurado)
    private val _heladera = MutableStateFlow<Heladera?>(null)
    val heladera = _heladera.asStateFlow()

    // Historial de lecturas (más nueva primero)
    private val _lecturas = MutableStateFlow<List<Lectura>>(emptyList())
    val lecturas = _lecturas.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Mensaje de error (null = sin error)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun loadLecturas(heladeraId: Long) {

        // Evito múltiples solicitudes simultáneas
        if (_isLoading.value)
            return
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Pido la heladera y su historial en paralelo
                coroutineScope {
                    val heladeraDeferred = async { heladeraService.getHeladera(heladeraId) }
                    val lecturasDeferred = async { heladeraService.getLecturas(heladeraId, CANTIDAD_LECTURAS) }

                    _heladera.value = heladeraDeferred.await()
                    _lecturas.value = lecturasDeferred.await()
                }

            } catch (e: Exception) {
                _errorMessage.value = "No se pudo cargar el historial. Revisá tu conexión e intentá de nuevo."
                Log.e(TAG, "Error al cargar las lecturas: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        private const val TAG = "LecturasViewModel"

        // Máximo que devuelve el servidor
        private const val CANTIDAD_LECTURAS = 100
    }
}

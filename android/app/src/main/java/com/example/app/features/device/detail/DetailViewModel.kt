package com.example.app.features.device.detail

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

class DetailViewModel(
    private val heladeraService: HeladeraService = ApiClient.heladeraService
) : ViewModel() {

    // Heladera (null hasta que termina la primera carga)
    private val _heladera = MutableStateFlow<Heladera?>(null)
    val heladera = _heladera.asStateFlow()

    // Últimas lecturas (más nueva primero)
    private val _lecturas = MutableStateFlow<List<Lectura>>(emptyList())
    val lecturas = _lecturas.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Mensaje de error (null = sin error)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Diálogo de eliminar
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog = _showDeleteDialog.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()

    // Pasa a true cuando se eliminó, para que la pantalla vuelva atrás
    private val _isDeleted = MutableStateFlow(false)
    val isDeleted = _isDeleted.asStateFlow()

    fun loadDetalle(heladeraId: Long) {

        // Evito múltiples solicitudes simultáneas
        if (_isLoading.value)
            return
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Pido la heladera y sus lecturas en paralelo.
                // coroutineScope hace que si una falla, el error llegue al catch.
                coroutineScope {
                    val heladeraDeferred = async { heladeraService.getHeladera(heladeraId) }
                    val lecturasDeferred = async { heladeraService.getLecturas(heladeraId, CANTIDAD_LECTURAS) }

                    _heladera.value = heladeraDeferred.await()
                    _lecturas.value = lecturasDeferred.await()
                }

            } catch (e: Exception) {
                _errorMessage.value = "No se pudo cargar el detalle de la heladera. Revisá tu conexión e intentá de nuevo."
                Log.e(TAG, "Error al cargar el detalle: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onDeleteClick() {
        _showDeleteDialog.value = true
    }

    fun onDismissDelete() {
        if (!_isDeleting.value) {
            _showDeleteDialog.value = false
        }
    }

    fun confirmDelete(heladeraId: Long) {
        if (_isDeleting.value)
            return
        _isDeleting.value = true

        viewModelScope.launch {
            try {
                heladeraService.eliminarHeladera(heladeraId)
                _isDeleted.value = true

            } catch (e: Exception) {
                _errorMessage.value = "No se pudo eliminar el dispositivo. Intentá de nuevo."
                Log.e(TAG, "Error al eliminar: ${e.message}", e)
            } finally {
                _isDeleting.value = false
                _showDeleteDialog.value = false
            }
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    companion object {
        private const val TAG = "DetailViewModel"
        private const val CANTIDAD_LECTURAS = 5
    }
}
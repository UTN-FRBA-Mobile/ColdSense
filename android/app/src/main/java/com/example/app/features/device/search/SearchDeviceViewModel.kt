package com.example.app.features.device.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.DispositivoEncontrado
import com.example.app.network.ApiClient
import com.example.app.network.DispositivoService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchDeviceViewModel(
    private val dispositivoService: DispositivoService = ApiClient.dispositivoService
) : ViewModel() {

    // Sensores encontrados (null = todavía no terminó ninguna búsqueda)
    private val _dispositivos = MutableStateFlow<List<DispositivoEncontrado>?>(null)
    val dispositivos = _dispositivos.asStateFlow()

    // Estado de error
    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    // Evita múltiples búsquedas simultáneas
    private var isSearching = false

    /**
     * Busca solo la primera vez que se abre la pantalla. Al volver desde
     * "Configurar dispositivo" se siguen mostrando los mismos resultados.
     */
    fun iniciarBusqueda() {
        if (_dispositivos.value == null && !_isError.value) buscar()
    }

    fun buscar() {
        if (isSearching)
            return
        isSearching = true
        _isError.value = false
        _dispositivos.value = null

        viewModelScope.launch {
            try {
                _dispositivos.value = dispositivoService.buscarDispositivos()

            } catch (e: Exception) {
                _isError.value = true
                Log.e(TAG, "Error al buscar dispositivos: ${e.message}", e)
            } finally {
                isSearching = false
            }
        }
    }

    companion object {
        private const val TAG = "SearchDeviceViewModel"
    }
}

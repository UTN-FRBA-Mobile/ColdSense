package ar.edu.utn.coldsense.features.device.edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.coldsense.features.device.ValidacionParametros
import ar.edu.utn.coldsense.features.device.aTextoDeCampo
import ar.edu.utn.coldsense.features.device.validarParametros
import ar.edu.utn.coldsense.model.Heladera
import ar.edu.utn.coldsense.model.ParametrosRequest
import ar.edu.utn.coldsense.network.ApiClient
import ar.edu.utn.coldsense.network.HeladeraService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditDeviceViewModel(
    private val heladeraService: HeladeraService = ApiClient.heladeraService
) : ViewModel() {

    // Heladera que se edita (null hasta que termina la carga)
    private val _heladera = MutableStateFlow<Heladera?>(null)
    val heladera = _heladera.asStateFlow()

    // Campos del formulario (se precargan con los valores actuales)
    private val _temperaturaMinima = MutableStateFlow("")
    val temperaturaMinima = _temperaturaMinima.asStateFlow()

    private val _temperaturaMaxima = MutableStateFlow("")
    val temperaturaMaxima = _temperaturaMaxima.asStateFlow()

    private val _intervalo = MutableStateFlow("")
    val intervalo = _intervalo.asStateFlow()

    // Error de validación del formulario (null = formulario válido)
    private val _validationError = MutableStateFlow<String?>(null)
    val validationError = _validationError.asStateFlow()

    // Estados de carga y guardado
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    // Mensaje de error del servidor (null = sin error)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Pasa a true cuando se guardó, para que la pantalla vuelva atrás
    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    fun loadHeladera(heladeraId: Long) {

        // Evito múltiples solicitudes y no piso lo que el usuario ya editó
        if (_isLoading.value || _heladera.value != null)
            return
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val heladera = heladeraService.getHeladera(heladeraId)
                _temperaturaMinima.value = heladera.temperaturaMinima.aTextoDeCampo()
                _temperaturaMaxima.value = heladera.temperaturaMaxima.aTextoDeCampo()
                _intervalo.value = heladera.intervaloLecturaMinutos.toString()
                _heladera.value = heladera

            } catch (e: Exception) {
                _errorMessage.value = "No se pudieron cargar los parámetros. Revisá tu conexión e intentá de nuevo."
                Log.e(TAG, "Error al cargar la heladera: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onTemperaturaMinimaChange(valor: String) {
        _temperaturaMinima.value = valor
        _validationError.value = null
    }

    fun onTemperaturaMaximaChange(valor: String) {
        _temperaturaMaxima.value = valor
        _validationError.value = null
    }

    fun onIntervaloChange(valor: String) {
        _intervalo.value = valor
        _validationError.value = null
    }

    fun guardar(heladeraId: Long) {

        // Evito múltiples solicitudes simultáneas
        if (_isSaving.value)
            return

        val resultado = validarParametros(
            temperaturaMinima = _temperaturaMinima.value,
            temperaturaMaxima = _temperaturaMaxima.value,
            intervalo = _intervalo.value
        )

        when (resultado) {
            is ValidacionParametros.Error -> _validationError.value = resultado.mensaje
            is ValidacionParametros.Ok -> actualizar(heladeraId, resultado.request)
        }
    }

    private fun actualizar(heladeraId: Long, request: ParametrosRequest) {
        _isSaving.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                heladeraService.actualizarParametros(heladeraId, request)
                _isSaved.value = true

            } catch (e: Exception) {
                _errorMessage.value = "No se pudieron guardar los cambios. Revisá tu conexión e intentá de nuevo."
                Log.e(TAG, "Error al actualizar los parámetros: ${e.message}", e)
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    companion object {
        private const val TAG = "EditDeviceViewModel"
    }
}

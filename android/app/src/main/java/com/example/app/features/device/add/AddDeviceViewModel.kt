package com.example.app.features.device.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.features.device.ValidacionParametros
import com.example.app.features.device.validarParametros
import com.example.app.model.HeladeraRequest
import com.example.app.network.ApiClient
import com.example.app.network.HeladeraService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddDeviceViewModel(
    private val heladeraService: HeladeraService = ApiClient.heladeraService
) : ViewModel() {

    // Campos del formulario (se guardan como texto, tal cual los escribe el usuario)
    private val _nombre = MutableStateFlow("")
    val nombre = _nombre.asStateFlow()

    private val _temperaturaMinima = MutableStateFlow("")
    val temperaturaMinima = _temperaturaMinima.asStateFlow()

    private val _temperaturaMaxima = MutableStateFlow("")
    val temperaturaMaxima = _temperaturaMaxima.asStateFlow()

    private val _intervalo = MutableStateFlow(INTERVALO_DEFAULT.toString())
    val intervalo = _intervalo.asStateFlow()

    // Error de validación del formulario (null = formulario válido)
    private val _validationError = MutableStateFlow<String?>(null)
    val validationError = _validationError.asStateFlow()

    // Estado de guardado
    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    // Mensaje de error del servidor (null = sin error)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Pasa a true cuando se creó, para que la pantalla vuelva atrás
    private val _isCreated = MutableStateFlow(false)
    val isCreated = _isCreated.asStateFlow()

    fun onNombreChange(valor: String) {
        _nombre.value = valor
        _validationError.value = null
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

    fun guardar() {

        // Evito múltiples solicitudes simultáneas
        if (_isSaving.value)
            return

        val resultado = validarNuevaHeladera(
            nombre = _nombre.value,
            temperaturaMinima = _temperaturaMinima.value,
            temperaturaMaxima = _temperaturaMaxima.value,
            intervalo = _intervalo.value
        )

        when (resultado) {
            is ValidacionHeladera.Error -> _validationError.value = resultado.mensaje
            is ValidacionHeladera.Ok -> crear(resultado.request)
        }
    }

    private fun crear(request: HeladeraRequest) {
        _isSaving.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                heladeraService.crearHeladera(request)
                _isCreated.value = true

            } catch (e: Exception) {
                _errorMessage.value = "No se pudo agregar el dispositivo. Revisá tu conexión e intentá de nuevo."
                Log.e(TAG, "Error al crear la heladera: ${e.message}", e)
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    companion object {
        private const val TAG = "AddDeviceViewModel"
        const val INTERVALO_DEFAULT = 10
    }
}

/**
 * Resultado de validar el formulario de alta de una heladera.
 */
sealed interface ValidacionHeladera {
    data class Ok(val request: HeladeraRequest) : ValidacionHeladera
    data class Error(val mensaje: String) : ValidacionHeladera
}

/**
 * Valida los campos del formulario (como texto) y, si son correctos,
 * arma el request para el servidor. Los límites y el intervalo se validan con validarParametros.
 */
fun validarNuevaHeladera(
    nombre: String,
    temperaturaMinima: String,
    temperaturaMaxima: String,
    intervalo: String
): ValidacionHeladera {

    if (nombre.isBlank()) {
        return ValidacionHeladera.Error("Ingresá un nombre para el dispositivo.")
    }

    return when (val parametros = validarParametros(temperaturaMinima, temperaturaMaxima, intervalo)) {
        is ValidacionParametros.Error -> ValidacionHeladera.Error(parametros.mensaje)
        is ValidacionParametros.Ok -> ValidacionHeladera.Ok(
            HeladeraRequest(
                nombre = nombre.trim(),
                temperaturaMinima = parametros.request.temperaturaMinima,
                temperaturaMaxima = parametros.request.temperaturaMaxima,
                intervaloLecturaMinutos = parametros.request.intervaloLecturaMinutos
            )
        )
    }
}

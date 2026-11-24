package ar.edu.utn.coldsense.features.device

import ar.edu.utn.coldsense.model.ParametrosRequest
import androidx.annotation.StringRes
import ar.edu.utn.coldsense.R

/**
 * Resultado de validar los parámetros de una heladera (límites e intervalo).
 */
sealed interface ValidacionParametros {
    data class Ok(val request: ParametrosRequest) : ValidacionParametros
    data class Error(@StringRes val mensaje: Int) : ValidacionParametros
}

/**
 * Valida los límites de temperatura y el intervalo de lectura (como texto) y,
 * si son correctos, arma el request. Acepta coma o punto como separador decimal.
 * La usan las pantallas "Agregar dispositivo" y "Editar parámetros".
 */
fun validarParametros(
    temperaturaMinima: String,
    temperaturaMaxima: String,
    intervalo: String
): ValidacionParametros {

    val minima = temperaturaMinima.aDecimal()
        ?: return ValidacionParametros.Error(R.string.validacion_minima_no_numero)

    val maxima = temperaturaMaxima.aDecimal()
        ?: return ValidacionParametros.Error(R.string.validacion_maxima_no_numero)

    if (minima > maxima) {
        return ValidacionParametros.Error(R.string.validacion_minima_mayor_que_maxima)
    }

    val minutos = intervalo.trim().toIntOrNull()
    if (minutos == null || minutos < 1) {
        return ValidacionParametros.Error(R.string.validacion_intervalo_minimo)
    }

    return ValidacionParametros.Ok(
        ParametrosRequest(
            temperaturaMinima = minima,
            temperaturaMaxima = maxima,
            intervaloLecturaMinutos = minutos
        )
    )
}

/** 2.0 -> "2"  |  -18.5 -> "-18.5"  (para precargar los campos del formulario) */
fun Double.aTextoDeCampo(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else toString()
}

/** "4,5" -> 4.5  |  "-18" -> -18.0  |  "abc" -> null */
private fun String.aDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()

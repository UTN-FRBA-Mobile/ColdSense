package com.example.app.features.device

import com.example.app.model.ParametrosRequest

/**
 * Resultado de validar los parámetros de una heladera (límites e intervalo).
 */
sealed interface ValidacionParametros {
    data class Ok(val request: ParametrosRequest) : ValidacionParametros
    data class Error(val mensaje: String) : ValidacionParametros
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
        ?: return ValidacionParametros.Error("La temperatura mínima debe ser un número.")

    val maxima = temperaturaMaxima.aDecimal()
        ?: return ValidacionParametros.Error("La temperatura máxima debe ser un número.")

    if (minima > maxima) {
        return ValidacionParametros.Error("La temperatura mínima no puede ser mayor que la máxima.")
    }

    val minutos = intervalo.trim().toIntOrNull()
    if (minutos == null || minutos < 1) {
        return ValidacionParametros.Error("El intervalo debe ser de al menos 1 minuto.")
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

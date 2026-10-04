package com.example.app.utils

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 4.2 -> "4.2 °C"  |  null -> "-- °C" */
fun formatTemperature(temperature: Double?): String {
    return if (temperature == null) {
        "-- °C"
    } else {
        "${String.format(Locale.US, "%.1f", temperature)} °C"
    }
}

/** 2.0 -> "2 °C"  |  2.5 -> "2.5 °C"*/
fun formatLimite(valor: Double): String {
    return if (valor % 1.0 == 0.0) {
        "${valor.toInt()} °C"
    } else {
        "${String.format(Locale.US, "%.1f", valor)} °C"
    }
}

/** Quita los milisegundos y devuelve el formato "HH:mm" */
fun formatHora(millis: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
}

/** Devuelve el formato "hoy HH:mm" o "dd/MM HH:mm"  |  null -> "sin datos" */
fun formatUltimaLectura(millis: Long?): String {
    if (millis == null) return "sin datos"

    val hora = formatHora(millis)
    return if (DateUtils.isToday(millis)) {
        "hoy $hora"
    } else {
        "${SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(millis))} $hora"
    }
}
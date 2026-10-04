package com.example.app.model

fun Heladera.calcularEstado(): HeladeraStatus {
    val temperatura = temperaturaActual ?: return HeladeraStatus.Disconnected

    return if (temperatura !in temperaturaMinima..temperaturaMaxima) {
        HeladeraStatus.Alert
    } else {
        HeladeraStatus.Normal
    }
}
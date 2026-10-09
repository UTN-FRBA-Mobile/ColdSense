package com.example.app.model

import kotlinx.serialization.Serializable

/**
 * Sensor nuevo que devuelve la búsqueda (POST /dispositivos/busqueda)
 * y todavía no está vinculado a ninguna heladera.
 */
@Serializable
data class DispositivoEncontrado(
    val numeroSerie: String,
    val modelo: String,
    val temperaturaActual: Double
)

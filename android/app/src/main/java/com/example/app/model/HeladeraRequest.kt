package com.example.app.model

import kotlinx.serialization.Serializable

/**
 * Body para crear (POST /heladeras) una heladera.
 * El id lo genera el servidor, por eso no viene acá.
 */
@Serializable
data class HeladeraRequest(
    val nombre: String,
    val temperaturaMinima: Double,
    val temperaturaMaxima: Double,
    val intervaloLecturaMinutos: Int
)

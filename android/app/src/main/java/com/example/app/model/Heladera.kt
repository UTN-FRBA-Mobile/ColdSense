package com.example.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Heladera(
    val id: Long,
    val nombre: String,
    val temperaturaActual: Double?,
    val temperaturaMinima: Double,
    val temperaturaMaxima: Double,
    val bateria: Int? = null,
    val ultimaLectura: Long? = null,
    val intervaloLecturaMinutos: Int = 10
)
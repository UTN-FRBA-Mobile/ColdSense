package com.example.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Lectura(
    val fecha: Long,
    val temperatura: Double
)
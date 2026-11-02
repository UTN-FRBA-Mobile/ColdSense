package ar.edu.utn.coldsense.model

import kotlinx.serialization.Serializable

@Serializable
data class Lectura(
    val fecha: Long,
    val temperatura: Double
)
package com.example.app.model

import kotlinx.serialization.Serializable

/**
 * Body de la pantalla "Editar parámetros" (PUT /heladeras/{id}/parametros).
 */
@Serializable
data class ParametrosRequest(
    val temperaturaMinima: Double,
    val temperaturaMaxima: Double,
    val intervaloLecturaMinutos: Int
)

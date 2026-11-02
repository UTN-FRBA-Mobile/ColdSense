package ar.edu.utn.coldsense.model

import kotlinx.serialization.Serializable

/**
 * Body para registrar este celular en el backend y recibir las alertas
 * (POST /notificaciones/registro). El fid es el ID de instalación que da Firebase.
 */
@Serializable
data class RegistroRequest(
    val fid: String
)

package com.example.app.network

import com.example.app.model.RegistroRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificacionService {

    @POST("notificaciones/registro")
    suspend fun registrarCelular(@Body request: RegistroRequest)
}

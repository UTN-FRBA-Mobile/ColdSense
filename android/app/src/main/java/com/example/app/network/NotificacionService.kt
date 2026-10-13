package com.example.app.network

import com.example.app.model.RegistroRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificacionService {

    @POST("notificaciones/registro")
    suspend fun registrarCelular(@Body request: RegistroRequest)

    @DELETE("notificaciones/registro/{fid}")
    suspend fun darDeBaja(@Path("fid") fid: String)
}

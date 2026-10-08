package com.example.app.network

import com.example.app.model.DispositivoEncontrado
import retrofit2.http.POST

interface DispositivoService {

    // Tarda unos segundos: el servidor simula la búsqueda de sensores cerca
    @POST("dispositivos/busqueda")
    suspend fun buscarDispositivos(): List<DispositivoEncontrado>
}

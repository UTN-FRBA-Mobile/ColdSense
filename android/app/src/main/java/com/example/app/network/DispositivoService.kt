package com.example.app.network

import com.example.app.model.DispositivoEncontrado
import retrofit2.http.POST

interface DispositivoService {

    // Tarda unos segundos: el bridge (backend) busca los sensores a su alcance, la app solo pide el resultado
    @POST("dispositivos/busqueda")
    suspend fun buscarDispositivos(): List<DispositivoEncontrado>
}

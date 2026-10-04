package com.example.app.network

import com.example.app.model.Heladera
import com.example.app.model.Lectura
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HeladeraService {

    @GET("heladeras")
    suspend fun getHeladeras(): List<Heladera>

    @GET("heladeras/{id}")
    suspend fun getHeladera(@Path("id") id: Long): Heladera

    @GET("heladeras/{id}/lecturas")
    suspend fun getLecturas(@Path("id") id: Long, @Query("limit") limit: Int): List<Lectura>

    @DELETE("heladeras/{id}")
    suspend fun eliminarHeladera(@Path("id") id: Long)
}
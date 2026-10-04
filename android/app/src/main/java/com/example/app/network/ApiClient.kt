package com.example.app.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Cliente único de red de la aplicación. Configura Retrofit una sola
 * vez y expone los servicios para llamar al servidor.
 */
object ApiClient {

    // Emulador: 10.0.2.2 apunta al localhost de tu PC.
// Celular físico: usar la IP de tu PC en la red (ej: http://192.168.0.10:8080/).
// La URL base debe terminar con "/".
    private const val BASE_URL = "http://192.168.1.40:8080/"

    // Ignora campos que lleguen del servidor y no estén en los DTO
    private val json = Json { ignoreUnknownKeys = true }

    // Muestra en el Logcat cada request y response (útil mientras desarrollás)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val heladeraService: HeladeraService = retrofit.create(HeladeraService::class.java)
}
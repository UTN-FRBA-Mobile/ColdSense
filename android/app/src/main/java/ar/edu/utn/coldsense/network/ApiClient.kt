package ar.edu.utn.coldsense.network

import ar.edu.utn.coldsense.BuildConfig
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Cliente único de red de la aplicación. Configura Retrofit una sola
 * vez y expone los servicios para llamar al servidor.
 */
object ApiClient {

    // Se configura en local.properties (api.baseUrl); por defecto apunta al backend local del emulador.
    private const val BASE_URL = BuildConfig.API_BASE_URL

    // Ignora campos que lleguen del servidor y no estén en los DTO
    private val json = Json { ignoreUnknownKeys = true }

    // El token de Firebase se adjunta en cada llamada autenticada al backend.
    private val firebaseAuthInterceptor = okhttp3.Interceptor { chain ->
        val user = FirebaseAuth.getInstance().currentUser
        val request = if (user == null) {
            chain.request()
        } else {
            val token = try {
                Tasks.await(user.getIdToken(false), 15, TimeUnit.SECONDS).token
            } catch (exception: Exception) {
                throw IOException("No se pudo obtener el token de Firebase.", exception)
            }

            if (token.isNullOrBlank()) {
                throw IOException("Firebase no devolvió un ID token válido.")
            }

            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        chain.proceed(request)
    }

    // No registrar el token Bearer en Logcat.
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(firebaseAuthInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val heladeraService: HeladeraService = retrofit.create(HeladeraService::class.java)
    val dispositivoService: DispositivoService = retrofit.create(DispositivoService::class.java)
}

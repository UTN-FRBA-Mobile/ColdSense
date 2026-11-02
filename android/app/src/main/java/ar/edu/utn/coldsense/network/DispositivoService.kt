package ar.edu.utn.coldsense.network

import ar.edu.utn.coldsense.model.DispositivoEncontrado
import retrofit2.http.POST

interface DispositivoService {

    // Tarda unos segundos: el bridge (backend) busca los sensores a su alcance, la app solo pide el resultado
    @POST("dispositivos/busqueda")
    suspend fun buscarDispositivos(): List<DispositivoEncontrado>
}

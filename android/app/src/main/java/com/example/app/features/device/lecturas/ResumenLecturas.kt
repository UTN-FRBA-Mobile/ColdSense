package com.example.app.features.device.lecturas

import com.example.app.model.Lectura

/**
 * Resumen del historial de lecturas de una heladera.
 *
 * @param minima Temperatura más baja registrada.
 * @param maxima Temperatura más alta registrada.
 * @param promedio Promedio de todas las lecturas.
 * @param fueraDeRango Cantidad de lecturas que salieron del rango configurado.
 */
data class ResumenLecturas(
    val minima: Double,
    val maxima: Double,
    val promedio: Double,
    val fueraDeRango: Int
)

/** true si la lectura quedó por debajo del mínimo o por encima del máximo configurado. */
fun Lectura.estaFueraDeRango(temperaturaMinima: Double, temperaturaMaxima: Double): Boolean {
    return temperatura !in temperaturaMinima..temperaturaMaxima
}

/** Calcula el resumen del historial  |  sin lecturas -> null */
fun resumirLecturas(
    lecturas: List<Lectura>,
    temperaturaMinima: Double,
    temperaturaMaxima: Double
): ResumenLecturas? {
    if (lecturas.isEmpty()) return null

    val temperaturas = lecturas.map { it.temperatura }

    return ResumenLecturas(
        minima = temperaturas.min(),
        maxima = temperaturas.max(),
        promedio = temperaturas.average(),
        fueraDeRango = lecturas.count { it.estaFueraDeRango(temperaturaMinima, temperaturaMaxima) }
    )
}

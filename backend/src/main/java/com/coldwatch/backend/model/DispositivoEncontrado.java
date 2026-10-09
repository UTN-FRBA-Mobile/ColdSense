package com.coldwatch.backend.model;

/**
 * Sensor nuevo que apareció en una búsqueda y todavía no está vinculado a ninguna heladera.
 * Misma estructura que DispositivoEncontrado.kt en la app Android.
 *
 * {
 *   "numeroSerie": "CS-4F2A",
 *   "modelo": "Sensor ColdSense T1",
 *   "temperaturaActual": 4.6          // lo que está midiendo ahora
 * }
 */
public record DispositivoEncontrado(
        String numeroSerie,
        String modelo,
        double temperaturaActual
) {
}

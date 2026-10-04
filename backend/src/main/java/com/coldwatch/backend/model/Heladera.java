package com.coldwatch.backend.model;

/**
 * Misma estructura que Heladera.kt en la app Android.
 *
 * {
 *   "id": 1,
 *   "nombre": "Heladera Cocina",
 *   "temperaturaActual": 4.2,          // null => "Sin conexión"
 *   "temperaturaMinima": 2.0,
 *   "temperaturaMaxima": 6.0,
 *   "bateria": 80,                     // % (puede ser null)
 *   "ultimaLectura": 1759598520000,    // epoch en milisegundos (puede ser null)
 *   "intervaloLecturaMinutos": 10
 * }
 */
public record Heladera(
        Long id,
        String nombre,
        Double temperaturaActual,
        double temperaturaMinima,
        double temperaturaMaxima,
        Integer bateria,
        Long ultimaLectura,
        int intervaloLecturaMinutos
) {

    public Heladera conId(Long nuevoId) {
        return new Heladera(nuevoId, nombre, temperaturaActual, temperaturaMinima,
                temperaturaMaxima, bateria, ultimaLectura, intervaloLecturaMinutos);
    }

    public Heladera conLectura(Double temperatura, Long fecha) {
        return new Heladera(id, nombre, temperatura, temperaturaMinima,
                temperaturaMaxima, bateria, fecha, intervaloLecturaMinutos);
    }

    public Heladera conParametros(String nuevoNombre, double minima, double maxima, int intervalo) {
        return new Heladera(id, nuevoNombre, temperaturaActual, minima,
                maxima, bateria, ultimaLectura, intervalo);
    }
}

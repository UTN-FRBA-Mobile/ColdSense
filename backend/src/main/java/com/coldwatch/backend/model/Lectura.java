package com.coldwatch.backend.model;

/**
 * Una lectura del sensor.
 *
 * @param fecha       epoch en milisegundos
 * @param temperatura temperatura medida en °C
 */
public record Lectura(
        long fecha,
        double temperatura
) {
}

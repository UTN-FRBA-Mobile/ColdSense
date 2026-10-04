package com.coldwatch.backend.dto;

/**
 * Body para simular una lectura del sensor.
 * Mandar { "temperatura": null } simula que el sensor se desconectó.
 */
public record TemperaturaRequest(
        Double temperatura
) {
}

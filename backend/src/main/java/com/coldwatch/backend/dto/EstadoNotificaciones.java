package com.coldwatch.backend.dto;

/**
 * Respuesta de GET /notificaciones/estado (la usa el simulador web).
 *
 * @param activadas true si están las credenciales de Firebase
 * @param celulares cantidad de celulares registrados para recibir alertas
 */
public record EstadoNotificaciones(
        boolean activadas,
        int celulares
) {
}

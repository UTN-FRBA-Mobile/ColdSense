package com.coldwatch.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Body para registrar el celular que recibe las alertas (POST /notificaciones/registro).
 * El fid es el ID de instalación que Firebase le da a la app en ese celular.
 */
public record RegistroRequest(
        @NotBlank(message = "El fid es obligatorio")
        String fid
) {
}

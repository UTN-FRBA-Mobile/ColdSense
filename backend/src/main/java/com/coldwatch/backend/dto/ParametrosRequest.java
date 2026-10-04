package com.coldwatch.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Body de la pantalla "Editar parámetros".
 */
public record ParametrosRequest(
        @NotNull(message = "La temperatura mínima es obligatoria")
        Double temperaturaMinima,

        @NotNull(message = "La temperatura máxima es obligatoria")
        Double temperaturaMaxima,

        @NotNull(message = "El intervalo de lectura es obligatorio")
        @Min(value = 1, message = "El intervalo debe ser de al menos 1 minuto")
        Integer intervaloLecturaMinutos
) {

    @JsonIgnore
    @AssertTrue(message = "La temperatura mínima no puede ser mayor que la máxima")
    public boolean isRangoValido() {
        if (temperaturaMinima == null || temperaturaMaxima == null) {
            return true;
        }
        return temperaturaMinima <= temperaturaMaxima;
    }
}

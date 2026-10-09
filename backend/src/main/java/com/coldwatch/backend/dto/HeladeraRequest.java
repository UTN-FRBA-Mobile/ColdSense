package com.coldwatch.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Body para crear (POST) o modificar (PUT) una heladera.
 * El id lo genera el servidor, por eso no viene acá.
 */
public record HeladeraRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        // Opcional: si no viene, la heladera arranca "Sin conexión"
        Double temperaturaActual,

        @NotNull(message = "La temperatura mínima es obligatoria")
        Double temperaturaMinima,

        @NotNull(message = "La temperatura máxima es obligatoria")
        Double temperaturaMaxima,

        // Opcional: si no viene, se usan 10 minutos
        @Min(value = 1, message = "El intervalo debe ser de al menos 1 minuto")
        Integer intervaloLecturaMinutos,

        // Opcional, solo al crear: sensor encontrado en POST /dispositivos/busqueda que se vincula
        // a la heladera. Si viene, la heladera arranca con la temperatura que mide el sensor.
        String numeroSerie
) {

    @JsonIgnore
    @AssertTrue(message = "La temperatura mínima no puede ser mayor que la máxima")
    public boolean isRangoValido() {
        if (temperaturaMinima == null || temperaturaMaxima == null) {
            return true; // ya lo informan los @NotNull
        }
        return temperaturaMinima <= temperaturaMaxima;
    }
}

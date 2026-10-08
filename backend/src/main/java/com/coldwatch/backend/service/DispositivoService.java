package com.coldwatch.backend.service;

import com.coldwatch.backend.model.DispositivoEncontrado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simula la búsqueda de sensores que hace el bridge, como "Agregar lámpara" en Philips Hue:
 * el bridge encuentra los sensores a su alcance (ej: por Zigbee) y la app solo pide el resultado.
 * Los sensores encontrados quedan pendientes hasta que se vinculan a una heladera.
 */
@Service
public class DispositivoService {

    private static final String MODELO = "Sensor ColdSense T1";

    // Lo que tarda la búsqueda, para que en la app se vea el "Buscando..."
    private static final long DURACION_BUSQUEDA_MS = 3_000L;

    // Sensores encontrados que todavía no se vincularon (clave: número de serie)
    private final Map<String, DispositivoEncontrado> pendientes = new ConcurrentHashMap<>();
    private final Random random = new Random();

    /**
     * Siempre encuentra un sensor nuevo, midiendo una temperatura típica de heladera (3 a 5 °C).
     */
    public List<DispositivoEncontrado> buscar() {
        esperar(DURACION_BUSQUEDA_MS);

        DispositivoEncontrado nuevo = new DispositivoEncontrado(
                nuevoNumeroSerie(),
                MODELO,
                Math.round((3 + random.nextDouble() * 2) * 10) / 10.0
        );
        pendientes.put(nuevo.numeroSerie(), nuevo);
        return List.of(nuevo);
    }

    /**
     * Saca el sensor de los pendientes para vincularlo a una heladera.
     * Tira 404 si no apareció en una búsqueda o si ya se vinculó.
     */
    public DispositivoEncontrado vincular(String numeroSerie) {
        DispositivoEncontrado dispositivo = pendientes.remove(numeroSerie);
        if (dispositivo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No hay un dispositivo pendiente con número de serie " + numeroSerie);
        }
        return dispositivo;
    }

    /** "CS-" + 4 dígitos hexadecimales, ej: CS-4F2A */
    private String nuevoNumeroSerie() {
        String numeroSerie;
        do {
            numeroSerie = String.format("CS-%04X", random.nextInt(0x10000));
        } while (pendientes.containsKey(numeroSerie));
        return numeroSerie;
    }

    private void esperar(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

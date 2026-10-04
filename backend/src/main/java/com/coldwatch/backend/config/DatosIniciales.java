package com.coldwatch.backend.config;

import com.coldwatch.backend.model.Heladera;
import com.coldwatch.backend.model.Lectura;
import com.coldwatch.backend.repository.HeladeraRepository;
import com.coldwatch.backend.repository.LecturaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Carga las heladeras de ejemplo (las mismas que tenía el mock de la app)
 * y les genera un historial de lecturas.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final int LECTURAS_POR_HELADERA = 24;
    private static final long UN_MINUTO = 60_000L;

    private final HeladeraRepository heladeraRepository;
    private final LecturaRepository lecturaRepository;

    // Semilla fija para que el historial sea siempre el mismo
    private final Random random = new Random(42);

    public DatosIniciales(HeladeraRepository heladeraRepository, LecturaRepository lecturaRepository) {
        this.heladeraRepository = heladeraRepository;
        this.lecturaRepository = lecturaRepository;
    }

    @Override
    public void run(String... args) {
        long ahora = System.currentTimeMillis();

        crear("Heladera Cocina", 4.2, 2.0, 6.0, 80, ahora, 4.2);
        crear("Freezer 1", -16.5, -20.0, -18.0, 65, ahora, -16.5);
        crear("Freezer 2", -16.5, -20.0, -18.0, 92, ahora, -16.5);
        crear("Freezer 3", -16.5, -20.0, -18.0, 40, ahora, -16.5);

        // Desconectada: sin temperatura actual, última lectura hace 2 horas
        crear("Heladera Laboratorio", null, 2.0, 8.0, 15, ahora - 120 * UN_MINUTO, 4.8);
    }

    private void crear(String nombre, Double temperaturaActual, double minima, double maxima,
                       Integer bateria, long ultimaLectura, double ultimaTemperatura) {

        int intervalo = 10;

        Heladera heladera = heladeraRepository.save(new Heladera(
                null, nombre, temperaturaActual, minima, maxima, bateria, ultimaLectura, intervalo
        ));

        // Genero lecturas hacia atrás cada "intervalo" minutos, con una pequeña variación
        long paso = intervalo * UN_MINUTO;
        for (int i = LECTURAS_POR_HELADERA - 1; i >= 0; i--) {
            double temperatura = (i == 0)
                    ? ultimaTemperatura
                    : ultimaTemperatura + (random.nextDouble() - 0.5) * 0.6;

            lecturaRepository.agregar(heladera.id(), new Lectura(
                    ultimaLectura - i * paso,
                    Math.round(temperatura * 10) / 10.0
            ));
        }
    }
}

package com.coldwatch.backend.repository;

import com.coldwatch.backend.model.Heladera;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * "Base de datos" en memoria. Los datos se pierden al reiniciar el servidor.
 * Los datos iniciales se cargan en config/DatosIniciales.
 */
@Repository
public class HeladeraRepository {

    private final Map<Long, Heladera> heladeras = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public List<Heladera> findAll() {
        return heladeras.values().stream()
                .sorted(Comparator.comparing(Heladera::id))
                .toList();
    }

    public Optional<Heladera> findById(Long id) {
        return Optional.ofNullable(heladeras.get(id));
    }

    /**
     * Si la heladera no tiene id, se crea con uno nuevo.
     * Si tiene id, se reemplaza la existente.
     */
    public Heladera save(Heladera heladera) {
        Heladera guardada = heladera.id() != null
                ? heladera
                : heladera.conId(idGenerator.incrementAndGet());

        heladeras.put(guardada.id(), guardada);
        return guardada;
    }

    public boolean deleteById(Long id) {
        return heladeras.remove(id) != null;
    }
}

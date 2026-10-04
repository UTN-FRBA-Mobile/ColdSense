package com.coldwatch.backend.repository;

import com.coldwatch.backend.model.Lectura;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Historial de lecturas en memoria, agrupado por id de heladera.
 */
@Repository
public class LecturaRepository {

    private final Map<Long, List<Lectura>> lecturasPorHeladera = new ConcurrentHashMap<>();

    public void agregar(Long heladeraId, Lectura lectura) {
        lecturasPorHeladera
                .computeIfAbsent(heladeraId, id -> Collections.synchronizedList(new ArrayList<>()))
                .add(lectura);
    }

    /**
     * Devuelve las últimas lecturas, de la más nueva a la más vieja.
     */
    public List<Lectura> ultimas(Long heladeraId, int limite) {
        List<Lectura> lecturas = lecturasPorHeladera.getOrDefault(heladeraId, List.of());

        synchronized (lecturas) {
            return lecturas.stream()
                    .sorted(Comparator.comparingLong(Lectura::fecha).reversed())
                    .limit(limite)
                    .toList();
        }
    }

    public void eliminarTodas(Long heladeraId) {
        lecturasPorHeladera.remove(heladeraId);
    }
}

package com.coldwatch.backend.service;

import com.coldwatch.backend.dto.HeladeraRequest;
import com.coldwatch.backend.dto.ParametrosRequest;
import com.coldwatch.backend.model.Heladera;
import com.coldwatch.backend.model.Lectura;
import com.coldwatch.backend.repository.HeladeraRepository;
import com.coldwatch.backend.repository.LecturaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HeladeraService {

    private static final int INTERVALO_DEFAULT = 10;
    private static final int BATERIA_INICIAL = 100;
    private static final int MAX_LECTURAS = 100;

    private final HeladeraRepository heladeraRepository;
    private final LecturaRepository lecturaRepository;

    public HeladeraService(HeladeraRepository heladeraRepository, LecturaRepository lecturaRepository) {
        this.heladeraRepository = heladeraRepository;
        this.lecturaRepository = lecturaRepository;
    }

    public List<Heladera> listar() {
        return heladeraRepository.findAll();
    }

    public Heladera obtener(Long id) {
        return heladeraRepository.findById(id)
                .orElseThrow(() -> noEncontrada(id));
    }

    public List<Lectura> ultimasLecturas(Long id, int limite) {
        obtener(id); // tira 404 si no existe
        int limiteSeguro = Math.max(1, Math.min(limite, MAX_LECTURAS));
        return lecturaRepository.ultimas(id, limiteSeguro);
    }

    public Heladera crear(HeladeraRequest request) {
        Long fecha = request.temperaturaActual() != null ? System.currentTimeMillis() : null;
        int intervalo = request.intervaloLecturaMinutos() != null
                ? request.intervaloLecturaMinutos()
                : INTERVALO_DEFAULT;

        Heladera nueva = heladeraRepository.save(new Heladera(
                null,
                request.nombre().trim(),
                request.temperaturaActual(),
                request.temperaturaMinima(),
                request.temperaturaMaxima(),
                BATERIA_INICIAL,
                fecha,
                intervalo
        ));

        if (fecha != null) {
            lecturaRepository.agregar(nueva.id(), new Lectura(fecha, request.temperaturaActual()));
        }
        return nueva;
    }

    /**
     * Modifica nombre, límites e intervalo. La temperatura se cambia con registrarLectura.
     */
    public Heladera actualizar(Long id, HeladeraRequest request) {
        Heladera actual = obtener(id);
        int intervalo = request.intervaloLecturaMinutos() != null
                ? request.intervaloLecturaMinutos()
                : actual.intervaloLecturaMinutos();

        return heladeraRepository.save(actual.conParametros(
                request.nombre().trim(),
                request.temperaturaMinima(),
                request.temperaturaMaxima(),
                intervalo
        ));
    }

    public Heladera actualizarParametros(Long id, ParametrosRequest request) {
        Heladera actual = obtener(id);

        return heladeraRepository.save(actual.conParametros(
                actual.nombre(),
                request.temperaturaMinima(),
                request.temperaturaMaxima(),
                request.intervaloLecturaMinutos()
        ));
    }

    /**
     * Simula una lectura del sensor. Con temperatura null se simula una desconexión
     * (se conserva la fecha de la última lectura válida).
     */
    public Heladera registrarLectura(Long id, Double temperatura) {
        Heladera actual = obtener(id);

        if (temperatura == null) {
            return heladeraRepository.save(actual.conLectura(null, actual.ultimaLectura()));
        }

        long ahora = System.currentTimeMillis();
        lecturaRepository.agregar(id, new Lectura(ahora, temperatura));
        return heladeraRepository.save(actual.conLectura(temperatura, ahora));
    }

    public void eliminar(Long id) {
        if (!heladeraRepository.deleteById(id)) {
            throw noEncontrada(id);
        }
        lecturaRepository.eliminarTodas(id);
    }

    private ResponseStatusException noEncontrada(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la heladera con id " + id);
    }
}

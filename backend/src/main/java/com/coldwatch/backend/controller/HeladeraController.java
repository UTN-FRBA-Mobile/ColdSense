package com.coldwatch.backend.controller;

import com.coldwatch.backend.dto.HeladeraRequest;
import com.coldwatch.backend.dto.ParametrosRequest;
import com.coldwatch.backend.dto.TemperaturaRequest;
import com.coldwatch.backend.model.Heladera;
import com.coldwatch.backend.model.Lectura;
import com.coldwatch.backend.service.HeladeraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints:
 *   GET    /heladeras                          -> lista todas
 *   GET    /heladeras/{id}                     -> una heladera
 *   GET    /heladeras/{id}/lecturas?limit=5    -> últimas lecturas (más nueva primero)
 *   POST   /heladeras                          -> crea una heladera (201). Con "numeroSerie" vincula un sensor encontrado
 *   PUT    /heladeras/{id}                     -> modifica nombre / límites / intervalo
 *   PUT    /heladeras/{id}/parametros          -> modifica límites e intervalo (pantalla Editar parámetros)
 *   PUT    /heladeras/{id}/temperatura         -> simula una lectura del sensor
 *   DELETE /heladeras/{id}                     -> elimina la heladera y su historial (204)
 */
@RestController
@RequestMapping("/heladeras")
public class HeladeraController {

    private final HeladeraService service;

    public HeladeraController(HeladeraService service) {
        this.service = service;
    }

    @GetMapping
    public List<Heladera> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Heladera obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/{id}/lecturas")
    public List<Lectura> lecturas(@PathVariable Long id,
                                  @RequestParam(defaultValue = "10") int limit) {
        return service.ultimasLecturas(id, limit);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Heladera crear(@Valid @RequestBody HeladeraRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public Heladera actualizar(@PathVariable Long id, @Valid @RequestBody HeladeraRequest request) {
        return service.actualizar(id, request);
    }

    @PutMapping("/{id}/parametros")
    public Heladera actualizarParametros(@PathVariable Long id, @Valid @RequestBody ParametrosRequest request) {
        return service.actualizarParametros(id, request);
    }

    @PutMapping("/{id}/temperatura")
    public Heladera registrarLectura(@PathVariable Long id, @RequestBody TemperaturaRequest request) {
        return service.registrarLectura(id, request.temperatura());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}

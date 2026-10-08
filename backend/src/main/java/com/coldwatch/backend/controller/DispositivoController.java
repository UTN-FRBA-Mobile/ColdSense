package com.coldwatch.backend.controller;

import com.coldwatch.backend.model.DispositivoEncontrado;
import com.coldwatch.backend.service.DispositivoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints:
 *   POST /dispositivos/busqueda   -> busca sensores nuevos (tarda unos segundos y siempre encuentra uno)
 *
 * Para vincular el sensor encontrado se crea la heladera con POST /heladeras enviando su "numeroSerie".
 */
@RestController
@RequestMapping("/dispositivos")
public class DispositivoController {

    private final DispositivoService service;

    public DispositivoController(DispositivoService service) {
        this.service = service;
    }

    @PostMapping("/busqueda")
    public List<DispositivoEncontrado> buscar() {
        return service.buscar();
    }
}

package com.coldwatch.backend.controller;

import com.coldwatch.backend.dto.RegistroRequest;
import com.coldwatch.backend.service.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints:
 *   POST /notificaciones/registro   -> registra un celular (su fid de Firebase) para recibir alertas (204)
 */
@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registrarCelular(@Valid @RequestBody RegistroRequest request) {
        service.registrarCelular(request.fid());
    }
}

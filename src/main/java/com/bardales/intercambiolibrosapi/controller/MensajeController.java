package com.bardales.intercambiolibrosapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bardales.intercambiolibrosapi.dto.MensajeDTO;
import com.bardales.intercambiolibrosapi.dto.MensajeEnviarDTO;
import com.bardales.intercambiolibrosapi.service.MensajeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin(origins = "*")
public class MensajeController {

    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping("/solicitud/{id}")
    public List<MensajeDTO> listarPorSolicitud(
            @PathVariable("id") int idSolicitud,
            @RequestHeader("X-User-Id") int idUsuario) {
        return mensajeService.listarMensajes(idSolicitud, idUsuario);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> enviar(
            @RequestHeader("X-User-Id") int idUsuario,
            @Valid @RequestBody MensajeEnviarDTO dto) {
        mensajeService.enviarMensaje(dto, idUsuario);
        return ResponseEntity.status(201)
                .body(Map.of("mensaje", "Mensaje enviado"));
    }
}

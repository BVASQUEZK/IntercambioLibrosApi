package com.bardales.intercambiolibrosapi.controller;

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

import com.bardales.intercambiolibrosapi.dto.ResenaCrearDTO;
import com.bardales.intercambiolibrosapi.dto.ResenaEstadoDTO;
import com.bardales.intercambiolibrosapi.service.ResenaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/resenas")
@CrossOrigin(origins = "*")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @GetMapping("/{idSolicitud}/estado")
    public ResenaEstadoDTO obtenerEstado(
            @PathVariable("idSolicitud") int idSolicitud,
            @RequestHeader("X-User-Id") int idUsuario) {
        return resenaService.obtenerEstadoResena(idSolicitud, idUsuario);
    }

    @PostMapping("/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> registrarResena(
            @PathVariable("idSolicitud") int idSolicitud,
            @RequestHeader("X-User-Id") int idUsuario,
            @Valid @RequestBody ResenaCrearDTO dto) {
        resenaService.registrarResena(idSolicitud, idUsuario, dto);
        return ResponseEntity.status(201)
                .body(Map.of("mensaje", "Resena registrada", "id_solicitud", idSolicitud));
    }
}

package com.bardales.intercambiolibrosapi.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.bardales.intercambiolibrosapi.dto.ResenaCrearDTO;
import com.bardales.intercambiolibrosapi.dto.ResenaEstadoDTO;
import com.bardales.intercambiolibrosapi.security.AuthenticatedUserUtil;
import com.bardales.intercambiolibrosapi.service.ResenaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @GetMapping("/{idSolicitud}/estado")
    public ResenaEstadoDTO obtenerEstado(
            @PathVariable("idSolicitud") int idSolicitud,
            Authentication authentication) {
        int idUsuario = AuthenticatedUserUtil.getUserId(authentication);
        return resenaService.obtenerEstadoResena(idSolicitud, idUsuario);
    }

    @PostMapping("/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> registrarResena(
            @PathVariable("idSolicitud") int idSolicitud,
            Authentication authentication,
            @Valid @RequestBody ResenaCrearDTO dto) {
        int idUsuario = AuthenticatedUserUtil.getUserId(authentication);
        resenaService.registrarResena(idSolicitud, idUsuario, dto);
        return ResponseEntity.status(201)
                .body(Map.of("mensaje", "Resena registrada", "id_solicitud", idSolicitud));
    }
}

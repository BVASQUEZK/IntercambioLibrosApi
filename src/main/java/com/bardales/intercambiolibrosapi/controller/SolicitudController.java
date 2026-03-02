package com.bardales.intercambiolibrosapi.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.bardales.intercambiolibrosapi.dto.SolicitudCrearDTO;
import com.bardales.intercambiolibrosapi.dto.SolicitudResumenDTO;
import com.bardales.intercambiolibrosapi.security.AuthenticatedUserUtil;
import com.bardales.intercambiolibrosapi.service.SolicitudService;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private static final Set<String> ESTADOS_PERMITIDOS = Set.of(
            "pendiente", "aceptado", "rechazado", "finalizado", "cancelado");

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody SolicitudCrearDTO dto) {
        Integer idSolicitud = solicitudService.registrarSolicitud(dto);
        return ResponseEntity.status(201)
                .body(Map.of("mensaje", "Solicitud registrada correctamente", "id_solicitud", idSolicitud));
    }

    @GetMapping("/usuario")
    public List<SolicitudResumenDTO> listarPorUsuarioHeader(Authentication authentication) {
        int idUsuario = AuthenticatedUserUtil.getUserId(authentication);
        return solicitudService.listarPorUsuario(idUsuario);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> actualizarEstado(
            @PathVariable("id") int idSolicitud,
            Authentication authentication,
            @RequestParam("estado") String nuevoEstado) {
        int idUsuario = AuthenticatedUserUtil.getUserId(authentication);
        String estado = nuevoEstado == null ? "" : nuevoEstado.trim().toLowerCase();
        if (!ESTADOS_PERMITIDOS.contains(estado)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Estado inválido", "estado", nuevoEstado));
        }
        solicitudService.actualizarEstado(idSolicitud, estado, idUsuario);
        return ResponseEntity.ok(Map.of("mensaje", "Estado actualizado", "id_solicitud", idSolicitud));
    }
}

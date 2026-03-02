package com.bardales.intercambiolibrosapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bardales.intercambiolibrosapi.dto.RegisterRequestDTO;
import com.bardales.intercambiolibrosapi.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioAuthController {

    private final UsuarioService usuarioService;

    public UsuarioAuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<java.util.Map<String, Object>> registrar(@RequestBody RegisterRequestDTO body) {
        String nombres = body.getNombres();
        String apellidos = body.getApellidos();
        String correo = body.getCorreo();
        String clave = body.getClave();
        String dni = body.getDni();
        if (nombres == null || nombres.isBlank()
                || apellidos == null || apellidos.isBlank()
                || correo == null || correo.isBlank()
                || clave == null || clave.isBlank()
                || dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("Faltan campos obligatorios");
        }
        java.util.Map<String, Object> response = usuarioService.registrarUsuario(
                nombres, apellidos, correo, clave, dni);
        return ResponseEntity.status(201).body(response);
    }
}

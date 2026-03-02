package com.bardales.intercambiolibrosapi.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.bardales.intercambiolibrosapi.dto.LoginResponseDTO;
import com.bardales.intercambiolibrosapi.dto.PerfilUsuarioDTO;
import com.bardales.intercambiolibrosapi.dto.UsuarioUpdateDTO;
import com.bardales.intercambiolibrosapi.security.AuthenticatedUserUtil;
import com.bardales.intercambiolibrosapi.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/perfil/{id}")
    public PerfilUsuarioDTO obtenerPerfil(@PathVariable("id") int idUsuario) {
        return usuarioService.obtenerPerfil(idUsuario);
    }

    @GetMapping("/me")
    public PerfilUsuarioDTO obtenerMiPerfil(Authentication authentication) {
        int idUsuario = AuthenticatedUserUtil.getUserId(authentication);
        return usuarioService.obtenerPerfil(idUsuario);
    }

    @PutMapping("/perfil")
    public LoginResponseDTO actualizarPerfil(
            Authentication authentication,
            @Valid @RequestBody UsuarioUpdateDTO dto) {
        int idUsuario = AuthenticatedUserUtil.getUserId(authentication);
        return usuarioService.actualizarPerfil(idUsuario, dto);
    }

    @PostMapping("/puntos/anuncio")
    public Map<String, Object> sumarPuntoPorAnuncio(Authentication authentication) {
        int idUsuario = AuthenticatedUserUtil.getUserId(authentication);
        Integer puntos = usuarioService.sumarPuntoPorAnuncio(idUsuario);
        return Map.of("mensaje", "Punto agregado", "puntos", puntos);
    }
}

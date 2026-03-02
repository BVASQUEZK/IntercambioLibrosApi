package com.bardales.intercambiolibrosapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bardales.intercambiolibrosapi.dto.LoginRequestDTO;
import com.bardales.intercambiolibrosapi.dto.LoginResponseDTO;
import com.bardales.intercambiolibrosapi.exception.TooManyRequestsException;
import com.bardales.intercambiolibrosapi.exception.UnauthorizedException;
import com.bardales.intercambiolibrosapi.security.LoginRateLimiter;
import com.bardales.intercambiolibrosapi.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioService usuarioService;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(UsuarioService usuarioService, LoginRateLimiter loginRateLimiter) {
        this.usuarioService = usuarioService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/api/usuarios/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest) {
        LOGGER.info("Peticion de login recibida para el usuario: {}", request.getCorreo());
        String limitKey = buildRateLimitKey(httpRequest, request == null ? null : request.getCorreo());
        if (loginRateLimiter.isBlocked(limitKey)) {
            long seconds = loginRateLimiter.secondsUntilRelease(limitKey);
            throw new TooManyRequestsException("Demasiados intentos. Intenta nuevamente en " + seconds + " segundos");
        }
        try {
            LoginResponseDTO response = usuarioService.login(request.getCorreo(), request.getPassword());
            loginRateLimiter.reset(limitKey);
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException ex) {
            loginRateLimiter.registerFailure(limitKey);
            throw ex;
        }
    }

    private String buildRateLimitKey(HttpServletRequest request, String correo) {
        String ip = extractClientIp(request);
        String email = correo == null ? "" : correo.trim().toLowerCase();
        return ip + "|" + email;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}

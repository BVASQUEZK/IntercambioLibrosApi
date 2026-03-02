package com.bardales.intercambiolibrosapi.security;

import org.springframework.security.core.Authentication;

import com.bardales.intercambiolibrosapi.exception.UnauthorizedException;

public final class AuthenticatedUserUtil {

    private AuthenticatedUserUtil() {
    }

    public static int getUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No autorizado");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Integer id) {
            return id;
        }
        if (principal instanceof String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                throw new UnauthorizedException("No autorizado");
            }
        }
        throw new UnauthorizedException("No autorizado");
    }
}

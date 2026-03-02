package com.bardales.intercambiolibrosapi.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.bardales.intercambiolibrosapi.dto.ErrorResponseDTO;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponseDTO body = new ErrorResponseDTO(LocalDateTime.now(), ex.getMessage(), "NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorized(UnauthorizedException ex) {
        ErrorResponseDTO body = new ErrorResponseDTO(LocalDateTime.now(), ex.getMessage(), "UNAUTHORIZED", "AUTH_001");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseDTO> handleForbidden(ForbiddenException ex) {
        ErrorResponseDTO body = new ErrorResponseDTO(LocalDateTime.now(), ex.getMessage(), "FORBIDDEN", "AUTH_002");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponseDTO> handleTooManyRequests(TooManyRequestsException ex) {
        ErrorResponseDTO body = new ErrorResponseDTO(LocalDateTime.now(), ex.getMessage(), "TOO_MANY_REQUESTS", "AUTH_003");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponseDTO body = new ErrorResponseDTO(LocalDateTime.now(), ex.getMessage(), "BAD_REQUEST", "REQ_001");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingHeader(MissingRequestHeaderException ex) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                "Solicitud invalida",
                "BAD_REQUEST",
                "REQ_002");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex) {
        LOGGER.error("Error interno no controlado", ex);
        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                "Ocurrio un error interno. Intenta nuevamente.",
                "INTERNAL_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

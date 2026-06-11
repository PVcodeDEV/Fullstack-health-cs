package com.clinica.persona.service;

/**
 * Thrown when a DNI fails módulo 11 check digit validation.
 * Mapped to HTTP 422 (Unprocessable Entity) by {@link com.clinica.config.GlobalExceptionHandler}.
 */
public class DniInvalidoException extends RuntimeException {

    public DniInvalidoException(String message) {
        super(message);
    }
}

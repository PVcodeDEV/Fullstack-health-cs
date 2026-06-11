package com.clinica.persona.service.api;

/**
 * Internal exception for external API consultation failures.
 * <p>
 * Used for flow control within API client implementations. Caught before
 * reaching callers and converted to {@link java.util.Optional#empty()}.
 * Messages must NOT contain PII (document numbers, names, or addresses).
 */
public class ApiConsultaException extends RuntimeException {

    public ApiConsultaException(String message) {
        super(message);
    }

    public ApiConsultaException(String message, Throwable cause) {
        super(message, cause);
    }
}

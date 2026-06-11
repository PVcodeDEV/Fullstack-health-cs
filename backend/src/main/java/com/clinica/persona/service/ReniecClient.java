package com.clinica.persona.service;

import com.clinica.persona.service.api.PersonaDatos;

import java.util.Optional;

/**
 * Pluggable client for querying identity document data from external APIs.
 * <p>
 * Implementations consult national registries (RENIEC, SUNAT, or secure providers)
 * to auto-fill Persona fields like names, address, and birthdate.
 * They must never throw — wrapping all failures in {@link Optional#empty()}.
 */
public interface ReniecClient {

    /**
     * Consults external identity API for a given DNI number.
     *
     * @param dni the 8-digit DNI number (without check digit)
     * @return {@link Optional} containing {@link PersonaDatos} if the API call succeeds,
     *         or {@link Optional#empty()} on any failure
     */
    Optional<PersonaDatos> consultarPorDni(String dni);
}

package com.clinica.persona.service.api;

import java.time.LocalDate;

/**
 * Data record returned by external identity API clients.
 * <p>
 * Fields may be partially populated depending on the provider:
 * <ul>
 *   <li>SUNAT (free): only {@code nombres}, {@code apellidoPaterno}, {@code apellidoMaterno}</li>
 *   <li>Secure (paid): all fields populated</li>
 * </ul>
 *
 * @param nombres           person's first names
 * @param apellidoPaterno   paternal surname
 * @param apellidoMaterno   maternal surname (also "apellido materno" in SUNAT terminology)
 * @param direccion         street address
 * @param ubigeoDistrito    UBIGEO code for district (6 digits)
 * @param fechaNacimiento   date of birth
 * @param sexo              sex (M/F)
 */
public record PersonaDatos(
    String nombres,
    String apellidoPaterno,
    String apellidoMaterno,
    String direccion,
    String ubigeoDistrito,
    LocalDate fechaNacimiento,
    String sexo
) {}

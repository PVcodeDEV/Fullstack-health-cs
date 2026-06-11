package com.clinica.persona.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Modulo11Validator {

    private static final Logger log = LoggerFactory.getLogger(Modulo11Validator.class);

    private static final int[] PESOS = {3, 2, 7, 6, 5, 4, 3, 2};

    /**
     * Validates a Peruvian DNI check digit using módulo 11 algorithm.
     * <p>
     * Accepts both formats:
     * <ul>
     *   <li>8 digits (no check digit) — always passes, no verification applied</li>
     *   <li>9 digits (8 + check digit) — validates the check digit using módulo 11</li>
     * </ul>
     * Each of the first 8 digits is multiplied by its corresponding weight (3,2,7,6,5,4,3,2),
     * the products are summed, and the remainder of division by 11 determines the expected check digit:
     * <ul>
     *   <li>remainder 0 → check digit must be 6</li>
     *   <li>remainder 1 → invalid DNI (no valid check digit exists)</li>
     *   <li>otherwise → expected = 11 - remainder</li>
     * </ul>
     *
     * @param dni the DNI string (8 or 9 digits)
     * @return {@code true} if valid or no check digit to verify, {@code false} if check digit is incorrect
     */
    public boolean validar(String dni) {
        if (dni == null || !dni.chars().allMatch(Character::isDigit)) {
            log.debug("DNI validation skipped: invalid format");
            return false;
        }

        // 8 digits → no check digit to verify, accept as-is
        if (dni.length() == 8) {
            return true;
        }

        // 9 digits → validate check digit
        if (dni.length() != 9) {
            log.debug("DNI validation skipped: unexpected length {}", dni.length());
            return false;
        }

        int suma = 0;
        for (int i = 0; i < 8; i++) {
            suma += Character.getNumericValue(dni.charAt(i)) * PESOS[i];
        }

        int resto = suma % 11;
        int digito = Character.getNumericValue(dni.charAt(8));

        if (resto == 0) {
            return digito == 6;
        }
        if (resto == 1) {
            return false;
        }

        int digitoEsperado = 11 - resto;
        return digito == digitoEsperado;
    }
}

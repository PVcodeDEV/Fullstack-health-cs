package com.clinica.entidad.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * RUC digit validator using modulo 11 algorithm with RUC-specific weight sequence.
 * <p>
 * RUC format: 11 digits. The last digit (position 10) is the check digit.
 * Weight sequence for the first 10 digits: {5, 4, 3, 2, 7, 6, 5, 4, 3, 2}
 * <p>
 * Algorithm:
 * <ol>
 *   <li>Multiply each of the first 10 digits by its corresponding weight</li>
 *   <li>Sum the products</li>
 *   <li>Compute remainder = sum % 11</li>
 *   <li>Expected check digit: remainder 0 → 0, remainder 1 → 1, otherwise → 11 - remainder</li>
 *   <li>Compare expected check digit with digit[10]</li>
 * </ol>
 */
@Component
public class RucValidator {

    private static final Logger log = LoggerFactory.getLogger(RucValidator.class);

    private static final int[] PESOS_RUC = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private static final int RUC_LENGTH = 11;

    /**
     * Validates a Peruvian RUC number using modulo 11 algorithm.
     *
     * @param ruc the RUC string (must be 11 digits)
     * @return {@code true} if the RUC has a valid check digit
     */
    public boolean validar(String ruc) {
        if (ruc == null || !ruc.chars().allMatch(Character::isDigit)) {
            log.debug("RUC validation skipped: invalid format");
            return false;
        }

        if (ruc.length() != RUC_LENGTH) {
            log.debug("RUC validation skipped: unexpected length {}", ruc.length());
            return false;
        }

        int suma = 0;
        for (int i = 0; i < 10; i++) {
            suma += Character.getNumericValue(ruc.charAt(i)) * PESOS_RUC[i];
        }

        int resto = suma % 11;
        int digito = Character.getNumericValue(ruc.charAt(10));

        if (resto == 0) {
            return digito == 0;
        }
        if (resto == 1) {
            return digito == 1;
        }

        int digitoEsperado = 11 - resto;
        return digito == digitoEsperado;
    }
}

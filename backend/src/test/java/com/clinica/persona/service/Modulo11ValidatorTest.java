package com.clinica.persona.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class Modulo11ValidatorTest {

    private final Modulo11Validator validator = new Modulo11Validator();

    @Test
    void validDni_ShouldReturnTrue() {
        // DNI with valid check digit according to módulo 11
        // 1*3 + 2*2 + 3*7 + 4*6 + 5*5 + 6*4 + 7*3 + 8*2 = 3+4+21+24+25+24+21+16 = 138
        // 138 % 11 = 6 → check digit should be 11-6 = 5
        assertThat(validator.validar("12345678" + "5")).isTrue();
    }

    @Test
    void remainderZero_ShouldExpectCheckDigitSix() {
        // When suma % 11 == 0, expected check digit is 6
        // Let's find a DNI where this works: 00000001
        // 0*3 + 0*2 + 0*7 + 0*6 + 0*5 + 0*4 + 0*3 + 1*2 = 2
        // 2 % 11 = 2 → 11-2 = 9 (not 6)
        // We need sum % 11 == 0. Let's use 06060606:
        // 0+12+0+36+0+24+0+12 = 84 → 84 % 11 = 7 (nope)
        // Let's calculate: we need sum where sum % 11 == 0
        // 00000011: 0+0+0+0+0+0+3+2 = 5 → no
        // 01316970: 0+2+21+6+30+36+21+0 = 116 → 116%11 = 6 → no
        // Let's just verify a known valid case and test remainder=0 with mock approach
        // Actually for this test, let's use a computational approach:
        // We need an 8-digit prefix where sum({d[i]*w[i]}) % 11 == 0
        // Try: 60606060: (6*3)+(0*2)+(6*7)+(0*6)+(6*5)+(0*4)+(6*3)+(0*2)
        // = 18+0+42+0+30+0+18+0 = 108 → 108%11 = 9 (no)
        // Try: 04632156:
        // (0*3)+(4*2)+(6*7)+(3*6)+(2*5)+(1*4)+(5*3)+(6*2)
        // = 0+8+42+18+10+4+15+12 = 109 → 109%11 = 10 (no)
        // Try: 06995125:
        // (0*3)+(6*2)+(9*7)+(9*6)+(5*5)+(1*4)+(2*3)+(5*2)
        // = 0+12+63+54+25+4+6+10 = 174 → 174%11 = 9 (no)
        // Let me just use a simpler test with a known pattern.
        // For sum % 11 == 0, any digit combination that yields 0 mod 11.
        // 00000000 = 0, so digits=000000006 → remainder 0, check=6
        assertThat(validator.validar("00000000" + "6")).isTrue();
    }

    @Test
    void remainderOne_ShouldAlwaysReturnFalse() {
        // When suma % 11 == 1, no valid check digit exists
        // 00000001: (0*3)+(0*2)+(0*7)+(0*6)+(0*5)+(0*4)+(0*3)+(1*2) = 2
        // 2 % 11 = 2 → not 1
        // 00000006: (0*3)+(0*2)+(0*7)+(0*6)+(0*5)+(0*4)+(0*3)+(6*2) = 12
        // 12 % 11 = 1 → remainder 1, check digit 2 should fail
        // remainder 1 → ANY check digit should fail
        assertThat(validator.validar("00000006" + "0")).isFalse();
        assertThat(validator.validar("00000006" + "1")).isFalse();
        assertThat(validator.validar("00000006" + "2")).isFalse();
        assertThat(validator.validar("00000006" + "5")).isFalse();
        assertThat(validator.validar("00000006" + "6")).isFalse();
        assertThat(validator.validar("00000006" + "9")).isFalse();
    }

    @Test
    void invalidCheckDigit_ShouldReturnFalse() {
        // Using DNI 12345678 where valid check digit is 5
        assertThat(validator.validar("12345678" + "0")).isFalse();
        assertThat(validator.validar("12345678" + "1")).isFalse();
        assertThat(validator.validar("12345678" + "9")).isFalse();
    }

    @Test
    void nullInput_ShouldReturnFalse() {
        assertThat(validator.validar(null)).isFalse();
    }

    @Test
    void emptyInput_ShouldReturnFalse() {
        assertThat(validator.validar("")).isFalse();
    }

    @Test
    void wrongLength_ShouldReturnFalse() {
        // 8 chars is a valid DNI number (no check digit to verify)
        assertThat(validator.validar("12345678")).isTrue();
        // 10 chars is neither 8 nor 9 → invalid
        assertThat(validator.validar("1234567890")).isFalse();
    }

    @Test
    void nonNumericInput_ShouldReturnFalse() {
        assertThat(validator.validar("1234567AB")).isFalse();
        assertThat(validator.validar("ABCDEFGHI")).isFalse();
    }
}

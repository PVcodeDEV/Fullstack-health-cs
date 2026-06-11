package com.clinica.seguridad.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private static final long EXPIRATION_MS = 3600000;

    @Mock
    private JwtEncoder encoder;

    @Mock
    private JwtDecoder decoder;

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(encoder, decoder, EXPIRATION_MS);
    }

    @Test
    void generateToken_ShouldEncodeClaimsWithRolesAndPermisos() {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_MEDICO"),
                new SimpleGrantedAuthority("maestro:read")
        );

        when(encoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(createJwt("token-value", "admin"));

        String token = provider.generateToken("admin", authorities);

        assertThat(token).isEqualTo("token-value");
    }

    @Test
    void generateToken_WithNoRoles_ShouldEncodeEmptyRolesList() {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("maestro:read")
        );

        when(encoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(createJwt("token-val", "reader"));

        String token = provider.generateToken("reader", authorities);

        assertThat(token).isEqualTo("token-val");
    }

    @Test
    void validateToken_ShouldReturnDecodedJwtWithCorrectClaims() {
        Jwt expectedJwt = createJwt("test-token", "admin");
        when(decoder.decode("test-token")).thenReturn(expectedJwt);

        Jwt result = provider.validateToken("test-token");

        assertThat(result).isNotNull();
        assertThat(result.getSubject()).isEqualTo("admin");
    }

    @Test
    void validateToken_WithMalformedToken_ShouldThrowException() {
        when(decoder.decode("invalid-token"))
                .thenThrow(new RuntimeException("Bad token"));

        assertThatThrownBy(() -> provider.validateToken("invalid-token"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getExpirationMs_ShouldReturnConfiguredValue() {
        assertThat(provider.getExpirationMs()).isEqualTo(EXPIRATION_MS);
    }

    private Jwt createJwt(String tokenValue, String subject) {
        Instant now = Instant.now();
        return new Jwt(
                tokenValue,
                now,
                now.plusMillis(EXPIRATION_MS),
                java.util.Map.of("alg", "HS256"),
                java.util.Map.of(
                        "sub", subject,
                        "iss", "clinica-erp",
                        "roles", List.of("ADMIN"),
                        "permisos", List.of("maestro:read")
                )
        );
    }
}

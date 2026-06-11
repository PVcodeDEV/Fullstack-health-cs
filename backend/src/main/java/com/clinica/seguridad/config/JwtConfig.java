package com.clinica.seguridad.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Provides {@link JwtDecoder} and {@link JwtEncoder} beans for the
 * OAuth2 Resource Server JWT authentication chain.
 *
 * <p>Reads the HMAC-SHA256 secret key from {@code app.jwt.secret}.</p>
 */
@Configuration
public class JwtConfig {

    private static final String ALGORITHM = "HmacSHA256";

    private final SecretKey secretKey;

    public JwtConfig(@Value("${app.jwt.secret}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.length() < 32) {
            throw new IllegalArgumentException(
                "app.jwt.secret must be at least 32 characters long");
        }
        this.secretKey = new SecretKeySpec(jwtSecret.getBytes(), ALGORITHM);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        JWKSource<SecurityContext> immutableSecret = new ImmutableSecret<>(secretKey);
        return new NimbusJwtEncoder(immutableSecret);
    }
}

package com.clinica.seguridad.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final long expirationMs;

    public JwtTokenProvider(JwtEncoder encoder,
                            JwtDecoder decoder,
                            @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT token for the given username with roles and permissions as claims.
     */
    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = authorities.stream()
            .filter(a -> a.getAuthority().startsWith("ROLE_"))
            .map(a -> a.getAuthority().substring(5))
            .toList();

        List<String> permisos = authorities.stream()
            .filter(a -> !a.getAuthority().startsWith("ROLE_"))
            .map(GrantedAuthority::getAuthority)
            .toList();

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("clinica-erp")
            .subject(username)
            .issuedAt(now)
            .expiresAt(now.plusMillis(expirationMs))
            .claim("roles", roles)
            .claim("permisos", permisos)
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        log.debug("JWT generated for user '{}' with {} roles and {} permisos",
            username, roles.size(), permisos.size());
        return token;
    }

    /**
     * Returns the configured JWT expiration time in milliseconds.
     */
    public long getExpirationMs() {
        return expirationMs;
    }

    /**
     * Validates a JWT token and returns the decoded Jwt.
     */
    public Jwt validateToken(String token) {
        return decoder.decode(token);
    }
}

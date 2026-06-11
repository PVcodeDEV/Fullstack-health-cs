package com.clinica.seguridad.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom {@link JwtAuthenticationConverter} that extracts roles and permissions
 * from the custom JWT claims ("roles" and "permisos") in addition to the
 * default scope-based authorities.
 *
 * <p>This allows {@code @PreAuthorize("hasRole('ADMIN')")} and
 * {@code @PreAuthorize("hasAuthority('maestro:read')")} to work with our
 * custom JWT claims.</p>
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // Add scope-level authorities from default converter
        authorities.addAll(delegate.convert(jwt));

        // Add ROLE_ authorities from "roles" claim
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
        }

        // Add permission authorities from "permisos" claim
        List<String> permisos = jwt.getClaimAsStringList("permisos");
        if (permisos != null) {
            permisos.stream()
                    .map(permiso -> new SimpleGrantedAuthority(permiso))
                    .forEach(authorities::add);
        }

        String principalName = jwt.getSubject();
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }
}

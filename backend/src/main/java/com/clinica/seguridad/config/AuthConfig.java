package com.clinica.seguridad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

/**
 * Exposes the {@link AuthenticationManager} as a bean for use in {@code AuthController}.
 *
 * <p>Spring Boot auto-configures the {@link AuthenticationConfiguration} when
 * {@code UsuarioDetailsService} and {@code PasswordEncoder} are present.
 * This config simply extracts the manager from that configuration.</p>
 */
@Configuration
public class AuthConfig {

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

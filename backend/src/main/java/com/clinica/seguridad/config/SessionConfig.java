package com.clinica.seguridad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Configures session infrastructure beans shared across all portal chains.
 *
 * <p>Extracted from {@link SecurityConfig} to avoid circular bean creation:
 * SecurityConfig's constructor needs {@link SessionRegistry}, but the
 * sessionRegistry() @Bean also lives on SecurityConfig — extracted here
 * so Spring can create the bean freely.</p>
 */
@Configuration
public class SessionConfig {

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}

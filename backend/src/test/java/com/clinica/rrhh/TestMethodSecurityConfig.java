package com.clinica.rrhh;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Minimal security config for @WebMvcTest controller tests.
 * Enables method-level @PreAuthorise so that @WithMockUser works.
 */
@Configuration
@EnableMethodSecurity
public class TestMethodSecurityConfig {
}

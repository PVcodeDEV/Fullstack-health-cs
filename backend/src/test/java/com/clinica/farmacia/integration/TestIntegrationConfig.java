package com.clinica.farmacia.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Shared test configuration for E2E integration tests.
 * Provides beans required by the full application context that are not
 * available in the test profile (e.g., Clock used by rrhh module).
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestIntegrationConfig {

    @Bean
    Clock clock() {
        return Clock.fixed(Instant.parse("2026-06-07T00:00:00Z"), ZoneId.systemDefault());
    }
}

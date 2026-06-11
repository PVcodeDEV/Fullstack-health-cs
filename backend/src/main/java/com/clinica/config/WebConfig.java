package com.clinica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 * <p>
 * CORS configuration has been moved to {@code com.clinica.seguridad.config.CorsConfig}
 * for profile-aware behavior (dev allows all origins, prod restricts).
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS is now configured in com.clinica.seguridad.config.CorsConfig
}

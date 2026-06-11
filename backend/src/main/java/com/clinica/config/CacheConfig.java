package com.clinica.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enables caching support across the application.
 *
 * <p>{@code @EnableCaching} is placed here (not on the main application class)
 * so that Spring Boot test slices like {@code @DataJpaTest} and
 * {@code @WebMvcTest} are not forced to provide a {@link CacheManager}.
 * This configuration is only loaded during full context boots.</p>
 *
 * <p>A fallback {@link CacheManager} bean is provided as a safety net for
 * sliced tests that still encounter caching support.</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
}

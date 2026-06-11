package com.clinica.seguridad.config;

import com.clinica.seguridad.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration with dual authentication chains:
 * <ol>
 *   <li>API chain (order 1): stateless JWT via OAuth2 Resource Server for {@code /api/**} endpoints</li>
 *   <li>Browser chain (order 2): form-based login with HTTP session for Thymeleaf views</li>
 * </ol>
 *
 * Both chains share the same {@link UsuarioDetailsService} for user lookup.
 * Method-level security is enabled via {@link EnableMethodSecurity}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtAuthConverter jwtAuthConverter;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService,
                          JwtAuthConverter jwtAuthConverter) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtAuthConverter = jwtAuthConverter;
    }

    /**
     * Auth endpoint chain — stateless, no authentication, permits login.
     * Must have the highest precedence so the login filter doesn't intercept.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/auth/login")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable());
        return http.build();
    }

    /**
     * API security chain — stateless JWT authentication.
     * Applied to all {@code /api/**} requests (excluding login).
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthConverter)
                )
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .userDetailsService(usuarioDetailsService);
        return http.build();
    }

    /**
     * Browser security chain — form-based login with HTTP session.
     * Applied to all requests not matched by the API chain.
     */
    @Bean
    @Order(3)
    public SecurityFilterChain browserFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout"))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()))
            .userDetailsService(usuarioDetailsService);
        return http.build();
    }
}

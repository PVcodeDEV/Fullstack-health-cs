package com.clinica.seguridad.config;

import com.clinica.seguridad.service.UsuarioDetailsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Security configuration with per-portal browser chains for session isolation.
 * <p>
 * Each portal has its own login page, login processing URL, and session cookie
 * scoped to its path. This means users authenticate SEPARATELY per portal.
 * <p>
 * Chain order:
 * <ol>
 *   <li>Auth endpoint (stateless, REST login)</li>
 *   <li>API (stateless, JWT)</li>
 *   <li>Administrativo portal (form login, Path=/administrativo)</li>
 *   <li>Farmacia portal (form login, Path=/farmacia)</li>
 *   <li>Caja portal (form login, Path=/caja)</li>
 *   <li>Asistencial portal (form login, Path=/asistencial)</li>
 *   <li>Default (shared pages: login, password change, static resources)</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtAuthConverter jwtAuthConverter;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService,
                          JwtAuthConverter jwtAuthConverter,
                          PasswordEncoder passwordEncoder) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtAuthConverter = jwtAuthConverter;
        this.passwordEncoder = passwordEncoder;
    }

    private DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Creates an AuthenticationSuccessHandler that scopes the JSESSIONID cookie
     * to the portal's path after successful login. This ensures portal sessions
     * are isolated — the browser only sends the cookie for requests under the portal path.
     */
    private static AuthenticationSuccessHandler portalSuccessHandler(String portal) {
        return (request, response, authentication) -> {
            var principal = (com.clinica.seguridad.service.UsuarioPrincipal) authentication.getPrincipal();
            scopeSessionCookie(request, response, portal);
            if (Boolean.TRUE.equals(principal.getUsuario().getPasswordChangeRequired())) {
                response.sendRedirect("/" + portal + "/cambiar-contrasena");
            } else {
                response.sendRedirect("/" + portal);
            }
        };
    }

    /**
     * Deletes the global (Path=/) JSESSIONID cookie and creates a new one
     * scoped to the portal path. This makes the browser only send the cookie
     * for requests within that portal's URL path, effectively isolating sessions.
     */
    private static void scopeSessionCookie(HttpServletRequest request, HttpServletResponse response, String portal) {
        String sessionId = request.getSession().getId();
        // Delete the global Path=/ cookie
        Cookie deleteGlobal = new Cookie("JSESSIONID", null);
        deleteGlobal.setPath("/");
        deleteGlobal.setMaxAge(0);
        deleteGlobal.setHttpOnly(true);
        response.addCookie(deleteGlobal);
        // Set portal-scoped cookie with the same session ID
        Cookie portalCookie = new Cookie("JSESSIONID", sessionId);
        portalCookie.setPath("/" + portal);
        portalCookie.setHttpOnly(true);
        portalCookie.setSecure(request.isSecure());
        response.addCookie(portalCookie);
    }

    /**
     * Creates a LogoutHandler that clears the JSESSIONID cookie for BOTH
     * Path=/ (global) and Path=/{portal} (portal-scoped), ensuring the
     * session cookie is fully removed from the browser regardless of path.
     */
    private static org.springframework.security.web.authentication.logout.LogoutHandler portalLogoutHandler(String portal) {
        return (request, response, authentication) -> {
            Cookie deleteGlobal = new Cookie("JSESSIONID", null);
            deleteGlobal.setPath("/");
            deleteGlobal.setMaxAge(0);
            deleteGlobal.setHttpOnly(true);
            response.addCookie(deleteGlobal);
            Cookie deletePortal = new Cookie("JSESSIONID", null);
            deletePortal.setPath("/" + portal);
            deletePortal.setMaxAge(0);
            deletePortal.setHttpOnly(true);
            response.addCookie(deletePortal);
        };
    }

    /**
     * Auth endpoint chain — stateless, no authentication, permits login.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/auth/login")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable());
        return http.build();
    }

    /**
     * API security chain — stateless JWT authentication.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthConverter)))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .userDetailsService(usuarioDetailsService);
        return http.build();
    }

    /**
     * Administrativo portal — isolated session at Path=/administrativo.
     */
    @Bean
    @Order(3)
    public SecurityFilterChain administrativoChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/administrativo/**")
            .authenticationProvider(authProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/administrativo/login", "/administrativo/cambiar-contrasena").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/administrativo/login")
                .loginProcessingUrl("/administrativo/login")
                .successHandler(portalSuccessHandler("administrativo"))
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("ClinicaErpAdminKey2026")
                .tokenValiditySeconds(1209600))
            .logout(logout -> logout
                .logoutUrl("/administrativo/logout")
                .logoutSuccessUrl("/administrativo/login?logout")
                .invalidateHttpSession(true)
                .addLogoutHandler(portalLogoutHandler("administrativo")))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    /**
     * Farmacia portal — isolated session at Path=/farmacia.
     */
    @Bean
    @Order(4)
    public SecurityFilterChain farmaciaChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/farmacia/**")
            .authenticationProvider(authProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/farmacia/login", "/farmacia/cambiar-contrasena").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/farmacia/login")
                .loginProcessingUrl("/farmacia/login")
                .successHandler(portalSuccessHandler("farmacia"))
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("ClinicaErpFarmaciaKey2026")
                .tokenValiditySeconds(1209600))
            .logout(logout -> logout
                .logoutUrl("/farmacia/logout")
                .logoutSuccessUrl("/farmacia/login?logout")
                .invalidateHttpSession(true)
                .addLogoutHandler(portalLogoutHandler("farmacia")))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    /**
     * Caja portal — isolated session at Path=/caja.
     */
    @Bean
    @Order(5)
    public SecurityFilterChain cajaChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/caja/**")
            .authenticationProvider(authProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/caja/login", "/caja/cambiar-contrasena").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/caja/login")
                .loginProcessingUrl("/caja/login")
                .successHandler(portalSuccessHandler("caja"))
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("ClinicaErpCajaKey2026")
                .tokenValiditySeconds(1209600))
            .logout(logout -> logout
                .logoutUrl("/caja/logout")
                .logoutSuccessUrl("/caja/login?logout")
                .invalidateHttpSession(true)
                .addLogoutHandler(portalLogoutHandler("caja")))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    /**
     * Asistencial portal — isolated session at Path=/asistencial.
     */
    @Bean
    @Order(6)
    public SecurityFilterChain asistencialChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/asistencial/**")
            .authenticationProvider(authProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/asistencial/login", "/asistencial/cambiar-contrasena").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/asistencial/login")
                .loginProcessingUrl("/asistencial/login")
                .successHandler(portalSuccessHandler("asistencial"))
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("ClinicaErpAsistencialKey2026")
                .tokenValiditySeconds(1209600))
            .logout(logout -> logout
                .logoutUrl("/asistencial/logout")
                .logoutSuccessUrl("/asistencial/login?logout")
                .invalidateHttpSession(true)
                .addLogoutHandler(portalLogoutHandler("asistencial")))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    /**
     * Default browser chain — shared pages outside portal paths.
     * Login, password change, static resources, h2-console.
     */
    @Bean
    @Order(7)
    public SecurityFilterChain defaultChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/cambiar-contrasena", "/css/**", "/js/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    var principal = (com.clinica.seguridad.service.UsuarioPrincipal) authentication.getPrincipal();
                    if (Boolean.TRUE.equals(principal.getUsuario().getPasswordChangeRequired())) {
                        response.sendRedirect("/cambiar-contrasena");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout"))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}

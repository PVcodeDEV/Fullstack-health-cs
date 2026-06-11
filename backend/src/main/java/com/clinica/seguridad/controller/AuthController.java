package com.clinica.seguridad.controller;

import com.clinica.seguridad.dto.LoginRequest;
import com.clinica.seguridad.dto.LoginResponse;
import com.clinica.seguridad.dto.UsuarioResponse;
import com.clinica.seguridad.entity.Usuario;
import com.clinica.seguridad.repository.UsuarioRepository;
import com.clinica.seguridad.repository.UsuarioRolRepository;
import com.clinica.seguridad.service.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UsuarioRepository usuarioRepository,
                          UsuarioRolRepository usuarioRolRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    /**
     * Public endpoint: authenticates user credentials and returns a JWT token.
     *
     * <p>This endpoint is {@code permitAll()} in the API security chain.
     * It uses {@link AuthenticationManager} to validate credentials against
     * {@code UsuarioDetailsService} and {@code BCryptPasswordEncoder}, then
     * generates a signed HMAC-SHA256 JWT via {@link JwtTokenProvider}.</p>
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login attempt for user '{}'", request.username());

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.username(), request.password());
        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtTokenProvider.generateToken(userDetails.getUsername(),
                userDetails.getAuthorities());

        log.info("User '{}' logged in successfully", request.username());
        return ResponseEntity.ok(
                LoginResponse.fromToken(jwt, request.username(), jwtTokenProvider.getExpirationMs()));
    }

    /**
     * Authenticated endpoint: returns the current user's profile information.
     *
     * <p>Extracts the username from the JWT subject claim via
     * {@link SecurityContextHolder}, loads the full {@link Usuario} entity,
     * and returns a {@link UsuarioResponse} with assigned roles.</p>
     */
    @GetMapping("/me")
    public UsuarioResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));

        List<String> roles = usuarioRolRepository.findByUsuarioId(usuario.getId()).stream()
                .map(ur -> ur.getRol().getCodigo())
                .toList();

        return UsuarioResponse.fromEntity(usuario, roles);
    }
}

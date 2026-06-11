package com.clinica.seguridad.controller;

import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.seguridad.dto.ConfiguracionApiRequest;
import com.clinica.seguridad.dto.ConfiguracionApiResponse;
import com.clinica.seguridad.dto.PermisoResponse;
import com.clinica.seguridad.dto.RolResponse;
import com.clinica.seguridad.dto.UsuarioRequest;
import com.clinica.seguridad.dto.UsuarioResponse;
import com.clinica.seguridad.entity.Rol;
import com.clinica.seguridad.entity.Usuario;
import com.clinica.seguridad.entity.UsuarioRol;
import com.clinica.seguridad.entity.UsuarioRolId;
import com.clinica.seguridad.repository.RolRepository;
import com.clinica.seguridad.repository.UsuarioRepository;
import com.clinica.seguridad.repository.UsuarioRolRepository;
import com.clinica.seguridad.service.ConfiguracionApiService;
import com.clinica.seguridad.service.PermisoService;
import com.clinica.seguridad.service.RolService;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin controller for security module management.
 *
 * <p>All endpoints require {@code ROLE_ADMIN}. Provides CRUD operations for
 * usuarios, roles, permisos, and api configuration entries.</p>
 */
@RestController
@RequestMapping("/api/v1/seguridad")
@PreAuthorize("hasRole('ADMIN')")
public class SeguridadAdminController {

    private static final Logger log = LoggerFactory.getLogger(SeguridadAdminController.class);

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PersonaRepository personaRepository;
    private final RolRepository rolRepository;
    private final RolService rolService;
    private final PermisoService permisoService;
    private final ConfiguracionApiService configuracionApiService;
    private final PasswordEncoder passwordEncoder;

    public SeguridadAdminController(UsuarioRepository usuarioRepository,
                                    UsuarioRolRepository usuarioRolRepository,
                                    PersonaRepository personaRepository,
                                    RolRepository rolRepository,
                                    RolService rolService,
                                    PermisoService permisoService,
                                    ConfiguracionApiService configuracionApiService,
                                    PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.personaRepository = personaRepository;
        this.rolRepository = rolRepository;
        this.rolService = rolService;
        this.permisoService = permisoService;
        this.configuracionApiService = configuracionApiService;
        this.passwordEncoder = passwordEncoder;
    }

    // ──────────────────────────────────────────────
    //  Usuarios
    // ──────────────────────────────────────────────

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listUsuarios() {
        log.debug("Listing all usuarios");
        return usuarioRepository.findAll().stream()
                .map(u -> {
                    List<String> roles = usuarioRolRepository.findByUsuarioId(u.getId()).stream()
                            .map(ur -> ur.getRol().getCodigo())
                            .toList();
                    return UsuarioResponse.fromEntity(u, roles);
                })
                .toList();
    }

    @PostMapping("/usuarios")
    @Transactional
    public ResponseEntity<UsuarioResponse> createUsuario(
            @Valid @RequestBody UsuarioRequest request) {
        log.debug("Creating usuario with username '{}'", request.username());

        if (usuarioRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el username: " + request.username());
        }

        Persona persona = personaRepository.findById(request.personaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Persona no encontrada con id: " + request.personaId()));

        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setUsername(request.username());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));

        if (request.trabajadorId() != null) {
            // Trabajador reference is optional — set via reflection or leave null
            // For now, we only set persona; trabajador can be linked later
            log.debug("Trabajador id {} provided for usuario '{}' but linking deferred",
                    request.trabajadorId(), request.username());
        }

        usuario = usuarioRepository.save(usuario);

        // Assign initial roles if provided
        List<String> roleCodes = List.of();
        if (request.rolIds() != null && !request.rolIds().isEmpty()) {
            roleCodes = assignRolesToUsuario(usuario.getId(), request.rolIds());
        }

        log.info("Usuario '{}' created with id {}", request.username(), usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioResponse.fromEntity(usuario, roleCodes));
    }

    @PutMapping("/usuarios/{id}/roles")
    @Transactional
    public UsuarioResponse assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        log.debug("Assigning roles to usuario {}: {} role ids", id, roleIds != null ? roleIds.size() : 0);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        List<String> roleCodes = assignRolesToUsuario(id, roleIds);

        return UsuarioResponse.fromEntity(usuario, roleCodes);
    }

    /**
     * Replaces all role assignments for a user with the given role IDs.
     */
    private List<String> assignRolesToUsuario(Long usuarioId, List<Long> roleIds) {
        // Remove existing assignments
        List<UsuarioRol> existing = usuarioRolRepository.findByUsuarioId(usuarioId);
        usuarioRolRepository.deleteAll(existing);

        // Add new assignments
        if (roleIds != null) {
            for (Long rolId : roleIds) {
                Rol rol = rolRepository.findById(rolId)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Rol no encontrado con id: " + rolId));
                Usuario usuario = usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Usuario no encontrado con id: " + usuarioId));
                UsuarioRol ur = new UsuarioRol(new UsuarioRolId(usuarioId, rolId), usuario, rol);
                usuarioRolRepository.save(ur);
            }
        }

        return roleIds != null
                ? roleIds.stream()
                    .map(rid -> rolRepository.findById(rid)
                            .map(Rol::getCodigo)
                            .orElse("UNKNOWN"))
                    .toList()
                : List.of();
    }

    // ──────────────────────────────────────────────
    //  Roles
    // ──────────────────────────────────────────────

    @GetMapping("/roles")
    public List<RolResponse> listRoles() {
        return rolService.findAll();
    }

    @PostMapping("/roles")
    public ResponseEntity<RolResponse> createRole(@Valid @RequestBody CreateRolRequest request) {
        RolResponse response = rolService.create(request.codigo(), request.nombre(), request.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/roles/{id}/permisos")
    public RolResponse assignPermisos(@PathVariable Long id, @RequestBody List<Long> permisoIds) {
        return rolService.assignPermisos(id, permisoIds);
    }

    // ──────────────────────────────────────────────
    //  Permisos
    // ──────────────────────────────────────────────

    @GetMapping("/permisos")
    public List<PermisoResponse> listPermisos() {
        return permisoService.findAll();
    }

    // ──────────────────────────────────────────────
    //  Configuración API
    // ──────────────────────────────────────────────

    @GetMapping("/configuracion")
    public List<ConfiguracionApiResponse> listConfiguracion() {
        return configuracionApiService.findAll();
    }

    @PutMapping("/configuracion")
    public List<ConfiguracionApiResponse> updateConfiguracion(
            @RequestBody @Valid List<ConfiguracionApiUpdate> updates) {
        log.debug("Updating {} configuracion entries", updates.size());
        return updates.stream()
                .map(u -> configuracionApiService.update(u.id(),
                        new ConfiguracionApiRequest(u.modulo(), u.clave(), u.valor(), u.tipo())))
                .toList();
    }

    // ──────────────────────────────────────────────
    //  Inner request records
    // ──────────────────────────────────────────────

    /**
     * Request body for {@code POST /api/v1/seguridad/roles}.
     */
    public record CreateRolRequest(
            @NotBlank(message = "El código del rol es obligatorio")
            String codigo,

            @NotBlank(message = "El nombre del rol es obligatorio")
            String nombre,

            String descripcion
    ) {}

    /**
     * Request body for {@code PUT /api/v1/seguridad/configuracion}.
     */
    public record ConfiguracionApiUpdate(
            @NotNull(message = "El id de configuración es obligatorio")
            Long id,

            @NotBlank(message = "El módulo es obligatorio")
            String modulo,

            @NotBlank(message = "La clave es obligatoria")
            String clave,

            String valor,

            String tipo
    ) {}
}

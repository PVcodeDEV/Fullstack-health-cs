package com.clinica.seguridad.controller;

import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.seguridad.dto.RolResponse;
import com.clinica.seguridad.dto.UsuarioResponse;
import com.clinica.seguridad.entity.Rol;
import com.clinica.seguridad.entity.Usuario;
import com.clinica.seguridad.entity.UsuarioRol;
import com.clinica.seguridad.entity.UsuarioRolId;
import com.clinica.seguridad.repository.PermisoRepository;
import com.clinica.seguridad.repository.RolRepository;
import com.clinica.seguridad.repository.UsuarioRepository;
import com.clinica.seguridad.repository.ConfiguracionApiRepository;
import com.clinica.seguridad.repository.UsuarioRolRepository;
import com.clinica.seguridad.service.PermisoService;
import com.clinica.seguridad.service.RolService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/seguridad")
@PreAuthorize("hasAnyAuthority('seguridad:ver', 'ROLE_ADMIN')")
public class SeguridadPortalController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final ConfiguracionApiRepository configuracionApiRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PersonaRepository personaRepository;
    private final RolService rolService;
    private final PermisoService permisoService;
    private final PasswordEncoder passwordEncoder;

    public SeguridadPortalController(UsuarioRepository usuarioRepository,
                                      RolRepository rolRepository,
                                      PermisoRepository permisoRepository,
                                      ConfiguracionApiRepository configuracionApiRepository,
                                      UsuarioRolRepository usuarioRolRepository,
                                      PersonaRepository personaRepository,
                                      RolService rolService,
                                      PermisoService permisoService,
                                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.configuracionApiRepository = configuracionApiRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.personaRepository = personaRepository;
        this.rolService = rolService;
        this.permisoService = permisoService;
        this.passwordEncoder = passwordEncoder;
    }

    private void setPortalAttributes(Model model, String activePage) {
        model.addAttribute("portalHeader", "portal-seguridad/fragments/header");
        model.addAttribute("portalSidebar", "portal-seguridad/fragments/sidebar");
        model.addAttribute("activePage", activePage);
    }

    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalRoles", rolRepository.count());
        model.addAttribute("totalPermisos", permisoRepository.count());
        model.addAttribute("totalConfigApi", configuracionApiRepository.count());
        setPortalAttributes(model, "dashboard");
        return "portal-seguridad/dashboard";
    }

    @GetMapping("/usuarios")
    public String listUsuarios(Model model) {
        List<UsuarioResponse> usuarios = usuarioRepository.findAll().stream()
                .map(u -> {
                    List<String> roles = usuarioRolRepository.findByUsuarioId(u.getId()).stream()
                            .map(ur -> ur.getRol().getCodigo())
                            .toList();
                    return UsuarioResponse.fromEntity(u, roles);
                })
                .toList();
        model.addAttribute("usuarios", usuarios);
        setPortalAttributes(model, "usuarios");
        return "portal-seguridad/usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuarioForm(Model model) {
        model.addAttribute("allRoles", rolRepository.findAll());
        model.addAttribute("allPersonas", personaRepository.findAll());
        setPortalAttributes(model, "usuarios");
        return "portal-seguridad/usuario-form";
    }

    @PostMapping("/usuarios/nuevo")
    @Transactional
    public String createUsuario(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam Long personaId,
                                 @RequestParam(required = false) List<Long> rolIds) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id: " + personaId));

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setPersona(persona);
        usuario = usuarioRepository.save(usuario);

        if (rolIds != null) {
            for (Long rolId : rolIds) {
                Rol rol = rolRepository.findById(rolId)
                        .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + rolId));
                UsuarioRol ur = new UsuarioRol(new UsuarioRolId(usuario.getId(), rolId), usuario, rol);
                usuarioRolRepository.save(ur);
            }
        }

        return "redirect:/seguridad/usuarios";
    }

    @GetMapping("/roles")
    public String listRoles(Model model) {
        model.addAttribute("roles", rolService.findAll());
        setPortalAttributes(model, "roles");
        return "portal-seguridad/roles";
    }

    @GetMapping("/roles/nuevo")
    public String nuevoRolForm(Model model) {
        model.addAttribute("allPermisos", permisoRepository.findAll());
        setPortalAttributes(model, "roles");
        return "portal-seguridad/rol-form";
    }

    @PostMapping("/roles/nuevo")
    @Transactional
    public String createRol(@RequestParam String codigo,
                             @RequestParam String nombre,
                             @RequestParam(required = false) String descripcion,
                             @RequestParam(required = false) List<Long> permisoIds) {
        RolResponse created = rolService.create(codigo, nombre, descripcion);
        if (permisoIds != null && !permisoIds.isEmpty()) {
            rolService.assignPermisos(created.id(), permisoIds);
        }
        return "redirect:/seguridad/roles";
    }

    @GetMapping("/permisos")
    public String listPermisos(Model model) {
        model.addAttribute("permisos", permisoService.findAll());
        setPortalAttributes(model, "permisos");
        return "portal-seguridad/permisos";
    }

    @GetMapping("/configuracion")
    public String listConfigApi(Model model) {
        setPortalAttributes(model, "config-api");
        return "portal-seguridad/config-api";
    }

    @GetMapping("/numeracion")
    public String listNumeracion(Model model) {
        setPortalAttributes(model, "numeracion");
        return "portal-seguridad/numeracion";
    }

    @GetMapping("/tipos-movimiento")
    public String listTiposMovimiento(Model model) {
        setPortalAttributes(model, "tipos-movimiento");
        return "portal-seguridad/tipos-movimiento";
    }

    @GetMapping("/cambiar-contrasena")
    public String cambiarContrasena(Model model) {
        setPortalAttributes(model, "cambiar-contrasena");
        return "portal-seguridad/cambiar-contrasena";
    }
}

package com.clinica.seguridad.controller;

import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.seguridad.dto.RolResponse;
import com.clinica.seguridad.dto.UsuarioResponse;
import com.clinica.seguridad.entity.NumeracionControl;
import com.clinica.seguridad.entity.Rol;
import com.clinica.seguridad.entity.TipoMovimiento;
import com.clinica.seguridad.entity.Usuario;
import com.clinica.seguridad.entity.UsuarioRol;
import com.clinica.seguridad.entity.UsuarioRolId;
import com.clinica.seguridad.repository.PermisoRepository;
import com.clinica.seguridad.repository.RolRepository;
import com.clinica.seguridad.repository.UsuarioRepository;
import com.clinica.seguridad.repository.ConfiguracionApiRepository;
import com.clinica.seguridad.repository.UsuarioRolRepository;
import com.clinica.seguridad.service.ConfiguracionApiService;
import com.clinica.seguridad.service.NumeracionControlService;
import com.clinica.seguridad.service.PermisoService;
import com.clinica.seguridad.service.RolService;
import com.clinica.seguridad.service.TipoMovimientoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ConfiguracionApiService configuracionApiService;
    private final NumeracionControlService numeracionControlService;
    private final TipoMovimientoService tipoMovimientoService;
    private final PasswordEncoder passwordEncoder;

    public SeguridadPortalController(UsuarioRepository usuarioRepository,
                                      RolRepository rolRepository,
                                      PermisoRepository permisoRepository,
                                      ConfiguracionApiRepository configuracionApiRepository,
                                      UsuarioRolRepository usuarioRolRepository,
                                      PersonaRepository personaRepository,
                                      RolService rolService,
                                      PermisoService permisoService,
                                      ConfiguracionApiService configuracionApiService,
                                      NumeracionControlService numeracionControlService,
                                      TipoMovimientoService tipoMovimientoService,
                                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.configuracionApiRepository = configuracionApiRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.personaRepository = personaRepository;
        this.rolService = rolService;
        this.permisoService = permisoService;
        this.configuracionApiService = configuracionApiService;
        this.numeracionControlService = numeracionControlService;
        this.tipoMovimientoService = tipoMovimientoService;
        this.passwordEncoder = passwordEncoder;
    }

    private void setPortalAttributes(Model model, String activePage) {
        model.addAttribute("portalHeader", "portal-administrativo/fragments/header");
        model.addAttribute("portalSidebar", "portal-administrativo/fragments/sidebar");
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
    @Transactional(readOnly = true)
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

    @GetMapping("/usuarios/{id}/editar")
    @Transactional(readOnly = true)
    public String editarUsuarioForm(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        List<Long> usuarioRolIds = usuarioRolRepository.findByUsuarioId(id).stream()
                .map(ur -> ur.getRol().getId())
                .toList();
        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarioRolIds", usuarioRolIds);
        model.addAttribute("allRoles", rolRepository.findAll());
        model.addAttribute("allPersonas", personaRepository.findAll());
        model.addAttribute("editMode", true);
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

    @PostMapping("/usuarios/{id}/editar")
    @Transactional
    public String updateUsuario(@PathVariable Long id,
                                 @RequestParam String username,
                                 @RequestParam(required = false) String password,
                                 @RequestParam Long personaId,
                                 @RequestParam(required = false) List<Long> rolIds) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id: " + personaId));

        usuario.setUsername(username);
        if (password != null && !password.isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(password));
        }
        usuario.setPersona(persona);
        usuarioRepository.save(usuario);

        // Reemplazar roles: eliminar existentes, asignar nuevos
        usuarioRolRepository.deleteAll(usuarioRolRepository.findByUsuarioId(id));
        if (rolIds != null) {
            for (Long rolId : rolIds) {
                Rol rol = rolRepository.findById(rolId)
                        .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + rolId));
                UsuarioRol ur = new UsuarioRol(new UsuarioRolId(id, rolId), usuario, rol);
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
        model.addAttribute("configApiList", configuracionApiService.findAll());
        setPortalAttributes(model, "config-api");
        return "portal-seguridad/config-api";
    }

    @GetMapping("/numeracion")
    public String listNumeracion(Model model) {
        model.addAttribute("numeraciones", numeracionControlService.findAll());
        setPortalAttributes(model, "numeracion");
        return "portal-seguridad/numeracion";
    }

    @GetMapping("/numeracion/nuevo")
    public String nuevaNumeracionForm(Model model) {
        setPortalAttributes(model, "numeracion");
        return "portal-seguridad/numeracion-form";
    }

    @GetMapping("/numeracion/{id}/editar")
    public String editarNumeracionForm(@PathVariable Long id, Model model) {
        model.addAttribute("numeracion", numeracionControlService.findById(id));
        setPortalAttributes(model, "numeracion");
        return "portal-seguridad/numeracion-form";
    }

    @PostMapping("/numeracion/nuevo")
    @Transactional
    public String createNumeracion(@RequestParam String entidad,
                                    @RequestParam String serie,
                                    @RequestParam int anio,
                                    @RequestParam(defaultValue = "0") Long correlativoActual,
                                    @RequestParam(required = false) String prefijo,
                                    @RequestParam(defaultValue = "6") int longitudCorrelativo) {
        NumeracionControl nc = new NumeracionControl();
        nc.setEntidad(entidad);
        nc.setSerie(serie);
        nc.setAnio(anio);
        nc.setCorrelativoActual(correlativoActual);
        nc.setPrefijo(prefijo);
        nc.setLongitudCorrelativo(longitudCorrelativo);
        numeracionControlService.create(nc);
        return "redirect:/seguridad/numeracion";
    }

    @PostMapping("/numeracion/{id}/toggle")
    @Transactional
    public String toggleNumeracion(@PathVariable Long id) {
        numeracionControlService.toggleActivo(id);
        return "redirect:/seguridad/numeracion";
    }

    @GetMapping("/tipos-movimiento")
    public String listTiposMovimiento(Model model) {
        model.addAttribute("tiposMovimiento", tipoMovimientoService.findAll());
        setPortalAttributes(model, "tipos-movimiento");
        return "portal-seguridad/tipos-movimiento";
    }

    @GetMapping("/tipos-movimiento/nuevo")
    public String nuevoTipoMovimientoForm(Model model) {
        setPortalAttributes(model, "tipos-movimiento");
        return "portal-seguridad/tipo-movimiento-form";
    }

    @GetMapping("/tipos-movimiento/{id}/editar")
    public String editarTipoMovimientoForm(@PathVariable Long id, Model model) {
        model.addAttribute("tipoMovimiento", tipoMovimientoService.findById(id));
        setPortalAttributes(model, "tipos-movimiento");
        return "portal-seguridad/tipo-movimiento-form";
    }

    @PostMapping("/tipos-movimiento/nuevo")
    @Transactional
    public String createTipoMovimiento(@RequestParam String codigo,
                                        @RequestParam String nombre,
                                        @RequestParam String modulo,
                                        @RequestParam(required = false) String descripcion) {
        TipoMovimiento tm = new TipoMovimiento();
        tm.setCodigo(codigo);
        tm.setNombre(nombre);
        tm.setModulo(modulo);
        tm.setDescripcion(descripcion);
        tipoMovimientoService.create(tm);
        return "redirect:/seguridad/tipos-movimiento";
    }

    @PostMapping("/tipos-movimiento/{id}/toggle")
    @Transactional
    public String toggleTipoMovimiento(@PathVariable Long id) {
        tipoMovimientoService.toggleActivo(id);
        return "redirect:/seguridad/tipos-movimiento";
    }

    @GetMapping("/cambiar-contrasena")
    public String cambiarContrasena(Model model) {
        setPortalAttributes(model, "cambiar-contrasena");
        return "portal-seguridad/cambiar-contrasena";
    }

    @PostMapping("/cambiar-contrasena")
    @Transactional
    public String cambiarContrasenaSubmit(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            org.springframework.security.core.Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
            jakarta.servlet.http.HttpServletRequest request) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/seguridad/cambiar-contrasena";
        }
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres");
            return "redirect:/seguridad/cambiar-contrasena";
        }
        if (authentication == null || !(authentication.getPrincipal() instanceof com.clinica.seguridad.service.UsuarioPrincipal up)) {
            return "redirect:/login";
        }
        Usuario usuario = up.getUsuario();
        if (!passwordEncoder.matches(currentPassword, usuario.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("error", "La contraseña actual no es correcta");
            return "redirect:/seguridad/cambiar-contrasena";
        }
        if (passwordEncoder.matches(newPassword, usuario.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("error", "La nueva contraseña no puede ser igual a la anterior");
            return "redirect:/seguridad/cambiar-contrasena";
        }
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuario.setPasswordChangeRequired(false);
        usuarioRepository.save(usuario);
        request.getSession().invalidate();
        redirectAttributes.addFlashAttribute("mensaje", "Contraseña cambiada correctamente. Inicie sesión con su nueva contraseña.");
        return "redirect:/login";
    }
}

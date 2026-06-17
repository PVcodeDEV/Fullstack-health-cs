package com.clinica.seguridad.controller;

import com.clinica.seguridad.repository.UsuarioRepository;
import com.clinica.seguridad.repository.RolRepository;
import com.clinica.seguridad.repository.PermisoRepository;
import com.clinica.seguridad.repository.ConfiguracionApiRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seguridad")
@PreAuthorize("hasAnyAuthority('seguridad:ver', 'ROLE_ADMIN')")
public class SeguridadPortalController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final ConfiguracionApiRepository configuracionApiRepository;

    public SeguridadPortalController(UsuarioRepository usuarioRepository,
                                     RolRepository rolRepository,
                                     PermisoRepository permisoRepository,
                                     ConfiguracionApiRepository configuracionApiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.configuracionApiRepository = configuracionApiRepository;
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
        setPortalAttributes(model, "usuarios");
        return "portal-seguridad/usuarios";
    }

    @GetMapping("/roles")
    public String listRoles(Model model) {
        setPortalAttributes(model, "roles");
        return "portal-seguridad/roles";
    }

    @GetMapping("/permisos")
    public String listPermisos(Model model) {
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

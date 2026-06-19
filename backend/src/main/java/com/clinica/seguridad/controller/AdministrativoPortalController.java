package com.clinica.seguridad.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasAnyAuthority('administrativo:ver', 'ROLE_ADMIN')")
public class AdministrativoPortalController {

    @GetMapping("/administrativo")
    public String dashboard(Model model) {
        setPortalAttributes(model, "administrativo");
        return "portal-administrativo/dashboard";
    }

    @GetMapping("/administrativo/rrhh")
    public String rrhh(Model model) {
        model.addAttribute("moduleName", "RRHH");
        model.addAttribute("moduleDescription", "Gestión de trabajadores, planillas y recursos humanos");
        setPortalAttributes(model, "rrhh");
        return "portal-administrativo/modulo";
    }

    @GetMapping("/administrativo/contabilidad")
    public String contabilidad(Model model) {
        model.addAttribute("moduleName", "Contabilidad");
        model.addAttribute("moduleDescription", "Gestión contable, libros, asientos y reportes financieros");
        setPortalAttributes(model, "contabilidad");
        return "portal-administrativo/modulo";
    }

    private void setPortalAttributes(Model model, String activePage) {
        model.addAttribute("portalHeader", "portal-administrativo/fragments/header");
        model.addAttribute("portalSidebar", "portal-administrativo/fragments/sidebar");
        model.addAttribute("activePage", activePage);
    }
}

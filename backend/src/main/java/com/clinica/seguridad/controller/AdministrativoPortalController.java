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
        model.addAttribute("portalHeader", "portal-administrativo/fragments/header");
        model.addAttribute("portalSidebar", "portal-administrativo/fragments/sidebar");
        model.addAttribute("activePage", "administrativo");
        return "portal-administrativo/dashboard";
    }
}

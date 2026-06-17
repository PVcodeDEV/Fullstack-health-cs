package com.clinica.clinica.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasAnyAuthority('asistencial:ver', 'ROLE_ADMIN')")
public class AsistencialPortalController {

    @GetMapping("/asistencial")
    public String dashboard(Authentication auth, Model model) {
        // Placeholder dashboard data - can be expanded with real data later
        model.addAttribute("totalPacientes", 0);
        model.addAttribute("totalAdmisionesHoy", 0);
        model.addAttribute("totalSopHoy", 0);

        // Set portal fragments for blue theme
        model.addAttribute("portalHeader", "portal-asistencial/fragments/header");
        model.addAttribute("portalSidebar", "portal-asistencial/fragments/sidebar");
        model.addAttribute("activePage", "asistencial");

        return "portal-asistencial/dashboard";
    }
}
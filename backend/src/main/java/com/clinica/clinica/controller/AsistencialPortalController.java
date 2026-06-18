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
        setPortalAttributes(model, "asistencial");
        return "portal-asistencial/dashboard";
    }

    @GetMapping("/asistencial/hce")
    public String hce(Model model) {
        model.addAttribute("moduleName", "Historia Clínica");
        model.addAttribute("moduleDescription", "Historia clínica electrónica y documentos clínicos");
        setPortalAttributes(model, "hce");
        return "portal-asistencial/modulo";
    }

    @GetMapping("/asistencial/sop")
    public String sop(Model model) {
        model.addAttribute("moduleName", "SOP");
        model.addAttribute("moduleDescription", "Reportes quirúrgicos y procedimientos");
        setPortalAttributes(model, "sop");
        return "portal-asistencial/modulo";
    }

    private void setPortalAttributes(Model model, String activePage) {
        model.addAttribute("portalHeader", "portal-asistencial/fragments/header");
        model.addAttribute("portalSidebar", "portal-asistencial/fragments/sidebar");
        model.addAttribute("activePage", activePage);
    }
}
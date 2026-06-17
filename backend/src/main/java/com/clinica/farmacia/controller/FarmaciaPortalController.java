package com.clinica.farmacia.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasAnyAuthority('farmacia:ver', 'ROLE_ADMIN')")
public class FarmaciaPortalController {

    @GetMapping("/farmacia")
    public String dashboard(Authentication auth, Model model) {
        // Placeholder dashboard data - can be expanded with real data later
        model.addAttribute("stockAlerts", 0);
        model.addAttribute("pendingDispatches", 0);
        model.addAttribute("recentTransactions", 0);

        // Set portal fragments for green theme
        model.addAttribute("portalHeader", "portal-farmacia/fragments/header");
        model.addAttribute("portalSidebar", "portal-farmacia/fragments/sidebar");
        model.addAttribute("activePage", "farmacia");

        return "portal-farmacia/dashboard";
    }
}
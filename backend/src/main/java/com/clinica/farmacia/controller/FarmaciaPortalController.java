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
        setPortalAttributes(model, "farmacia");
        return "portal-farmacia/dashboard";
    }

    @GetMapping("/farmacia/productos")
    public String productos(Model model) {
        model.addAttribute("moduleName", "Productos");
        model.addAttribute("moduleDescription", "Catálogo de productos farmacéuticos");
        setPortalAttributes(model, "productos");
        return "portal-farmacia/modulo";
    }

    @GetMapping("/farmacia/almacenes")
    public String almacenes(Model model) {
        model.addAttribute("moduleName", "Almacenes");
        model.addAttribute("moduleDescription", "Gestión de almacenes y ubicaciones");
        setPortalAttributes(model, "almacenes");
        return "portal-farmacia/modulo";
    }

    @GetMapping("/farmacia/stock")
    public String stock(Model model) {
        model.addAttribute("moduleName", "Stock");
        model.addAttribute("moduleDescription", "Control de inventario y alertas de stock");
        setPortalAttributes(model, "stock");
        return "portal-farmacia/modulo";
    }

    @GetMapping("/farmacia/despachos")
    public String despachos(Model model) {
        model.addAttribute("moduleName", "Despachos");
        model.addAttribute("moduleDescription", "Despacho de recetas y pedidos");
        setPortalAttributes(model, "despachos");
        return "portal-farmacia/modulo";
    }

    @GetMapping("/farmacia/mermas")
    public String mermas(Model model) {
        model.addAttribute("moduleName", "Mermas");
        model.addAttribute("moduleDescription", "Control de mermas y vencimientos");
        setPortalAttributes(model, "mermas");
        return "portal-farmacia/modulo";
    }

    @GetMapping("/farmacia/movimientos")
    public String movimientos(Model model) {
        model.addAttribute("moduleName", "Movimientos");
        model.addAttribute("moduleDescription", "Historial de movimientos de inventario");
        setPortalAttributes(model, "movimientos");
        return "portal-farmacia/modulo";
    }

    private void setPortalAttributes(Model model, String activePage) {
        model.addAttribute("portalHeader", "portal-farmacia/fragments/header");
        model.addAttribute("portalSidebar", "portal-farmacia/fragments/sidebar");
        model.addAttribute("activePage", activePage);
    }
}
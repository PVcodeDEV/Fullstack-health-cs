package com.clinica.caja.controller;

import com.clinica.caja.liquidacion.service.LiquidacionService;
import com.clinica.caja.sesion.service.SesionCajaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@PreAuthorize("hasAnyAuthority('caja:ver', 'ROLE_ADMIN')")
public class CajaPortalController {

    private final SesionCajaService sesionCajaService;
    private final LiquidacionService liquidacionService;

    public CajaPortalController(SesionCajaService sesionCajaService,
                                LiquidacionService liquidacionService) {
        this.sesionCajaService = sesionCajaService;
        this.liquidacionService = liquidacionService;
    }

    @GetMapping("/caja")
    public String dashboard(Authentication auth, Model model) {
        Long usuarioId = extractUsuarioId(auth);

        // Sesión actual
        try {
            var sesion = sesionCajaService.getSessionActual(usuarioId);
            model.addAttribute("tieneSesionAbierta", true);
            model.addAttribute("sesionActual", sesion);
            model.addAttribute("montoApertura", sesion.montoApertura() != null ? sesion.montoApertura() : BigDecimal.ZERO);
            model.addAttribute("totalVentas", sesion.totalVentas() != null ? sesion.totalVentas() : BigDecimal.ZERO);
        } catch (EntityNotFoundException e) {
            model.addAttribute("tieneSesionAbierta", false);
        }

        // Últimas liquidaciones (últimas 5)
        try {
            List<?> ultimasLiquidaciones = liquidacionService.findRecentByUsuario(usuarioId, 5);
            model.addAttribute("ultimasLiquidaciones", ultimasLiquidaciones);
        } catch (Exception e) {
            model.addAttribute("ultimasLiquidaciones", Collections.emptyList());
        }

        // Comprobantes de hoy (count only)
        model.addAttribute("totalComprobantesHoy", 0);

        // Set portal fragments
        model.addAttribute("portalHeader", "portal-caja/fragments/header");
        model.addAttribute("portalSidebar", "portal-caja/fragments/sidebar");
        model.addAttribute("activePage", "caja");

        return "portal-caja/dashboard";
    }

    private Long extractUsuarioId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return 0L;
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
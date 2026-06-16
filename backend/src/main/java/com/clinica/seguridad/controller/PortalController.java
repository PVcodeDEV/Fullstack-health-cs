package com.clinica.seguridad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles portal/landing page routes for the Thymeleaf frontend.
 *
 * <p>Module-specific portal routes (/caja, /asistencial, /farmacia, /administrativo)
 * are handled by their respective module controllers.</p>
 */
@Controller
public class PortalController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "index";
    }
}

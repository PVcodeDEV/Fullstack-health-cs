package com.clinica.seguridad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles portal/landing page routes for the Thymeleaf frontend.
 *
 * <p>These are entry points after form-based login redirects
 * via {@link com.clinica.seguridad.handler.PortalAuthenticationSuccessHandler}.</p>
 */
@Controller
public class PortalController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "index";
    }

    @GetMapping("/administrativo")
    public String administrativo() {
        return "index";
    }

    @GetMapping("/asistencial")
    public String asistencial() {
        return "index";
    }

    @GetMapping("/farmacia")
    public String farmacia() {
        return "index";
    }

    @GetMapping("/caja")
    public String caja() {
        return "index";
    }
}

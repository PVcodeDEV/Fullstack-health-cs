package com.clinica.clinica.controller;

import com.clinica.clinica.admision.entity.SolicitudHospitalizacion;
import com.clinica.clinica.admision.repository.SolicitudHospitalizacionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@PreAuthorize("hasAnyAuthority('admision:ver', 'ROLE_ADMIN')")
@RequestMapping("/asistencial/admisiones")
public class AdmisionPortalController {

    private final SolicitudHospitalizacionRepository solicitudRepository;

    public AdmisionPortalController(SolicitudHospitalizacionRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    @GetMapping
    public String list(Model model) {
        List<SolicitudHospitalizacion> pendientes = solicitudRepository.findByEstado("PENDIENTE");
        List<SolicitudConAlerta> alertas = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        for (SolicitudHospitalizacion s : pendientes) {
            boolean masDeDosHoras = s.getFechaSolicitud() != null
                    && s.getFechaSolicitud().plusHours(2).isBefore(ahora);
            if (masDeDosHoras) {
                alertas.add(new SolicitudConAlerta(s, true));
            }
        }

        model.addAttribute("portalHeader", "portal-asistencial/fragments/header");
        model.addAttribute("portalSidebar", "portal-asistencial/fragments/sidebar");
        model.addAttribute("activePage", "admisiones");
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("alertas", alertas);

        return "portal-asistencial/admisiones/list";
    }

    // Helper record for alert display
    public record SolicitudConAlerta(SolicitudHospitalizacion solicitud, boolean alerta) {}
}

package com.clinica.clinica.controller;

import com.clinica.clinica.admision.entity.Cuenta;
import com.clinica.clinica.admision.repository.CuentaRepository;
import com.clinica.clinica.paciente.dto.PacienteResponse;
import com.clinica.clinica.paciente.service.PacienteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@PreAuthorize("hasAnyAuthority('paciente:ver', 'ROLE_ADMIN')")
public class PacientePortalController {

    private final PacienteService pacienteService;
    private final CuentaRepository cuentaRepository;

    public PacientePortalController(PacienteService pacienteService, CuentaRepository cuentaRepository) {
        this.pacienteService = pacienteService;
        this.cuentaRepository = cuentaRepository;
    }

    @GetMapping("/asistencial/pacientes")
    public String search(@RequestParam(required = false) String q, Model model) {
        List<PacienteResponse> pacientes = List.of();

        if (q != null && !q.isBlank()) {
            pacientes = pacienteService.searchPacientes(q);
        }

        model.addAttribute("portalHeader", "portal-asistencial/fragments/header");
        model.addAttribute("portalSidebar", "portal-asistencial/fragments/sidebar");
        model.addAttribute("activePage", "pacientes");
        model.addAttribute("searchQuery", q);
        model.addAttribute("pacientes", pacientes);

        return "portal-asistencial/pacientes/search";
    }

    @GetMapping("/asistencial/pacientes/{id}")
    public String detail(@PathVariable Long id, Model model) {
        PacienteResponse paciente = pacienteService.findById(id);
        List<Cuenta> admisiones = cuentaRepository.findByPacienteId(id);

        model.addAttribute("portalHeader", "portal-asistencial/fragments/header");
        model.addAttribute("portalSidebar", "portal-asistencial/fragments/sidebar");
        model.addAttribute("activePage", "pacientes");
        model.addAttribute("paciente", paciente);
        model.addAttribute("admisiones", admisiones);

        return "portal-asistencial/pacientes/detail";
    }
}
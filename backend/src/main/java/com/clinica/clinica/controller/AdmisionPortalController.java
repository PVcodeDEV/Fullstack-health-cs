package com.clinica.clinica.controller;

import com.clinica.clinica.admision.entity.Cuenta;
import com.clinica.clinica.admision.entity.CuentaPaquete;
import com.clinica.clinica.admision.entity.SolicitudHospitalizacion;
import com.clinica.clinica.admision.repository.CuentaRepository;
import com.clinica.clinica.admision.repository.SolicitudHospitalizacionRepository;
import com.clinica.clinica.admision.service.AdmisionService;
import com.clinica.clinica.cama.dto.CamaResponse;
import com.clinica.clinica.cama.service.CamaService;
import com.clinica.clinica.paciente.dto.PacienteResponse;
import com.clinica.clinica.paciente.service.PacienteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasAnyAuthority('admision:ver', 'ROLE_ADMIN')")
@RequestMapping("/asistencial/admisiones")
public class AdmisionPortalController {

    private final AdmisionService admisionService;
    private final PacienteService pacienteService;
    private final CamaService camaService;
    private final CuentaRepository cuentaRepository;
    private final SolicitudHospitalizacionRepository solicitudRepository;

    public AdmisionPortalController(AdmisionService admisionService,
                                    PacienteService pacienteService,
                                    CamaService camaService,
                                    CuentaRepository cuentaRepository,
                                    SolicitudHospitalizacionRepository solicitudRepository) {
        this.admisionService = admisionService;
        this.pacienteService = pacienteService;
        this.camaService = camaService;
        this.cuentaRepository = cuentaRepository;
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

    @GetMapping("/nueva")
    @PreAuthorize("hasAnyAuthority('admision:crear', 'ROLE_ADMIN')")
    public String nueva(@RequestParam(required = false) String q, Model model, HttpSession session) {
        session.removeAttribute("wizardPacienteId");
        session.removeAttribute("wizardPaqueteId");
        session.removeAttribute("wizardCamaId");
        session.removeAttribute("wizardCuentaId");

        if (q != null && !q.isBlank()) {
            var personas = admisionService.buscarPaciente(q);
            // Convert Persona to PacienteResponse via PacienteService
            model.addAttribute("pacientes", personas.stream()
                    .map(p -> {
                        try { return pacienteService.findByPersonaId(p.getId()); }
                        catch (Exception e) { return null; }
                    })
                    .filter(r -> r != null)
                    .toList());
            model.addAttribute("searchQuery", q);
        }

        model.addAttribute("portalHeader", "portal-asistencial/fragments/header");
        model.addAttribute("portalSidebar", "portal-asistencial/fragments/sidebar");
        model.addAttribute("activePage", "admisiones");
        model.addAttribute("step", 1);

        return "portal-asistencial/admisiones/nueva";
    }

    @PostMapping("/wizard/paciente")
    @PreAuthorize("hasAnyAuthority('admision:crear', 'ROLE_ADMIN')")
    public String wizardPaciente(@RequestParam Long pacienteId, HttpSession session, Model model) {
        session.setAttribute("wizardPacienteId", pacienteId);

        List<CuentaPaquete> paquetes = admisionService.getPaquetesActivos();

        model.addAttribute("portalHeader", "portal-asistencial/fragments/header");
        model.addAttribute("portalSidebar", "portal-asistencial/fragments/sidebar");
        model.addAttribute("activePage", "admisiones");
        model.addAttribute("step", 2);
        model.addAttribute("paquetes", paquetes);

        return "portal-asistencial/admisiones/wizard/step-paquete";
    }

    @PostMapping("/wizard/paquete")
    @PreAuthorize("hasAnyAuthority('admision:crear', 'ROLE_ADMIN')")
    public String wizardPaquete(@RequestParam Long paqueteId, HttpSession session, Model model) {
        session.setAttribute("wizardPaqueteId", paqueteId);

        CuentaPaquete paquete = admisionService.getPaqueteById(paqueteId);
        List<CamaResponse> camas = camaService.findDisponiblesByTipoHabitacion(paquete.getTipoHabitacionId());

        model.addAttribute("portalHeader", "portal-asistencial/fragments/header");
        model.addAttribute("portalSidebar", "portal-asistencial/fragments/sidebar");
        model.addAttribute("activePage", "admisiones");
        model.addAttribute("step", 3);
        model.addAttribute("camas", camas);
        model.addAttribute("tipoHabitacionId", paquete.getTipoHabitacionId());

        return "portal-asistencial/admisiones/wizard/step-cama";
    }

    @PostMapping("/wizard/cama")
    @PreAuthorize("hasAnyAuthority('admision:asignar_cama', 'ROLE_ADMIN')")
    public String wizardCama(@RequestParam(required = false) Long camaId,
                             HttpSession session, Model model) {
        Long pacienteId = (Long) session.getAttribute("wizardPacienteId");
        Long paqueteId = (Long) session.getAttribute("wizardPaqueteId");

        Cuenta cuenta = admisionService.crearCuentaConSolicitud(pacienteId, paqueteId, camaId);

        if (camaId != null) {
            session.setAttribute("wizardCamaId", camaId);
        }

        session.setAttribute("wizardCuentaId", cuenta.getId());

        model.addAttribute("portalHeader", "portal-asistencial/fragments/header");
        model.addAttribute("portalSidebar", "portal-asistencial/fragments/sidebar");
        model.addAttribute("activePage", "admisiones");
        model.addAttribute("step", 4);
        model.addAttribute("cuentaId", cuenta.getId());

        return "portal-asistencial/admisiones/wizard/step-diagnostico";
    }

    @PostMapping("/wizard/diagnostico")
    @PreAuthorize("hasAnyAuthority('admision:editar', 'ROLE_ADMIN')")
    public String wizardDiagnostico(@ModelAttribute DiagnosticoForm form,
                                    HttpSession session, Model model) {
        Long cuentaId = (Long) session.getAttribute("wizardCuentaId");

        admisionService.registrarDiagnostico(cuentaId, form.getCodigoCIE11(),
                form.getDescripcion(), form.getTipo());

        session.removeAttribute("wizardPacienteId");
        session.removeAttribute("wizardPaqueteId");
        session.removeAttribute("wizardCamaId");
        session.removeAttribute("wizardCuentaId");

        Cuenta cuenta = cuentaRepository.findById(cuentaId).orElseThrow();
        List<SolicitudHospitalizacion> solicitudes = solicitudRepository.findByCuentaId(cuentaId);
        SolicitudHospitalizacion solicitud = solicitudes.isEmpty() ? null : solicitudes.get(0);

        model.addAttribute("portalHeader", "portal-asistencial/fragments/header");
        model.addAttribute("portalSidebar", "portal-asistencial/fragments/sidebar");
        model.addAttribute("activePage", "admisiones");
        model.addAttribute("step", 5);
        model.addAttribute("cuenta", cuenta);
        model.addAttribute("solicitud", solicitud);

        return "portal-asistencial/admisiones/wizard/step-completado";
    }

    // Helper record for alert display
    public record SolicitudConAlerta(SolicitudHospitalizacion solicitud, boolean alerta) {}

    // Form DTO for diagnosis step
    public static class DiagnosticoForm {
        private String codigoCIE11;
        private String descripcion;
        private String tipo;

        public String getCodigoCIE11() { return codigoCIE11; }
        public void setCodigoCIE11(String codigoCIE11) { this.codigoCIE11 = codigoCIE11; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
    }
}

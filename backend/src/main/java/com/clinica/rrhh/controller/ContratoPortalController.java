package com.clinica.rrhh.controller;

import com.clinica.maestro.repository.rrhh.TipoContratoRepository;
import com.clinica.rrhh.contrato.dto.ContratoRequest;
import com.clinica.rrhh.contrato.dto.ContratoResponse;
import com.clinica.rrhh.contrato.dto.ContratoUpdateRequest;
import com.clinica.rrhh.contrato.service.ContratoService;
import com.clinica.rrhh.trabajador.dto.TrabajadorResponse;
import com.clinica.rrhh.trabajador.service.TrabajadorService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/administrativo/rrhh/contratos")
@PreAuthorize("hasAnyAuthority('administrativo:ver', 'ROLE_ADMIN')")
public class ContratoPortalController {

    private static final Logger log = LoggerFactory.getLogger(ContratoPortalController.class);

    private final ContratoService contratoService;
    private final TrabajadorService trabajadorService;
    private final TipoContratoRepository tipoContratoRepository;

    public ContratoPortalController(ContratoService contratoService,
                                    TrabajadorService trabajadorService,
                                    TipoContratoRepository tipoContratoRepository) {
        this.contratoService = contratoService;
        this.trabajadorService = trabajadorService;
        this.tipoContratoRepository = tipoContratoRepository;
    }

    private void setPortalAttributes(Model model, String activePage) {
        model.addAttribute("portalHeader", "portal-administrativo/fragments/header");
        model.addAttribute("portalSidebar", "portal-administrativo/fragments/sidebar");
        model.addAttribute("activePage", activePage);
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long trabajadorId, Model model) {
        List<ContratoResponse> contratos;
        if (trabajadorId != null) {
            contratos = contratoService.findByTrabajadorId(trabajadorId);
        } else {
            contratos = contratoService.findAll();
        }
        model.addAttribute("contratos", contratos);
        model.addAttribute("trabajadorId", trabajadorId);
        List<TrabajadorResponse> trabajadores = trabajadorService.findAll();
        model.addAttribute("trabajadores", trabajadores);
        setPortalAttributes(model, "contratos");
        return "portal-administrativo/rrhh/contratos/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String createForm(@RequestParam(required = false) Long trabajadorId, Model model) {
        model.addAttribute("allTrabajadores", trabajadorService.findAll());
        model.addAttribute("allTiposContrato", tipoContratoRepository.findAll());
        model.addAttribute("selectedTrabajadorId", trabajadorId);
        model.addAttribute("editMode", false);
        return "portal-administrativo/rrhh/contratos/form :: modal";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String create(@RequestParam Long trabajadorId,
                         @RequestParam Long tipoContratoId,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                         @RequestParam(required = false) Integer periodoPruebaMeses,
                         @RequestParam BigDecimal remuneracion,
                         @RequestParam(required = false) String jornada,
                         RedirectAttributes redirectAttributes) {
        try {
            ContratoRequest request = new ContratoRequest(
                    trabajadorId, tipoContratoId, fechaInicio, fechaFin,
                    periodoPruebaMeses, remuneracion, jornada
            );
            contratoService.create(request);
            redirectAttributes.addFlashAttribute("mensaje", "Contrato creado correctamente");
            log.debug("Contrato creado via portal para trabajadorId={}", trabajadorId);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/administrativo/rrhh/contratos";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            ContratoResponse contrato = contratoService.findById(id);
            model.addAttribute("contrato", contrato);
            // Load linked trabajador info for display
            try {
                TrabajadorResponse trabajador = trabajadorService.findById(contrato.trabajadorId());
                model.addAttribute("trabajador", trabajador);
            } catch (EntityNotFoundException e) {
                // Trabajador may be deleted; show contrato info without it
            }
            setPortalAttributes(model, "contratos");
            return "portal-administrativo/rrhh/contratos/detail";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return list(null, model);
        }
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            ContratoResponse contrato = contratoService.findById(id);
            model.addAttribute("contrato", contrato);
            model.addAttribute("allTiposContrato", tipoContratoRepository.findAll());
            model.addAttribute("editMode", true);
            return "portal-administrativo/rrhh/contratos/form :: modal";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "portal-administrativo/rrhh/contratos/form :: modal";
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String update(@PathVariable Long id,
                         @RequestParam Long tipoContratoId,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                         @RequestParam(required = false) Integer periodoPruebaMeses,
                         @RequestParam BigDecimal remuneracion,
                         @RequestParam(required = false) String jornada,
                         RedirectAttributes redirectAttributes) {
        try {
            ContratoUpdateRequest request = new ContratoUpdateRequest(
                    tipoContratoId, fechaInicio, fechaFin,
                    periodoPruebaMeses, remuneracion, jornada
            );
            contratoService.update(id, request);
            redirectAttributes.addFlashAttribute("mensaje", "Contrato actualizado correctamente");
            log.debug("Contrato id={} actualizado via portal", id);
        } catch (IllegalArgumentException | EntityNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/administrativo/rrhh/contratos/" + id;
    }

    @PostMapping("/{id}/resolver")
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String resolver(@PathVariable Long id,
                           @RequestParam String motivoCese,
                           RedirectAttributes redirectAttributes) {
        try {
            contratoService.resolver(id, motivoCese);
            redirectAttributes.addFlashAttribute("mensaje", "Contrato resuelto correctamente");
            log.debug("Contrato id={} resuelto via portal", id);
        } catch (IllegalArgumentException | EntityNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/administrativo/rrhh/contratos/" + id;
    }

    @PostMapping("/{id}/suspender")
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String suspender(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            contratoService.suspender(id);
            redirectAttributes.addFlashAttribute("mensaje", "Contrato suspendido correctamente");
            log.debug("Contrato id={} suspendido via portal", id);
        } catch (IllegalArgumentException | EntityNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/administrativo/rrhh/contratos/" + id;
    }

    @PostMapping("/{id}/reactivar")
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String reactivar(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            contratoService.reactivar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Contrato reactivado correctamente");
            log.debug("Contrato id={} reactivado via portal", id);
        } catch (IllegalArgumentException | EntityNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/administrativo/rrhh/contratos/" + id;
    }

    // === Confirmation modal endpoints ===

    @GetMapping("/{id}/confirmar-resolver")
    public String confirmarResolver(@PathVariable Long id, Model model) {
        try {
            ContratoResponse contrato = contratoService.findById(id);
            model.addAttribute("contrato", contrato);
            return "portal-administrativo/rrhh/contratos/action-confirm :: resolver";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "portal-administrativo/rrhh/contratos/action-confirm :: resolver";
        }
    }

    @GetMapping("/{id}/confirmar-suspender")
    public String confirmarSuspender(@PathVariable Long id, Model model) {
        try {
            ContratoResponse contrato = contratoService.findById(id);
            model.addAttribute("contrato", contrato);
            return "portal-administrativo/rrhh/contratos/action-confirm :: suspender";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "portal-administrativo/rrhh/contratos/action-confirm :: suspender";
        }
    }

    @GetMapping("/{id}/confirmar-reactivar")
    public String confirmarReactivar(@PathVariable Long id, Model model) {
        try {
            ContratoResponse contrato = contratoService.findById(id);
            model.addAttribute("contrato", contrato);
            return "portal-administrativo/rrhh/contratos/action-confirm :: reactivar";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "portal-administrativo/rrhh/contratos/action-confirm :: reactivar";
        }
    }
}

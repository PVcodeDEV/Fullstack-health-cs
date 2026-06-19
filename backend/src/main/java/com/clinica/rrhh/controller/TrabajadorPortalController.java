package com.clinica.rrhh.controller;

import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.contrato.dto.ContratoResponse;
import com.clinica.rrhh.contrato.service.ContratoService;
import com.clinica.rrhh.periodo.dto.PeriodoLaboralResponse;
import com.clinica.rrhh.periodo.service.PeriodoLaboralService;
import com.clinica.rrhh.trabajador.dto.TrabajadorRequest;
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

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/administrativo/rrhh/trabajadores")
@PreAuthorize("hasAnyAuthority('administrativo:ver', 'ROLE_ADMIN')")
public class TrabajadorPortalController {

    private static final Logger log = LoggerFactory.getLogger(TrabajadorPortalController.class);

    private final TrabajadorService trabajadorService;
    private final ContratoService contratoService;
    private final PeriodoLaboralService periodoLaboralService;
    private final PersonaRepository personaRepository;

    public TrabajadorPortalController(TrabajadorService trabajadorService,
                                      ContratoService contratoService,
                                      PeriodoLaboralService periodoLaboralService,
                                      PersonaRepository personaRepository) {
        this.trabajadorService = trabajadorService;
        this.contratoService = contratoService;
        this.periodoLaboralService = periodoLaboralService;
        this.personaRepository = personaRepository;
    }

    private void setPortalAttributes(Model model, String activePage) {
        model.addAttribute("portalHeader", "portal-administrativo/fragments/header");
        model.addAttribute("portalSidebar", "portal-administrativo/fragments/sidebar");
        model.addAttribute("activePage", activePage);
    }

    @GetMapping
    public String list(Model model) {
        List<TrabajadorResponse> trabajadores = trabajadorService.findAll();
        model.addAttribute("trabajadores", trabajadores);
        setPortalAttributes(model, "trabajadores");
        return "portal-administrativo/rrhh/trabajadores/list";
    }

    @GetMapping("/table")
    public String tableFragment(@RequestParam(required = false) String tipo,
                                @RequestParam(required = false) String regimenLaboral,
                                @RequestParam(required = false) String search,
                                Model model) {
        List<TrabajadorResponse> trabajadores = trabajadorService.findAll();

        // Apply server-side filtering
        if (tipo != null && !tipo.isEmpty()) {
            trabajadores = trabajadores.stream()
                    .filter(t -> tipo.equals(t.tipo()))
                    .toList();
        }
        if (regimenLaboral != null && !regimenLaboral.isEmpty()) {
            trabajadores = trabajadores.stream()
                    .filter(t -> regimenLaboral.equals(t.regimenLaboral()))
                    .toList();
        }
        if (search != null && !search.isEmpty()) {
            String lower = search.toLowerCase();
            trabajadores = trabajadores.stream()
                    .filter(t -> (t.personaNombres() != null && t.personaNombres().toLowerCase().contains(lower))
                            || (t.personaApellidoPaterno() != null && t.personaApellidoPaterno().toLowerCase().contains(lower))
                            || (t.personaNumeroDocumento() != null && t.personaNumeroDocumento().toLowerCase().contains(lower)))
                    .toList();
        }

        model.addAttribute("trabajadores", trabajadores);
        return "portal-administrativo/rrhh/trabajadores/table :: table";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("allPersonas", personaRepository.findAllByActivoTrue());
        model.addAttribute("editMode", false);
        return "portal-administrativo/rrhh/trabajadores/form :: modal";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String create(@RequestParam Long personaId,
                         @RequestParam String codigoTrabajador,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIngreso,
                         @RequestParam(required = false) String tipo,
                         @RequestParam(required = false) String regimenLaboral,
                         @RequestParam(required = false) String cargo,
                         @RequestParam(required = false) Long areaFuncionalId,
                         @RequestParam(required = false) String banco,
                         @RequestParam(required = false) String cuentaSueldo,
                         @RequestParam(required = false) String cci,
                         @RequestParam(required = false) String contactoNombre,
                         @RequestParam(required = false) String contactoTelefono,
                         @RequestParam(required = false) Integer cantidadHijos,
                         @RequestParam(required = false) String nroColegiatura,
                         @RequestParam(required = false) Long tipoColegiaturaId,
                         @RequestParam(required = false) Boolean discapacidad,
                         @RequestParam(required = false) Boolean sindicalizado,
                         RedirectAttributes redirectAttributes) {
        try {
            TrabajadorRequest request = new TrabajadorRequest(
                    personaId, codigoTrabajador, fechaIngreso, tipo, regimenLaboral, cargo,
                    areaFuncionalId, banco, cuentaSueldo, cci, contactoNombre, contactoTelefono,
                    cantidadHijos, nroColegiatura, tipoColegiaturaId, discapacidad, sindicalizado
            );
            trabajadorService.create(request);
            redirectAttributes.addFlashAttribute("mensaje", "Trabajador creado correctamente");
            log.debug("Trabajador creado via portal");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/administrativo/rrhh/trabajadores";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            TrabajadorResponse trabajador = trabajadorService.findById(id);
            model.addAttribute("trabajador", trabajador);
            setPortalAttributes(model, "trabajadores");
            return "portal-administrativo/rrhh/trabajadores/detail";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return list(model);
        }
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            TrabajadorResponse trabajador = trabajadorService.findById(id);
            model.addAttribute("trabajador", trabajador);
            model.addAttribute("allPersonas", personaRepository.findAllByActivoTrue());
            model.addAttribute("editMode", true);
            return "portal-administrativo/rrhh/trabajadores/form :: modal";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "portal-administrativo/rrhh/trabajadores/form :: modal";
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String update(@PathVariable Long id,
                         @RequestParam Long personaId,
                         @RequestParam String codigoTrabajador,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIngreso,
                         @RequestParam(required = false) String tipo,
                         @RequestParam(required = false) String regimenLaboral,
                         @RequestParam(required = false) String cargo,
                         @RequestParam(required = false) Long areaFuncionalId,
                         @RequestParam(required = false) String banco,
                         @RequestParam(required = false) String cuentaSueldo,
                         @RequestParam(required = false) String cci,
                         @RequestParam(required = false) String contactoNombre,
                         @RequestParam(required = false) String contactoTelefono,
                         @RequestParam(required = false) Integer cantidadHijos,
                         @RequestParam(required = false) String nroColegiatura,
                         @RequestParam(required = false) Long tipoColegiaturaId,
                         @RequestParam(required = false) Boolean discapacidad,
                         @RequestParam(required = false) Boolean sindicalizado,
                         RedirectAttributes redirectAttributes) {
        try {
            TrabajadorRequest request = new TrabajadorRequest(
                    personaId, codigoTrabajador, fechaIngreso, tipo, regimenLaboral, cargo,
                    areaFuncionalId, banco, cuentaSueldo, cci, contactoNombre, contactoTelefono,
                    cantidadHijos, nroColegiatura, tipoColegiaturaId, discapacidad, sindicalizado
            );
            trabajadorService.update(id, request);
            redirectAttributes.addFlashAttribute("mensaje", "Trabajador actualizado correctamente");
            log.debug("Trabajador id={} actualizado via portal", id);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/administrativo/rrhh/trabajadores";
    }

    @PostMapping("/{id}/reingreso")
    @PreAuthorize("hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')")
    public String reingreso(@PathVariable Long id,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                            RedirectAttributes redirectAttributes) {
        try {
            periodoLaboralService.registrarIngreso(id, fechaInicio, true);
            redirectAttributes.addFlashAttribute("mensaje", "Reingreso registrado correctamente");
            log.debug("Reingreso registrado para trabajadorId={}", id);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/administrativo/rrhh/trabajadores/" + id;
    }

    // === Sub-tab fragments ===

    @GetMapping("/{id}/contratos")
    public String contratosFragment(@PathVariable Long id, Model model) {
        List<ContratoResponse> contratos = contratoService.findByTrabajadorId(id);
        model.addAttribute("contratos", contratos);
        return "portal-administrativo/rrhh/trabajadores/fragments :: contratos";
    }

    @GetMapping("/{id}/periodos")
    public String periodosFragment(@PathVariable Long id, Model model) {
        List<PeriodoLaboralResponse> periodos = periodoLaboralService.findByTrabajadorId(id);
        model.addAttribute("periodos", periodos);
        return "portal-administrativo/rrhh/trabajadores/fragments :: periodos";
    }
}

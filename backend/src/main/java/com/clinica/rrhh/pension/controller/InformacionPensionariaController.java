package com.clinica.rrhh.pension.controller;

import com.clinica.rrhh.pension.dto.InformacionPensionariaRequest;
import com.clinica.rrhh.pension.dto.InformacionPensionariaResponse;
import com.clinica.rrhh.pension.service.InformacionPensionariaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trabajadores/{trabajadorId}/informacion-pensionaria")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class InformacionPensionariaController {

    private final InformacionPensionariaService informacionPensionariaService;

    public InformacionPensionariaController(InformacionPensionariaService informacionPensionariaService) {
        this.informacionPensionariaService = informacionPensionariaService;
    }

    @GetMapping
    public InformacionPensionariaResponse get(@PathVariable Long trabajadorId) {
        return informacionPensionariaService.getByTrabajadorId(trabajadorId);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public InformacionPensionariaResponse upsert(
            @PathVariable Long trabajadorId,
            @Valid @RequestBody InformacionPensionariaRequest request) {
        return informacionPensionariaService.upsert(trabajadorId, request);
    }
}

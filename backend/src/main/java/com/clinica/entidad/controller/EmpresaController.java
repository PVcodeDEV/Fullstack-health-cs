package com.clinica.entidad.controller;

import com.clinica.entidad.dto.EmpresaRequest;
import com.clinica.entidad.dto.EmpresaResponse;
import com.clinica.entidad.dto.SunatRucResponse;
import com.clinica.entidad.entity.Empresa.Estado;
import com.clinica.entidad.entity.Empresa.Rol;
import com.clinica.entidad.service.EmpresaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/entidad")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    // --- CRUD ---

    @PostMapping("/empresa")
    @PreAuthorize("hasAuthority('entidad:crear')")
    public ResponseEntity<EmpresaResponse> create(@Valid @RequestBody EmpresaRequest request) {
        var response = empresaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/empresa/{id}")
    @PreAuthorize("hasAnyAuthority('entidad:ver', 'caja:ver')")
    public EmpresaResponse findById(@PathVariable Long id) {
        return empresaService.findById(id);
    }

    @GetMapping("/empresa/ruc/{ruc}")
    @PreAuthorize("hasAnyAuthority('entidad:ver', 'caja:ver')")
    public EmpresaResponse findByRuc(@PathVariable String ruc) {
        return empresaService.findByRuc(ruc);
    }

    @GetMapping("/empresa")
    @PreAuthorize("hasAnyAuthority('entidad:ver', 'caja:ver')")
    public Page<EmpresaResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Estado estado,
            Pageable pageable) {
        return empresaService.search(q, rol, estado, pageable);
    }

    @PutMapping("/empresa/{id}")
    @PreAuthorize("hasAuthority('entidad:editar')")
    public EmpresaResponse update(@PathVariable Long id, @Valid @RequestBody EmpresaRequest request) {
        return empresaService.update(id, request);
    }

    @DeleteMapping("/empresa/{id}")
    @PreAuthorize("hasAuthority('entidad:editar')")
    public EmpresaResponse softDelete(@PathVariable Long id) {
        return empresaService.softDelete(id);
    }

    // --- SUNAT Consult ---

    @GetMapping("/sunat/consultar/{ruc}")
    @PreAuthorize("hasAuthority('entidad:consultar-sunat')")
    public ResponseEntity<SunatRucResponse> consultarSunat(
            @PathVariable String ruc,
            HttpServletRequest request) {
        String ipOrigen = request.getRemoteAddr();
        // usuarioId from security context (simplified: null for now)
        SunatRucResponse result = empresaService.consultarSunat(ruc, ipOrigen, null);
        if (!result.exito()) {
            // ENT-003-3: SUNAT unavailable/timeout → 503
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
        }
        return ResponseEntity.ok(result);
    }
}

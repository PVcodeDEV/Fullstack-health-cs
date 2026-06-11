package com.clinica.clinica.hospitalizacion.controller;

import com.clinica.clinica.hospitalizacion.dto.AltaMedicaRequest;
import com.clinica.clinica.hospitalizacion.dto.AltaMedicaResponse;
import com.clinica.clinica.hospitalizacion.dto.CambioHabitacionRequest;
import com.clinica.clinica.hospitalizacion.dto.CambioHabitacionResponse;
import com.clinica.clinica.hospitalizacion.dto.NotaEvolucionRequest;
import com.clinica.clinica.hospitalizacion.dto.NotaEvolucionResponse;
import com.clinica.clinica.hospitalizacion.dto.SolicitudMedicamentoRequest;
import com.clinica.clinica.hospitalizacion.dto.SolicitudMedicamentoResponse;
import com.clinica.clinica.hospitalizacion.service.HospitalizacionService;
import com.clinica.seguridad.service.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hospitalizacion")
@PreAuthorize("hasAuthority('hospitalizacion:ver')")
public class HospitalizacionController {

    private final HospitalizacionService service;

    public HospitalizacionController(HospitalizacionService service) {
        this.service = service;
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal up) {
            return up.getUsuario().getId();
        }
        throw new SecurityException("Usuario no autenticado");
    }

    @PostMapping("/{id}/cambiar-cama")
    @PreAuthorize("hasAuthority('hospitalizacion:editar')")
    public CambioHabitacionResponse cambiarCama(
            @PathVariable Long id,
            @Valid @RequestBody CambioHabitacionRequest request) {
        return service.cambiarCama(id, request, getCurrentUserId());
    }

    @PostMapping("/{id}/notas")
    @PreAuthorize("hasAuthority('hospitalizacion:editar')")
    public ResponseEntity<NotaEvolucionResponse> registrarNota(
            @PathVariable Long id,
            @Valid @RequestBody NotaEvolucionRequest request) {
        var response = service.registrarNota(id, request, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/medicamentos")
    @PreAuthorize("hasAuthority('hospitalizacion:editar')")
    public ResponseEntity<SolicitudMedicamentoResponse> solicitarMedicamento(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudMedicamentoRequest request) {
        var response = service.solicitarMedicamento(id, request, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/alta")
    @PreAuthorize("hasAuthority('hospitalizacion:editar')")
    public AltaMedicaResponse darAlta(
            @PathVariable Long id,
            @Valid @RequestBody AltaMedicaRequest request) {
        return service.darAlta(id, request);
    }
}

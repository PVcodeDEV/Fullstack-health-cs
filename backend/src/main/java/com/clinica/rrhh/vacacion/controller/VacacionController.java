package com.clinica.rrhh.vacacion.controller;

import com.clinica.rrhh.vacacion.dto.ProgramarRequest;
import com.clinica.rrhh.vacacion.dto.VacacionGoceResponse;
import com.clinica.rrhh.vacacion.dto.VacacionRecordResponse;
import com.clinica.rrhh.vacacion.service.VacacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vacaciones")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class VacacionController {

    private final VacacionService service;

    public VacacionController(VacacionService service) {
        this.service = service;
    }

    @PostMapping("/calcular")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<List<VacacionRecordResponse>> calcular(
            @RequestParam(defaultValue = "0") Integer diasReduccion) {
        var records = service.calcular(diasReduccion);
        return ResponseEntity.status(HttpStatus.CREATED).body(records);
    }

    @PostMapping("/programar")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<VacacionGoceResponse> programar(@Valid @RequestBody ProgramarRequest request) {
        var goce = service.programar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(goce);
    }

    @PostMapping("/goces/{id}/iniciar")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public VacacionGoceResponse iniciar(@PathVariable Long id) {
        return service.iniciar(id);
    }

    @PostMapping("/goces/{id}/completar")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public VacacionGoceResponse completar(@PathVariable Long id) {
        return service.completar(id);
    }

    @PostMapping("/goces/{id}/cancelar")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public VacacionGoceResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @GetMapping("/records")
    public List<VacacionRecordResponse> findRecords(@RequestParam(required = false) Long trabajadorId) {
        if (trabajadorId != null) {
            return service.findRecordsByTrabajador(trabajadorId);
        }
        return List.of();
    }

    @GetMapping("/records/{id}")
    public VacacionRecordResponse findRecordById(@PathVariable Long id) {
        return service.findRecordById(id);
    }

    @GetMapping("/records/{recordId}/goces")
    public List<VacacionGoceResponse> findGocesByRecord(@PathVariable Long recordId) {
        return service.findGocesByRecord(recordId);
    }
}

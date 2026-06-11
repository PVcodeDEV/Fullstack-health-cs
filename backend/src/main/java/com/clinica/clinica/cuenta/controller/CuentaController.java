package com.clinica.clinica.cuenta.controller;

import com.clinica.clinica.cuenta.dto.CargoAdicionalRequest;
import com.clinica.clinica.cuenta.dto.CargoAdicionalResponse;
import com.clinica.clinica.cuenta.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cuenta")
@PreAuthorize("hasAuthority('cuenta:ver')")
public class CuentaController {

    private final CuentaService service;

    public CuentaController(CuentaService service) {
        this.service = service;
    }

    @PostMapping("/cargos")
    @PreAuthorize("hasAuthority('cuenta:editar')")
    public ResponseEntity<CargoAdicionalResponse> agregarCargo(@Valid @RequestBody CargoAdicionalRequest request) {
        var response = service.agregarCargo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/cargos")
    public List<CargoAdicionalResponse> listarCargos(@RequestParam Long cuentaId) {
        return service.listarCargos(cuentaId);
    }

    @GetMapping("/cuentas/{id}")
    public Object obtenerCuenta(@PathVariable Long id) {
        return service.obtenerCuenta(id);
    }

    @PostMapping("/cuentas/{id}/confirmar-cobro")
    @PreAuthorize("hasAuthority('cuenta:editar')")
    public ResponseEntity<Map<String, String>> confirmarCobro(@PathVariable Long id) {
        service.confirmarCobro(id);
        return ResponseEntity.ok(Map.of("mensaje", "Cobro confirmado para cuenta " + id));
    }
}

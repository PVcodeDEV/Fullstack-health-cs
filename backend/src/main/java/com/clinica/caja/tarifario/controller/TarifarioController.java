package com.clinica.caja.tarifario.controller;

import com.clinica.caja.tarifario.dto.PaqueteRequest;
import com.clinica.caja.tarifario.dto.PaqueteResponse;
import com.clinica.caja.tarifario.dto.PriceChangeRequest;
import com.clinica.caja.tarifario.dto.PrecioResponse;
import com.clinica.caja.tarifario.dto.TarifarioItemRequest;
import com.clinica.caja.tarifario.dto.TarifarioItemResponse;
import com.clinica.caja.tarifario.entity.Tarifario;
import com.clinica.caja.tarifario.service.TarifarioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/caja")
public class TarifarioController {

    private final TarifarioService tarifarioService;

    public TarifarioController(TarifarioService tarifarioService) {
        this.tarifarioService = tarifarioService;
    }

    // --- Tarifario CRUD ---

    @GetMapping("/tarifario")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'entidad:ver')")
    public List<Tarifario> listarTarifarios() {
        return tarifarioService.listarTarifarios();
    }

    @GetMapping("/tarifario/{id}")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'entidad:ver')")
    public Tarifario obtenerTarifario(@PathVariable Long id) {
        return tarifarioService.obtenerTarifario(id);
    }

    @PostMapping("/tarifario")
    @PreAuthorize("hasAuthority('caja:crear')")
    public ResponseEntity<Tarifario> crearTarifario(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Long aseguradoraId) {
        var tarifario = tarifarioService.crearTarifario(nombre, descripcion, aseguradoraId);
        return ResponseEntity.status(HttpStatus.CREATED).body(tarifario);
    }

    // --- Tarifario Items ---

    @GetMapping("/tarifario-item")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'entidad:ver')")
    public List<TarifarioItemResponse> listItems(@RequestParam Long tarifarioId) {
        return tarifarioService.listItemsByTarifario(tarifarioId);
    }

    @GetMapping("/tarifario-item/{id}")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'entidad:ver')")
    public TarifarioItemResponse getItem(@PathVariable Long id) {
        return tarifarioService.findItemById(id);
    }

    @PostMapping("/tarifario-item")
    @PreAuthorize("hasAuthority('caja:crear')")
    public ResponseEntity<TarifarioItemResponse> createItem(@Valid @RequestBody TarifarioItemRequest request) {
        var response = tarifarioService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/tarifario-item/price-change")
    @PreAuthorize("hasAuthority('caja:editar')")
    public ResponseEntity<TarifarioItemResponse> priceChange(@Valid @RequestBody PriceChangeRequest request) {
        var response = tarifarioService.priceChange(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Resolve effective price for a codigo at a given date (TRF-006).
     */
    @GetMapping("/tarifario-item/{codigo}/precio")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'entidad:ver')")
    public PrecioResponse resolvePrecio(
            @PathVariable String codigo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return tarifarioService.resolvePrecio(codigo, fecha);
    }

    // --- Paquetes ---

    @GetMapping("/paquete")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'entidad:ver')")
    public List<PaqueteResponse> listPaquetes() {
        return tarifarioService.listPaquetes();
    }

    @GetMapping("/paquete/{id}")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'entidad:ver')")
    public PaqueteResponse getPaquete(@PathVariable Long id) {
        return tarifarioService.findPaqueteById(id);
    }

    @PostMapping("/paquete")
    @PreAuthorize("hasAuthority('caja:crear')")
    public ResponseEntity<PaqueteResponse> createPaquete(@Valid @RequestBody PaqueteRequest request) {
        var response = tarifarioService.createPaquete(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/paquete/{id}")
    @PreAuthorize("hasAuthority('caja:editar')")
    public PaqueteResponse deletePaquete(@PathVariable Long id) {
        return tarifarioService.softDeletePaquete(id);
    }
}

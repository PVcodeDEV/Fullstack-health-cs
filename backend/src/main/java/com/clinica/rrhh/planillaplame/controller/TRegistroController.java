package com.clinica.rrhh.planillaplame.controller;

import com.clinica.rrhh.planillaplame.dto.ArchivoPlanillaResponse;
import com.clinica.rrhh.planillaplame.dto.TRegistroEventoResponse;
import com.clinica.rrhh.planillaplame.entity.ArchivoPlanilla;
import com.clinica.rrhh.planillaplame.service.TRegistroService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/t-registro")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class TRegistroController {

    private final TRegistroService service;

    public TRegistroController(TRegistroService service) {
        this.service = service;
    }

    @PostMapping("/generar")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<ArchivoPlanillaResponse> generar(@RequestParam Long periodoPlanillaId) {
        var response = service.generar(periodoPlanillaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/eventos")
    public List<TRegistroEventoResponse> getEventos(@RequestParam Long periodoPlanillaId) {
        return service.getEventos(periodoPlanillaId);
    }

    @GetMapping("/archivos/{id}/descargar")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        ArchivoPlanilla archivo = service.getArchivoParaDescarga(id);
        byte[] content = archivo.getContenido().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(content.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo.getPeriodoPlanilla().getId() + ".treg\"")
                .body(content);
    }
}

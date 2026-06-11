package com.clinica.rrhh.planillaplame.controller;

import com.clinica.rrhh.planilla.config.PlanillaProperties;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planillaplame.dto.ArchivoPlanillaResponse;
import com.clinica.rrhh.planillaplame.entity.ArchivoPlanilla;
import com.clinica.rrhh.planillaplame.service.PlameService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/v1/plame")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class PlameController {

    private final PlameService service;
    private final PeriodoPlanillaRepository periodoPlanillaRepository;
    private final PlanillaProperties properties;

    public PlameController(PlameService service,
                           PeriodoPlanillaRepository periodoPlanillaRepository,
                           PlanillaProperties properties) {
        this.service = service;
        this.periodoPlanillaRepository = periodoPlanillaRepository;
        this.properties = properties;
    }

    @PostMapping("/generar")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<List<ArchivoPlanillaResponse>> generar(@RequestParam Long periodoPlanillaId) {
        List<ArchivoPlanillaResponse> responses = service.generar(periodoPlanillaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/archivos")
    public List<ArchivoPlanillaResponse> getArchivos(@RequestParam Long periodoPlanillaId) {
        return service.getArchivos(periodoPlanillaId);
    }

    @GetMapping("/archivos/{id}/descargar")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        ArchivoPlanilla archivo = service.getArchivoParaDescarga(id);
        return buildFileResponse(archivo, archivo.getTipo().toLowerCase());
    }

    @GetMapping("/descargar")
    public ResponseEntity<byte[]> descargarPorTipo(@RequestParam Long periodoPlanillaId,
                                                    @RequestParam String tipo) {
        ArchivoPlanilla archivo = service.getArchivoPorPeriodoTipo(periodoPlanillaId, tipo);
        return buildFileResponse(archivo, tipo.toLowerCase());
    }

    @GetMapping("/descargar-zip")
    public ResponseEntity<byte[]> descargarZip(@RequestParam Long periodoPlanillaId) {
        PeriodoPlanilla periodo = periodoPlanillaRepository.findById(periodoPlanillaId)
                .orElseThrow(() -> new EntityNotFoundException("Periodo no encontrado: " + periodoPlanillaId));

        List<ArchivoPlanilla> archivos = service.getArchivosPorPeriodo(periodoPlanillaId);
        String ruc = properties.getRucEmpleador();
        String periodoStr = String.format("%04d%02d", periodo.getAnio(), periodo.getMes());
        String filename = ruc + "-" + periodoStr + ".zip";

        byte[] zipBytes = buildZip(archivos);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipBytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(zipBytes);
    }

    private ResponseEntity<byte[]> buildFileResponse(ArchivoPlanilla archivo, String extension) {
        byte[] content = archivo.getContenido().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(content.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + properties.getRucEmpleador() + "." + extension + "\"")
                .body(content);
    }

    private byte[] buildZip(List<ArchivoPlanilla> archivos) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {

            for (ArchivoPlanilla archivo : archivos) {
                String entryName = properties.getRucEmpleador() + "." + archivo.getTipo().toLowerCase();
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                byte[] content = archivo.getContenido().getBytes(StandardCharsets.UTF_8);
                zos.write(content);
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar ZIP de PLAME", e);
        }
    }
}

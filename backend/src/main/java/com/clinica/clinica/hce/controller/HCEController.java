package com.clinica.clinica.hce.controller;

import com.clinica.clinica.hce.dto.DocumentoClinicoRequest;
import com.clinica.clinica.hce.dto.DocumentoClinicoResponse;
import com.clinica.clinica.hce.service.HCEService;
import com.clinica.seguridad.service.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/api/v1/hce")
@PreAuthorize("hasAuthority('hce:ver')")
public class HCEController {

    private final HCEService service;

    @Autowired
    private HttpServletRequest request;

    public HCEController(HCEService service) {
        this.service = service;
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        return ip;
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal up) {
            return up.getUsuario().getId();
        }
        throw new SecurityException("Usuario no autenticado");
    }

    @PostMapping("/documentos")
    @PreAuthorize("hasAuthority('hce:editar')")
    public ResponseEntity<DocumentoClinicoResponse> crearDocumento(@Valid @RequestBody DocumentoClinicoRequest body) {
        var response = service.crearDocumento(body, getCurrentUserId(), getClientIp());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/documentos")
    public List<DocumentoClinicoResponse> listarDocumentos(@RequestParam Long hospitalizacionId) {
        return service.listarDocumentos(hospitalizacionId);
    }

    @GetMapping("/documentos/{id}/verificar")
    public Map<String, Object> verificarFirma(@PathVariable Long id) {
        boolean valida = service.verificarFirma(id);
        return Map.of("documentoId", id, "firmaValida", valida);
    }
}

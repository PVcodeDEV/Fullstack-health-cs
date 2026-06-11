package com.clinica.clinica.hce.controller;

import com.clinica.clinica.hce.dto.DocumentoClinicoResponse;
import com.clinica.clinica.hce.service.HCEService;
import com.clinica.seguridad.entity.Usuario;
import com.clinica.seguridad.service.UsuarioPrincipal;
import com.clinica.config.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HCEController.class)
@Import(GlobalExceptionHandler.class)
class HCEControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HCEService service;

    @BeforeEach
    void setUpSecurity() {
        var usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("testuser");
        var principal = new UsuarioPrincipal(usuario, Set.of(
                new SimpleGrantedAuthority("hce:ver"),
                new SimpleGrantedAuthority("hce:editar")
        ));
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void crearDocumento_ShouldReturn201() throws Exception {
        when(service.crearDocumento(any(), anyLong(), anyString())).thenReturn(
                new DocumentoClinicoResponse(1L, 1L, "INFORME", null, null, "Dr. Perez",
                        LocalDateTime.now(), true, "a".repeat(64)));

        mockMvc.perform(post("/api/v1/hce/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hospitalizacionId\":1,\"tipoDocumento\":\"INFORME\",\"contenido\":\"Contenido del documento\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoDocumento").value("INFORME"))
                .andExpect(jsonPath("$.firmaPresente").value(true));
    }

    @Test
    void crearDocumento_WithInvalidBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/hce/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hospitalizacionId\":null,\"tipoDocumento\":\"\",\"contenido\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarDocumentos_ShouldReturn200() throws Exception {
        when(service.listarDocumentos(1L)).thenReturn(List.of(
                new DocumentoClinicoResponse(1L, 1L, "INFORME", null, null, "Dr. Perez",
                        LocalDateTime.now(), true, "a".repeat(64))));

        mockMvc.perform(get("/api/v1/hce/documentos")
                        .param("hospitalizacionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoDocumento").value("INFORME"));
    }

    @Test
    void verificarFirma_WithValidDocumento_ShouldReturn200() throws Exception {
        when(service.verificarFirma(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/hce/documentos/1/verificar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firmaValida").value(true))
                .andExpect(jsonPath("$.documentoId").value(1));
    }

    @Test
    void verificarFirma_WithInvalidDocumento_ShouldReturn404() throws Exception {
        when(service.verificarFirma(99L))
                .thenThrow(new EntityNotFoundException("Documento no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/hce/documentos/99/verificar"))
                .andExpect(status().isNotFound());
    }
}

package com.clinica.rrhh.pension.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.pension.dto.InformacionPensionariaResponse;
import com.clinica.rrhh.pension.service.InformacionPensionariaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InformacionPensionariaController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"rrhh:ver", "rrhh:editar"})
class InformacionPensionariaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InformacionPensionariaService informacionPensionariaService;

    private InformacionPensionariaResponse createResponse(Long trabajadorId) {
        return new InformacionPensionariaResponse(
            1L, trabajadorId, 1L, "PRIMA", "Prima AFP",
            "123456789012", "FLUJO", false,
            LocalDate.of(2025, 1, 1), "ACTIVO", null);
    }

    @Test
    void get_ShouldReturn200() throws Exception {
        when(informacionPensionariaService.getByTrabajadorId(1L))
            .thenReturn(createResponse(1L));

        mockMvc.perform(get("/api/v1/trabajadores/1/informacion-pensionaria"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ACTIVO"))
            .andExpect(jsonPath("$.afpCodigo").value("PRIMA"))
            .andExpect(jsonPath("$.comisionTipo").value("FLUJO"));
    }

    @Test
    void get_ShouldReturn404() throws Exception {
        when(informacionPensionariaService.getByTrabajadorId(99L))
            .thenThrow(new EntityNotFoundException("Información pensionaria no encontrada para trabajador id: 99"));

        mockMvc.perform(get("/api/v1/trabajadores/99/informacion-pensionaria"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})  // no rrhh:editar
    void upsert_WithoutEditAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(put("/api/v1/trabajadores/1/informacion-pensionaria")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "afpId": 1,
                        "cuspp": "123456789012",
                        "comisionTipo": "FLUJO",
                        "sctr": false,
                        "fechaAfiliacion": "2025-01-01"
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void upsert_ShouldCreateAndReturn200() throws Exception {
        when(informacionPensionariaService.upsert(eq(1L), any()))
            .thenReturn(createResponse(1L));

        mockMvc.perform(put("/api/v1/trabajadores/1/informacion-pensionaria")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "afpId": 1,
                        "cuspp": "123456789012",
                        "comisionTipo": "FLUJO",
                        "sctr": false,
                        "fechaAfiliacion": "2025-01-01"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void upsert_ShouldRejectInvalidCuspp() throws Exception {
        when(informacionPensionariaService.upsert(eq(1L), any()))
            .thenThrow(new IllegalArgumentException("CUSPP debe tener 12 dígitos"));

        mockMvc.perform(put("/api/v1/trabajadores/1/informacion-pensionaria")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "afpId": 1,
                        "cuspp": "12345",
                        "comisionTipo": "FLUJO",
                        "sctr": false,
                        "fechaAfiliacion": "2025-01-01"
                    }
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = {})
    void upsert_WithoutAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(put("/api/v1/trabajadores/1/informacion-pensionaria")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "afpId": 1,
                        "cuspp": "123456789012",
                        "comisionTipo": "FLUJO",
                        "sctr": false,
                        "fechaAfiliacion": "2025-01-01"
                    }
                    """))
            .andExpect(status().isForbidden());
    }
}

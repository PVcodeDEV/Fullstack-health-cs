package com.clinica.rrhh.contrato.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.contrato.dto.ContratoResponse;
import com.clinica.rrhh.contrato.service.ContratoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContratoController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"rrhh:ver", "rrhh:editar"})
class ContratoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContratoService contratoService;

    private ContratoResponse createResponse(Long id, String estado) {
        return new ContratoResponse(id, 1L, 1L, "Indeterminado",
                LocalDate.of(2025, 1, 1), null, null,
                new BigDecimal("2500.00"), "REGULAR", estado, null, true);
    }

    @Test
    void findAll_ShouldReturn200() throws Exception {
        when(contratoService.findAll()).thenReturn(List.of(createResponse(1L, "ACTIVO")));

        mockMvc.perform(get("/api/v1/contratos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        when(contratoService.findById(1L)).thenReturn(createResponse(1L, "ACTIVO"));

        mockMvc.perform(get("/api/v1/contratos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void findById_ShouldReturn404() throws Exception {
        when(contratoService.findById(99L)).thenThrow(new EntityNotFoundException("Contrato no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/contratos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        when(contratoService.create(any())).thenReturn(createResponse(1L, "ACTIVO"));

        mockMvc.perform(post("/api/v1/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "trabajadorId": 1,
                                    "tipoContratoId": 1,
                                    "fechaInicio": "2025-01-01",
                                    "remuneracion": 2500.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void create_WithInvalidBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void suspender_ShouldReturn200() throws Exception {
        when(contratoService.suspender(1L)).thenReturn(createResponse(1L, "SUSPENDIDO"));

        mockMvc.perform(put("/api/v1/contratos/1/suspender"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SUSPENDIDO"));
    }

    @Test
    void reactivar_ShouldReturn200() throws Exception {
        when(contratoService.reactivar(1L)).thenReturn(createResponse(1L, "ACTIVO"));

        mockMvc.perform(put("/api/v1/contratos/1/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void resolver_ShouldReturn200() throws Exception {
        when(contratoService.resolver(eq(1L), any())).thenReturn(createResponse(1L, "RESUELTO"));

        mockMvc.perform(put("/api/v1/contratos/1/resolver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTO"));
    }

    @Test
    void resolver_IllegalState_ShouldReturn409() throws Exception {
        when(contratoService.resolver(eq(1L), any())).thenThrow(new IllegalStateException("No se puede resolver"));

        mockMvc.perform(put("/api/v1/contratos/1/resolver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    void update_ShouldReturn200() throws Exception {
        when(contratoService.update(eq(1L), any())).thenReturn(createResponse(1L, "ACTIVO"));

        mockMvc.perform(put("/api/v1/contratos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tipoContratoId": 1,
                                    "fechaInicio": "2025-01-01",
                                    "remuneracion": 2500.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void update_WithInvalidBody_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/api/v1/contratos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

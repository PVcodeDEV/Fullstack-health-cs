package com.clinica.rrhh.gratificacion.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.gratificacion.dto.GratificacionResponse;
import com.clinica.rrhh.gratificacion.service.GratificacionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GratificacionController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"rrhh:ver", "rrhh:editar"})
class GratificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GratificacionService gratificacionService;

    private GratificacionResponse createResponse(Long id) {
        return new GratificacionResponse(id, 1L, "2026-06", 1L, "JUAN PEREZ",
            1L, "ENERO-JUNIO", 6, new BigDecimal("2000.00"),
            new BigDecimal("1000.00"), new BigDecimal("90.00"),
            new BigDecimal("1090.00"), "CALCULADO");
    }

    @Test
    void findAll_ShouldReturn200() throws Exception {
        when(gratificacionService.findAll()).thenReturn(List.of(createResponse(1L)));

        mockMvc.perform(get("/api/v1/gratificaciones"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].semestre").value("ENERO-JUNIO"))
            .andExpect(jsonPath("$[0].trabajadorNombre").value("JUAN PEREZ"));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        when(gratificacionService.findById(1L)).thenReturn(createResponse(1L));

        mockMvc.perform(get("/api/v1/gratificaciones/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.semestre").value("ENERO-JUNIO"));
    }

    @Test
    void findById_ShouldReturn404() throws Exception {
        when(gratificacionService.findById(99L))
            .thenThrow(new EntityNotFoundException("Gratificación no encontrada: 99"));

        mockMvc.perform(get("/api/v1/gratificaciones/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void calcular_ShouldReturn201() throws Exception {
        when(gratificacionService.calcular(1L)).thenReturn(List.of(createResponse(1L)));

        mockMvc.perform(post("/api/v1/gratificaciones/calcular")
                .param("periodoPlanillaId", "1"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].semestre").value("ENERO-JUNIO"));
    }

    @Test
    void calcular_WithoutEditar_ShouldReturn403() throws Exception {
        var test = mockMvc.perform(post("/api/v1/gratificaciones/calcular")
                .param("periodoPlanillaId", "1"));
        // Test is run with rrhh:editar authority, so we need a separate test user
        // This test will be done via the method below
    }

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void calcular_WithoutEditarAuthority_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/gratificaciones/calcular")
                .param("periodoPlanillaId", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void calcular_InvalidPeriodo_ShouldReturn400() throws Exception {
        when(gratificacionService.calcular(anyLong()))
            .thenThrow(new IllegalArgumentException("Periodo inválido para gratificación"));

        mockMvc.perform(post("/api/v1/gratificaciones/calcular")
                .param("periodoPlanillaId", "3"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void findAll_WithVerAuthority_ShouldReturn200() throws Exception {
        when(gratificacionService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/gratificaciones"))
            .andExpect(status().isOk());
    }
}

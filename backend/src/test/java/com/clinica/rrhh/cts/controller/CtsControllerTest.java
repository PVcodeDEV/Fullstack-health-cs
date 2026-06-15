package com.clinica.rrhh.cts.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.cts.dto.DepositoCtsResponse;
import com.clinica.rrhh.cts.service.CtsService;
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

@WebMvcTest(CtsController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"rrhh:ver", "rrhh:editar"})
class CtsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CtsService ctsService;

    private DepositoCtsResponse createResponse(Long id) {
        return new DepositoCtsResponse(id, 1L, "2026-05", 1L, "JUAN PEREZ",
            1L, "MAYO-OCTUBRE", 180, new BigDecimal("2000.00"),
            new BigDecimal("0.00"), new BigDecimal("0.00"),
            new BigDecimal("1000.00"), "CALCULADO");
    }

    @Test
    void findAll_ShouldReturn200() throws Exception {
        when(ctsService.findAll()).thenReturn(List.of(createResponse(1L)));

        mockMvc.perform(get("/api/v1/cts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].semestre").value("MAYO-OCTUBRE"))
            .andExpect(jsonPath("$[0].trabajadorNombre").value("JUAN PEREZ"));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        when(ctsService.findById(1L)).thenReturn(createResponse(1L));

        mockMvc.perform(get("/api/v1/cts/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.semestre").value("MAYO-OCTUBRE"));
    }

    @Test
    void findById_ShouldReturn404() throws Exception {
        when(ctsService.findById(99L))
            .thenThrow(new EntityNotFoundException("CTS no encontrado: 99"));

        mockMvc.perform(get("/api/v1/cts/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void calcular_ShouldReturn201() throws Exception {
        when(ctsService.calcular(1L)).thenReturn(List.of(createResponse(1L)));

        mockMvc.perform(post("/api/v1/cts/calcular")
                .param("periodoPlanillaId", "1"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].semestre").value("MAYO-OCTUBRE"));
    }

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void calcular_WithoutEditarAuthority_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/cts/calcular")
                .param("periodoPlanillaId", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void calcular_InvalidPeriodo_ShouldReturn400() throws Exception {
        when(ctsService.calcular(anyLong()))
            .thenThrow(new IllegalArgumentException("Periodo inválido para CTS"));

        mockMvc.perform(post("/api/v1/cts/calcular")
                .param("periodoPlanillaId", "3"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void findAll_WithVerAuthority_ShouldReturn200() throws Exception {
        when(ctsService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/cts"))
            .andExpect(status().isOk());
    }
}

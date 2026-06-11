package com.clinica.clinica.cama.controller;

import com.clinica.clinica.cama.dto.CamaRequest;
import com.clinica.clinica.cama.dto.CamaResponse;
import com.clinica.clinica.cama.service.CamaService;
import com.clinica.config.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CamaController.class)
@Import(GlobalExceptionHandler.class)
class CamaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CamaService service;

    @Test
    void findAll_ShouldReturn200() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                new CamaResponse(1L, 1L, "CAMA-001", "DISPONIBLE", true, null)));

        mockMvc.perform(get("/api/v1/camas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("CAMA-001"));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        when(service.findById(1L)).thenReturn(
                new CamaResponse(1L, 1L, "CAMA-001", "DISPONIBLE", true, null));

        mockMvc.perform(get("/api/v1/camas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("CAMA-001"));
    }

    @Test
    void findById_ShouldReturn404() throws Exception {
        when(service.findById(99L)).thenThrow(new EntityNotFoundException("Cama no encontrada con id: 99"));

        mockMvc.perform(get("/api/v1/camas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        when(service.create(any())).thenReturn(
                new CamaResponse(1L, 1L, "CAMA-NEW", "DISPONIBLE", true, null));

        mockMvc.perform(post("/api/v1/camas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"habitacionId\":1,\"codigo\":\"CAMA-NEW\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("CAMA-NEW"));
    }

    @Test
    void create_WithInvalidBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/camas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"habitacionId\":null,\"codigo\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarEstado_ShouldReturn200() throws Exception {
        when(service.cambiarEstado(eq(1L), eq("OCUPADO"))).thenReturn(
                new CamaResponse(1L, 1L, "CAMA-001", "OCUPADO", true, null));

        mockMvc.perform(put("/api/v1/camas/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"OCUPADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OCUPADO"));
    }

    @Test
    void softDelete_ShouldReturn200() throws Exception {
        when(service.softDelete(1L)).thenReturn(
                new CamaResponse(1L, 1L, "CAMA-001", "DISPONIBLE", false, null));

        mockMvc.perform(delete("/api/v1/camas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }
}

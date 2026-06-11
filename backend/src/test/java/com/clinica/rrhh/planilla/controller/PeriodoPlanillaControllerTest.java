package com.clinica.rrhh.planilla.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.planilla.dto.PeriodoPlanillaRequest;
import com.clinica.rrhh.planilla.dto.PeriodoPlanillaResponse;
import com.clinica.rrhh.planilla.service.PeriodoPlanillaService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PeriodoPlanillaController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"rrhh:ver", "rrhh:editar"})
class PeriodoPlanillaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PeriodoPlanillaService service;

    private PeriodoPlanillaResponse createResponse(Long id, int anio, int mes) {
        return new PeriodoPlanillaResponse(id, anio, mes,
            LocalDate.of(anio, mes, 1),
            LocalDate.of(anio, mes, mes == 2 ? 28 : 30),
            "ABIERTO", true);
    }

    @Test
    void findAll_ShouldReturn200() throws Exception {
        when(service.findAll()).thenReturn(List.of(
            createResponse(1L, 2026, 2),
            createResponse(2L, 2026, 1)));

        mockMvc.perform(get("/api/v1/periodos-planilla"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        when(service.findById(1L)).thenReturn(createResponse(1L, 2026, 1));

        mockMvc.perform(get("/api/v1/periodos-planilla/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.estado").value("ABIERTO"));
    }

    @Test
    void findById_ShouldReturn404() throws Exception {
        when(service.findById(99L))
            .thenThrow(new EntityNotFoundException("Periodo no encontrado: 99"));

        mockMvc.perform(get("/api/v1/periodos-planilla/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        when(service.create(any())).thenReturn(createResponse(1L, 2026, 1));

        mockMvc.perform(post("/api/v1/periodos-planilla")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "anio": 2026,
                        "mes": 1,
                        "fechaInicio": "2026-01-01",
                        "fechaFin": "2026-01-31"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_Duplicate_ShouldReturn409() throws Exception {
        when(service.create(any()))
            .thenThrow(new IllegalArgumentException("Ya existe un periodo para 2026-1"));

        mockMvc.perform(post("/api/v1/periodos-planilla")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "anio": 2026,
                        "mes": 1,
                        "fechaInicio": "2026-01-01",
                        "fechaFin": "2026-01-31"
                    }
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    void create_InvalidInput_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/periodos-planilla")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "anio": 1900,
                        "mes": 13,
                        "fechaInicio": null,
                        "fechaFin": null
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void cerrar_ShouldReturn200() throws Exception {
        when(service.cerrar(1L)).thenReturn(
            new PeriodoPlanillaResponse(1L, 2026, 1,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                "CERRADO", true));

        mockMvc.perform(put("/api/v1/periodos-planilla/1/cerrar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CERRADO"));
    }

    @Test
    void cerrar_WithoutPlanilla_ShouldReturn409() throws Exception {
        when(service.cerrar(1L))
            .thenThrow(new IllegalStateException("No se puede cerrar un periodo sin planilla generada"));

        mockMvc.perform(put("/api/v1/periodos-planilla/1/cerrar"))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void create_WithoutEditAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/periodos-planilla")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "anio": 2026,
                        "mes": 1,
                        "fechaInicio": "2026-01-01",
                        "fechaFin": "2026-01-31"
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {})
    void find_WithoutAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/periodos-planilla"))
            .andExpect(status().isForbidden());
    }
}

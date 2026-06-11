package com.clinica.rrhh.derechohabiente.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.derechohabiente.dto.DerechohabienteResponse;
import com.clinica.rrhh.derechohabiente.service.DerechohabienteService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DerechohabienteController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"rrhh:ver", "rrhh:editar"})
class DerechohabienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DerechohabienteService derechohabienteService;

    private DerechohabienteResponse createResponse(Long id, String relacion, String estado) {
        return new DerechohabienteResponse(id, 1L, 2L, "HIJO RAMIREZ",
                "44444444", relacion, LocalDate.of(2025, 1, 1),
                LocalDate.of(2043, 1, 1), estado, true);
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        when(derechohabienteService.findById(1L)).thenReturn(createResponse(1L, "HIJO", "ACTIVO"));

        mockMvc.perform(get("/api/v1/derechohabientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.relacion").value("HIJO"));
    }

    @Test
    void findById_ShouldReturn404() throws Exception {
        when(derechohabienteService.findById(99L))
                .thenThrow(new EntityNotFoundException("Derechohabiente no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/derechohabientes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        when(derechohabienteService.create(any())).thenReturn(createResponse(1L, "HIJO", "ACTIVO"));

        mockMvc.perform(post("/api/v1/derechohabientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "trabajadorId": 1,
                                    "personaId": 2,
                                    "relacion": "HIJO",
                                    "fechaInicio": "2025-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.relacion").value("HIJO"));
    }

    @Test
    void create_WithInvalidBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/derechohabientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByTrabajadorId_ShouldReturn200() throws Exception {
        when(derechohabienteService.findByTrabajadorId(1L))
                .thenReturn(List.of(createResponse(1L, "HIJO", "ACTIVO")));

        mockMvc.perform(get("/api/v1/derechohabientes/trabajador/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].relacion").value("HIJO"));
    }

    @Test
    void inactivar_ShouldReturn200() throws Exception {
        when(derechohabienteService.inactivar(1L)).thenReturn(createResponse(1L, "HIJO", "INACTIVO"));

        mockMvc.perform(put("/api/v1/derechohabientes/1/inactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }
}

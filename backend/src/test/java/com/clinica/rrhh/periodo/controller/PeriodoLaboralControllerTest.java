package com.clinica.rrhh.periodo.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.periodo.dto.PeriodoLaboralResponse;
import com.clinica.rrhh.periodo.service.PeriodoLaboralService;
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

@WebMvcTest(PeriodoLaboralController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"rrhh:ver", "rrhh:editar"})
class PeriodoLaboralControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PeriodoLaboralService periodoLaboralService;

    private PeriodoLaboralResponse createResponse(Long id) {
        return new PeriodoLaboralResponse(id, 1L, LocalDate.of(2025, 1, 1),
                null, null, false, true);
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        when(periodoLaboralService.findById(1L)).thenReturn(createResponse(1L));

        mockMvc.perform(get("/api/v1/periodos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findById_ShouldReturn404() throws Exception {
        when(periodoLaboralService.findById(99L))
                .thenThrow(new EntityNotFoundException("Periodo laboral no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/periodos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrarCese_ShouldReturn200() throws Exception {
        var response = new PeriodoLaboralResponse(1L, 1L, LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31), "Renuncia voluntaria", false, false);
        when(periodoLaboralService.registrarCese(eq(1L), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/periodos/1/cese")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fechaCese": "2025-12-31",
                                    "motivo": "Renuncia voluntaria"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaCese").value("2025-12-31"))
                .andExpect(jsonPath("$.motivoCese").value("Renuncia voluntaria"));
    }

    @Test
    void registrarCese_InvalidBody_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/api/v1/periodos/1/cese")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

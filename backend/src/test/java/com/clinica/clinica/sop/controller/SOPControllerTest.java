package com.clinica.clinica.sop.controller;

import com.clinica.clinica.sop.dto.ReporteQuirurgicoResponse;
import com.clinica.clinica.sop.dto.URPARegistroResponse;
import com.clinica.clinica.sop.service.SOPService;
import com.clinica.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SOPController.class)
@Import(GlobalExceptionHandler.class)
class SOPControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SOPService service;

    @Test
    void crearReporte_ShouldReturn201() throws Exception {
        when(service.crearReporte(any())).thenReturn(
                new ReporteQuirurgicoResponse(1L, 1L, 1L, null, 2L, null, "APENDICITIS AGUDA",
                        "APENDICECTOMIA", null, null, LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(10, 0),
                        "BORRADOR", LocalDateTime.now()));

        mockMvc.perform(post("/api/v1/sop/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cirujanoId\":1,\"diagnosticoPreoperatorio\":\"APENDICITIS AGUDA\"," +
                                "\"procedimientoRealizado\":\"APENDICECTOMIA\",\"fechaCirugia\":\"2026-06-02\"," +
                                "\"horaInicio\":\"08:00:00\",\"horaFin\":\"10:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"));
    }

    @Test
    void completarReporte_ShouldReturn200() throws Exception {
        when(service.completarReporte(1L)).thenReturn(
                new ReporteQuirurgicoResponse(1L, 1L, 1L, null, null, null, "Dx",
                        "Procedimiento", null, null, LocalDate.now(), LocalTime.of(8, 0), null,
                        "COMPLETADO", LocalDateTime.now()));

        mockMvc.perform(put("/api/v1/sop/reportes/1/completar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    @Test
    void completarReporte_WithInvalidEstado_ShouldReturn400() throws Exception {
        when(service.completarReporte(1L))
                .thenThrow(new IllegalStateException("Solo se pueden completar reportes en estado BORRADOR"));

        mockMvc.perform(put("/api/v1/sop/reportes/1/completar"))
                .andExpect(status().isConflict());
    }

    @Test
    void registrarURPA_ShouldReturn201() throws Exception {
        when(service.registrarURPA(anyLong(), any())).thenReturn(
                new URPARegistroResponse(1L, 1L, LocalDateTime.now(), null, "ESTABLE", null, 10, null, null));

        mockMvc.perform(post("/api/v1/sop/reportes/1/urpa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"escalaAldreteIngreso\":10,\"condicionIngreso\":\"ESTABLE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.escalaAldreteIngreso").value(10));
    }
}

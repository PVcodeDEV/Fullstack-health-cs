package com.clinica.clinica.admision.controller;

import com.clinica.clinica.admision.dto.AdmisionDiagnosticoResponse;
import com.clinica.clinica.admision.dto.CuentaResponse;
import com.clinica.clinica.admision.service.AdmisionService;
import com.clinica.clinica.hospitalizacion.entity.Hospitalizacion;
import com.clinica.config.GlobalExceptionHandler;
import com.clinica.persona.dto.PersonaSearchResponse;
import com.clinica.persona.entity.Persona;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdmisionController.class)
@Import(GlobalExceptionHandler.class)
class AdmisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdmisionService service;

    @Test
    void buscarPaciente_ShouldReturn200() throws Exception {
        var persona = new Persona();
        persona.setId(1L);
        persona.setNombres("JUAN");
        persona.setApellidoPaterno("PEREZ");
        when(service.buscarPaciente("12345678")).thenReturn(List.of(persona));

        mockMvc.perform(get("/api/v1/admision/pacientes")
                        .param("query", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombres").value("JUAN"));
    }

    @Test
    void crearCuenta_ShouldReturn201() throws Exception {
        when(service.crearCuenta(any())).thenReturn(
                new CuentaResponse(1L, 1L, "JUAN PEREZ", null, null, "1", null, LocalDateTime.now(), "ABIERTA", null));

        mockMvc.perform(post("/api/v1/admision/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":1,\"tipoSeguroId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTA"));
    }

    @Test
    void crearCuenta_WithInvalidBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/admision/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarDiagnostico_ShouldReturn201() throws Exception {
        when(service.registrarDiagnostico(eq(1L), any())).thenReturn(
                new AdmisionDiagnosticoResponse(1L, 1L, "AA00.0", "Dx Principal", "PRINCIPAL", null));

        mockMvc.perform(post("/api/v1/admision/cuentas/1/diagnosticos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoCie11\":\"AA00.0\",\"tipoDiagnostico\":\"PRINCIPAL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoCie11").value("AA00.0"));
    }

    @Test
    void registrarDiagnostico_WithInvalidCuenta_ShouldReturn404() throws Exception {
        when(service.registrarDiagnostico(eq(99L), any()))
                .thenThrow(new EntityNotFoundException("Cuenta no encontrada con id: 99"));

        mockMvc.perform(post("/api/v1/admision/cuentas/99/diagnosticos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoCie11\":\"AA00.0\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void asignarCama_ShouldReturn200() throws Exception {
        var hosp = new Hospitalizacion();
        hosp.setId(1L);
        hosp.setEstado("HOSPITALIZADO");
        when(service.asignarCama(any())).thenReturn(hosp);

        mockMvc.perform(post("/api/v1/admision/cuentas/1/asignar-cama")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"camaId\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void asignarCama_WithCamaOcupada_ShouldReturn400() throws Exception {
        when(service.asignarCama(any()))
                .thenThrow(new IllegalStateException("La cama no está disponible"));

        mockMvc.perform(post("/api/v1/admision/cuentas/1/asignar-cama")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"camaId\":1}"))
                .andExpect(status().isConflict());
    }
}

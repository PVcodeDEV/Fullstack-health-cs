package com.clinica.clinica.hospitalizacion.controller;

import com.clinica.clinica.hospitalizacion.dto.*;
import com.clinica.clinica.hospitalizacion.service.HospitalizacionService;
import com.clinica.seguridad.entity.Usuario;
import com.clinica.seguridad.service.UsuarioPrincipal;
import com.clinica.config.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HospitalizacionController.class)
@Import(GlobalExceptionHandler.class)
class HospitalizacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HospitalizacionService service;

    @BeforeEach
    void setUpSecurity() {
        var usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("testuser");
        var principal = new UsuarioPrincipal(usuario, Set.of(
                new SimpleGrantedAuthority("hospitalizacion:ver"),
                new SimpleGrantedAuthority("hospitalizacion:editar")
        ));
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cambiarCama_ShouldReturn200() throws Exception {
        when(service.cambiarCama(anyLong(), any(), anyLong())).thenReturn(
                new CambioHabitacionResponse(1L, 1L, "CAMA-001", "CAMA-002", LocalDateTime.now(), "Mejor habitación", "1"));

        mockMvc.perform(post("/api/v1/hospitalizacion/1/cambiar-cama")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"camaDestinoId\":2,\"motivo\":\"Mejor habitación\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo").value("Mejor habitación"));
    }

    @Test
    void registrarNota_ShouldReturn201() throws Exception {
        when(service.registrarNota(anyLong(), any(), anyLong())).thenReturn(
                new NotaEvolucionResponse(1L, 1L, LocalDateTime.now(), "1", "Paciente estable", "Continuar tx", "EVOLUCION", "TA: 120/80"));

        mockMvc.perform(post("/api/v1/hospitalizacion/1/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descripcion\":\"Paciente estable\",\"plan\":\"Continuar tx\",\"tipo\":\"EVOLUCION\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descripcion").value("Paciente estable"));
    }

    @Test
    void registrarNota_WithInvalidBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/hospitalizacion/1/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descripcion\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void solicitarMedicamento_ShouldReturn201() throws Exception {
        when(service.solicitarMedicamento(anyLong(), any(), anyLong())).thenReturn(
                new SolicitudMedicamentoResponse(1L, 1L, 1L, "Paracetamol", "500mg", "Cada 8h", 1L,
                        LocalDate.now(), LocalDate.now().plusDays(7), "PENDIENTE", "1"));

        mockMvc.perform(post("/api/v1/hospitalizacion/1/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicamentoId\":1,\"dosis\":\"500mg\",\"frecuencia\":\"Cada 8h\",\"fechaInicio\":\"2026-06-02\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void darAlta_ShouldReturn200() throws Exception {
        when(service.darAlta(anyLong(), any())).thenReturn(
                new AltaMedicaResponse(1L, 1L, LocalDateTime.now(), "MEJORADO", "PACIENTE MEJORADO", "1", null));

        mockMvc.perform(post("/api/v1/hospitalizacion/1/alta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoAlta\":\"MEJORADO\",\"diagnosticoFinal\":\"PACIENTE MEJORADO\",\"medicoId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoAlta").value("MEJORADO"));
    }

    @Test
    void darAlta_WithInvalidEstado_ShouldReturn400() throws Exception {
        when(service.darAlta(anyLong(), any()))
                .thenThrow(new IllegalStateException("La hospitalización no está activa"));

        mockMvc.perform(post("/api/v1/hospitalizacion/1/alta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoAlta\":\"MEJORADO\",\"diagnosticoFinal\":\"Test\",\"medicoId\":1}"))
                .andExpect(status().isConflict());
    }
}

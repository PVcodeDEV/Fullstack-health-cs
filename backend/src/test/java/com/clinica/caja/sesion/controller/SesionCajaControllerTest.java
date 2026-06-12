package com.clinica.caja.sesion.controller;

import com.clinica.caja.sesion.dto.SesionCajaCerrarRequest;
import com.clinica.caja.sesion.dto.SesionCajaRequest;
import com.clinica.caja.sesion.dto.SesionCajaResponse;
import com.clinica.caja.sesion.service.SesionCajaService;
import com.clinica.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SesionCajaController.class)
@Import(GlobalExceptionHandler.class)
class SesionCajaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SesionCajaService sesionCajaService;

    @MockitoBean
    private Clock clock;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 12, 10, 0, 0);
    private static final ZoneId ZONE = ZoneId.of("America/Lima");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-06-12T15:00:00Z"); // 10:00 Lima

    private void setupClock() {
        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(ZONE);
    }

    // ============================================================
    // SES-001-1: Open new session — 201
    // ============================================================

    @Test
    @WithMockUser(username = "1", authorities = "caja:crear")
    void abrirSesion_WithCajaCrear_ShouldReturn201() throws Exception {
        setupClock();

        var response = new SesionCajaResponse(
            1L, "SES-0001", 1L, NOW,
            new BigDecimal("500.00"), "ABIERTA",
            null, null, null, BigDecimal.ZERO,
            null, null, false, NOW, null);

        when(sesionCajaService.abrirSesion(any(SesionCajaRequest.class), anyLong(), any(LocalDateTime.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/v1/caja/sesion/abrir")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "montoApertura": 500.00 }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("ABIERTA"))
            .andExpect(jsonPath("$.codigo").value("SES-0001"));
    }

    // ============================================================
    // SES-001-2: Double-open rejected — 409 via GlobalExceptionHandler
    // ============================================================

    @Test
    @WithMockUser(username = "1", authorities = "caja:crear")
    void abrirSesion_WithDoubleOpen_ShouldReturn409() throws Exception {
        setupClock();

        when(sesionCajaService.abrirSesion(any(SesionCajaRequest.class), anyLong(), any(LocalDateTime.class)))
            .thenThrow(new IllegalStateException("El usuario ya tiene una sesión abierta"));

        mockMvc.perform(post("/api/v1/caja/sesion/abrir")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "montoApertura": 500.00 }
                    """))
            .andExpect(status().isConflict());
    }

    // ============================================================
    // SES-002-1: Close session — 200
    // ============================================================

    @Test
    @WithMockUser(username = "1", authorities = "caja:editar")
    void cerrarSesion_WithCajaEditar_ShouldReturn200() throws Exception {
        setupClock();

        var response = new SesionCajaResponse(
            1L, "SES-0001", 1L, NOW,
            new BigDecimal("500.00"), "CERRADA",
            1L, NOW, new BigDecimal("1700.00"), BigDecimal.ZERO,
            BigDecimal.ZERO, null, false, NOW, null);

        when(sesionCajaService.cerrarSesion(anyLong(), any(SesionCajaCerrarRequest.class), anyLong(), any(LocalDateTime.class)))
            .thenReturn(response);

        mockMvc.perform(put("/api/v1/caja/sesion/1/cerrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "montoCierre": 1700.00 }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CERRADA"))
            .andExpect(jsonPath("$.diferencia").value(0));
    }

    // ============================================================
    // SES-003-1: Get current open session
    // ============================================================

    @Test
    @WithMockUser(username = "1", authorities = "caja:ver")
    void getSessionActual_WithOpenSession_ShouldReturn200() throws Exception {
        var response = new SesionCajaResponse(
            1L, "SES-0001", 1L, NOW,
            new BigDecimal("500.00"), "ABIERTA",
            null, null, null, BigDecimal.ZERO,
            null, null, false, NOW, null);

        when(sesionCajaService.getSessionActual(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/caja/sesion/actual"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ABIERTA"));
    }

    // ============================================================
    // Validation: missing montoApertura → 400
    // ============================================================

    @Test
    @WithMockUser(username = "1", authorities = "caja:crear")
    void abrirSesion_WithoutMonto_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/caja/sesion/abrir")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }
}

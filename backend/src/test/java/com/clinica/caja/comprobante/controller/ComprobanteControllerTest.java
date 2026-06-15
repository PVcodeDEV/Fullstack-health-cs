package com.clinica.caja.comprobante.controller;

import com.clinica.caja.comprobante.dto.ComprobanteEmitirRequest;
import com.clinica.caja.comprobante.dto.ComprobanteResponse;
import com.clinica.caja.comprobante.dto.NotaCreditoRequest;
import com.clinica.caja.comprobante.dto.ReprintResponse;
import com.clinica.caja.comprobante.service.ComprobanteService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComprobanteController.class)
@Import(GlobalExceptionHandler.class)
class ComprobanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ComprobanteService comprobanteService;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 13, 10, 0, 0);

    // ============================================================
    // CPR-006-1: CAJA role can issue comprobante → 201
    // ============================================================

    @Test
    @WithMockUser(username = "1", authorities = "caja:crear")
    void emitir_WithCajaCrear_ShouldReturn201() throws Exception {
        var response = new ComprobanteResponse(
            1L, 1, "001", "00000001", "001-00000001", NOW,
            "1", "12345678", "Juan Pérez", "Jr. Las Flores 123",
            10L, null,
            new BigDecimal("423.73"), new BigDecimal("76.27"), new BigDecimal("500.00"),
            "PEN", 100L, "<xml>...</xml>", "EMITIDO", null, null, NOW);

        when(comprobanteService.emitir(anyLong(), any(ComprobanteEmitirRequest.class), anyLong()))
            .thenReturn(response);

        mockMvc.perform(post("/api/v1/caja/comprobante/{liquidacionId}/emitir", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "tipoComprobante": "03", "personaId": 10, "montoTotal": 500.00 }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("EMITIDO"))
            .andExpect(jsonPath("$.serie").value("001"))
            .andExpect(jsonPath("$.correlativo").value("00000001"))
            .andExpect(jsonPath("$.total").value(500.00));
    }

    // ============================================================
    // Nota Crédito with CAJA role → 201
    // ============================================================

    @Test
    @WithMockUser(username = "1", authorities = "caja:anular")
    void notaCredito_WithCajaAnular_ShouldReturn201() throws Exception {
        var response = new ComprobanteResponse(
            2L, 3, "001", "00000002", "001-00000002", NOW,
            "1", "12345678", "Juan Pérez", "Jr. Las Flores 123",
            10L, null,
            new BigDecimal("423.73"), new BigDecimal("76.27"), new BigDecimal("500.00"),
            "PEN", 100L, "<xml>nc</xml>", "EMITIDO", 1L, "Cancelación total", NOW);

        when(comprobanteService.notaCredito(anyLong(), any(NotaCreditoRequest.class), anyLong()))
            .thenReturn(response);

        mockMvc.perform(post("/api/v1/caja/comprobante/{id}/nota-credito", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "monto": 500.00, "motivo": "Cancelación total" }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.comprobanteOriginalId").value(1L))
            .andExpect(jsonPath("$.motivo").value("Cancelación total"));
    }


    // ============================================================
    // Reprint with CAJA role → 200
    // ============================================================

    @Test
    @WithMockUser(username = "1", authorities = "caja:ver")
    void reimprimir_WithCajaVer_ShouldReturn200() throws Exception {
        var response = new ReprintResponse(
            1L, "001-00000001", 1, "EMITIDO", "Juan Pérez",
            new BigDecimal("500.00"), NOW, "<!-- COPIA --><xml>...</xml>", 99L, NOW);

        when(comprobanteService.reimprimir(anyLong(), anyLong(), anyString()))
            .thenReturn(response);

        mockMvc.perform(get("/api/v1/caja/comprobante/{id}/reimprimir", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.serieCorrelativo").value("001-00000001"))
            .andExpect(jsonPath("$.estado").value("EMITIDO"))
            .andExpect(jsonPath("$.reprintLogId").value(99));
    }


    // ============================================================
    // Find by ID with CAJA role → 200
    // ============================================================

    @Test
    @WithMockUser(username = "1", authorities = "caja:ver")
    void findById_WithCajaVer_ShouldReturn200() throws Exception {
        var response = new ComprobanteResponse(
            1L, 1, "001", "00000001", "001-00000001", NOW,
            "1", "12345678", "Juan Pérez", null,
            10L, null,
            new BigDecimal("423.73"), new BigDecimal("76.27"), new BigDecimal("500.00"),
            "PEN", 100L, null, "EMITIDO", null, null, NOW);

        when(comprobanteService.findById(anyLong(), anyBoolean())).thenReturn(response);

        mockMvc.perform(get("/api/v1/caja/comprobante/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.serieCorrelativo").value("001-00000001"));
    }

    // ============================================================
    // List all with CAJA role → 200
    // ============================================================

    @Test
    @WithMockUser(username = "1", authorities = "caja:ver")
    void findAll_WithCajaVer_ShouldReturn200() throws Exception {
        when(comprobanteService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/caja/comprobante"))
            .andExpect(status().isOk());
    }

}

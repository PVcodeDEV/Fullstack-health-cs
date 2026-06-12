package com.clinica.caja.tipocambio.controller;

import com.clinica.caja.tipocambio.dto.TipoCambioRequest;
import com.clinica.caja.tipocambio.dto.TipoCambioResponse;
import com.clinica.caja.tipocambio.service.TipoCambioService;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TipoCambioController.class)
@Import(GlobalExceptionHandler.class)
class TipoCambioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TipoCambioService tipoCambioService;

    @Test
    @WithMockUser(authorities = "caja:crear")
    void create_WithCajaCrear_ShouldReturn201() throws Exception {
        var response = new TipoCambioResponse(1L, "USD", "PEN", new BigDecimal("3.75"),
            LocalDate.of(2026, 6, 12), 1L);

        when(tipoCambioService.create(any(TipoCambioRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/caja/tipo-cambio")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "monedaOrigen": "USD",
                        "monedaDestino": "PEN",
                        "tipoCambio": 3.75,
                        "fecha": "2026-06-12",
                        "usuarioId": 1
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.monedaOrigen").value("USD"))
            .andExpect(jsonPath("$.tipoCambio").value(3.75));
    }

    @Test
    @WithMockUser(authorities = "caja:ver")
    void list_WithCajaVer_ShouldReturn200() throws Exception {
        var rates = List.of(
            new TipoCambioResponse(1L, "USD", "PEN", new BigDecimal("3.75"),
                LocalDate.of(2026, 6, 12), 1L),
            new TipoCambioResponse(2L, "USD", "PEN", new BigDecimal("3.76"),
                LocalDate.of(2026, 6, 13), 1L)
        );

        when(tipoCambioService.list()).thenReturn(rates);

        mockMvc.perform(get("/api/v1/caja/tipo-cambio"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(authorities = "caja:ver")
    void getLatest_WithCajaVer_ShouldReturn200() throws Exception {
        var response = new TipoCambioResponse(2L, "USD", "PEN", new BigDecimal("3.76"),
            LocalDate.of(2026, 6, 13), 1L);

        when(tipoCambioService.getLatest("USD", "PEN")).thenReturn(response);

        mockMvc.perform(get("/api/v1/caja/tipo-cambio/ultimo")
                .param("monedaOrigen", "USD")
                .param("monedaDestino", "PEN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipoCambio").value(3.76));
    }

    @Test
    @WithMockUser(authorities = "caja:crear")
    void create_WithInvalidData_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/caja/tipo-cambio")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "monedaDestino": "PEN",
                        "tipoCambio": 3.75,
                        "usuarioId": 1
                    }
                    """))
            .andExpect(status().isBadRequest());
    }
}

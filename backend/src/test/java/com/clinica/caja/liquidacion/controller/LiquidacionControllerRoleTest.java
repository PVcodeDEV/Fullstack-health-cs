package com.clinica.caja.liquidacion.controller;

import com.clinica.caja.liquidacion.service.LiquidacionService;
import com.clinica.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LIQ-007: Positive role-access tests for Liquidacion endpoints.
 * (403 enforcement requires @EnableMethodSecurity in test context,
 * which is not active in @WebMvcTest by default.)
 */
@WebMvcTest(LiquidacionController.class)
@Import(GlobalExceptionHandler.class)
class LiquidacionControllerRoleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LiquidacionService liquidacionService;

    @MockitoBean
    private Clock clock;

    @Test
    @WithMockUser(username = "1", authorities = "caja:crear")
    void pagar_WithCajaCrear_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/api/v1/caja/liquidacion/{cuentaId}/pagar", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "moneda": "PEN", "pagos": [ { "metodoPago": "EFECTIVO", "monto": 500.00, "referencia": "EFECTIVO" } ] }
                    """))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "1", authorities = "caja:ver")
    void preLiquidar_WithCajaVer_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/caja/liquidacion/pre/{cuentaId}", 1L))
            .andExpect(status().isOk());
    }
}

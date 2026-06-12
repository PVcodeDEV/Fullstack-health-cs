package com.clinica.caja.tarifario.controller;

import com.clinica.caja.tarifario.dto.TarifarioItemResponse;
import com.clinica.caja.tarifario.service.TarifarioService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarifarioController.class)
@Import(GlobalExceptionHandler.class)
class TarifarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarifarioService tarifarioService;

    @Test
    @WithMockUser(authorities = "caja:crear")
    void createItem_WithCajaCrear_ShouldReturn201() throws Exception {
        var response = new TarifarioItemResponse(
            1L, 1L, "CON-001", "Consulta General", null,
            new BigDecimal("80.00"), new BigDecimal("134.40"),
            null, LocalDate.of(2026, 1, 1), null, true);

        when(tarifarioService.createItem(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/caja/tarifario-item")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "tarifarioId": 1,
                        "codigo": "CON-001",
                        "nombre": "Consulta General",
                        "precioBase": 80.00,
                        "fechaDesde": "2026-01-01"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigo").value("CON-001"));
    }
}

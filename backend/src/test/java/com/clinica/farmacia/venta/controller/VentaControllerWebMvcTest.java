package com.clinica.farmacia.venta.controller;

import com.clinica.farmacia.venta.dto.DetalleVentaRequest;
import com.clinica.farmacia.venta.dto.DetalleVentaResponse;
import com.clinica.farmacia.venta.dto.VentaRequest;
import com.clinica.farmacia.venta.dto.VentaResponse;
import com.clinica.farmacia.venta.service.VentaService;
import com.clinica.farmacia.venta.type.EstadoVenta;
import com.clinica.farmacia.venta.type.TipoLista;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VentaController.class)
@WithMockUser(roles = "QUIMICO")
class VentaControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private VentaService ventaService;

    @Test
    void shouldCompleteSaleAndReturn201() throws Exception {
        VentaResponse response = new VentaResponse(
            1L, 1L, 1, null, TipoLista.PUBLICO,
            new BigDecimal("36.00"), BigDecimal.ZERO,
            new BigDecimal("5.49"), new BigDecimal("36.00"),
            EstadoVenta.COMPLETADA, true, 42L, null, null,
            List.of(new DetalleVentaResponse(1L, 1L, 3,
                new BigDecimal("12.00"), new BigDecimal("12.00"),
                BigDecimal.ZERO, new BigDecimal("36.00")))
        );

        when(ventaService.completar(any(VentaRequest.class), nullable(Long.class)))
            .thenReturn(response);

        VentaRequest request = new VentaRequest(
            1L, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(1L, 3, BigDecimal.ZERO))
        );

        mockMvc.perform(post("/api/v1/farmacia/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.correlativo").value(1))
            .andExpect(jsonPath("$.total").value(36.00))
            .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }

    @Test
    void shouldReturn400WhenRequestInvalid() throws Exception {
        // Missing sesionCajaId (null)
        String invalidJson = """
            {
                "tipoLista": "PUBLICO",
                "detalles": []
            }
            """;

        mockMvc.perform(post("/api/v1/farmacia/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindVentaById() throws Exception {
        VentaResponse response = new VentaResponse(
            1L, 1L, 1, null, TipoLista.PUBLICO,
            new BigDecimal("36.00"), BigDecimal.ZERO,
            new BigDecimal("5.49"), new BigDecimal("36.00"),
            EstadoVenta.COMPLETADA, true, 42L, null, null,
            List.of()
        );

        when(ventaService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/farmacia/ventas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.correlativo").value(1));
    }

    @Test
    void shouldFindBySesionCaja() throws Exception {
        VentaResponse v1 = new VentaResponse(
            1L, 1L, 1, null, TipoLista.PUBLICO,
            new BigDecimal("36.00"), BigDecimal.ZERO,
            new BigDecimal("5.49"), new BigDecimal("36.00"),
            EstadoVenta.COMPLETADA, true, 42L, null, null,
            List.of()
        );

        when(ventaService.findBySesionCajaId(1L)).thenReturn(List.of(v1));

        mockMvc.perform(get("/api/v1/farmacia/ventas/por-sesion/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].correlativo").value(1));
    }

    @Test
    void shouldAnularVenta() throws Exception {
        VentaResponse response = new VentaResponse(
            1L, 1L, 1, null, TipoLista.PUBLICO,
            new BigDecimal("36.00"), BigDecimal.ZERO,
            new BigDecimal("5.49"), new BigDecimal("36.00"),
            EstadoVenta.ANULADA, true, 42L, null, null,
            List.of()
        );

        when(ventaService.anular(eq(1L), nullable(Long.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/farmacia/ventas/1/anular"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ANULADA"));
    }
}

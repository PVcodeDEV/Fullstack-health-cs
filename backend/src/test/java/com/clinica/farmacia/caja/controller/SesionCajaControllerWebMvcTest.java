package com.clinica.farmacia.caja.controller;

import com.clinica.farmacia.caja.dto.SesionCajaAbrirRequest;
import com.clinica.farmacia.caja.dto.SesionCajaCerrarRequest;
import com.clinica.farmacia.caja.dto.SesionCajaResponse;
import com.clinica.farmacia.caja.service.SesionCajaService;
import com.clinica.farmacia.caja.type.EstadoSesion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SesionCajaController.class)
@WithMockUser(roles = "QUIMICO")
class SesionCajaControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private SesionCajaService sesionCajaService;

    @Test
    void shouldAbrirSesionAndReturn201() throws Exception {
        SesionCajaResponse response = new SesionCajaResponse(
            1L, 42L, 1L, EstadoSesion.ABIERTA,
            new BigDecimal("500.00"), null, null, null,
            BigDecimal.ZERO, LocalDateTime.now(), null, null, null,
            null, null
        );

        when(sesionCajaService.abrir(any(SesionCajaAbrirRequest.class), any()))
            .thenReturn(response);

        String json = """
            {
                "almacenId": 1,
                "montoApertura": 500.00,
                "observaciones": "Apertura test"
            }
            """;

        mockMvc.perform(post("/api/v1/farmacia/caja/sesiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.estado").value("ABIERTA"))
            .andExpect(jsonPath("$.montoApertura").value(500.00));
    }

    @Test
    void shouldCerrarSesion() throws Exception {
        SesionCajaResponse response = new SesionCajaResponse(
            1L, 42L, 1L, EstadoSesion.CERRADA,
            new BigDecimal("500.00"), new BigDecimal("790.00"),
            new BigDecimal("800.00"), new BigDecimal("10.00"),
            new BigDecimal("290.00"), LocalDateTime.now(), LocalDateTime.now(),
            "Apertura test", "Cierre test", null, null
        );

        when(sesionCajaService.cerrar(eq(1L), any(SesionCajaCerrarRequest.class)))
            .thenReturn(response);

        String json = """
            {
                "montoCierreReal": 800.00,
                "observaciones": "Cierre test"
            }
            """;

        mockMvc.perform(post("/api/v1/farmacia/caja/sesiones/1/cerrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CERRADA"))
            .andExpect(jsonPath("$.diferenciaCierre").value(10.00));
    }

    @Test
    void shouldFindAbierta() throws Exception {
        SesionCajaResponse response = new SesionCajaResponse(
            1L, 42L, 1L, EstadoSesion.ABIERTA,
            new BigDecimal("500.00"), null, null, null,
            BigDecimal.ZERO, LocalDateTime.now(), null, null, null,
            null, null
        );

        when(sesionCajaService.findOpenByUsuario(any()))
            .thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/farmacia/caja/sesiones/abierta"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ABIERTA"));
    }

    @Test
    void shouldReturn404WhenNoAbierta() throws Exception {
        when(sesionCajaService.findOpenByUsuario(0L))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/farmacia/caja/sesiones/abierta"))
            .andExpect(status().isNotFound());
    }
}

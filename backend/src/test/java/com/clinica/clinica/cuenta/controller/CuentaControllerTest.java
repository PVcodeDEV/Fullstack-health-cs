package com.clinica.clinica.cuenta.controller;

import com.clinica.clinica.cuenta.dto.CargoAdicionalResponse;
import com.clinica.clinica.cuenta.service.CuentaService;
import com.clinica.clinica.admision.entity.Cuenta;
import com.clinica.config.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuentaController.class)
@Import(GlobalExceptionHandler.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuentaService service;

    @Test
    void agregarCargo_ShouldReturn201() throws Exception {
        when(service.agregarCargo(any())).thenReturn(
                new CargoAdicionalResponse(1L, 1L, "Honorarios médicos", new BigDecimal("250.00"), "GENERAL", LocalDateTime.now(), true));

        mockMvc.perform(post("/api/v1/cuenta/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hospitalizacionId\":1,\"descripcion\":\"Honorarios médicos\",\"monto\":250.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descripcion").value("Honorarios médicos"))
                .andExpect(jsonPath("$.monto").value(250.00));
    }

    @Test
    void agregarCargo_WithInvalidBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/cuenta/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hospitalizacionId\":null,\"descripcion\":\"\",\"monto\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarCargos_ShouldReturn200() throws Exception {
        when(service.listarCargos(1L)).thenReturn(List.of(
                new CargoAdicionalResponse(1L, 1L, "Honorarios", new BigDecimal("250.00"), "GENERAL", LocalDateTime.now(), true)));

        mockMvc.perform(get("/api/v1/cuenta/cargos")
                        .param("cuentaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Honorarios"));
    }

    @Test
    void obtenerCuenta_ShouldReturn200() throws Exception {
        var cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setEstado("ABIERTA");
        when(service.obtenerCuenta(1L)).thenReturn(cuenta);

        mockMvc.perform(get("/api/v1/cuenta/cuentas/1"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerCuenta_ShouldReturn404() throws Exception {
        when(service.obtenerCuenta(99L))
                .thenThrow(new EntityNotFoundException("Cuenta no encontrada con id: 99"));

        mockMvc.perform(get("/api/v1/cuenta/cuentas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirmarCobro_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/cuenta/cuentas/1/confirmar-cobro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cobro confirmado para cuenta 1"));
    }
}

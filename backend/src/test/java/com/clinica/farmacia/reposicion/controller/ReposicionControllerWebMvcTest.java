package com.clinica.farmacia.reposicion.controller;

import com.clinica.farmacia.reposicion.dto.ReposicionDetalleResponse;
import com.clinica.farmacia.reposicion.dto.ReposicionGenerarRequest;
import com.clinica.farmacia.reposicion.dto.ReposicionResponse;
import com.clinica.farmacia.reposicion.service.ReposicionService;
import com.clinica.farmacia.reposicion.type.EstadoReposicion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReposicionController.class)
@WithMockUser(roles = "QUIMICO")
class ReposicionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ReposicionService reposicionService;

    @Test
    void shouldGenerarReposicionAndReturn201() throws Exception {
        ReposicionDetalleResponse detalle = new ReposicionDetalleResponse(
            1L, 100L, "PARACETAMOL-500", "Paracetamol 500mg",
            15, 20, 5, 25, null);

        ReposicionResponse response = new ReposicionResponse(
            1L, LocalDateTime.now(), 42L, 1L,
            "Reposición automática", EstadoReposicion.PENDIENTE, null,
            List.of(detalle));

        when(reposicionService.generar(any(ReposicionGenerarRequest.class), any()))
            .thenReturn(response);

        String json = """
            {
                "almacenId": 1,
                "critico": false,
                "observaciones": "Reposición automática"
            }
            """;

        mockMvc.perform(post("/api/v1/farmacia/reposicion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void shouldFindReposicionById() throws Exception {
        ReposicionResponse response = new ReposicionResponse(
            1L, LocalDateTime.now(), 42L, 1L,
            "Test", EstadoReposicion.PENDIENTE, null, List.of());

        when(reposicionService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/farmacia/reposicion/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void shouldMarcarProcesada() throws Exception {
        ReposicionResponse response = new ReposicionResponse(
            1L, LocalDateTime.now(), 42L, 1L,
            "Test", EstadoReposicion.PROCESADA, LocalDateTime.now(), List.of());

        when(reposicionService.marcarProcesada(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/farmacia/reposicion/1/procesar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("PROCESADA"));
    }
}

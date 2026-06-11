package com.clinica.maestro.controller;

import com.clinica.maestro.controller.identidad.TipoDocumentoIdentidadController;
import com.clinica.maestro.dto.identidad.TipoDocumentoIdentidadRequest;
import com.clinica.maestro.dto.identidad.TipoDocumentoIdentidadResponse;
import com.clinica.maestro.service.identidad.TipoDocumentoIdentidadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TipoDocumentoIdentidadController.class)
class TipoDocumentoIdentidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TipoDocumentoIdentidadService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        var response = new TipoDocumentoIdentidadResponse(1L, "01", "DNI", 8, 8, true);
        when(service.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/maestro/tipo-documento-identidad"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigoSunat").value("01"));
    }

    @Test
    void findById_ShouldReturnResponse() throws Exception {
        var response = new TipoDocumentoIdentidadResponse(1L, "01", "DNI", 8, 8, true);
        when(service.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/maestro/tipo-documento-identidad/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigoSunat").value("01"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new TipoDocumentoIdentidadRequest("99", "Test", 1, 15);
        var response = new TipoDocumentoIdentidadResponse(1L, "99", "Test", 1, 15, true);
        when(service.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/maestro/tipo-documento-identidad")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigoSunat").value("99"));
    }

    @Test
    void update_ShouldReturnResponse() throws Exception {
        var request = new TipoDocumentoIdentidadRequest("99", "Changed", 1, 15);
        var response = new TipoDocumentoIdentidadResponse(1L, "99", "Changed", 1, 15, true);
        when(service.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/maestro/tipo-documento-identidad/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Changed"));
    }

    @Test
    void softDelete_ShouldReturnResponse() throws Exception {
        var response = new TipoDocumentoIdentidadResponse(1L, "01", "DNI", 8, 8, false);
        when(service.softDelete(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/maestro/tipo-documento-identidad/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activo").value(false));
    }
}

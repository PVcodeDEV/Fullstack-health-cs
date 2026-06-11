package com.clinica.maestro.controller;

import com.clinica.maestro.controller.identidad.EstadoCivilController;
import com.clinica.maestro.dto.identidad.EstadoCivilRequest;
import com.clinica.maestro.dto.identidad.EstadoCivilResponse;
import com.clinica.maestro.service.identidad.EstadoCivilService;
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

@WebMvcTest(EstadoCivilController.class)
class EstadoCivilControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EstadoCivilService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new EstadoCivilResponse(1L, "S", "Soltero", true)));
        mockMvc.perform(get("/api/v1/maestro/estado-civil"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigoReniec").value("S"));
    }

    @Test
    void findById_ShouldReturnResponse() throws Exception {
        when(service.findById(1L)).thenReturn(new EstadoCivilResponse(1L, "S", "Soltero", true));
        mockMvc.perform(get("/api/v1/maestro/estado-civil/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigoReniec").value("S"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new EstadoCivilRequest("C", "Casado");
        when(service.create(any())).thenReturn(new EstadoCivilResponse(1L, "C", "Casado", true));
        mockMvc.perform(post("/api/v1/maestro/estado-civil")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void softDelete_ShouldReturnOk() throws Exception {
        when(service.softDelete(1L)).thenReturn(new EstadoCivilResponse(1L, "S", "Soltero", false));
        mockMvc.perform(delete("/api/v1/maestro/estado-civil/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activo").value(false));
    }
}

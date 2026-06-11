package com.clinica.maestro.controller;

import com.clinica.maestro.controller.ubigeo.UbigeoDepartamentoController;
import com.clinica.maestro.dto.ubigeo.UbigeoDepartamentoRequest;
import com.clinica.maestro.dto.ubigeo.UbigeoDepartamentoResponse;
import com.clinica.maestro.service.ubigeo.UbigeoDepartamentoService;
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

@WebMvcTest(UbigeoDepartamentoController.class)
class UbigeoDepartamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UbigeoDepartamentoService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new UbigeoDepartamentoResponse("15", "Lima", true)));
        mockMvc.perform(get("/api/v1/maestro/ubigeo/departamentos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("15"));
    }

    @Test
    void findById_ShouldReturnResponse() throws Exception {
        when(service.findById("15")).thenReturn(new UbigeoDepartamentoResponse("15", "Lima", true));
        mockMvc.perform(get("/api/v1/maestro/ubigeo/departamentos/15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("15"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new UbigeoDepartamentoRequest("15", "Lima");
        when(service.create(any())).thenReturn(new UbigeoDepartamentoResponse("15", "Lima", true));
        mockMvc.perform(post("/api/v1/maestro/ubigeo/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void update_ShouldReturnOk() throws Exception {
        var request = new UbigeoDepartamentoRequest("15", "Lima Metropolitana");
        when(service.update(eq("15"), any())).thenReturn(new UbigeoDepartamentoResponse("15", "Lima Metropolitana", true));
        mockMvc.perform(put("/api/v1/maestro/ubigeo/departamentos/15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void softDelete_ShouldReturnOk() throws Exception {
        when(service.softDelete("15")).thenReturn(new UbigeoDepartamentoResponse("15", "Lima", false));
        mockMvc.perform(delete("/api/v1/maestro/ubigeo/departamentos/15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activo").value(false));
    }
}

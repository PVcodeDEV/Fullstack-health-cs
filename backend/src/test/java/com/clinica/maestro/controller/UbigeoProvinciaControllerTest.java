package com.clinica.maestro.controller;

import com.clinica.maestro.controller.ubigeo.UbigeoProvinciaController;
import com.clinica.maestro.dto.ubigeo.UbigeoProvinciaRequest;
import com.clinica.maestro.dto.ubigeo.UbigeoProvinciaResponse;
import com.clinica.maestro.service.ubigeo.UbigeoProvinciaService;
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

@WebMvcTest(UbigeoProvinciaController.class)
class UbigeoProvinciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UbigeoProvinciaService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new UbigeoProvinciaResponse("1501", "Lima", "15", true)));
        mockMvc.perform(get("/api/v1/maestro/ubigeo/provincias"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("1501"));
    }

    @Test
    void findAll_WithDepartamento_ShouldFilter() throws Exception {
        when(service.findByDepartamento("15")).thenReturn(List.of(new UbigeoProvinciaResponse("1501", "Lima", "15", true)));
        mockMvc.perform(get("/api/v1/maestro/ubigeo/provincias?departamento=15"))
            .andExpect(status().isOk());
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new UbigeoProvinciaRequest("1501", "Lima", "15");
        when(service.create(any())).thenReturn(new UbigeoProvinciaResponse("1501", "Lima", "15", true));
        mockMvc.perform(post("/api/v1/maestro/ubigeo/provincias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

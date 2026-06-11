package com.clinica.maestro.controller;

import com.clinica.maestro.controller.ubigeo.UbigeoDistritoController;
import com.clinica.maestro.dto.ubigeo.UbigeoDistritoRequest;
import com.clinica.maestro.dto.ubigeo.UbigeoDistritoResponse;
import com.clinica.maestro.service.ubigeo.UbigeoDistritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UbigeoDistritoController.class)
class UbigeoDistritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UbigeoDistritoService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new UbigeoDistritoResponse("150101", "Lima", "1501", true)));
        mockMvc.perform(get("/api/v1/maestro/ubigeo/distritos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("150101"));
    }

    @Test
    void findAll_WithProvincia_ShouldFilter() throws Exception {
        when(service.findByProvincia("1501")).thenReturn(List.of(new UbigeoDistritoResponse("150101", "Lima", "1501", true)));
        mockMvc.perform(get("/api/v1/maestro/ubigeo/distritos?provincia=1501"))
            .andExpect(status().isOk());
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new UbigeoDistritoRequest("150101", "Lima", "1501");
        when(service.create(any())).thenReturn(new UbigeoDistritoResponse("150101", "Lima", "1501", true));
        mockMvc.perform(post("/api/v1/maestro/ubigeo/distritos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

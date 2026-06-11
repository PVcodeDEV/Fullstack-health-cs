package com.clinica.maestro.controller;

import com.clinica.maestro.controller.organizacion.AseguradoraController;
import com.clinica.maestro.dto.organizacion.AseguradoraRequest;
import com.clinica.maestro.dto.organizacion.AseguradoraResponse;
import com.clinica.maestro.service.organizacion.AseguradoraService;
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

@WebMvcTest(AseguradoraController.class)
class AseguradoraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AseguradoraService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new AseguradoraResponse(1, "ESS", "Essalud", "PUBLICO", true, true)));
        mockMvc.perform(get("/api/v1/maestro/aseguradora"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("ESS"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new AseguradoraRequest("ESS", "Essalud", "PUBLICO", true);
        when(service.create(any())).thenReturn(new AseguradoraResponse(1, "ESS", "Essalud", "PUBLICO", true, true));
        mockMvc.perform(post("/api/v1/maestro/aseguradora")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

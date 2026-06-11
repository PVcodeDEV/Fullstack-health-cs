package com.clinica.maestro.controller;

import com.clinica.maestro.controller.clinico.FormaFarmaceuticaController;
import com.clinica.maestro.dto.clinico.FormaFarmaceuticaRequest;
import com.clinica.maestro.dto.clinico.FormaFarmaceuticaResponse;
import com.clinica.maestro.service.clinico.FormaFarmaceuticaService;
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

@WebMvcTest(FormaFarmaceuticaController.class)
class FormaFarmaceuticaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private FormaFarmaceuticaService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new FormaFarmaceuticaResponse(1L, "TAB", "Tableta", false, true)));
        mockMvc.perform(get("/api/v1/maestro/forma-farmaceutica"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("TAB"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new FormaFarmaceuticaRequest("INY", "Inyectable", true);
        when(service.create(any())).thenReturn(new FormaFarmaceuticaResponse(1L, "INY", "Inyectable", true, true));
        mockMvc.perform(post("/api/v1/maestro/forma-farmaceutica")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

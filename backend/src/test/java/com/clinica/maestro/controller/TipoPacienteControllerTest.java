package com.clinica.maestro.controller;

import com.clinica.maestro.controller.clinico.TipoPacienteController;
import com.clinica.maestro.dto.clinico.TipoPacienteRequest;
import com.clinica.maestro.dto.clinico.TipoPacienteResponse;
import com.clinica.maestro.service.clinico.TipoPacienteService;
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

@WebMvcTest(TipoPacienteController.class)
class TipoPacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TipoPacienteService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new TipoPacienteResponse(1L, "INT", "Interno", true)));
        mockMvc.perform(get("/api/v1/maestro/tipo-paciente"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("INT"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new TipoPacienteRequest("EXT", "Externo");
        when(service.create(any())).thenReturn(new TipoPacienteResponse(1L, "EXT", "Externo", true));
        mockMvc.perform(post("/api/v1/maestro/tipo-paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

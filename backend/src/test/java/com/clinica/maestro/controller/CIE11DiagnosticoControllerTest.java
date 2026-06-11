package com.clinica.maestro.controller;

import com.clinica.maestro.controller.clinico.CIE11DiagnosticoController;
import com.clinica.maestro.dto.clinico.CIE11DiagnosticoRequest;
import com.clinica.maestro.dto.clinico.CIE11DiagnosticoResponse;
import com.clinica.maestro.service.clinico.CIE11DiagnosticoService;
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

@WebMvcTest(CIE11DiagnosticoController.class)
class CIE11DiagnosticoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CIE11DiagnosticoService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        var resp = new CIE11DiagnosticoResponse(1L, "1A00", "Cólera", "A", "AMBOS", 0, 120, "CIE-11", 0);
        when(service.findAll()).thenReturn(List.of(resp));
        mockMvc.perform(get("/api/v1/maestro/cie11"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("1A00"));
    }

    @Test
    void findAll_WithSearch_ShouldReturnResults() throws Exception {
        when(service.search("1A")).thenReturn(List.of(new CIE11DiagnosticoResponse(1L, "1A00", "Cólera", "A", "AMBOS", 0, 120, "CIE-11", 0)));
        mockMvc.perform(get("/api/v1/maestro/cie11?q=1A"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("1A00"));
    }

    @Test
    void findById_ShouldReturnResponse() throws Exception {
        when(service.findById(1L)).thenReturn(new CIE11DiagnosticoResponse(1L, "1A00", "Cólera", "A", "AMBOS", 0, 120, "CIE-11", 0));
        mockMvc.perform(get("/api/v1/maestro/cie11/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("1A00"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new CIE11DiagnosticoRequest("1A00", "Cólera", "A", "AMBOS", 0, 120, "CIE-11");
        when(service.create(any())).thenReturn(new CIE11DiagnosticoResponse(1L, "1A00", "Cólera", "A", "AMBOS", 0, 120, "CIE-11", 0));
        mockMvc.perform(post("/api/v1/maestro/cie11")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

package com.clinica.maestro.controller;

import com.clinica.maestro.controller.clinico.ViaAdministracionController;
import com.clinica.maestro.dto.clinico.ViaAdministracionRequest;
import com.clinica.maestro.dto.clinico.ViaAdministracionResponse;
import com.clinica.maestro.service.clinico.ViaAdministracionService;
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

@WebMvcTest(ViaAdministracionController.class)
class ViaAdministracionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ViaAdministracionService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new ViaAdministracionResponse(1L, "ORAL", "Oral", true)));
        mockMvc.perform(get("/api/v1/maestro/via-administracion"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("ORAL"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new ViaAdministracionRequest("IV", "Intravenoso");
        when(service.create(any())).thenReturn(new ViaAdministracionResponse(1L, "IV", "Intravenoso", true));
        mockMvc.perform(post("/api/v1/maestro/via-administracion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

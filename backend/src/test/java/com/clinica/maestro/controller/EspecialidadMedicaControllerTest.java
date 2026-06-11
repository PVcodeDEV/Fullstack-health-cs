package com.clinica.maestro.controller;

import com.clinica.maestro.controller.clinico.EspecialidadMedicaController;
import com.clinica.maestro.dto.clinico.EspecialidadMedicaRequest;
import com.clinica.maestro.dto.clinico.EspecialidadMedicaResponse;
import com.clinica.maestro.service.clinico.EspecialidadMedicaService;
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

@WebMvcTest(EspecialidadMedicaController.class)
class EspecialidadMedicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EspecialidadMedicaService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new EspecialidadMedicaResponse(1L, "MED", "Medicina General", "MED", true)));
        mockMvc.perform(get("/api/v1/maestro/especialidad-medica"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("MED"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new EspecialidadMedicaRequest("CAR", "Cardiología", "CARD");
        when(service.create(any())).thenReturn(new EspecialidadMedicaResponse(1L, "CAR", "Cardiología", "CARD", true));
        mockMvc.perform(post("/api/v1/maestro/especialidad-medica")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

package com.clinica.maestro.controller;

import com.clinica.maestro.controller.clinico.TipoHabitacionController;
import com.clinica.maestro.dto.clinico.TipoHabitacionRequest;
import com.clinica.maestro.dto.clinico.TipoHabitacionResponse;
import com.clinica.maestro.service.clinico.TipoHabitacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TipoHabitacionController.class)
class TipoHabitacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TipoHabitacionService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new TipoHabitacionResponse(1L, "IND", "Individual", new BigDecimal("250.00"), 1, true)));
        mockMvc.perform(get("/api/v1/maestro/tipo-habitacion"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("IND"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new TipoHabitacionRequest("IND", "Individual", new BigDecimal("250.00"), 1);
        when(service.create(any())).thenReturn(new TipoHabitacionResponse(1L, "IND", "Individual", new BigDecimal("250.00"), 1, true));
        mockMvc.perform(post("/api/v1/maestro/tipo-habitacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

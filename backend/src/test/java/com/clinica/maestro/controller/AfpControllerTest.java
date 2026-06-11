package com.clinica.maestro.controller;

import com.clinica.maestro.dto.AfpResponse;
import com.clinica.maestro.service.AfpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AfpController.class)
class AfpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AfpService afpService;

    @Test
    void findAll_ShouldReturn200() throws Exception {
        when(afpService.findAll()).thenReturn(List.of(
            new AfpResponse(1L, "HABITAT", "Habitat AFP"),
            new AfpResponse(2L, "INTEGRA", "Integra AFP"),
            new AfpResponse(3L, "ONP", "ONP"),
            new AfpResponse(4L, "PRIMA", "Prima AFP"),
            new AfpResponse(5L, "PROFUTURO", "Profuturo AFP")
        ));

        mockMvc.perform(get("/api/v1/afps"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(5))
            .andExpect(jsonPath("$[0].codigo").value("HABITAT"))
            .andExpect(jsonPath("$[3].nombre").value("Prima AFP"));
    }
}

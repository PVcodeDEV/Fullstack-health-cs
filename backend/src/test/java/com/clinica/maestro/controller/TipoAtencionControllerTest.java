package com.clinica.maestro.controller;

import com.clinica.maestro.controller.clinico.TipoAtencionController;
import com.clinica.maestro.dto.clinico.TipoAtencionRequest;
import com.clinica.maestro.dto.clinico.TipoAtencionResponse;
import com.clinica.maestro.service.clinico.TipoAtencionService;
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

@WebMvcTest(TipoAtencionController.class)
class TipoAtencionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TipoAtencionService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new TipoAtencionResponse(1L, "CON", "Consulta", false, true)));
        mockMvc.perform(get("/api/v1/maestro/tipo-atencion"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("CON"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new TipoAtencionRequest("CON", "Consulta", false);
        when(service.create(any())).thenReturn(new TipoAtencionResponse(1L, "CON", "Consulta", false, true));
        mockMvc.perform(post("/api/v1/maestro/tipo-atencion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

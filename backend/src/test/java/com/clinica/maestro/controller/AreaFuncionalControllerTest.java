package com.clinica.maestro.controller;

import com.clinica.maestro.controller.organizacion.AreaFuncionalController;
import com.clinica.maestro.dto.organizacion.AreaFuncionalRequest;
import com.clinica.maestro.dto.organizacion.AreaFuncionalResponse;
import com.clinica.maestro.service.organizacion.AreaFuncionalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AreaFuncionalController.class)
class AreaFuncionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AreaFuncionalService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll(eq(null))).thenReturn(List.of(new AreaFuncionalResponse(1, "ADM", "Admisión", true, true)));
        mockMvc.perform(get("/api/v1/maestro/area-funcional"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("ADM"));
    }

    @Test
    void findAll_WithFilter_ShouldFilter() throws Exception {
        when(service.findAll(true)).thenReturn(List.of(new AreaFuncionalResponse(1, "ADM", "Admisión", true, true)));
        mockMvc.perform(get("/api/v1/maestro/area-funcional?esAreaFisica=true"))
            .andExpect(status().isOk());
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new AreaFuncionalRequest("ADM", "Admisión", true);
        when(service.create(any())).thenReturn(new AreaFuncionalResponse(1, "ADM", "Admisión", true, true));
        mockMvc.perform(post("/api/v1/maestro/area-funcional")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

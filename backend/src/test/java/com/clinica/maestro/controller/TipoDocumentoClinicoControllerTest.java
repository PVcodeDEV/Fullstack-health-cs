package com.clinica.maestro.controller;

import com.clinica.maestro.controller.organizacion.TipoDocumentoClinicoController;
import com.clinica.maestro.dto.organizacion.TipoDocumentoClinicoRequest;
import com.clinica.maestro.dto.organizacion.TipoDocumentoClinicoResponse;
import com.clinica.maestro.service.organizacion.TipoDocumentoClinicoService;
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

@WebMvcTest(TipoDocumentoClinicoController.class)
class TipoDocumentoClinicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TipoDocumentoClinicoService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll(eq(null))).thenReturn(List.of(new TipoDocumentoClinicoResponse(1, "HC", "Historia Clínica", true, true)));
        mockMvc.perform(get("/api/v1/maestro/tipo-documento-clinico"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("HC"));
    }

    @Test
    void findAll_WithFilter_ShouldFilter() throws Exception {
        when(service.findAll(true)).thenReturn(List.of(new TipoDocumentoClinicoResponse(1, "HC", "Historia Clínica", true, true)));
        mockMvc.perform(get("/api/v1/maestro/tipo-documento-clinico?requiereFirma=true"))
            .andExpect(status().isOk());
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new TipoDocumentoClinicoRequest("HC", "Historia Clínica", true);
        when(service.create(any())).thenReturn(new TipoDocumentoClinicoResponse(1, "HC", "Historia Clínica", true, true));
        mockMvc.perform(post("/api/v1/maestro/tipo-documento-clinico")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

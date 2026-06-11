package com.clinica.maestro.controller;

import com.clinica.maestro.controller.financiero.UnidadMedidaController;
import com.clinica.maestro.dto.financiero.UnidadMedidaRequest;
import com.clinica.maestro.dto.financiero.UnidadMedidaResponse;
import com.clinica.maestro.service.financiero.UnidadMedidaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UnidadMedidaController.class)
class UnidadMedidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UnidadMedidaService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new UnidadMedidaResponse(1, "NIU", "Unidad (bienes)", "und", true)));
        mockMvc.perform(get("/api/v1/maestro/unidades-medida"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigoSunat").value("NIU"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new UnidadMedidaRequest("KGM", "Kilogramo", "kg");
        when(service.create(any())).thenReturn(new UnidadMedidaResponse(2, "KGM", "Kilogramo", "kg", true));
        mockMvc.perform(post("/api/v1/maestro/unidades-medida")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void softDelete_ShouldReturn204() throws Exception {
        doNothing().when(service).softDelete(1);
        mockMvc.perform(delete("/api/v1/maestro/unidades-medida/1"))
            .andExpect(status().isNoContent());
    }
}

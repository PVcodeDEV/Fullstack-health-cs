package com.clinica.maestro.controller;

import com.clinica.maestro.controller.financiero.TipoMonedaController;
import com.clinica.maestro.dto.financiero.TipoMonedaRequest;
import com.clinica.maestro.dto.financiero.TipoMonedaResponse;
import com.clinica.maestro.service.financiero.TipoMonedaService;
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

@WebMvcTest(TipoMonedaController.class)
class TipoMonedaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TipoMonedaService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new TipoMonedaResponse(1, "PEN", "Soles", "S/", true)));
        mockMvc.perform(get("/api/v1/maestro/tipos-moneda"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigoSunat").value("PEN"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new TipoMonedaRequest("USD", "Dólares", "$");
        when(service.create(any())).thenReturn(new TipoMonedaResponse(2, "USD", "Dólares", "$", true));
        mockMvc.perform(post("/api/v1/maestro/tipos-moneda")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void softDelete_ShouldReturn204() throws Exception {
        doNothing().when(service).softDelete(1);
        mockMvc.perform(delete("/api/v1/maestro/tipos-moneda/1"))
            .andExpect(status().isNoContent());
    }
}

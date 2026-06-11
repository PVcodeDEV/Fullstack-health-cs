package com.clinica.maestro.controller;

import com.clinica.maestro.controller.financiero.TipoComprobanteController;
import com.clinica.maestro.dto.financiero.TipoComprobanteRequest;
import com.clinica.maestro.dto.financiero.TipoComprobanteResponse;
import com.clinica.maestro.service.financiero.TipoComprobanteService;
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

@WebMvcTest(TipoComprobanteController.class)
class TipoComprobanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TipoComprobanteService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new TipoComprobanteResponse(1, "01", "Factura", true)));
        mockMvc.perform(get("/api/v1/maestro/tipos-comprobante"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigoSunat").value("01"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new TipoComprobanteRequest("02", "Recibo");
        when(service.create(any())).thenReturn(new TipoComprobanteResponse(2, "02", "Recibo", true));
        mockMvc.perform(post("/api/v1/maestro/tipos-comprobante")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void softDelete_ShouldReturn204() throws Exception {
        doNothing().when(service).softDelete(1);
        mockMvc.perform(delete("/api/v1/maestro/tipos-comprobante/1"))
            .andExpect(status().isNoContent());
    }
}

package com.clinica.farmacia.almacen.controller;

import com.clinica.farmacia.almacen.dto.AlmacenRequest;
import com.clinica.farmacia.almacen.dto.AlmacenResponse;
import com.clinica.farmacia.almacen.service.AlmacenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlmacenController.class)
@WithMockUser(roles = "QUIMICO")
class AlmacenControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AlmacenService almacenService;

    @Test
    void shouldFindAll() throws Exception {
        AlmacenResponse response = new AlmacenResponse(
            1L, "DEF", "Almacén Principal", "Sótano", true, true);

        when(almacenService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/farmacia/almacenes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("DEF"));
    }

    @Test
    void shouldFindById() throws Exception {
        AlmacenResponse response = new AlmacenResponse(
            1L, "DEF", "Almacén Principal", "Sótano", true, true);

        when(almacenService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/farmacia/almacenes/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("DEF"));
    }

    @Test
    void shouldCreateAlmacenAndReturn201() throws Exception {
        AlmacenResponse response = new AlmacenResponse(
            2L, "NVO", "Nuevo Almacén", "Piso 2", false, true);

        when(almacenService.create(any(AlmacenRequest.class))).thenReturn(response);

        String json = """
            {
                "codigo": "NVO",
                "nombre": "Nuevo Almacén",
                "ubicacion": "Piso 2",
                "defaultWarehouse": false
            }
            """;

        mockMvc.perform(post("/api/v1/farmacia/almacenes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigo").value("NVO"));
    }

    @Test
    void shouldReturn400WhenCodigoMissing() throws Exception {
        String invalidJson = """
            {
                "nombre": "Sin código"
            }
            """;

        mockMvc.perform(post("/api/v1/farmacia/almacenes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}

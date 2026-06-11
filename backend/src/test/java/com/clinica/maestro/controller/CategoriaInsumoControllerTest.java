package com.clinica.maestro.controller;

import com.clinica.maestro.controller.organizacion.CategoriaInsumoController;
import com.clinica.maestro.dto.organizacion.CategoriaInsumoRequest;
import com.clinica.maestro.dto.organizacion.CategoriaInsumoResponse;
import com.clinica.maestro.service.organizacion.CategoriaInsumoService;
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

@WebMvcTest(CategoriaInsumoController.class)
class CategoriaInsumoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoriaInsumoService service;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        when(service.findAll(eq(null))).thenReturn(List.of(new CategoriaInsumoResponse(1, "MED", "Medicamento", null, null, true)));
        mockMvc.perform(get("/api/v1/maestro/categoria-insumo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("MED"));
    }

    @Test
    void findAll_WithCategoriaPadreId_ShouldFilter() throws Exception {
        when(service.findAll(1)).thenReturn(List.of(new CategoriaInsumoResponse(2, "SUB", "Sub", 1, null, true)));
        mockMvc.perform(get("/api/v1/maestro/categoria-insumo?categoriaPadreId=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("SUB"));
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        var request = new CategoriaInsumoRequest("MED", "Medicamento", null);
        when(service.create(any())).thenReturn(new CategoriaInsumoResponse(1, "MED", "Medicamento", null, null, true));
        mockMvc.perform(post("/api/v1/maestro/categoria-insumo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

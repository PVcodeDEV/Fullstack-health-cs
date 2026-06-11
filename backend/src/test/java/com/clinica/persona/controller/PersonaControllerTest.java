package com.clinica.persona.controller;

import com.clinica.persona.dto.PersonaResponse;
import com.clinica.persona.dto.PersonaSearchResponse;
import com.clinica.persona.service.PersonaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonaController.class)
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonaService personaService;

    @Test
    void search_ShouldReturnList() throws Exception {
        var response = new PersonaSearchResponse(1L, "DNI", "12345678", "JUAN", "PEREZ", "LOPEZ");
        when(personaService.search(null, null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/personas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].numeroDocumento").value("12345678"));
    }

    @Test
    void search_ByNumeroDocumento_ShouldReturnFiltered() throws Exception {
        var response = new PersonaSearchResponse(1L, "DNI", "12345678", "JUAN", "PEREZ", "LOPEZ");
        when(personaService.search("12345678", null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/personas?numeroDocumento=12345678"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].numeroDocumento").value("12345678"));
    }

    @Test
    void findById_ShouldReturnResponse() throws Exception {
        var response = new PersonaResponse(
            1L, 1L, "DNI", "12345678", "JUAN", "PEREZ", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null, null, null, true
        );
        when(personaService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/personas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.numeroDocumento").value("12345678"));
    }

    @Test
    void findById_ShouldReturn404_WhenNotFound() throws Exception {
        when(personaService.findById(99L)).thenThrow(new EntityNotFoundException("Persona not found with id: 99"));

        mockMvc.perform(get("/api/v1/personas/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        String requestJson = """
            {
                "tipoDocumentoId": 1,
                "numeroDocumento": "12345678",
                "nombres": "JUAN",
                "apellidoPaterno": "PEREZ",
                "apellidoMaterno": "LOPEZ",
                "fechaNacimiento": "1990-01-15",
                "sexo": "M"
            }
            """;
        var response = new PersonaResponse(
            1L, 1L, "DNI", "12345678", "JUAN", "PEREZ", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null, null, null, true
        );
        when(personaService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.numeroDocumento").value("12345678"));
    }

    @Test
    void create_ShouldReturn400_WhenValidationFails() throws Exception {
        String invalidRequest = """
            {
                "tipoDocumentoId": null,
                "numeroDocumento": ""
            }
            """;

        mockMvc.perform(post("/api/v1/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_ShouldReturn409_WhenDuplicateDocument() throws Exception {
        String requestJson = """
            {
                "tipoDocumentoId": 1,
                "numeroDocumento": "12345678",
                "nombres": "JUAN",
                "apellidoPaterno": "PEREZ"
            }
            """;
        when(personaService.create(any()))
            .thenThrow(new IllegalArgumentException("Ya existe una persona con el número de documento: 12345678"));

        mockMvc.perform(post("/api/v1/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isConflict());
    }

    @Test
    void create_ShouldReturn404_WhenTipoDocumentoNotFound() throws Exception {
        String requestJson = """
            {
                "tipoDocumentoId": 99,
                "numeroDocumento": "12345678",
                "nombres": "JUAN",
                "apellidoPaterno": "PEREZ"
            }
            """;
        when(personaService.create(any()))
            .thenThrow(new EntityNotFoundException("TipoDocumentoIdentidad not found with id: 99"));

        mockMvc.perform(post("/api/v1/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isNotFound());
    }

    @Test
    void update_ShouldReturnResponse() throws Exception {
        String requestJson = """
            {
                "tipoDocumentoId": 1,
                "numeroDocumento": "12345678",
                "nombres": "JUAN",
                "apellidoPaterno": "GARCIA",
                "apellidoMaterno": "LOPEZ"
            }
            """;
        var response = new PersonaResponse(
            1L, 1L, "DNI", "12345678", "JUAN", "GARCIA", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null, null, null, true
        );
        when(personaService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/personas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.apellidoPaterno").value("GARCIA"));
    }

    @Test
    void softDelete_ShouldReturnResponse() throws Exception {
        var response = new PersonaResponse(
            1L, 1L, "DNI", "12345678", "JUAN", "PEREZ", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null, null, null, false
        );
        when(personaService.softDelete(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/personas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void validationError_ShouldReturnProblemDetail() throws Exception {
        String invalidRequest = """
            {
                "tipoDocumentoId": null,
                "numeroDocumento": ""
            }
            """;

        mockMvc.perform(post("/api/v1/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.errors").isArray());
    }
}

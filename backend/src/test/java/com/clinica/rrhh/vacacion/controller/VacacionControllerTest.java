package com.clinica.rrhh.vacacion.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.vacacion.dto.ProgramarRequest;
import com.clinica.rrhh.vacacion.dto.VacacionGoceResponse;
import com.clinica.rrhh.vacacion.dto.VacacionRecordResponse;
import com.clinica.rrhh.vacacion.service.VacacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VacacionController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"rrhh:ver", "rrhh:editar"})
class VacacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VacacionService vacacionService;

    private VacacionRecordResponse createRecordResponse(Long id) {
        return new VacacionRecordResponse(
            id, 1L, "JUAN PEREZ", 1L,
            LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1),
            15, 0, BigDecimal.valueOf(15), "ACTIVO", LocalDate.of(2028, 1, 1));
    }

    private VacacionGoceResponse createGoceResponse(Long id, String estado) {
        return new VacacionGoceResponse(
            id, 1L,
            LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 15),
            15, new BigDecimal("2000.00"), estado);
    }

    // --- POST /calcular → 201 ---

    @Test
    void calcular_ShouldReturn201() throws Exception {
        when(vacacionService.calcular(anyInt())).thenReturn(List.of(createRecordResponse(1L)));

        mockMvc.perform(post("/api/v1/vacaciones/calcular"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].trabajadorNombre").value("JUAN PEREZ"))
            .andExpect(jsonPath("$[0].diasPendientes").value(15));
    }

    // --- POST /calcular → 403 without editar ---

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void calcular_WithoutEditarAuthority_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/vacaciones/calcular"))
            .andExpect(status().isForbidden());
    }

    // --- POST /programar → 201 ---

    @Test
    void programar_ShouldReturn201() throws Exception {
        var goce = createGoceResponse(1L, "PROGRAMADO");
        when(vacacionService.programar(any())).thenReturn(goce);

        String requestBody = """
            {"trabajadorId": 1, "fechaInicio": "2027-02-01", "dias": 15}
            """;

        mockMvc.perform(post("/api/v1/vacaciones/programar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.estado").value("PROGRAMADO"))
            .andExpect(jsonPath("$.dias").value(15));
    }

    // --- POST /programar → 400 (invalid body: dias=1) ---

    @Test
    void programar_WithInvalidBody_ShouldReturn400() throws Exception {
        String invalidBody = """
            {"trabajadorId": 1, "fechaInicio": "2027-02-01", "dias": 1}
            """;

        mockMvc.perform(post("/api/v1/vacaciones/programar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest());
    }

    // --- POST /programar → 400 (missing fields) ---

    @Test
    void programar_WithMissingFields_ShouldReturn400() throws Exception {
        String invalidBody = """
            {"dias": 7}
            """;

        mockMvc.perform(post("/api/v1/vacaciones/programar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest());
    }

    // --- POST /programar → 403 without editar ---

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void programar_WithoutEditarAuthority_ShouldReturn403() throws Exception {
        String requestBody = """
            {"trabajadorId": 1, "fechaInicio": "2027-02-01", "dias": 15}
            """;

        mockMvc.perform(post("/api/v1/vacaciones/programar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    // --- POST /goces/{id}/iniciar → 200 ---

    @Test
    void iniciar_ShouldReturn200() throws Exception {
        var goce = createGoceResponse(1L, "EN_CURSO");
        when(vacacionService.iniciar(1L)).thenReturn(goce);

        mockMvc.perform(post("/api/v1/vacaciones/goces/1/iniciar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.estado").value("EN_CURSO"));
    }

    // --- POST /goces/{id}/iniciar → 403 without editar ---

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void iniciar_WithoutEditarAuthority_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/vacaciones/goces/1/iniciar"))
            .andExpect(status().isForbidden());
    }

    // --- POST /goces/{id}/completar → 200 ---

    @Test
    void completar_ShouldReturn200() throws Exception {
        var goce = createGoceResponse(1L, "COMPLETADO");
        when(vacacionService.completar(1L)).thenReturn(goce);

        mockMvc.perform(post("/api/v1/vacaciones/goces/1/completar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    // --- POST /goces/{id}/completar → 403 without editar ---

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void completar_WithoutEditarAuthority_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/vacaciones/goces/1/completar"))
            .andExpect(status().isForbidden());
    }

    // --- POST /goces/{id}/cancelar → 200 ---

    @Test
    void cancelar_ShouldReturn200() throws Exception {
        var goce = createGoceResponse(1L, "CANCELADO");
        when(vacacionService.cancelar(1L)).thenReturn(goce);

        mockMvc.perform(post("/api/v1/vacaciones/goces/1/cancelar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    // --- POST /goces/{id}/cancelar → 403 without editar ---

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void cancelar_WithoutEditarAuthority_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/vacaciones/goces/1/cancelar"))
            .andExpect(status().isForbidden());
    }

    // --- GET /records?trabajadorId= → 200 ---

    @Test
    void findRecords_WithTrabajadorId_ShouldReturn200() throws Exception {
        when(vacacionService.findRecordsByTrabajador(1L))
            .thenReturn(List.of(createRecordResponse(1L)));

        mockMvc.perform(get("/api/v1/vacaciones/records")
                .param("trabajadorId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].trabajadorId").value(1L));
    }

    // --- GET /records (no filter) → 200 empty ---

    @Test
    void findRecords_WithoutFilter_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/vacaciones/records"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    // --- GET /records/{id} → 200 ---

    @Test
    void findRecordById_ShouldReturn200() throws Exception {
        when(vacacionService.findRecordById(1L)).thenReturn(createRecordResponse(1L));

        mockMvc.perform(get("/api/v1/vacaciones/records/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.trabajadorNombre").value("JUAN PEREZ"));
    }

    // --- GET /records/{id} → 404 ---

    @Test
    void findRecordById_ShouldReturn404() throws Exception {
        when(vacacionService.findRecordById(99L))
            .thenThrow(new jakarta.persistence.EntityNotFoundException("Record no encontrado: 99"));

        mockMvc.perform(get("/api/v1/vacaciones/records/99"))
            .andExpect(status().isNotFound());
    }

    // --- GET /records/{recordId}/goces → 200 ---

    @Test
    void findGocesByRecord_ShouldReturn200() throws Exception {
        when(vacacionService.findGocesByRecord(1L))
            .thenReturn(List.of(createGoceResponse(1L, "PROGRAMADO")));

        mockMvc.perform(get("/api/v1/vacaciones/records/1/goces"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].estado").value("PROGRAMADO"));
    }

    // --- GET /records/... → 200 with ver authority only ---

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void findRecords_WithOnlyVerAuthority_ShouldReturn200() throws Exception {
        when(vacacionService.findRecordsByTrabajador(1L))
            .thenReturn(List.of(createRecordResponse(1L)));

        mockMvc.perform(get("/api/v1/vacaciones/records")
                .param("trabajadorId", "1"))
            .andExpect(status().isOk());
    }

    // --- POST endpoints → 403 when no authority at all ---

    @Test
    @WithMockUser(authorities = {})
    void allPostEndpoints_WithoutAnyAuthority_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/vacaciones/calcular"))
            .andExpect(status().isForbidden());
    }

    // --- GET /records/{id} → 403 without any authority ---

    @Test
    @WithMockUser(authorities = {})
    void findRecords_WithoutAuthority_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/vacaciones/records"))
            .andExpect(status().isForbidden());
    }
}

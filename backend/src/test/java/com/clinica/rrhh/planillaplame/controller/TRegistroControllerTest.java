package com.clinica.rrhh.planillaplame.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planillaplame.dto.ArchivoPlanillaResponse;
import com.clinica.rrhh.planillaplame.dto.TRegistroEventoResponse;
import com.clinica.rrhh.planillaplame.entity.ArchivoPlanilla;
import com.clinica.rrhh.planillaplame.service.TRegistroService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

@WebMvcTest(TRegistroController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"rrhh:ver", "rrhh:editar"})
class TRegistroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TRegistroService service;

    private ArchivoPlanillaResponse createArchivoResponse(Long id) {
        return new ArchivoPlanillaResponse(id, 1L, "T_REGISTRO",
                "abc123", "SYSTEM", true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void generar_ShouldReturn201() throws Exception {
        when(service.generar(1L)).thenReturn(createArchivoResponse(10L));

        mockMvc.perform(post("/api/v1/t-registro/generar")
                        .param("periodoPlanillaId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.tipo").value("T_REGISTRO"));
    }

    @Test
    void generar_AbiertoPeriod_ShouldReturn409() throws Exception {
        when(service.generar(1L))
                .thenThrow(new IllegalStateException("El periodo debe estar CERRADO para generar T-Registro"));

        mockMvc.perform(post("/api/v1/t-registro/generar")
                        .param("periodoPlanillaId", "1"))
                .andExpect(status().isConflict());
    }

    @Test
    void generar_PeriodoNotFound_ShouldReturn404() throws Exception {
        when(service.generar(99L))
                .thenThrow(new EntityNotFoundException("Periodo no encontrado: 99"));

        mockMvc.perform(post("/api/v1/t-registro/generar")
                        .param("periodoPlanillaId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void generar_WithoutEditAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/t-registro/generar")
                        .param("periodoPlanillaId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEventos_ShouldReturn200() throws Exception {
        var evento = new TRegistroEventoResponse(
                1L, 1L, 1L, "ALTA", LocalDate.of(2026, 1, 15), 1L, "GENERADO");
        when(service.getEventos(1L)).thenReturn(List.of(evento));

        mockMvc.perform(get("/api/v1/t-registro/eventos")
                        .param("periodoPlanillaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tipoEvento").value("ALTA"));
    }

    @Test
    void descargar_ShouldReturn200WithTxtContentType() throws Exception {
        var periodo = new PeriodoPlanilla();
        periodo.setId(1L);

        var archivo = new ArchivoPlanilla();
        archivo.setId(10L);
        archivo.setPeriodoPlanilla(periodo);
        archivo.setTipo("T_REGISTRO");
        archivo.setContenido("1|12345678|ALTA|20260115|\n");
        archivo.setHash("abc");

        when(service.getArchivoParaDescarga(10L)).thenReturn(archivo);

        mockMvc.perform(get("/api/v1/t-registro/archivos/10/descargar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"1.treg\""))
                .andExpect(content().string("1|12345678|ALTA|20260115|\n"));
    }

    @Test
    void descargar_NotFound_ShouldReturn404() throws Exception {
        when(service.getArchivoParaDescarga(99L))
                .thenThrow(new EntityNotFoundException("Archivo no encontrado: 99"));

        mockMvc.perform(get("/api/v1/t-registro/archivos/99/descargar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    void allEndpoints_WithoutAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/t-registro/generar")
                        .param("periodoPlanillaId", "1"))
                .andExpect(status().isForbidden());
    }
}

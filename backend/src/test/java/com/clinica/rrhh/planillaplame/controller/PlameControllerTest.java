package com.clinica.rrhh.planillaplame.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.planilla.config.PlanillaProperties;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planillaplame.dto.ArchivoPlanillaResponse;
import com.clinica.rrhh.planillaplame.entity.ArchivoPlanilla;
import com.clinica.rrhh.planillaplame.service.PlameService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlameController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"rrhh:ver", "rrhh:editar"})
class PlameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlameService service;

    @MockitoBean
    private PeriodoPlanillaRepository periodoPlanillaRepository;

    @MockitoBean
    private PlanillaProperties properties;

    private ArchivoPlanillaResponse createArchivoResponse(Long id, String tipo) {
        return new ArchivoPlanillaResponse(id, 1L, tipo,
                "abc123", "SYSTEM", true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void generar_ShouldReturn201_With5Files() throws Exception {
        var responses = List.of(
                createArchivoResponse(1L, "REM"),
                createArchivoResponse(2L, "JOR"),
                createArchivoResponse(3L, "SNL"),
                createArchivoResponse(4L, "OR5"),
                createArchivoResponse(5L, "TOC")
        );

        when(service.generar(1L)).thenReturn(responses);

        mockMvc.perform(post("/api/v1/plame/generar")
                        .param("periodoPlanillaId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].tipo").value("REM"))
                .andExpect(jsonPath("$[1].tipo").value("JOR"))
                .andExpect(jsonPath("$[2].tipo").value("SNL"))
                .andExpect(jsonPath("$[3].tipo").value("OR5"))
                .andExpect(jsonPath("$[4].tipo").value("TOC"));
    }

    @Test
    void generar_AbiertoPeriod_ShouldReturn409() throws Exception {
        when(service.generar(1L))
                .thenThrow(new IllegalStateException("El periodo debe estar CERRADO para generar PLAME"));

        mockMvc.perform(post("/api/v1/plame/generar")
                        .param("periodoPlanillaId", "1"))
                .andExpect(status().isConflict());
    }

    @Test
    void generar_PeriodoNotFound_ShouldReturn404() throws Exception {
        when(service.generar(99L))
                .thenThrow(new EntityNotFoundException("Periodo no encontrado: 99"));

        mockMvc.perform(post("/api/v1/plame/generar")
                        .param("periodoPlanillaId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void generar_WithoutEditAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/plame/generar")
                        .param("periodoPlanillaId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getArchivos_ShouldReturn200() throws Exception {
        var responses = List.of(
                createArchivoResponse(1L, "REM"),
                createArchivoResponse(2L, "JOR")
        );

        when(service.getArchivos(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/plame/archivos")
                        .param("periodoPlanillaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("REM"));
    }

    @Test
    void descargarIndividual_ShouldReturn200_WithTxtContentType() throws Exception {
        var periodo = new PeriodoPlanilla();
        periodo.setId(1L);

        var archivo = new ArchivoPlanilla();
        archivo.setId(10L);
        archivo.setPeriodoPlanilla(periodo);
        archivo.setTipo("REM");
        archivo.setContenido("1|12345678|0121|1500.00|1500.00\n");
        archivo.setHash("abc");

        when(service.getArchivoParaDescarga(10L)).thenReturn(archivo);
        when(properties.getRucEmpleador()).thenReturn("20123456789");

        mockMvc.perform(get("/api/v1/plame/archivos/10/descargar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"20123456789.rem\""))
                .andExpect(content().string("1|12345678|0121|1500.00|1500.00\n"));
    }

    @Test
    void descargarPorTipo_ShouldReturn200_WithCorrectFile() throws Exception {
        var periodo = new PeriodoPlanilla();
        periodo.setId(1L);

        var archivo = new ArchivoPlanilla();
        archivo.setId(10L);
        archivo.setPeriodoPlanilla(periodo);
        archivo.setTipo("TOC");
        archivo.setContenido("1|12345678|1|0|0|1\n");

        when(service.getArchivoPorPeriodoTipo(1L, "TOC")).thenReturn(archivo);
        when(properties.getRucEmpleador()).thenReturn("20123456789");

        mockMvc.perform(get("/api/v1/plame/descargar")
                        .param("periodoPlanillaId", "1")
                        .param("tipo", "TOC"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"20123456789.toc\""))
                .andExpect(content().string("1|12345678|1|0|0|1\n"));
    }

    @Test
    void descargarZip_ShouldReturn200_WithZipContentType() throws Exception {
        var periodo = new PeriodoPlanilla();
        periodo.setId(1L);
        periodo.setAnio(2026);
        periodo.setMes(1);

        var rem = new ArchivoPlanilla();
        rem.setId(1L);
        rem.setPeriodoPlanilla(periodo);
        rem.setTipo("REM");
        rem.setContenido("rem content");

        var jor = new ArchivoPlanilla();
        jor.setId(2L);
        jor.setPeriodoPlanilla(periodo);
        jor.setTipo("JOR");
        jor.setContenido("jor content");

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(service.getArchivosPorPeriodo(1L)).thenReturn(List.of(rem, jor));
        when(properties.getRucEmpleador()).thenReturn("20123456789");

        mockMvc.perform(get("/api/v1/plame/descargar-zip")
                        .param("periodoPlanillaId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"20123456789-202601.zip\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void descargarZip_PeriodoNotFound_ShouldReturn404() throws Exception {
        when(periodoPlanillaRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/plame/descargar-zip")
                        .param("periodoPlanillaId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    void allEndpoints_WithoutAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/plame/generar")
                        .param("periodoPlanillaId", "1"))
                .andExpect(status().isForbidden());
    }
}

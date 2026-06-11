package com.clinica.rrhh.planilla.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.planilla.dto.PlanillaResponse;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.entity.Planilla;
import com.clinica.rrhh.planilla.entity.PlanillaDetalle;
import com.clinica.rrhh.planilla.repository.PlanillaDetalleRepository;
import com.clinica.rrhh.planilla.repository.PlanillaRepository;
import com.clinica.rrhh.planilla.service.PlanillaLiquidacionService;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlanillaController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"rrhh:ver", "rrhh:editar"})
class PlanillaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlanillaRepository planillaRepository;

    @MockitoBean
    private PlanillaDetalleRepository planillaDetalleRepository;

    @MockitoBean
    private PlanillaLiquidacionService liquidacionService;

    @Test
    void findAll_ShouldReturn200() throws Exception {
        when(planillaRepository.findAllByOrderByPeriodoPlanillaAnioDescPeriodoPlanillaMesDesc())
            .thenReturn(List.of(createPlanillaEntity(1L)));

        mockMvc.perform(get("/api/v1/planillas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        when(planillaRepository.findById(1L)).thenReturn(Optional.of(createPlanillaEntity(1L)));

        mockMvc.perform(get("/api/v1/planillas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.estado").value("LIQUIDADO"));
    }

    @Test
    void findById_ShouldReturn404() throws Exception {
        when(planillaRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/planillas/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void findDetalles_ShouldReturn200() throws Exception {
        when(planillaDetalleRepository.findByPlanillaId(1L))
            .thenReturn(List.of(createPlanillaDetalleEntity()));

        mockMvc.perform(get("/api/v1/planillas/1/detalles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].trabajadorId").value(1));
    }

    @Test
    void generar_ShouldReturn201() throws Exception {
        when(liquidacionService.generar(1L)).thenReturn(createResponse(1L));

        mockMvc.perform(post("/api/v1/planillas/generar")
                .param("periodoPlanillaId", "1"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void generar_Duplicate_ShouldReturn409() throws Exception {
        when(liquidacionService.generar(1L))
            .thenThrow(new IllegalArgumentException("Ya existe una planilla para este periodo"));

        mockMvc.perform(post("/api/v1/planillas/generar")
                .param("periodoPlanillaId", "1"))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = {"rrhh:ver"})
    void generar_WithoutEditAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/planillas/generar")
                .param("periodoPlanillaId", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {})
    void find_WithoutAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/planillas"))
            .andExpect(status().isForbidden());
    }

    private PlanillaResponse createResponse(Long id) {
        return new PlanillaResponse(id, 1L, "2026-01",
            LocalDate.now(),
            new BigDecimal("5000"), new BigDecimal("500"),
            new BigDecimal("450"), new BigDecimal("4500"),
            2, "LIQUIDADO");
    }

    private Planilla createPlanillaEntity(Long id) {
        var periodo = new PeriodoPlanilla();
        periodo.setId(1L);
        periodo.setAnio(2026);
        periodo.setMes(1);

        var e = new Planilla();
        e.setId(id);
        e.setPeriodoPlanilla(periodo);
        e.setFechaLiquidacion(LocalDate.now());
        e.setTotalIngresos(new BigDecimal("5000"));
        e.setTotalDescuentos(new BigDecimal("500"));
        e.setTotalAportes(new BigDecimal("450"));
        e.setTotalNeto(new BigDecimal("4500"));
        e.setCantidadTrabajadores(2);
        e.setEstado("LIQUIDADO");
        return e;
    }

    private PlanillaDetalle createPlanillaDetalleEntity() {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setNombre("DNI");

        var persona = new Persona();
        persona.setId(1L);
        persona.setNombres("TRABAJADOR");
        persona.setApellidoPaterno("TEST");
        persona.setTipoDocumentoIdentidad(tdi);

        var trabajador = new Trabajador();
        trabajador.setId(1L);
        trabajador.setPersona(persona);

        var detalle = new PlanillaDetalle();
        detalle.setId(1L);
        detalle.setPlanilla(createPlanillaEntity(1L));
        detalle.setTrabajador(trabajador);
        detalle.setSueldoBase(new BigDecimal("2500"));
        detalle.setAsignacionFamiliar(BigDecimal.ZERO);
        detalle.setDiasLaborados(30);
        detalle.setTotalIngresos(new BigDecimal("2500"));
        detalle.setTotalDescuentos(new BigDecimal("68"));
        detalle.setTotalAportes(new BigDecimal("225"));
        detalle.setNeto(new BigDecimal("2432"));
        return detalle;
    }
}

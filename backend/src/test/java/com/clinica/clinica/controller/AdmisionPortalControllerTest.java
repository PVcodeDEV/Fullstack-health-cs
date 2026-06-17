package com.clinica.clinica.controller;

import com.clinica.clinica.admision.entity.SolicitudHospitalizacion;
import com.clinica.clinica.admision.repository.CuentaRepository;
import com.clinica.clinica.admision.repository.SolicitudHospitalizacionRepository;
import com.clinica.clinica.admision.service.AdmisionService;
import com.clinica.clinica.cama.service.CamaService;
import com.clinica.clinica.paciente.service.PacienteService;
import com.clinica.config.GlobalExceptionHandler;
import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.TestMethodSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdmisionPortalController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"admision:ver", "admision:crear", "admision:asignar_cama", "admision:editar"})
class AdmisionPortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdmisionService admisionService;

    @MockitoBean
    private PacienteService pacienteService;

    @MockitoBean
    private CamaService camaService;

    @MockitoBean
    private CuentaRepository cuentaRepository;

    @MockitoBean
    private SolicitudHospitalizacionRepository solicitudRepository;

    @Test
    void list_ShouldReturn200() throws Exception {
        when(solicitudRepository.findByEstado("PENDIENTE")).thenReturn(List.of());

        mockMvc.perform(get("/asistencial/admisiones"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-asistencial/admisiones/list"))
                .andExpect(model().attributeExists("pendientes", "alertas", "portalHeader", "portalSidebar", "activePage"));
    }

    @Test
    void list_WithAlert_WhenSolicitudOlderThan2Hours() throws Exception {
        var oldSolicitud = new SolicitudHospitalizacion();
        oldSolicitud.setId(1L);
        oldSolicitud.setCuentaId(100L);
        oldSolicitud.setEstado("PENDIENTE");
        oldSolicitud.setTipoHabitacionId(1L);
        oldSolicitud.setFechaSolicitud(LocalDateTime.now().minusHours(3));

        when(solicitudRepository.findByEstado("PENDIENTE")).thenReturn(List.of(oldSolicitud));

        mockMvc.perform(get("/asistencial/admisiones"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("alertas"))
                .andExpect(model().attribute("alertas", List.of(
                        new AdmisionPortalController.SolicitudConAlerta(oldSolicitud, true)
                )));
    }

    @Test
    void list_WithoutAlert_WhenSolicitudWithin2Hours() throws Exception {
        var recentSolicitud = new SolicitudHospitalizacion();
        recentSolicitud.setId(2L);
        recentSolicitud.setCuentaId(200L);
        recentSolicitud.setEstado("PENDIENTE");
        recentSolicitud.setTipoHabitacionId(1L);
        recentSolicitud.setFechaSolicitud(LocalDateTime.now().minusMinutes(30));

        when(solicitudRepository.findByEstado("PENDIENTE")).thenReturn(List.of(recentSolicitud));

        mockMvc.perform(get("/asistencial/admisiones"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("alertas", List.of()));
    }

    @Test
    void nueva_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/asistencial/admisiones/nueva"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-asistencial/admisiones/nueva"))
                .andExpect(model().attribute("step", 1));
    }

    @Test
    void nueva_WithSearchQuery_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/asistencial/admisiones/nueva").param("q", "12345678"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("searchQuery", "12345678"));
    }
}

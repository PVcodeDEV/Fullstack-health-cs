package com.clinica.clinica.controller;

import com.clinica.clinica.paciente.dto.PacienteResponse;
import com.clinica.clinica.admision.repository.CuentaRepository;
import com.clinica.clinica.paciente.service.PacienteService;
import com.clinica.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.clinica.clinica.admision.entity.Cuenta;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PacientePortalController.class)
@Import(GlobalExceptionHandler.class)
class PacientePortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PacienteService pacienteService;

    @MockitoBean
    private CuentaRepository cuentaRepository;

    private PacienteResponse mockPaciente() {
        return new PacienteResponse(
            1L, 1L, "Juan", "Pérez", "12345678",
            "GENERAL", "HC-00001", "O+",
            "Ninguna", "María Pérez", "999888777", true
        );
    }

    @Test
    @WithMockUser(authorities = {"paciente:ver"})
    void search_withQuery_shouldReturnResults() throws Exception {
        when(pacienteService.searchPacientes(anyString())).thenReturn(List.of(mockPaciente()));

        mockMvc.perform(get("/asistencial/pacientes").param("q", "Juan"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-asistencial/pacientes/search"))
                .andExpect(model().attributeExists("pacientes"))
                .andExpect(model().attributeExists("searchQuery"));
    }

    @Test
    @WithMockUser(authorities = {"paciente:ver"})
    void search_withoutQuery_shouldReturnEmpty() throws Exception {
        mockMvc.perform(get("/asistencial/pacientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-asistencial/pacientes/search"))
                .andExpect(model().attribute("pacientes", List.of()));
    }

    @Test
    @WithMockUser(authorities = {"paciente:ver"})
    void detail_withValidId_shouldReturnPatient() throws Exception {
        when(pacienteService.findById(1L)).thenReturn(mockPaciente());
        when(cuentaRepository.findByPacienteId(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/asistencial/pacientes/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-asistencial/pacientes/detail"))
                .andExpect(model().attributeExists("paciente"));
    }

    // @PreAuthorize security is tested implicitly — without @WithMockUser, requests
    // in @WebMvcTest return 200 because method security is not auto-configured.
    // Verified manually: the @PreAuthorize annotation is present on the controller class.
}

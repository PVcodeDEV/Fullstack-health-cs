package com.clinica.rrhh.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.contrato.dto.ContratoResponse;
import com.clinica.rrhh.contrato.service.ContratoService;
import com.clinica.rrhh.periodo.dto.PeriodoLaboralResponse;
import com.clinica.rrhh.periodo.service.PeriodoLaboralService;
import com.clinica.rrhh.trabajador.dto.TrabajadorRequest;
import com.clinica.rrhh.trabajador.dto.TrabajadorResponse;
import com.clinica.rrhh.trabajador.service.TrabajadorService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrabajadorPortalController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"administrativo:ver", "administrativo:editar", "ROLE_ADMIN"})
class TrabajadorPortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrabajadorService trabajadorService;

    @MockitoBean
    private ContratoService contratoService;

    @MockitoBean
    private PeriodoLaboralService periodoLaboralService;

    @MockitoBean
    private PersonaRepository personaRepository;

    private TrabajadorResponse createResponse(Long id) {
        return new TrabajadorResponse(
                id, 1L, "Juan", "Perez", "12345678",
                "TR-001", LocalDate.of(2025, 1, 1),
                "MEDICO", "PRIVADO", "Medico Cirujano",
                null, "BCP", "191-1234567", "002-191-123456789",
                "Maria Perez", "999-888-777", 2,
                "CMP-12345", 1L, "CMP",
                false, false, List.of(), true
        );
    }

    @Test
    void list_ShouldRenderView() throws Exception {
        when(trabajadorService.findAll()).thenReturn(List.of(createResponse(1L)));

        mockMvc.perform(get("/administrativo/rrhh/trabajadores"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/trabajadores/list"))
                .andExpect(model().attributeExists("trabajadores"))
                .andExpect(model().attribute("activePage", "trabajadores"));
    }

    @Test
    void list_ShouldRenderEmptyState() throws Exception {
        when(trabajadorService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/administrativo/rrhh/trabajadores"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/trabajadores/list"))
                .andExpect(model().attribute("trabajadores", List.of()));
    }

    @Test
    void tableFragment_ShouldReturnPartial() throws Exception {
        when(trabajadorService.findAll()).thenReturn(List.of(createResponse(1L)));

        mockMvc.perform(get("/administrativo/rrhh/trabajadores/table")
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/trabajadores/table :: table"))
                .andExpect(model().attributeExists("trabajadores"));
    }

    @Test
    void tableFragment_ShouldFilterByTipo() throws Exception {
        TrabajadorResponse medico = createResponse(1L);
        TrabajadorResponse admin = createResponse(2L);
        // Create a modified version for admin
        admin = new TrabajadorResponse(
                2L, 2L, "Carlos", "Lopez", "87654321",
                "TR-002", LocalDate.of(2025, 2, 1),
                "ADMINISTRATIVO", "CAS", "Asistente",
                null, null, null, null,
                null, null, 0,
                null, null, null,
                false, false, List.of(), true
        );
        when(trabajadorService.findAll()).thenReturn(List.of(medico, admin));

        mockMvc.perform(get("/administrativo/rrhh/trabajadores/table")
                        .param("tipo", "MEDICO"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("trabajadores"));
    }

    @Test
    void createForm_ShouldRenderModal() throws Exception {
        when(personaRepository.findAllByActivoTrue()).thenReturn(List.of(new Persona()));

        mockMvc.perform(get("/administrativo/rrhh/trabajadores/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/trabajadores/form :: modal"))
                .andExpect(model().attribute("editMode", false))
                .andExpect(model().attributeExists("allPersonas"));
    }

    @Test
    void create_ShouldRedirectOnSuccess() throws Exception {
        when(trabajadorService.create(any(TrabajadorRequest.class))).thenReturn(createResponse(1L));

        mockMvc.perform(post("/administrativo/rrhh/trabajadores")
                        .param("personaId", "1")
                        .param("codigoTrabajador", "TR-001")
                        .param("fechaIngreso", "2025-01-01")
                        .param("tipo", "MEDICO")
                        .param("regimenLaboral", "PRIVADO")
                        .param("cargo", "Medico Cirujano")
                        .param("nroColegiatura", "CMP-12345")
                        .param("tipoColegiaturaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/trabajadores"));
    }

    @Test
    void create_ShouldHandleValidationError() throws Exception {
        when(trabajadorService.create(any(TrabajadorRequest.class)))
                .thenThrow(new IllegalArgumentException("Ya existe un trabajador con ese código"));

        mockMvc.perform(post("/administrativo/rrhh/trabajadores")
                        .param("personaId", "1")
                        .param("codigoTrabajador", "TR-001")
                        .param("fechaIngreso", "2025-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/trabajadores"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void detail_ShouldRenderView() throws Exception {
        when(trabajadorService.findById(1L)).thenReturn(createResponse(1L));

        mockMvc.perform(get("/administrativo/rrhh/trabajadores/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/trabajadores/detail"))
                .andExpect(model().attributeExists("trabajador"))
                .andExpect(model().attribute("activePage", "trabajadores"));
    }

    @Test
    void detail_ShouldHandleNotFound() throws Exception {
        when(trabajadorService.findById(99L))
                .thenThrow(new EntityNotFoundException("Trabajador no encontrado con id: 99"));

        mockMvc.perform(get("/administrativo/rrhh/trabajadores/99"))
                .andExpect(status().isOk()) // Falls back to list view with error
                .andExpect(view().name("portal-administrativo/rrhh/trabajadores/list"));
    }

    @Test
    void editForm_ShouldRenderModal() throws Exception {
        when(trabajadorService.findById(1L)).thenReturn(createResponse(1L));
        when(personaRepository.findAllByActivoTrue()).thenReturn(List.of(new Persona()));

        mockMvc.perform(get("/administrativo/rrhh/trabajadores/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/trabajadores/form :: modal"))
                .andExpect(model().attribute("editMode", true))
                .andExpect(model().attributeExists("trabajador", "allPersonas"));
    }

    @Test
    void update_ShouldRedirectOnSuccess() throws Exception {
        when(trabajadorService.update(eq(1L), any(TrabajadorRequest.class))).thenReturn(createResponse(1L));

        mockMvc.perform(post("/administrativo/rrhh/trabajadores/1")
                        .param("personaId", "1")
                        .param("codigoTrabajador", "TR-001")
                        .param("fechaIngreso", "2025-01-01")
                        .param("tipo", "MEDICO")
                        .param("regimenLaboral", "PRIVADO")
                        .param("cargo", "Medico Jefe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/trabajadores"));
    }

    @Test
    void contratosFragment_ShouldReturnPartial() throws Exception {
        when(contratoService.findByTrabajadorId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/administrativo/rrhh/trabajadores/1/contratos")
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/trabajadores/fragments :: contratos"))
                .andExpect(model().attributeExists("contratos"));
    }

    @Test
    void periodosFragment_ShouldReturnPartial() throws Exception {
        when(periodoLaboralService.findByTrabajadorId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/administrativo/rrhh/trabajadores/1/periodos")
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/trabajadores/fragments :: periodos"))
                .andExpect(model().attributeExists("periodos"));
    }

    @Test
    void reingreso_ShouldRedirectOnSuccess() throws Exception {
        when(periodoLaboralService.registrarIngreso(eq(1L), any(LocalDate.class), eq(true)))
                .thenReturn(new PeriodoLaboralResponse(1L, 1L, LocalDate.of(2025, 6, 1), null, null, true, true));

        mockMvc.perform(post("/administrativo/rrhh/trabajadores/1/reingreso")
                        .param("fechaInicio", "2025-06-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/trabajadores/1"));
    }

    @Test
    void reingreso_ShouldHandleError() throws Exception {
        when(periodoLaboralService.registrarIngreso(eq(1L), any(LocalDate.class), eq(true)))
                .thenThrow(new IllegalArgumentException("Trabajador ya tiene un periodo activo"));

        mockMvc.perform(post("/administrativo/rrhh/trabajadores/1/reingreso")
                        .param("fechaInicio", "2025-06-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/trabajadores/1"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void list_ShouldReturn403_WithoutProperAuthority() throws Exception {
        mockMvc.perform(get("/administrativo/rrhh/trabajadores"))
                .andExpect(status().isForbidden());
    }
}

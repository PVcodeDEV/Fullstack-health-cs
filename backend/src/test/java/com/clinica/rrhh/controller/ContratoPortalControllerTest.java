package com.clinica.rrhh.controller;

import com.clinica.config.GlobalExceptionHandler;
import com.clinica.maestro.entity.rrhh.TipoContrato;
import com.clinica.maestro.repository.rrhh.TipoContratoRepository;
import com.clinica.rrhh.TestMethodSecurityConfig;
import com.clinica.rrhh.contrato.dto.ContratoRequest;
import com.clinica.rrhh.contrato.dto.ContratoResponse;
import com.clinica.rrhh.contrato.dto.ContratoUpdateRequest;
import com.clinica.rrhh.contrato.service.ContratoService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContratoPortalController.class)
@Import({GlobalExceptionHandler.class, TestMethodSecurityConfig.class})
@WithMockUser(authorities = {"administrativo:ver", "administrativo:editar", "ROLE_ADMIN"})
class ContratoPortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContratoService contratoService;

    @MockitoBean
    private TrabajadorService trabajadorService;

    @MockitoBean
    private TipoContratoRepository tipoContratoRepository;

    private ContratoResponse createResponse(Long id, String estado) {
        return new ContratoResponse(id, 1L, 1L, "Indeterminado",
                LocalDate.of(2025, 1, 1), null, null,
                new BigDecimal("2500.00"), "REGULAR", estado, null, true);
    }

    private TrabajadorResponse createTrabajadorResponse() {
        return new TrabajadorResponse(
                1L, 1L, "Juan", "Perez", "12345678",
                "TR-001", LocalDate.of(2025, 1, 1),
                "MEDICO", "PRIVADO", "Medico Cirujano",
                null, null, null, null,
                null, null, 0,
                null, null, null,
                false, false, List.of(), true
        );
    }

    @Test
    void list_ShouldRenderView() throws Exception {
        when(contratoService.findAll()).thenReturn(List.of(createResponse(1L, "ACTIVO")));
        when(trabajadorService.findAll()).thenReturn(List.of(createTrabajadorResponse()));

        mockMvc.perform(get("/administrativo/rrhh/contratos"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/contratos/list"))
                .andExpect(model().attributeExists("contratos", "trabajadores"))
                .andExpect(model().attribute("activePage", "contratos"));
    }

    @Test
    void list_ShouldFilterByTrabajador() throws Exception {
        when(contratoService.findByTrabajadorId(1L)).thenReturn(List.of(createResponse(1L, "ACTIVO")));
        when(trabajadorService.findAll()).thenReturn(List.of(createTrabajadorResponse()));

        mockMvc.perform(get("/administrativo/rrhh/contratos?trabajadorId=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/contratos/list"))
                .andExpect(model().attribute("trabajadorId", 1L));
    }

    @Test
    void list_ShouldRenderEmptyState() throws Exception {
        when(contratoService.findAll()).thenReturn(List.of());
        when(trabajadorService.findAll()).thenReturn(List.of(createTrabajadorResponse()));

        mockMvc.perform(get("/administrativo/rrhh/contratos"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("contratos", List.of()));
    }

    @Test
    void createForm_ShouldRenderModal() throws Exception {
        when(trabajadorService.findAll()).thenReturn(List.of(createTrabajadorResponse()));
        when(tipoContratoRepository.findAll()).thenReturn(List.of(new TipoContrato()));

        mockMvc.perform(get("/administrativo/rrhh/contratos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/contratos/form :: modal"))
                .andExpect(model().attribute("editMode", false))
                .andExpect(model().attributeExists("allTrabajadores", "allTiposContrato"));
    }

    @Test
    void createForm_WithTrabajadorId_ShouldPreselect() throws Exception {
        when(trabajadorService.findAll()).thenReturn(List.of(createTrabajadorResponse()));
        when(tipoContratoRepository.findAll()).thenReturn(List.of(new TipoContrato()));

        mockMvc.perform(get("/administrativo/rrhh/contratos/nuevo?trabajadorId=1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedTrabajadorId", 1L));
    }

    @Test
    void create_ShouldRedirectOnSuccess() throws Exception {
        when(contratoService.create(any(ContratoRequest.class))).thenReturn(createResponse(1L, "ACTIVO"));

        mockMvc.perform(post("/administrativo/rrhh/contratos")
                        .param("trabajadorId", "1")
                        .param("tipoContratoId", "1")
                        .param("fechaInicio", "2025-01-01")
                        .param("remuneracion", "2500.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/contratos"));
    }

    @Test
    void create_ShouldHandleValidationError() throws Exception {
        when(contratoService.create(any(ContratoRequest.class)))
                .thenThrow(new IllegalArgumentException("Contrato DETERMINADO requiere fecha de fin"));

        mockMvc.perform(post("/administrativo/rrhh/contratos")
                        .param("trabajadorId", "1")
                        .param("tipoContratoId", "1")
                        .param("fechaInicio", "2025-01-01")
                        .param("remuneracion", "2500.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/contratos"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void detail_ShouldRenderView() throws Exception {
        when(contratoService.findById(1L)).thenReturn(createResponse(1L, "ACTIVO"));
        when(trabajadorService.findById(1L)).thenReturn(createTrabajadorResponse());

        mockMvc.perform(get("/administrativo/rrhh/contratos/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/contratos/detail"))
                .andExpect(model().attributeExists("contrato", "trabajador"))
                .andExpect(model().attribute("activePage", "contratos"));
    }

    @Test
    void detail_ShouldHandleNotFound() throws Exception {
        when(contratoService.findById(99L))
                .thenThrow(new EntityNotFoundException("Contrato no encontrado con id: 99"));

        mockMvc.perform(get("/administrativo/rrhh/contratos/99"))
                .andExpect(status().isOk()) // Falls back to list view
                .andExpect(view().name("portal-administrativo/rrhh/contratos/list"));
    }

    @Test
    void editForm_ShouldRenderModal() throws Exception {
        when(contratoService.findById(1L)).thenReturn(createResponse(1L, "ACTIVO"));
        when(tipoContratoRepository.findAll()).thenReturn(List.of(new TipoContrato()));

        mockMvc.perform(get("/administrativo/rrhh/contratos/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/contratos/form :: modal"))
                .andExpect(model().attribute("editMode", true))
                .andExpect(model().attributeExists("contrato", "allTiposContrato"));
    }

    @Test
    void update_ShouldRedirectOnSuccess() throws Exception {
        when(contratoService.update(eq(1L), any(ContratoUpdateRequest.class))).thenReturn(createResponse(1L, "ACTIVO"));

        mockMvc.perform(post("/administrativo/rrhh/contratos/1")
                        .param("tipoContratoId", "1")
                        .param("fechaInicio", "2025-01-01")
                        .param("remuneracion", "3000.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/contratos/1"));
    }

    @Test
    void resolver_ShouldRedirectOnSuccess() throws Exception {
        when(contratoService.resolver(eq(1L), anyString())).thenReturn(createResponse(1L, "RESUELTO"));

        mockMvc.perform(post("/administrativo/rrhh/contratos/1/resolver")
                        .param("motivoCese", "Renuncia voluntaria"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/contratos/1"));
    }

    @Test
    void resolver_ShouldHandleError() throws Exception {
        when(contratoService.resolver(eq(1L), anyString()))
                .thenThrow(new IllegalStateException("No se puede resolver un contrato en estado VENCIDO"));

        mockMvc.perform(post("/administrativo/rrhh/contratos/1/resolver")
                        .param("motivoCese", "Renuncia"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/contratos/1"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void suspender_ShouldRedirectOnSuccess() throws Exception {
        when(contratoService.suspender(1L)).thenReturn(createResponse(1L, "SUSPENDIDO"));

        mockMvc.perform(post("/administrativo/rrhh/contratos/1/suspender"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/contratos/1"));
    }

    @Test
    void reactivar_ShouldRedirectOnSuccess() throws Exception {
        when(contratoService.reactivar(1L)).thenReturn(createResponse(1L, "ACTIVO"));

        mockMvc.perform(post("/administrativo/rrhh/contratos/1/reactivar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/rrhh/contratos/1"));
    }

    @Test
    void confirmarResolver_ShouldRenderModal() throws Exception {
        when(contratoService.findById(1L)).thenReturn(createResponse(1L, "ACTIVO"));

        mockMvc.perform(get("/administrativo/rrhh/contratos/1/confirmar-resolver"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/contratos/action-confirm :: resolver"))
                .andExpect(model().attributeExists("contrato"));
    }

    @Test
    void confirmarSuspender_ShouldRenderModal() throws Exception {
        when(contratoService.findById(1L)).thenReturn(createResponse(1L, "ACTIVO"));

        mockMvc.perform(get("/administrativo/rrhh/contratos/1/confirmar-suspender"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/contratos/action-confirm :: suspender"))
                .andExpect(model().attributeExists("contrato"));
    }

    @Test
    void confirmarReactivar_ShouldRenderModal() throws Exception {
        when(contratoService.findById(1L)).thenReturn(createResponse(1L, "SUSPENDIDO"));

        mockMvc.perform(get("/administrativo/rrhh/contratos/1/confirmar-reactivar"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal-administrativo/rrhh/contratos/action-confirm :: reactivar"))
                .andExpect(model().attributeExists("contrato"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void list_ShouldReturn403_WithoutProperAuthority() throws Exception {
        mockMvc.perform(get("/administrativo/rrhh/contratos"))
                .andExpect(status().isForbidden());
    }
}

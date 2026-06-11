package com.clinica.rrhh.trabajador.service;

import com.clinica.maestro.repository.rrhh.TipoColegiaturaRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.periodo.entity.PeriodoLaboral;
import com.clinica.rrhh.periodo.repository.PeriodoLaboralRepository;
import com.clinica.rrhh.trabajador.dto.TrabajadorRequest;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrabajadorServiceTest {

    @Mock
    private TrabajadorRepository trabajadorRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private TipoColegiaturaRepository tipoColegiaturaRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private PeriodoLaboralRepository periodoLaboralRepository;

    @InjectMocks
    private TrabajadorService trabajadorService;

    @Captor
    private ArgumentCaptor<Trabajador> trabajadorCaptor;

    private Persona createPersona(Long id) {
        var p = new Persona();
        p.setId(id);
        p.setNombres("JUAN");
        p.setApellidoPaterno("PEREZ");
        return p;
    }

    private Trabajador createTrabajador(Long id, Persona persona) {
        var t = new Trabajador();
        t.setId(id);
        t.setPersona(persona);
        t.setCodigoTrabajador("TR-001");
        t.setActivo(true);
        return t;
    }

    @Test
    void findAll_ShouldReturnList() {
        var persona = createPersona(1L);
        var trabajador = createTrabajador(1L, persona);
        when(trabajadorRepository.findAllByActivoTrue()).thenReturn(List.of(trabajador));

        var result = trabajadorService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).codigoTrabajador()).isEqualTo("TR-001");
    }

    @Test
    void findById_ShouldReturnResponse() {
        var persona = createPersona(1L);
        var trabajador = createTrabajador(1L, persona);
        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));

        var result = trabajadorService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(trabajadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trabajadorService.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_ShouldSaveAndReturnResponse() {
        var persona = createPersona(1L);
        when(trabajadorRepository.existsByPersonaId(1L)).thenReturn(false);
        when(trabajadorRepository.existsByCodigoTrabajador("TR-001")).thenReturn(false);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));

        var saved = createTrabajador(1L, persona);
        when(trabajadorRepository.save(any())).thenReturn(saved);

        when(periodoLaboralRepository.save(any(PeriodoLaboral.class))).thenAnswer(i -> i.getArgument(0));

        var request = new TrabajadorRequest(1L, "TR-001", LocalDate.of(2025, 1, 1), null, null, "Medico", null, null, null, null, null, null, 0, null, null, false, false);

        var result = trabajadorService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.codigoTrabajador()).isEqualTo("TR-001");
        verify(trabajadorRepository).save(trabajadorCaptor.capture());
        assertThat(trabajadorCaptor.getValue().getCodigoTrabajador()).isEqualTo("TR-001");
    }

    @Test
    void create_ShouldRejectDuplicatePersona() {
        when(trabajadorRepository.existsByPersonaId(1L)).thenReturn(true);

        var request = new TrabajadorRequest(1L, "TR-001", null, null, null, null, null, null, null, null, null, null, 0, null, null, false, false);

        assertThatThrownBy(() -> trabajadorService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("1");
        verify(trabajadorRepository, never()).save(any());
    }

    @Test
    void create_ShouldRejectDuplicateCodigo() {
        when(trabajadorRepository.existsByPersonaId(1L)).thenReturn(false);
        when(trabajadorRepository.existsByCodigoTrabajador("TR-001")).thenReturn(true);

        var request = new TrabajadorRequest(1L, "TR-001", null, null, null, null, null, null, null, null, null, null, 0, null, null, false, false);

        assertThatThrownBy(() -> trabajadorService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TR-001");
        verify(trabajadorRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowWhenPersonaNotFound() {
        when(trabajadorRepository.existsByPersonaId(99L)).thenReturn(false);
        when(trabajadorRepository.existsByCodigoTrabajador("TR-001")).thenReturn(false);
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new TrabajadorRequest(99L, "TR-001", null, null, null, null, null, null, null, null, null, null, 0, null, null, false, false);

        assertThatThrownBy(() -> trabajadorService.create(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void update_ShouldModifyAndReturn() {
        var persona = createPersona(1L);
        var existing = createTrabajador(1L, persona);
        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(trabajadorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new TrabajadorRequest(1L, "TR-001", LocalDate.of(2025, 6, 1), null, null, "Jefe de Clinica", null, null, null, null, null, null, 0, null, null, false, false);

        var result = trabajadorService.update(1L, request);

        assertThat(result.cargo()).isEqualTo("Jefe de Clinica");
    }

    @Test
    void update_ShouldThrowWhenNotFound() {
        when(trabajadorRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new TrabajadorRequest(1L, "TR-001", null, null, null, null, null, null, null, null, null, null, 0, null, null, false, false);

        assertThatThrownBy(() -> trabajadorService.update(99L, request))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var persona = createPersona(1L);
        var trabajador = createTrabajador(1L, persona);
        trabajador.setActivo(true);
        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(trabajadorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = trabajadorService.softDelete(1L);

        assertThat(result.activo()).isFalse();
    }

    @Test
    void softDelete_ShouldThrowWhenNotFound() {
        when(trabajadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trabajadorService.softDelete(99L))
            .isInstanceOf(EntityNotFoundException.class);
    }
}

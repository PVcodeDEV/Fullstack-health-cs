package com.clinica.clinica.medico.service;

import com.clinica.clinica.medico.dto.MedicoRequest;
import com.clinica.clinica.medico.entity.Medico;
import com.clinica.clinica.medico.repository.MedicoRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private TrabajadorRepository trabajadorRepository;

    @InjectMocks
    private MedicoService medicoService;

    @Captor
    private ArgumentCaptor<Medico> medicoCaptor;

    private Persona createPersona(Long id) {
        var p = new Persona();
        p.setId(id);
        p.setNombres("JUAN");
        p.setApellidoPaterno("PEREZ");
        p.setNumeroDocumento("12345678");
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

    private Medico createMedico(Long id, Persona persona, Trabajador trabajador) {
        var m = new Medico();
        m.setId(id);
        m.setPersona(persona);
        m.setTrabajador(trabajador);
        m.setCmp("12345");
        m.setEsEspecialista(false);
        m.setActivo(true);
        return m;
    }

    @Test
    void findAll_ShouldReturnList() {
        var persona = createPersona(1L);
        var trabajador = createTrabajador(1L, persona);
        var medico = createMedico(1L, persona, trabajador);
        when(medicoRepository.findAllByActivoTrue()).thenReturn(List.of(medico));

        var result = medicoService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).cmp()).isEqualTo("12345");
    }

    @Test
    void findById_ShouldReturnResponse() {
        var persona = createPersona(1L);
        var trabajador = createTrabajador(1L, persona);
        var medico = createMedico(1L, persona, trabajador);
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));

        var result = medicoService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.cmp()).isEqualTo("12345");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicoService.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_ShouldSaveAndReturnResponse() {
        var persona = createPersona(1L);
        var trabajador = createTrabajador(1L, persona);
        when(medicoRepository.existsByPersonaId(1L)).thenReturn(false);
        when(medicoRepository.existsByCmp("12345")).thenReturn(false);
        when(medicoRepository.existsByTrabajadorId(1L)).thenReturn(false);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));

        var saved = createMedico(1L, persona, trabajador);
        when(medicoRepository.save(any())).thenReturn(saved);

        var request = new MedicoRequest(1L, 1L, "12345", null, false);

        var result = medicoService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.cmp()).isEqualTo("12345");
        verify(medicoRepository).save(medicoCaptor.capture());
        assertThat(medicoCaptor.getValue().getCmp()).isEqualTo("12345");
    }

    @Test
    void create_WithExternalDoctor_ShouldAllowNullTrabajador() {
        var persona = createPersona(1L);
        when(medicoRepository.existsByPersonaId(1L)).thenReturn(false);
        when(medicoRepository.existsByCmp("12345")).thenReturn(false);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));

        var saved = new Medico();
        saved.setId(1L);
        saved.setPersona(persona);
        saved.setTrabajador(null);
        saved.setCmp("12345");
        saved.setEsEspecialista(false);
        saved.setActivo(true);
        when(medicoRepository.save(any())).thenReturn(saved);

        var request = new MedicoRequest(1L, null, "12345", null, false);

        var result = medicoService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.trabajadorId()).isNull();
        assertThat(result.personaId()).isEqualTo(1L);
        verify(personaRepository).findById(1L);
        verify(trabajadorRepository, never()).findById(any());
    }

    @Test
    void create_ShouldRejectDuplicatePersona() {
        when(medicoRepository.existsByPersonaId(1L)).thenReturn(true);

        var request = new MedicoRequest(1L, 1L, "12345", null, false);

        assertThatThrownBy(() -> medicoService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("persona");
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void create_ShouldRejectDuplicateCmp() {
        when(medicoRepository.existsByPersonaId(1L)).thenReturn(false);
        when(medicoRepository.existsByCmp("12345")).thenReturn(true);

        var request = new MedicoRequest(1L, 1L, "12345", null, false);

        assertThatThrownBy(() -> medicoService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("12345");
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowWhenPersonaNotFound() {
        when(medicoRepository.existsByPersonaId(99L)).thenReturn(false);
        when(medicoRepository.existsByCmp("12345")).thenReturn(false);
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new MedicoRequest(99L, 1L, "12345", null, false);

        assertThatThrownBy(() -> medicoService.create(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_WithEspecialistaTrue_ShouldSetFlag() {
        var persona = createPersona(1L);
        var trabajador = createTrabajador(1L, persona);
        when(medicoRepository.existsByPersonaId(1L)).thenReturn(false);
        when(medicoRepository.existsByCmp("99999")).thenReturn(false);
        when(medicoRepository.existsByTrabajadorId(1L)).thenReturn(false);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));

        var saved = createMedico(1L, persona, trabajador);
        saved.setEsEspecialista(true);
        when(medicoRepository.save(any())).thenReturn(saved);

        var request = new MedicoRequest(1L, 1L, "99999", 1L, true);

        var result = medicoService.create(request);

        assertThat(result.esEspecialista()).isTrue();
    }

    @Test
    void update_ShouldModifyAndReturn() {
        var persona = createPersona(1L);
        var trabajador = createTrabajador(1L, persona);
        var existing = createMedico(1L, persona, trabajador);
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(medicoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new MedicoRequest(1L, 1L, "54321", null, true);

        var result = medicoService.update(1L, request);

        assertThat(result.cmp()).isEqualTo("54321");
        assertThat(result.esEspecialista()).isTrue();
    }

    @Test
    void update_ShouldThrowWhenNotFound() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new MedicoRequest(1L, 1L, "12345", null, false);

        assertThatThrownBy(() -> medicoService.update(99L, request))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var persona = createPersona(1L);
        var trabajador = createTrabajador(1L, persona);
        var medico = createMedico(1L, persona, trabajador);
        medico.setActivo(true);
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(medicoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = medicoService.softDelete(1L);

        assertThat(result.activo()).isFalse();
    }

    @Test
    void softDelete_ShouldThrowWhenNotFound() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicoService.softDelete(99L))
            .isInstanceOf(EntityNotFoundException.class);
    }
}

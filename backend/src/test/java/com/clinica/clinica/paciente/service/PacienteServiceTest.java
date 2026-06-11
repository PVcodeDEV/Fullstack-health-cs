package com.clinica.clinica.paciente.service;

import com.clinica.clinica.paciente.dto.PacienteRequest;
import com.clinica.clinica.paciente.entity.Paciente;
import com.clinica.clinica.paciente.repository.PacienteRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
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
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private PacienteService pacienteService;

    @Captor
    private ArgumentCaptor<Paciente> pacienteCaptor;

    private Persona createPersona(Long id) {
        var p = new Persona();
        p.setId(id);
        p.setNombres("JUAN");
        p.setApellidoPaterno("PEREZ");
        return p;
    }

    private Paciente createPaciente(Long id, Persona persona) {
        var p = new Paciente();
        p.setId(id);
        p.setPersona(persona);
        p.setTipoPaciente("PARTICULAR");
        p.setActivo(true);
        return p;
    }

    @Test
    void findAll_ShouldReturnList() {
        var persona = createPersona(1L);
        var paciente = createPaciente(1L, persona);
        when(pacienteRepository.findAllByActivoTrue()).thenReturn(List.of(paciente));

        var result = pacienteService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).tipoPaciente()).isEqualTo("PARTICULAR");
    }

    @Test
    void findById_ShouldReturnResponse() {
        var persona = createPersona(1L);
        var paciente = createPaciente(1L, persona);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        var result = pacienteService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_ShouldSaveAndReturnResponse() {
        var persona = createPersona(1L);
        when(pacienteRepository.existsByPersonaId(1L)).thenReturn(false);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));

        var savedPaciente = createPaciente(1L, persona);
        when(pacienteRepository.save(any())).thenReturn(savedPaciente);

        var request = new PacienteRequest(1L, "PARTICULAR", null, null, null, null, null);

        var result = pacienteService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.tipoPaciente()).isEqualTo("PARTICULAR");
        verify(pacienteRepository).save(pacienteCaptor.capture());
        assertThat(pacienteCaptor.getValue().getTipoPaciente()).isEqualTo("PARTICULAR");
    }

    @Test
    void create_ShouldRejectDuplicatePersona() {
        when(pacienteRepository.existsByPersonaId(1L)).thenReturn(true);

        var request = new PacienteRequest(1L, "PARTICULAR", null, null, null, null, null);

        assertThatThrownBy(() -> pacienteService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("1");
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowWhenPersonaNotFound() {
        when(pacienteRepository.existsByPersonaId(99L)).thenReturn(false);
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new PacienteRequest(99L, "PARTICULAR", null, null, null, null, null);

        assertThatThrownBy(() -> pacienteService.create(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void update_ShouldModifyAndReturn() {
        var persona = createPersona(1L);
        var existing = createPaciente(1L, persona);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(pacienteRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new PacienteRequest(1L, "VIP", "HC-2025-00001", "O+", null, "Maria Lopez", "987654321");

        var result = pacienteService.update(1L, request);

        assertThat(result.tipoPaciente()).isEqualTo("VIP");
        assertThat(result.nroHistoriaClinica()).isEqualTo("HC-2025-00001");
    }

    @Test
    void update_ShouldThrowWhenNotFound() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new PacienteRequest(1L, "PARTICULAR", null, null, null, null, null);

        assertThatThrownBy(() -> pacienteService.update(99L, request))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var persona = createPersona(1L);
        var paciente = createPaciente(1L, persona);
        paciente.setActivo(true);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = pacienteService.softDelete(1L);

        assertThat(result.activo()).isFalse();
    }

    @Test
    void softDelete_ShouldThrowWhenNotFound() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.softDelete(99L))
            .isInstanceOf(EntityNotFoundException.class);
    }
}

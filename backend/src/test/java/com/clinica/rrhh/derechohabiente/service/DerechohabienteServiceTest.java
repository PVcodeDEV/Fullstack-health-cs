package com.clinica.rrhh.derechohabiente.service;

import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.derechohabiente.dto.DerechohabienteRequest;
import com.clinica.rrhh.derechohabiente.entity.Derechohabiente;
import com.clinica.rrhh.derechohabiente.repository.DerechohabienteRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.TipoRelacionDerechohabiente;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DerechohabienteServiceTest {

    @Mock
    private DerechohabienteRepository derechohabienteRepository;

    @Mock
    private TrabajadorRepository trabajadorRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private DerechohabienteService derechohabienteService;

    @Captor
    private ArgumentCaptor<Derechohabiente> dhCaptor;

    private Trabajador createTrabajador(Long id) {
        var t = new Trabajador();
        t.setId(id);
        t.setCodigoTrabajador("TR-" + id);
        return t;
    }

    private Persona createPersona(Long id) {
        var p = new Persona();
        p.setId(id);
        p.setNombres("BENEFICIARIO");
        p.setApellidoPaterno("TEST");
        return p;
    }

    @Test
    void create_HIJO_AutoCalculaFechaFin() {
        var trabajador = createTrabajador(1L);
        var persona = createPersona(2L);

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(persona));
        when(derechohabienteRepository.save(any())).thenAnswer(i -> {
            var dh = (Derechohabiente) i.getArgument(0);
            dh.setId(1L);
            return dh;
        });

        var request = new DerechohabienteRequest(1L, 2L, "HIJO",
                LocalDate.of(2025, 1, 1), null);
        var result = derechohabienteService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.relacion()).isEqualTo("HIJO");
        assertThat(result.fechaFin()).isEqualTo(LocalDate.of(2043, 1, 1)); // +18 years
        assertThat(result.estado()).isEqualTo("ACTIVO");
    }

    @Test
    void create_NO_HIJO_UsesProvidedFechaFin() {
        var trabajador = createTrabajador(1L);
        var persona = createPersona(2L);

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(persona));
        when(derechohabienteRepository.save(any())).thenAnswer(i -> {
            var dh = (Derechohabiente) i.getArgument(0);
            dh.setId(2L);
            return dh;
        });

        var request = new DerechohabienteRequest(1L, 2L, "CONYUGE",
                LocalDate.of(2025, 1, 1), LocalDate.of(2030, 1, 1));
        var result = derechohabienteService.create(request);

        assertThat(result.relacion()).isEqualTo("CONYUGE");
        assertThat(result.fechaFin()).isEqualTo(LocalDate.of(2030, 1, 1));
    }

    @Test
    void create_ShouldThrowWhenTrabajadorNotFound() {
        when(trabajadorRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new DerechohabienteRequest(99L, 2L, "HIJO",
                LocalDate.now(), null);

        assertThatThrownBy(() -> derechohabienteService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_ShouldThrowWhenPersonaNotFound() {
        var trabajador = createTrabajador(1L);
        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new DerechohabienteRequest(1L, 99L, "HIJO",
                LocalDate.now(), null);

        assertThatThrownBy(() -> derechohabienteService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void inactivarPorTrabajador_ChangesAllActiveToInactivo() {
        var trabajador = createTrabajador(1L);
        var persona = createPersona(2L);

        var dh1 = new Derechohabiente();
        dh1.setId(1L);
        dh1.setTrabajador(trabajador);
        dh1.setPersona(persona);
        dh1.setRelacion(TipoRelacionDerechohabiente.CONYUGE);
        dh1.setEstado("ACTIVO");

        var dh2 = new Derechohabiente();
        dh2.setId(2L);
        dh2.setTrabajador(trabajador);
        dh2.setPersona(persona);
        dh2.setRelacion(TipoRelacionDerechohabiente.HIJO);
        dh2.setEstado("ACTIVO");

        when(derechohabienteRepository.findByTrabajadorIdAndEstadoOrderByFechaInicioDesc(
                1L, "ACTIVO")).thenReturn(List.of(dh1, dh2));
        when(derechohabienteRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        derechohabienteService.inactivarPorTrabajador(1L);

        assertThat(dh1.getEstado()).isEqualTo("INACTIVO");
        assertThat(dh2.getEstado()).isEqualTo("INACTIVO");
        verify(derechohabienteRepository, times(2)).save(any());
    }

    @Test
    void inactivarPorTrabajador_NoActiveEntries_DoesNothing() {
        when(derechohabienteRepository.findByTrabajadorIdAndEstadoOrderByFechaInicioDesc(
                1L, "ACTIVO")).thenReturn(new ArrayList<>());

        derechohabienteService.inactivarPorTrabajador(1L);

        verify(derechohabienteRepository, never()).save(any());
    }

    @Test
    void findById_ShouldReturnResponse() {
        var trabajador = createTrabajador(1L);
        var persona = createPersona(2L);
        var dh = new Derechohabiente();
        dh.setId(1L);
        dh.setTrabajador(trabajador);
        dh.setPersona(persona);
        dh.setRelacion(TipoRelacionDerechohabiente.CONYUGE);
        dh.setEstado("ACTIVO");

        when(derechohabienteRepository.findById(1L)).thenReturn(Optional.of(dh));

        var result = derechohabienteService.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.estado()).isEqualTo("ACTIVO");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(derechohabienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> derechohabienteService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void inactivar_ById_Succeeds() {
        var trabajador = createTrabajador(1L);
        var persona = createPersona(2L);
        var dh = new Derechohabiente();
        dh.setId(1L);
        dh.setTrabajador(trabajador);
        dh.setPersona(persona);
        dh.setRelacion(TipoRelacionDerechohabiente.CONYUGE);
        dh.setEstado("ACTIVO");

        when(derechohabienteRepository.findById(1L)).thenReturn(Optional.of(dh));
        when(derechohabienteRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = derechohabienteService.inactivar(1L);

        assertThat(result.estado()).isEqualTo("INACTIVO");
    }
}

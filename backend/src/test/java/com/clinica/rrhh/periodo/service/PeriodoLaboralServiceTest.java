package com.clinica.rrhh.periodo.service;

import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.periodo.entity.PeriodoLaboral;
import com.clinica.rrhh.periodo.repository.PeriodoLaboralRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeriodoLaboralServiceTest {

    @Mock
    private PeriodoLaboralRepository periodoLaboralRepository;

    @Mock
    private TrabajadorRepository trabajadorRepository;

    @InjectMocks
    private PeriodoLaboralService periodoLaboralService;

    @Captor
    private ArgumentCaptor<PeriodoLaboral> periodCaptor;

    private Trabajador createTrabajador(Long id) {
        var t = new Trabajador();
        t.setId(id);
        t.setCodigoTrabajador("TR-" + id);
        return t;
    }

    @Test
    void registrarIngreso_CreatesPeriodAndClosesPrevious() {
        var trabajador = createTrabajador(1L);
        var previous = new PeriodoLaboral();
        previous.setId(99L);
        previous.setTrabajador(trabajador);
        previous.setFechaInicio(LocalDate.of(2024, 1, 1));
        previous.setActivo(true);

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(periodoLaboralRepository.findByTrabajadorIdAndActivoTrue(1L))
                .thenReturn(Optional.of(previous));
        when(periodoLaboralRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = periodoLaboralService.registrarIngreso(1L, LocalDate.of(2025, 1, 1), false);

        assertThat(result).isNotNull();
        assertThat(result.fechaInicio()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.esReingreso()).isFalse();
        assertThat(previous.getActivo()).isFalse();
        verify(periodoLaboralRepository, times(2)).save(any());
    }

    @Test
    void registrarIngreso_NoPreviousActive() {
        var trabajador = createTrabajador(1L);

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(periodoLaboralRepository.findByTrabajadorIdAndActivoTrue(1L))
                .thenReturn(Optional.empty());
        when(periodoLaboralRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = periodoLaboralService.registrarIngreso(1L, LocalDate.of(2025, 1, 1), false);

        assertThat(result).isNotNull();
        assertThat(result.activo()).isTrue();
        verify(periodoLaboralRepository, times(1)).save(any());
    }

    @Test
    void registrarIngreso_reingreso() {
        var trabajador = createTrabajador(1L);

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(periodoLaboralRepository.findByTrabajadorIdAndActivoTrue(1L))
                .thenReturn(Optional.empty());
        when(periodoLaboralRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = periodoLaboralService.registrarIngreso(1L, LocalDate.of(2025, 6, 1), true);

        assertThat(result.esReingreso()).isTrue();
        assertThat(result.fechaInicio()).isEqualTo(LocalDate.of(2025, 6, 1));
    }

    @Test
    void registrarIngreso_ShouldThrowWhenTrabajadorNotFound() {
        when(trabajadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodoLaboralService.registrarIngreso(99L, LocalDate.now(), false))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void registrarCese_SetsFechaCeseAndMotivo() {
        var trabajador = createTrabajador(1L);
        var periodo = new PeriodoLaboral();
        periodo.setId(1L);
        periodo.setTrabajador(trabajador);
        periodo.setFechaInicio(LocalDate.of(2025, 1, 1));
        periodo.setActivo(true);

        when(periodoLaboralRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(periodoLaboralRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = periodoLaboralService.registrarCese(1L, LocalDate.of(2025, 12, 31), "Renuncia voluntaria");

        assertThat(result.fechaCese()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(result.motivoCese()).isEqualTo("Renuncia voluntaria");
        assertThat(result.activo()).isFalse();
    }

    @Test
    void registrarCese_ShouldThrowWhenNotFound() {
        when(periodoLaboralRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodoLaboralService.registrarCese(99L, LocalDate.now(), "Motivo"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findById_ShouldReturnResponse() {
        var trabajador = createTrabajador(1L);
        var periodo = new PeriodoLaboral();
        periodo.setId(1L);
        periodo.setTrabajador(trabajador);
        periodo.setFechaInicio(LocalDate.of(2025, 1, 1));
        periodo.setActivo(true);

        when(periodoLaboralRepository.findById(1L)).thenReturn(Optional.of(periodo));

        var result = periodoLaboralService.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.fechaInicio()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(periodoLaboralRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodoLaboralService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }
}

package com.clinica.rrhh.planilla.service;

import com.clinica.rrhh.planilla.dto.PeriodoPlanillaRequest;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planilla.repository.PlanillaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class PeriodoPlanillaServiceTest {

    @Mock
    private PeriodoPlanillaRepository periodoPlanillaRepository;

    @Mock
    private PlanillaRepository planillaRepository;

    @InjectMocks
    private PeriodoPlanillaService service;

    @Test
    void create_ShouldCreateAndReturnResponse() {
        var request = new PeriodoPlanillaRequest(
            2026, 1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        when(periodoPlanillaRepository.existsByAnioAndMes(2026, 1)).thenReturn(false);
        when(periodoPlanillaRepository.save(any())).thenAnswer(i -> {
            var entity = (PeriodoPlanilla) i.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        var result = service.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.anio()).isEqualTo(2026);
        assertThat(result.mes()).isEqualTo(1);
        assertThat(result.estado()).isEqualTo("ABIERTO");

        verify(periodoPlanillaRepository).save(any());
    }

    @Test
    void create_Duplicate_Throws() {
        var request = new PeriodoPlanillaRequest(
            2026, 1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        when(periodoPlanillaRepository.existsByAnioAndMes(2026, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Ya existe");

        verify(periodoPlanillaRepository, never()).save(any());
    }

    @Test
    void cerrar_ShouldClosePeriod() {
        var entity = new PeriodoPlanilla();
        entity.setId(1L);
        entity.setAnio(2026);
        entity.setMes(1);
        entity.setEstado("ABIERTO");

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(planillaRepository.existsByPeriodoPlanillaId(1L)).thenReturn(true);
        when(periodoPlanillaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.cerrar(1L);

        assertThat(result.estado()).isEqualTo("CERRADO");
    }

    @Test
    void cerrar_NotFound_Throws() {
        when(periodoPlanillaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cerrar(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void cerrar_NotAbierto_Throws() {
        var entity = new PeriodoPlanilla();
        entity.setId(1L);
        entity.setEstado("CERRADO");

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.cerrar(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no está ABIERTO");
    }

    @Test
    void cerrar_NoPlanilla_Throws() {
        var entity = new PeriodoPlanilla();
        entity.setId(1L);
        entity.setEstado("ABIERTO");

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(planillaRepository.existsByPeriodoPlanillaId(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.cerrar(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("sin planilla");
    }

    @Test
    void findAll_ShouldReturnList() {
        var entity1 = new PeriodoPlanilla();
        entity1.setId(1L);
        entity1.setAnio(2026);
        entity1.setMes(2);
        entity1.setEstado("ABIERTO");

        var entity2 = new PeriodoPlanilla();
        entity2.setId(2L);
        entity2.setAnio(2026);
        entity2.setMes(1);
        entity2.setEstado("ABIERTO");

        when(periodoPlanillaRepository.findAllByOrderByAnioDescMesDesc())
            .thenReturn(List.of(entity1, entity2));

        var result = service.findAll();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).anio()).isEqualTo(2026);
        assertThat(result.get(0).mes()).isEqualTo(2);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new PeriodoPlanilla();
        entity.setId(1L);
        entity.setAnio(2026);
        entity.setMes(1);
        entity.setEstado("ABIERTO");

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(entity));

        var result = service.findById(1L);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.estado()).isEqualTo("ABIERTO");
    }

    @Test
    void findById_NotFound_Throws() {
        when(periodoPlanillaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }
}

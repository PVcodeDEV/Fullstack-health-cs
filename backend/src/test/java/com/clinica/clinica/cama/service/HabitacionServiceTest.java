package com.clinica.clinica.cama.service;

import com.clinica.clinica.cama.dto.HabitacionRequest;
import com.clinica.clinica.cama.entity.Habitacion;
import com.clinica.clinica.cama.repository.HabitacionRepository;
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
class HabitacionServiceTest {

    @Mock
    private HabitacionRepository repository;

    @InjectMocks
    private HabitacionService service;

    @Captor
    private ArgumentCaptor<Habitacion> habitacionCaptor;

    private Habitacion createHabitacion(Long id) {
        var h = new Habitacion();
        h.setId(id);
        h.setCodigo("HAB-001");
        h.setNombre("Habitación 101");
        h.setTipoHabitacionId(1L);
        h.setCapacidad(2);
        return h;
    }

    @Test
    void findAll_ShouldReturnList() {
        var hab = createHabitacion(1L);
        when(repository.findAllByActivoTrue()).thenReturn(List.of(hab));

        var result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nombre()).isEqualTo("Habitación 101");
    }

    @Test
    void findById_ShouldReturnResponse() {
        var hab = createHabitacion(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(hab));

        var result = service.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_ShouldSaveAndReturnResponse() {
        var saved = createHabitacion(1L);
        when(repository.save(any())).thenReturn(saved);

        var request = new HabitacionRequest(1L, "Habitación 101", "Piso 1", 2, null);
        var result = service.create(request);

        assertThat(result).isNotNull();
        assertThat(result.nombre()).isEqualTo("Habitación 101");
        verify(repository).save(habitacionCaptor.capture());
        assertThat(habitacionCaptor.getValue().getTipoHabitacionId()).isEqualTo(1L);
    }

    @Test
    void create_ShouldGenerateCodigoFromNombre() {
        var saved = createHabitacion(1L);
        when(repository.save(any())).thenReturn(saved);

        var request = new HabitacionRequest(1L, "SALA DE OPERACIONES", "Piso 2", 1, null);
        service.create(request);

        verify(repository).save(habitacionCaptor.capture());
        assertThat(habitacionCaptor.getValue().getCodigo()).isEqualTo("SALA DE OPERACIONES");
    }

    @Test
    void update_ShouldModifyAndReturn() {
        var existing = createHabitacion(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new HabitacionRequest(2L, "Nuevo Nombre", "Piso 3", 4, null);
        var result = service.update(1L, request);

        assertThat(result.nombre()).isEqualTo("Nuevo Nombre");
        assertThat(result.capacidad()).isEqualTo(4);
    }

    @Test
    void update_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        var request = new HabitacionRequest(1L, "Test", "Piso 1", 1, null);
        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var hab = createHabitacion(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(hab));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.softDelete(1L);

        assertThat(result.activo()).isFalse();
    }
}

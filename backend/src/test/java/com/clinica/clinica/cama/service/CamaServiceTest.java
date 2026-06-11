package com.clinica.clinica.cama.service;

import com.clinica.clinica.cama.dto.CamaRequest;
import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.entity.EstadoCama;
import com.clinica.clinica.cama.repository.CamaRepository;
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
class CamaServiceTest {

    @Mock
    private CamaRepository repository;

    @InjectMocks
    private CamaService service;

    @Captor
    private ArgumentCaptor<Cama> camaCaptor;

    private Cama createCama(Long id, String codigo, EstadoCama estado, boolean activo) {
        var c = new Cama();
        c.setId(id);
        c.setHabitacionId(1L);
        c.setCodigo(codigo);
        c.setEstado(estado);
        if (!activo) c.markAsInactive();
        return c;
    }

    @Test
    void findAll_ShouldReturnList() {
        var cama = createCama(1L, "CAMA-001", EstadoCama.DISPONIBLE, true);
        when(repository.findAllByActivoTrue()).thenReturn(List.of(cama));

        var result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).codigo()).isEqualTo("CAMA-001");
    }

    @Test
    void findById_ShouldReturnResponse() {
        var cama = createCama(1L, "CAMA-001", EstadoCama.DISPONIBLE, true);
        when(repository.findById(1L)).thenReturn(Optional.of(cama));

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
    void findDisponibles_ShouldReturnOnlyAvailable() {
        var disp = createCama(1L, "CAMA-001", EstadoCama.DISPONIBLE, true);
        var ocup = createCama(2L, "CAMA-002", EstadoCama.OCUPADO, true);
        when(repository.findByEstado(EstadoCama.DISPONIBLE)).thenReturn(List.of(disp));

        var result = service.findDisponibles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).codigo()).isEqualTo("CAMA-001");
    }

    @Test
    void create_ShouldSaveAndReturnResponse() {
        var savedCama = createCama(1L, "CAMA-NEW", EstadoCama.DISPONIBLE, true);
        when(repository.save(any())).thenReturn(savedCama);

        var request = new CamaRequest(1L, "CAMA-NEW", null);
        var result = service.create(request);

        assertThat(result).isNotNull();
        assertThat(result.codigo()).isEqualTo("CAMA-NEW");
        verify(repository).save(camaCaptor.capture());
        assertThat(camaCaptor.getValue().getEstado()).isEqualTo(EstadoCama.DISPONIBLE);
    }

    @Test
    void cambiarEstado_DisponibleToOcupado_ShouldSucceed() {
        var cama = createCama(1L, "CAMA-001", EstadoCama.DISPONIBLE, true);
        when(repository.findById(1L)).thenReturn(Optional.of(cama));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.cambiarEstado(1L, "OCUPADO");

        assertThat(result.estado()).isEqualTo("OCUPADO");
    }

    @Test
    void cambiarEstado_OcupadoToDisponible_ShouldSucceed() {
        var cama = createCama(1L, "CAMA-001", EstadoCama.OCUPADO, true);
        when(repository.findById(1L)).thenReturn(Optional.of(cama));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.cambiarEstado(1L, "DISPONIBLE");

        assertThat(result.estado()).isEqualTo("DISPONIBLE");
    }

    @Test
    void cambiarEstado_InvalidTransition_ShouldThrow() {
        var cama = createCama(1L, "CAMA-001", EstadoCama.OCUPADO, true);
        when(repository.findById(1L)).thenReturn(Optional.of(cama));

        assertThatThrownBy(() -> service.cambiarEstado(1L, "MANTENIMIENTO"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cambiarEstado_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cambiarEstado(99L, "OCUPADO"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var cama = createCama(1L, "CAMA-001", EstadoCama.DISPONIBLE, true);
        when(repository.findById(1L)).thenReturn(Optional.of(cama));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.softDelete(1L);

        assertThat(result.activo()).isFalse();
    }

    @Test
    void softDelete_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.softDelete(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

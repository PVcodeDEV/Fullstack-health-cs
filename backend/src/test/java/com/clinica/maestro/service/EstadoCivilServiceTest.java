package com.clinica.maestro.service;

import com.clinica.maestro.dto.identidad.EstadoCivilRequest;
import com.clinica.maestro.entity.identidad.EstadoCivil;
import com.clinica.maestro.repository.identidad.EstadoCivilRepository;
import com.clinica.maestro.service.identidad.EstadoCivilService;
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
class EstadoCivilServiceTest {

    @Mock
    private EstadoCivilRepository repository;

    @InjectMocks
    private EstadoCivilService service;

    @Captor
    private ArgumentCaptor<EstadoCivil> entityCaptor;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new EstadoCivil();
        entity.setId(1L);
        entity.setCodigoReniec("S");
        entity.setNombre("Soltero");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));

        var result = service.findAll();
        assertThat(result).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new EstadoCivil();
        entity.setId(1L);
        entity.setCodigoReniec("S");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        var result = service.findById(1L);
        assertThat(result.codigoReniec()).isEqualTo("S");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveAndReturnResponse() {
        var request = new EstadoCivilRequest("S", "Soltero");
        when(repository.existsByCodigoReniec("S")).thenReturn(false);
        var saved = new EstadoCivil();
        saved.setId(1L);
        saved.setCodigoReniec("S");
        saved.setNombre("Soltero");
        when(repository.save(any())).thenReturn(saved);

        var result = service.create(request);
        assertThat(result.codigoReniec()).isEqualTo("S");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new EstadoCivilRequest("S", "Soltero");
        when(repository.existsByCodigoReniec("S")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_ShouldModifyAndReturn() {
        var existing = new EstadoCivil();
        existing.setId(1L);
        existing.setCodigoReniec("S");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        var request = new EstadoCivilRequest("C", "Casado");
        when(repository.existsByCodigoReniec("C")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.update(1L, request);
        assertThat(result.codigoReniec()).isEqualTo("C");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new EstadoCivil();
        entity.setId(1L);
        entity.setActivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.softDelete(1L);
        assertThat(result.activo()).isFalse();
    }
}

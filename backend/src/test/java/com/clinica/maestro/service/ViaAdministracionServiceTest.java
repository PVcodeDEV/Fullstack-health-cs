package com.clinica.maestro.service;

import com.clinica.maestro.dto.clinico.ViaAdministracionRequest;
import com.clinica.maestro.entity.clinico.ViaAdministracion;
import com.clinica.maestro.repository.clinico.ViaAdministracionRepository;
import com.clinica.maestro.service.clinico.ViaAdministracionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ViaAdministracionServiceTest {

    @Mock
    private ViaAdministracionRepository repository;

    @InjectMocks
    private ViaAdministracionService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new ViaAdministracion();
        entity.setId(1L);
        entity.setCodigo("ORAL");
        entity.setNombre("Oral");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new ViaAdministracion();
        entity.setId(1L);
        entity.setCodigo("ORAL");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1L).codigo()).isEqualTo("ORAL");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveAndReturn() {
        var request = new ViaAdministracionRequest("IV", "Intravenoso");
        when(repository.existsByCodigo("IV")).thenReturn(false);
        var saved = new ViaAdministracion();
        saved.setId(1L);
        saved.setCodigo("IV");
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigo()).isEqualTo("IV");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new ViaAdministracionRequest("ORAL", "Oral");
        when(repository.existsByCodigo("ORAL")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_ShouldModify() {
        var existing = new ViaAdministracion();
        existing.setId(1L);
        existing.setCodigo("ORAL");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        var request = new ViaAdministracionRequest("IV", "Intravenoso");
        when(repository.existsByCodigo("IV")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.update(1L, request).codigo()).isEqualTo("IV");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new ViaAdministracion();
        entity.setId(1L);
        entity.setActivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete(1L).activo()).isFalse();
    }
}

package com.clinica.maestro.service;

import com.clinica.maestro.dto.clinico.TipoAtencionRequest;
import com.clinica.maestro.entity.clinico.TipoAtencion;
import com.clinica.maestro.repository.clinico.TipoAtencionRepository;
import com.clinica.maestro.service.clinico.TipoAtencionService;
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
class TipoAtencionServiceTest {

    @Mock
    private TipoAtencionRepository repository;

    @InjectMocks
    private TipoAtencionService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new TipoAtencion();
        entity.setId(1L);
        entity.setCodigo("CON");
        entity.setNombre("Consulta");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new TipoAtencion();
        entity.setId(1L);
        entity.setCodigo("CON");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1L).codigo()).isEqualTo("CON");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveWithRequiereHabitacion() {
        var request = new TipoAtencionRequest("HOS", "Hospitalización", true);
        when(repository.existsByCodigo("HOS")).thenReturn(false);
        var saved = new TipoAtencion();
        saved.setId(1L);
        saved.setCodigo("HOS");
        saved.setRequiereHabitacion(true);
        when(repository.save(any())).thenReturn(saved);

        var result = service.create(request);
        assertThat(result.codigo()).isEqualTo("HOS");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new TipoAtencionRequest("CON", "Consulta", false);
        when(repository.existsByCodigo("CON")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_ShouldModifyFields() {
        var existing = new TipoAtencion();
        existing.setId(1L);
        existing.setCodigo("CON");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        var request = new TipoAtencionRequest("HOS", "Hospitalización", true);
        when(repository.existsByCodigo("HOS")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.update(1L, request);
        assertThat(result.codigo()).isEqualTo("HOS");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new TipoAtencion();
        entity.setId(1L);
        entity.setActivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete(1L).activo()).isFalse();
    }
}

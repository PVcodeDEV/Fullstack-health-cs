package com.clinica.maestro.service;

import com.clinica.maestro.dto.organizacion.AreaFuncionalRequest;
import com.clinica.maestro.entity.organizacion.AreaFuncional;
import com.clinica.maestro.repository.organizacion.AreaFuncionalRepository;
import com.clinica.maestro.service.organizacion.AreaFuncionalService;
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
class AreaFuncionalServiceTest {

    @Mock
    private AreaFuncionalRepository repository;

    @InjectMocks
    private AreaFuncionalService service;

    @Test
    void findAll_WithoutFilter_ShouldReturnAll() {
        var entity = new AreaFuncional();
        entity.setId(1);
        entity.setCodigo("ADM");
        entity.setNombre("Admisión");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll(null)).hasSize(1);
    }

    @Test
    void findAll_WithFilter_ShouldFilter() {
        when(repository.findByEsAreaFisica(true)).thenReturn(List.of(new AreaFuncional()));
        assertThat(service.findAll(true)).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new AreaFuncional();
        entity.setId(1);
        entity.setCodigo("ADM");
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1).codigo()).isEqualTo("ADM");
    }

    @Test
    void create_WithEsAreaFisica() {
        var request = new AreaFuncionalRequest("ADM", "Admisión", true);
        when(repository.existsByCodigo("ADM")).thenReturn(false);
        var saved = new AreaFuncional();
        saved.setId(1);
        saved.setCodigo("ADM");
        saved.setEsAreaFisica(true);
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigo()).isEqualTo("ADM");
    }

    @Test
    void update_ShouldModifyEsAreaFisica() {
        var existing = new AreaFuncional();
        existing.setId(1);
        existing.setCodigo("ADM");
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        var request = new AreaFuncionalRequest("FARM", "Farmacia", false);
        when(repository.existsByCodigo("FARM")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.update(1, request).codigo()).isEqualTo("FARM");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new AreaFuncional();
        entity.setId(1);
        entity.setActivo(true);
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete(1).activo()).isFalse();
    }
}

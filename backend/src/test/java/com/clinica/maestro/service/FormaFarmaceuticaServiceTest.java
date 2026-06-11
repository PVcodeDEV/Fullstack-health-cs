package com.clinica.maestro.service;

import com.clinica.maestro.dto.clinico.FormaFarmaceuticaRequest;
import com.clinica.maestro.entity.clinico.FormaFarmaceutica;
import com.clinica.maestro.repository.clinico.FormaFarmaceuticaRepository;
import com.clinica.maestro.service.clinico.FormaFarmaceuticaService;
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
class FormaFarmaceuticaServiceTest {

    @Mock
    private FormaFarmaceuticaRepository repository;

    @InjectMocks
    private FormaFarmaceuticaService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new FormaFarmaceutica();
        entity.setId(1L);
        entity.setCodigo("TAB");
        entity.setNombre("Tableta");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new FormaFarmaceutica();
        entity.setId(1L);
        entity.setCodigo("TAB");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1L).codigo()).isEqualTo("TAB");
    }

    @Test
    void create_ShouldSaveRequierePreparacion() {
        var request = new FormaFarmaceuticaRequest("INY", "Inyectable", true);
        when(repository.existsByCodigo("INY")).thenReturn(false);
        var saved = new FormaFarmaceutica();
        saved.setId(1L);
        saved.setCodigo("INY");
        saved.setRequierePreparacion(true);
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigo()).isEqualTo("INY");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new FormaFarmaceuticaRequest("TAB", "Tableta", false);
        when(repository.existsByCodigo("TAB")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_ShouldModifyRequierePreparacion() {
        var existing = new FormaFarmaceutica();
        existing.setId(1L);
        existing.setCodigo("TAB");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        var request = new FormaFarmaceuticaRequest("INY", "Inyectable", true);
        when(repository.existsByCodigo("INY")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.update(1L, request).codigo()).isEqualTo("INY");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new FormaFarmaceutica();
        entity.setId(1L);
        entity.setActivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete(1L).activo()).isFalse();
    }
}

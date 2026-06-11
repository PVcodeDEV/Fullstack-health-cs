package com.clinica.maestro.service;

import com.clinica.maestro.dto.clinico.EspecialidadMedicaRequest;
import com.clinica.maestro.entity.clinico.EspecialidadMedica;
import com.clinica.maestro.repository.clinico.EspecialidadMedicaRepository;
import com.clinica.maestro.service.clinico.EspecialidadMedicaService;
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
class EspecialidadMedicaServiceTest {

    @Mock
    private EspecialidadMedicaRepository repository;

    @InjectMocks
    private EspecialidadMedicaService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new EspecialidadMedica();
        entity.setId(1L);
        entity.setCodigo("MED");
        entity.setNombre("Medicina General");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new EspecialidadMedica();
        entity.setId(1L);
        entity.setCodigo("MED");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1L).codigo()).isEqualTo("MED");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveWithAbreviatura() {
        var request = new EspecialidadMedicaRequest("CAR", "Cardiología", "CARD");
        when(repository.existsByCodigo("CAR")).thenReturn(false);
        var saved = new EspecialidadMedica();
        saved.setId(1L);
        saved.setCodigo("CAR");
        saved.setAbreviatura("CARD");
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).abreviatura()).isEqualTo("CARD");
    }

    @Test
    void update_ShouldModifyAbreviatura() {
        var existing = new EspecialidadMedica();
        existing.setId(1L);
        existing.setCodigo("MED");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        var request = new EspecialidadMedicaRequest("CAR", "Cardiología", "CARD");
        when(repository.existsByCodigo("CAR")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.update(1L, request).abreviatura()).isEqualTo("CARD");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new EspecialidadMedica();
        entity.setId(1L);
        entity.setActivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete(1L).activo()).isFalse();
    }
}

package com.clinica.maestro.service;

import com.clinica.maestro.dto.clinico.TipoHabitacionRequest;
import com.clinica.maestro.entity.clinico.TipoHabitacion;
import com.clinica.maestro.repository.clinico.TipoHabitacionRepository;
import com.clinica.maestro.service.clinico.TipoHabitacionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoHabitacionServiceTest {

    @Mock
    private TipoHabitacionRepository repository;

    @InjectMocks
    private TipoHabitacionService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new TipoHabitacion();
        entity.setId(1L);
        entity.setCodigo("IND");
        entity.setNombre("Individual");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new TipoHabitacion();
        entity.setId(1L);
        entity.setCodigo("IND");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1L).codigo()).isEqualTo("IND");
    }

    @Test
    void create_ShouldSaveTarifaBase() {
        var request = new TipoHabitacionRequest("IND", "Individual", new BigDecimal("250.00"), 1);
        when(repository.existsByCodigo("IND")).thenReturn(false);
        var saved = new TipoHabitacion();
        saved.setId(1L);
        saved.setCodigo("IND");
        saved.setTarifaBase(new BigDecimal("250.00"));
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigo()).isEqualTo("IND");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new TipoHabitacion();
        entity.setId(1L);
        entity.setActivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete(1L).activo()).isFalse();
    }
}

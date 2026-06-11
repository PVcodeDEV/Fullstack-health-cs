package com.clinica.maestro.service;

import com.clinica.maestro.dto.clinico.TipoPacienteRequest;
import com.clinica.maestro.entity.clinico.TipoPaciente;
import com.clinica.maestro.repository.clinico.TipoPacienteRepository;
import com.clinica.maestro.service.clinico.TipoPacienteService;
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
class TipoPacienteServiceTest {

    @Mock
    private TipoPacienteRepository repository;

    @InjectMocks
    private TipoPacienteService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new TipoPaciente();
        entity.setId(1L);
        entity.setCodigo("INT");
        entity.setNombre("Interno");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));

        var result = service.findAll();
        assertThat(result).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new TipoPaciente();
        entity.setId(1L);
        entity.setCodigo("INT");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        var result = service.findById(1L);
        assertThat(result.codigo()).isEqualTo("INT");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveAndReturnResponse() {
        var request = new TipoPacienteRequest("EXT", "Externo");
        when(repository.existsByCodigo("EXT")).thenReturn(false);
        var saved = new TipoPaciente();
        saved.setId(1L);
        saved.setCodigo("EXT");
        saved.setNombre("Externo");
        when(repository.save(any())).thenReturn(saved);
        var result = service.create(request);
        assertThat(result.codigo()).isEqualTo("EXT");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new TipoPacienteRequest("INT", "Interno");
        when(repository.existsByCodigo("INT")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_ShouldModifyAndReturn() {
        var existing = new TipoPaciente();
        existing.setId(1L);
        existing.setCodigo("INT");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        var request = new TipoPacienteRequest("EXT", "Externo");
        when(repository.existsByCodigo("EXT")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var result = service.update(1L, request);
        assertThat(result.codigo()).isEqualTo("EXT");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new TipoPaciente();
        entity.setId(1L);
        entity.setActivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var result = service.softDelete(1L);
        assertThat(result.activo()).isFalse();
    }
}

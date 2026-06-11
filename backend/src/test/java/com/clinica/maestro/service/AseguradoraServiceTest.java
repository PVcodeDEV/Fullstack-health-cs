package com.clinica.maestro.service;

import com.clinica.maestro.dto.organizacion.AseguradoraRequest;
import com.clinica.maestro.entity.organizacion.Aseguradora;
import com.clinica.maestro.repository.organizacion.AseguradoraRepository;
import com.clinica.maestro.service.organizacion.AseguradoraService;
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
class AseguradoraServiceTest {

    @Mock
    private AseguradoraRepository repository;

    @InjectMocks
    private AseguradoraService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new Aseguradora();
        entity.setId(1);
        entity.setCodigo("ESS");
        entity.setNombre("Essalud");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new Aseguradora();
        entity.setId(1);
        entity.setCodigo("ESS");
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1).codigo()).isEqualTo("ESS");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveWithTipo() {
        var request = new AseguradoraRequest("ESS", "Essalud", "PUBLICO", true);
        when(repository.existsByCodigo("ESS")).thenReturn(false);
        var saved = new Aseguradora();
        saved.setId(1);
        saved.setCodigo("ESS");
        saved.setTipo("PUBLICO");
        when(repository.save(any())).thenReturn(saved);
        var result = service.create(request);
        assertThat(result.codigo()).isEqualTo("ESS");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new AseguradoraRequest("ESS", "Essalud", "PUBLICO", true);
        when(repository.existsByCodigo("ESS")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_ShouldModify() {
        var existing = new Aseguradora();
        existing.setId(1);
        existing.setCodigo("ESS");
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        var request = new AseguradoraRequest("RIM", "Rímac", "PRIVADO", true);
        when(repository.existsByCodigo("RIM")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.update(1, request).codigo()).isEqualTo("RIM");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new Aseguradora();
        entity.setId(1);
        entity.setActivo(true);
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete(1).activo()).isFalse();
    }
}

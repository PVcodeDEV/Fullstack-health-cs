package com.clinica.maestro.service;

import com.clinica.maestro.dto.ubigeo.UbigeoDepartamentoRequest;
import com.clinica.maestro.entity.ubigeo.UbigeoDepartamento;
import com.clinica.maestro.repository.ubigeo.UbigeoDepartamentoRepository;
import com.clinica.maestro.service.ubigeo.UbigeoDepartamentoService;
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
class UbigeoDepartamentoServiceTest {

    @Mock
    private UbigeoDepartamentoRepository repository;

    @InjectMocks
    private UbigeoDepartamentoService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new UbigeoDepartamento();
        entity.setCodigo("15");
        entity.setNombre("Lima");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new UbigeoDepartamento();
        entity.setCodigo("15");
        entity.setNombre("Lima");
        when(repository.findById("15")).thenReturn(Optional.of(entity));
        assertThat(service.findById("15").codigo()).isEqualTo("15");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById("99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById("99")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveAndReturn() {
        var request = new UbigeoDepartamentoRequest("15", "Lima");
        when(repository.existsById("15")).thenReturn(false);
        var saved = new UbigeoDepartamento();
        saved.setCodigo("15");
        saved.setNombre("Lima");
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigo()).isEqualTo("15");
    }

    @Test
    void create_ShouldThrowWhenExists() {
        var request = new UbigeoDepartamentoRequest("15", "Lima");
        when(repository.existsById("15")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_ShouldModify() {
        var existing = new UbigeoDepartamento();
        existing.setCodigo("15");
        existing.setNombre("Lima");
        when(repository.findById("15")).thenReturn(Optional.of(existing));
        var request = new UbigeoDepartamentoRequest("15", "Lima Metropolitana");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.update("15", request).nombre()).isEqualTo("Lima Metropolitana");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new UbigeoDepartamento();
        entity.setCodigo("15");
        entity.setActivo(true);
        when(repository.findById("15")).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete("15").activo()).isFalse();
    }
}

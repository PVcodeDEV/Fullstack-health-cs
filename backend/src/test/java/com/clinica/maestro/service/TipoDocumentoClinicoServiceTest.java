package com.clinica.maestro.service;

import com.clinica.maestro.dto.organizacion.TipoDocumentoClinicoRequest;
import com.clinica.maestro.entity.organizacion.TipoDocumentoClinico;
import com.clinica.maestro.repository.organizacion.TipoDocumentoClinicoRepository;
import com.clinica.maestro.service.organizacion.TipoDocumentoClinicoService;
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
class TipoDocumentoClinicoServiceTest {

    @Mock
    private TipoDocumentoClinicoRepository repository;

    @InjectMocks
    private TipoDocumentoClinicoService service;

    @Test
    void findAll_WithoutFilter_ShouldReturnAll() {
        var entity = new TipoDocumentoClinico();
        entity.setId(1);
        entity.setCodigo("HC");
        entity.setNombre("Historia Clínica");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll(null)).hasSize(1);
    }

    @Test
    void findAll_WithFilter_ShouldFilter() {
        when(repository.findByRequiereFirma(true)).thenReturn(List.of(new TipoDocumentoClinico()));
        assertThat(service.findAll(true)).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new TipoDocumentoClinico();
        entity.setId(1);
        entity.setCodigo("HC");
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1).codigo()).isEqualTo("HC");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveRequiereFirma() {
        var request = new TipoDocumentoClinicoRequest("HC", "Historia Clínica", true);
        when(repository.existsByCodigo("HC")).thenReturn(false);
        var saved = new TipoDocumentoClinico();
        saved.setId(1);
        saved.setCodigo("HC");
        saved.setRequiereFirma(true);
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigo()).isEqualTo("HC");
    }

    @Test
    void update_ShouldModify() {
        var existing = new TipoDocumentoClinico();
        existing.setId(1);
        existing.setCodigo("HC");
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        var request = new TipoDocumentoClinicoRequest("REC", "Receta", true);
        when(repository.existsByCodigo("REC")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.update(1, request).codigo()).isEqualTo("REC");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new TipoDocumentoClinico();
        entity.setId(1);
        entity.setActivo(true);
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete(1).activo()).isFalse();
    }
}

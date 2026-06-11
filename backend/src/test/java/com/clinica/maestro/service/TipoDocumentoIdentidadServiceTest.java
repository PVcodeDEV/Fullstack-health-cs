package com.clinica.maestro.service;

import com.clinica.maestro.dto.identidad.TipoDocumentoIdentidadRequest;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.maestro.service.identidad.TipoDocumentoIdentidadService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
class TipoDocumentoIdentidadServiceTest {

    @Mock
    private TipoDocumentoIdentidadRepository repository;

    @InjectMocks
    private TipoDocumentoIdentidadService service;

    @Captor
    private ArgumentCaptor<TipoDocumentoIdentidad> entityCaptor;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new TipoDocumentoIdentidad();
        entity.setId(1L);
        entity.setCodigoSunat("01");
        entity.setNombre("DNI");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));

        var result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).codigoSunat()).isEqualTo("01");
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new TipoDocumentoIdentidad();
        entity.setId(1L);
        entity.setCodigoSunat("01");
        entity.setNombre("DNI");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        var result = service.findById(1L);

        assertThat(result.codigoSunat()).isEqualTo("01");
        assertThat(result.nombre()).isEqualTo("DNI");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_ShouldSaveAndReturnResponse() {
        var request = new TipoDocumentoIdentidadRequest("99", "Test", 1, 15);
        when(repository.existsByCodigoSunat("99")).thenReturn(false);
        var saved = new TipoDocumentoIdentidad();
        saved.setId(1L);
        saved.setCodigoSunat("99");
        saved.setNombre("Test");
        saved.setLongitudMinima(1);
        saved.setLongitudMaxima(15);
        when(repository.save(any())).thenReturn(saved);

        var result = service.create(request);

        assertThat(result.codigoSunat()).isEqualTo("99");
        assertThat(result.nombre()).isEqualTo("Test");
        verify(repository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getCodigoSunat()).isEqualTo("99");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new TipoDocumentoIdentidadRequest("01", "Dup", 1, 15);
        when(repository.existsByCodigoSunat("01")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("01");
        verify(repository, never()).save(any());
    }

    @Test
    void update_ShouldModifyAndReturn() {
        var existing = new TipoDocumentoIdentidad();
        existing.setId(1L);
        existing.setCodigoSunat("01");
        existing.setNombre("DNI");
        existing.setLongitudMinima(8);
        existing.setLongitudMaxima(8);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        var request = new TipoDocumentoIdentidadRequest("99", "Changed", 1, 15);
        when(repository.existsByCodigoSunat("99")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.update(1L, request);

        assertThat(result.codigoSunat()).isEqualTo("99");
        assertThat(result.nombre()).isEqualTo("Changed");
    }

    @Test
    void update_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new TipoDocumentoIdentidadRequest("X", "X", 1, 1)))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new TipoDocumentoIdentidad();
        entity.setId(1L);
        entity.setActivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.softDelete(1L);

        assertThat(result.activo()).isFalse();
    }

    @Test
    void softDelete_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.softDelete(99L))
            .isInstanceOf(EntityNotFoundException.class);
    }
}

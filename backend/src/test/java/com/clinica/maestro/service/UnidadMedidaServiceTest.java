package com.clinica.maestro.service;

import com.clinica.maestro.dto.financiero.UnidadMedidaRequest;
import com.clinica.maestro.entity.financiero.UnidadMedida;
import com.clinica.maestro.repository.financiero.UnidadMedidaRepository;
import com.clinica.maestro.service.financiero.UnidadMedidaService;
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
class UnidadMedidaServiceTest {

    @Mock
    private UnidadMedidaRepository repository;

    @InjectMocks
    private UnidadMedidaService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new UnidadMedida();
        entity.setId(1);
        entity.setCodigoSunat("NIU");
        entity.setNombre("Unidad");
        when(repository.findAllByOrderByCodigoSunatAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new UnidadMedida();
        entity.setId(1);
        entity.setCodigoSunat("NIU");
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1).codigoSunat()).isEqualTo("NIU");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveAbreviatura() {
        var request = new UnidadMedidaRequest("KGM", "Kilogramo", "kg");
        when(repository.existsByCodigoSunat("KGM")).thenReturn(false);
        var saved = new UnidadMedida();
        saved.setId(2);
        saved.setCodigoSunat("KGM");
        saved.setAbreviatura("kg");
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).abreviatura()).isEqualTo("kg");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new UnidadMedidaRequest("NIU", "Unidad", "und");
        when(repository.existsByCodigoSunat("NIU")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new UnidadMedida();
        entity.setId(1);
        entity.setActivo(true);
        when(repository.findById(1)).thenReturn(Optional.of(entity));

        service.softDelete(1);

        verify(repository).save(entity);
        assertThat(entity.getActivo()).isFalse();
    }
}

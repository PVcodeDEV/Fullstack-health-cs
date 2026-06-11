package com.clinica.maestro.service;

import com.clinica.maestro.dto.financiero.TipoMonedaRequest;
import com.clinica.maestro.entity.financiero.TipoMoneda;
import com.clinica.maestro.repository.financiero.TipoMonedaRepository;
import com.clinica.maestro.service.financiero.TipoMonedaService;
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
class TipoMonedaServiceTest {

    @Mock
    private TipoMonedaRepository repository;

    @InjectMocks
    private TipoMonedaService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new TipoMoneda();
        entity.setId(1);
        entity.setCodigoSunat("PEN");
        entity.setNombre("Soles");
        when(repository.findAllByOrderByCodigoSunatAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new TipoMoneda();
        entity.setId(1);
        entity.setCodigoSunat("PEN");
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1).codigoSunat()).isEqualTo("PEN");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveSimbolo() {
        var request = new TipoMonedaRequest("USD", "Dólares", "$");
        when(repository.existsByCodigoSunat("USD")).thenReturn(false);
        var saved = new TipoMoneda();
        saved.setId(2);
        saved.setCodigoSunat("USD");
        saved.setSimbolo("$");
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).simbolo()).isEqualTo("$");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new TipoMonedaRequest("PEN", "Soles", "S/");
        when(repository.existsByCodigoSunat("PEN")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new TipoMoneda();
        entity.setId(1);
        entity.setActivo(true);
        when(repository.findById(1)).thenReturn(Optional.of(entity));

        service.softDelete(1);

        verify(repository).save(entity);
        assertThat(entity.getActivo()).isFalse();
    }
}

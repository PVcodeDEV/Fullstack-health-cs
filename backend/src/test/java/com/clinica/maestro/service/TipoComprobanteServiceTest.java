package com.clinica.maestro.service;

import com.clinica.maestro.dto.financiero.TipoComprobanteRequest;
import com.clinica.maestro.entity.financiero.TipoComprobante;
import com.clinica.maestro.repository.financiero.TipoComprobanteRepository;
import com.clinica.maestro.service.financiero.TipoComprobanteService;
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
class TipoComprobanteServiceTest {

    @Mock
    private TipoComprobanteRepository repository;

    @InjectMocks
    private TipoComprobanteService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new TipoComprobante();
        entity.setId(1);
        entity.setCodigoSunat("01");
        entity.setNombre("Factura");
        when(repository.findAllByOrderByCodigoSunatAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new TipoComprobante();
        entity.setId(1);
        entity.setCodigoSunat("01");
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1).codigoSunat()).isEqualTo("01");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveAndReturn() {
        var request = new TipoComprobanteRequest("02", "Recibo");
        when(repository.existsByCodigoSunat("02")).thenReturn(false);
        var saved = new TipoComprobante();
        saved.setId(2);
        saved.setCodigoSunat("02");
        saved.setNombre("Recibo");
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigoSunat()).isEqualTo("02");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new TipoComprobanteRequest("01", "Factura");
        when(repository.existsByCodigoSunat("01")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new TipoComprobante();
        entity.setId(1);
        entity.setActivo(true);
        when(repository.findById(1)).thenReturn(Optional.of(entity));

        service.softDelete(1);

        verify(repository).save(entity);
        assertThat(entity.getActivo()).isFalse();
    }
}

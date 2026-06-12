package com.clinica.caja.tipocambio.service;

import com.clinica.caja.tipocambio.dto.TipoCambioRequest;
import com.clinica.caja.tipocambio.dto.TipoCambioResponse;
import com.clinica.caja.tipocambio.entity.TipoCambio;
import com.clinica.caja.tipocambio.repository.TipoCambioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoCambioServiceTest {

    @Mock
    private TipoCambioRepository tipoCambioRepository;

    @Captor
    private ArgumentCaptor<TipoCambio> tipoCambioCaptor;

    private TipoCambioService service;

    @Test
    void create_ShouldPersistAndReturnResponse() {
        service = new TipoCambioService(tipoCambioRepository);

        var request = new TipoCambioRequest("USD", "PEN", new BigDecimal("3.75"), LocalDate.of(2026, 6, 12), 1L);

        when(tipoCambioRepository.save(any())).thenAnswer(invocation -> {
            TipoCambio entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        TipoCambioResponse response = service.create(request);

        assertThat(response.monedaOrigen()).isEqualTo("USD");
        assertThat(response.monedaDestino()).isEqualTo("PEN");
        assertThat(response.tipoCambio()).isEqualByComparingTo(new BigDecimal("3.75"));
        assertThat(response.fecha()).isEqualTo(LocalDate.of(2026, 6, 12));
        assertThat(response.usuarioId()).isEqualTo(1L);
    }

    @Test
    void create_ShouldUppercaseCurrencyCodes() {
        service = new TipoCambioService(tipoCambioRepository);

        var request = new TipoCambioRequest("usd", "pen", new BigDecimal("3.75"), LocalDate.of(2026, 6, 12), 1L);

        when(tipoCambioRepository.save(any())).thenAnswer(invocation -> {
            TipoCambio entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        service.create(request);

        verify(tipoCambioRepository).save(tipoCambioCaptor.capture());
        TipoCambio saved = tipoCambioCaptor.getValue();
        assertThat(saved.getMonedaOrigen()).isEqualTo("USD");
        assertThat(saved.getMonedaDestino()).isEqualTo("PEN");
    }

    @Test
    void list_ShouldReturnAllRates() {
        service = new TipoCambioService(tipoCambioRepository);

        TipoCambio rate1 = new TipoCambio();
        rate1.setId(1L);
        rate1.setMonedaOrigen("USD");
        rate1.setMonedaDestino("PEN");
        rate1.setTipoCambio(new BigDecimal("3.75"));
        rate1.setFecha(LocalDate.of(2026, 6, 12));
        rate1.setUsuarioId(1L);

        TipoCambio rate2 = new TipoCambio();
        rate2.setId(2L);
        rate2.setMonedaOrigen("USD");
        rate2.setMonedaDestino("PEN");
        rate2.setTipoCambio(new BigDecimal("3.76"));
        rate2.setFecha(LocalDate.of(2026, 6, 13));
        rate2.setUsuarioId(1L);

        when(tipoCambioRepository.findAll()).thenReturn(List.of(rate1, rate2));

        List<TipoCambioResponse> results = service.list();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).tipoCambio()).isEqualByComparingTo(new BigDecimal("3.75"));
        assertThat(results.get(1).tipoCambio()).isEqualByComparingTo(new BigDecimal("3.76"));
    }

    @Test
    void getLatest_WithExistingRate_ShouldReturnIt() {
        service = new TipoCambioService(tipoCambioRepository);

        TipoCambio latest = new TipoCambio();
        latest.setId(2L);
        latest.setMonedaOrigen("USD");
        latest.setMonedaDestino("PEN");
        latest.setTipoCambio(new BigDecimal("3.76"));
        latest.setFecha(LocalDate.of(2026, 6, 13));
        latest.setUsuarioId(1L);

        when(tipoCambioRepository.findLatestByMonedas("USD", "PEN"))
            .thenReturn(Optional.of(latest));

        TipoCambioResponse response = service.getLatest("USD", "PEN");

        assertThat(response.tipoCambio()).isEqualByComparingTo(new BigDecimal("3.76"));
        assertThat(response.fecha()).isEqualTo(LocalDate.of(2026, 6, 13));
    }

    @Test
    void getLatest_WithNoRate_ShouldThrow() {
        service = new TipoCambioService(tipoCambioRepository);

        when(tipoCambioRepository.findLatestByMonedas("EUR", "PEN"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLatest("EUR", "PEN"))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("EUR/PEN");
    }
}

package com.clinica.farmacia.caja.service;

import com.clinica.farmacia.caja.dto.SesionCajaAbrirRequest;
import com.clinica.farmacia.caja.dto.SesionCajaCerrarRequest;
import com.clinica.farmacia.caja.dto.SesionCajaResponse;
import com.clinica.farmacia.caja.entity.SesionCaja;
import com.clinica.farmacia.caja.repository.SesionCajaRepository;
import com.clinica.farmacia.caja.type.EstadoSesion;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SesionCajaServiceTest {

    @Mock
    private SesionCajaRepository repository;

    @Captor
    private ArgumentCaptor<SesionCaja> sesionCaptor;

    private SesionCajaService service;
    private static final Long USUARIO_ID = 42L;

    @BeforeEach
    void setUp() {
        service = new SesionCajaService(repository);
    }

    @Test
    void shouldAbrirSesion() {
        // SC-13: Open session → estado=ABIERTA
        SesionCajaAbrirRequest request = new SesionCajaAbrirRequest(
            1L, new BigDecimal("500.00"), "Apertura matutina");

        when(repository.existsByUsuarioIdAndEstado(USUARIO_ID, EstadoSesion.ABIERTA))
            .thenReturn(false);
        when(repository.save(any(SesionCaja.class))).thenAnswer(inv -> {
            SesionCaja s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SesionCajaResponse response = service.abrir(request, USUARIO_ID);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.usuarioId()).isEqualTo(USUARIO_ID);
        assertThat(response.almacenId()).isEqualTo(1L);
        assertThat(response.estado()).isEqualTo(EstadoSesion.ABIERTA);
        assertThat(response.montoApertura()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.fechaApertura()).isNotNull();

        verify(repository).save(sesionCaptor.capture());
        SesionCaja saved = sesionCaptor.getValue();
        assertThat(saved.getEstado()).isEqualTo(EstadoSesion.ABIERTA);
        assertThat(saved.getUsuarioId()).isEqualTo(USUARIO_ID);
    }

    @Test
    void shouldRejectAbrirWhenAlreadyOpen() {
        // User already has an open session → 409 / IllegalStateException
        SesionCajaAbrirRequest request = new SesionCajaAbrirRequest(
            1L, new BigDecimal("500.00"), null);

        when(repository.existsByUsuarioIdAndEstado(USUARIO_ID, EstadoSesion.ABIERTA))
            .thenReturn(true);

        assertThatThrownBy(() -> service.abrir(request, USUARIO_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ya tiene una sesión de caja abierta");
    }

    @Test
    void shouldCerrarSesion() {
        // SC-14: Close con montoCierre=510, totalVentas=0, diferencia=10
        SesionCaja sesion = new SesionCaja();
        sesion.setId(1L);
        sesion.setUsuarioId(USUARIO_ID);
        sesion.setEstado(EstadoSesion.ABIERTA);
        sesion.setMontoApertura(new BigDecimal("500.00"));
        sesion.setTotalVentas(new BigDecimal("0.00"));
        sesion.setFechaApertura(LocalDateTime.now());

        SesionCajaCerrarRequest request = new SesionCajaCerrarRequest(
            new BigDecimal("510.00"), "Cierre sin ventas");

        when(repository.findById(1L)).thenReturn(Optional.of(sesion));
        when(repository.save(any(SesionCaja.class))).thenAnswer(inv -> inv.getArgument(0));

        SesionCajaResponse response = service.cerrar(1L, request);

        assertThat(response.estado()).isEqualTo(EstadoSesion.CERRADA);
        assertThat(response.montoCierreReal()).isEqualByComparingTo(new BigDecimal("510.00"));
        assertThat(response.montoCierreEsperado()).isEqualByComparingTo(new BigDecimal("500.00"));
        // diferencia = 510 - 500 = 10
        assertThat(response.diferenciaCierre()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(response.fechaCierre()).isNotNull();
    }

    @Test
    void shouldRejectCerrarWhenNotAbierta() {
        SesionCaja sesion = new SesionCaja();
        sesion.setId(1L);
        sesion.setEstado(EstadoSesion.CERRADA);
        sesion.setMontoApertura(BigDecimal.ZERO);

        SesionCajaCerrarRequest request = new SesionCajaCerrarRequest(
            new BigDecimal("0.00"), null);

        when(repository.findById(1L)).thenReturn(Optional.of(sesion));

        assertThatThrownBy(() -> service.cerrar(1L, request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no está abierta");
    }

    @Test
    void shouldListByEstado() {
        SesionCaja s1 = new SesionCaja();
        s1.setId(1L);
        s1.setEstado(EstadoSesion.ABIERTA);
        s1.setMontoApertura(BigDecimal.ZERO);
        s1.setTotalVentas(BigDecimal.ZERO);
        s1.setFechaApertura(LocalDateTime.now());

        SesionCaja s2 = new SesionCaja();
        s2.setId(2L);
        s2.setEstado(EstadoSesion.ABIERTA);
        s2.setMontoApertura(BigDecimal.ZERO);
        s2.setTotalVentas(BigDecimal.ZERO);
        s2.setFechaApertura(LocalDateTime.now());

        when(repository.findByEstadoOrderByFechaAperturaDesc(EstadoSesion.ABIERTA))
            .thenReturn(List.of(s1, s2));

        List<SesionCajaResponse> results = service.listByEstado(EstadoSesion.ABIERTA);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).id()).isEqualTo(1L);
        assertThat(results.get(1).id()).isEqualTo(2L);
    }

    @Test
    void shouldRejectMontoAperturaNegativo() {
        SesionCajaAbrirRequest request = new SesionCajaAbrirRequest(
            1L, new BigDecimal("-100.00"), null);

        assertThatThrownBy(() -> service.abrir(request, USUARIO_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser negativo");
    }
}

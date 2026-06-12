package com.clinica.caja.sesion.service;

import com.clinica.caja.sesion.dto.SesionCajaCerrarRequest;
import com.clinica.caja.sesion.dto.SesionCajaRequest;
import com.clinica.caja.sesion.dto.SesionCajaResponse;
import com.clinica.caja.sesion.entity.SesionCaja;
import com.clinica.caja.sesion.entity.SesionCaja.Estado;
import com.clinica.caja.sesion.repository.ClinicaSesionCajaRepository;
import com.clinica.config.CajaSesionProperties;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SesionCajaServiceTest {

    @Mock
    private ClinicaSesionCajaRepository sesionCajaRepository;

    private SesionCajaService service;

    @Captor
    private ArgumentCaptor<SesionCaja> sessionCaptor;

    private static final Long USUARIO_ID = 1L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 12, 10, 0, 0);
    private static final BigDecimal TOLERANCIA = new BigDecimal("1.00");

    @BeforeEach
    void setUp() {
        var properties = new CajaSesionProperties(TOLERANCIA);
        service = new SesionCajaService(sesionCajaRepository, properties);
    }

    // ============================================================
    // SES-001-2: Double-open rejection
    // ============================================================

    @Test
    void abrirSesion_WithExistingOpenSession_ShouldThrow() {
        // GIVEN the user already has an open session
        when(sesionCajaRepository.existsByUsuarioAperturaIdAndEstadoAndActivoTrue(USUARIO_ID, Estado.ABIERTA))
            .thenReturn(true);

        var request = new SesionCajaRequest(new BigDecimal("500.00"));

        // WHEN / THEN
        assertThatThrownBy(() -> service.abrirSesion(request, USUARIO_ID, NOW))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("sesión abierta");

        verify(sesionCajaRepository, never()).save(any());
    }

    @Test
    void abrirSesion_WithNoOpenSession_ShouldCreateSession() {
        // GIVEN no open session exists
        when(sesionCajaRepository.existsByUsuarioAperturaIdAndEstadoAndActivoTrue(USUARIO_ID, Estado.ABIERTA))
            .thenReturn(false);
        when(sesionCajaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new SesionCajaRequest(new BigDecimal("500.00"));

        // WHEN
        SesionCajaResponse response = service.abrirSesion(request, USUARIO_ID, NOW);

        // THEN
        assertThat(response.montoApertura()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.estado()).isEqualTo("ABIERTA");
        assertThat(response.fechaApertura()).isEqualTo(NOW);
        assertThat(response.usuarioAperturaId()).isEqualTo(USUARIO_ID);
        assertThat(response.codigo()).startsWith("SES-");

        verify(sesionCajaRepository).save(sessionCaptor.capture());
        SesionCaja saved = sessionCaptor.getValue();
        assertThat(saved.getEstado()).isEqualTo(Estado.ABIERTA);
        assertThat(saved.getMontoApertura()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(saved.getFechaApertura()).isEqualTo(NOW);
        assertThat(saved.getTotalVentas()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ============================================================
    // SES-002-1: Close with matching amounts (diferencia = 0)
    // ============================================================

    @Test
    void cerrarSesion_WithMatchingAmounts_ShouldCloseWithZeroDiferencia() {
        // GIVEN a session with montoApertura=500.00, totalVentas=1200.00
        SesionCaja session = createOpenSession(500.00, 1200.00);
        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sesionCajaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN cerrar with montoCierre=1700.00
        var request = new SesionCajaCerrarRequest(new BigDecimal("1700.00"));
        SesionCajaResponse response = service.cerrarSesion(1L, request, USUARIO_ID, NOW);

        // THEN
        assertThat(response.estado()).isEqualTo("CERRADA");
        assertThat(response.diferencia()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.discrepanciaWarning()).isFalse();
        assertThat(response.montoCierre()).isEqualByComparingTo(new BigDecimal("1700.00"));
        assertThat(response.fechaCierre()).isEqualTo(NOW);
    }

    // ============================================================
    // SES-002-2: Close with small discrepancy (within tolerance)
    // ============================================================

    @Test
    void cerrarSesion_WithSmallDiscrepancy_ShouldCloseWithoutWarning() {
        // GIVEN montoApertura=500.00, totalVentas=1200.00
        SesionCaja session = createOpenSession(500.00, 1200.00);
        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sesionCajaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN cerrar with montoCierre=1700.50 (diferencia=0.50, within 1.00 tolerance)
        var request = new SesionCajaCerrarRequest(new BigDecimal("1700.50"));
        SesionCajaResponse response = service.cerrarSesion(1L, request, USUARIO_ID, NOW);

        // THEN
        assertThat(response.estado()).isEqualTo("CERRADA");
        assertThat(response.diferencia()).isEqualByComparingTo(new BigDecimal("0.50"));
        assertThat(response.discrepanciaWarning()).isFalse();
    }

    // ============================================================
    // SES-002-3: Close with significant discrepancy (outside tolerance)
    // ============================================================

    @Test
    void cerrarSesion_WithSignificantDiscrepancy_ShouldCloseWithWarning() {
        // GIVEN montoApertura=500.00, totalVentas=1200.00
        SesionCaja session = createOpenSession(500.00, 1200.00);
        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sesionCajaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN cerrar with montoCierre=1690.00 (diferencia=-10.00, outside 1.00 tolerance)
        var request = new SesionCajaCerrarRequest(new BigDecimal("1690.00"));
        SesionCajaResponse response = service.cerrarSesion(1L, request, USUARIO_ID, NOW);

        // THEN
        assertThat(response.estado()).isEqualTo("CERRADA");
        assertThat(response.diferencia()).isEqualByComparingTo(new BigDecimal("-10.00"));
        assertThat(response.discrepanciaWarning()).isTrue();
    }

    // ============================================================
    // SES-002-4: Close already closed session
    // ============================================================

    @Test
    void cerrarSesion_WithClosedSession_ShouldThrow() {
        // GIVEN a closed session
        SesionCaja session = createOpenSession(500.00, 0.00);
        session.cerrar(new BigDecimal("500.00"), USUARIO_ID, NOW);
        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(session));

        // WHEN / THEN
        var request = new SesionCajaCerrarRequest(new BigDecimal("500.00"));
        assertThatThrownBy(() -> service.cerrarSesion(1L, request, USUARIO_ID, NOW))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ya está cerrada");
    }

    // ============================================================
    // SES-003-1: Get current open session
    // ============================================================

    @Test
    void getSessionActual_WithOpenSession_ShouldReturnSession() {
        // GIVEN the user has an open session
        SesionCaja session = createOpenSession(500.00, 0.00);
        when(sesionCajaRepository.findByUsuarioAperturaIdAndEstadoAndActivoTrue(USUARIO_ID, Estado.ABIERTA))
            .thenReturn(Optional.of(session));

        // WHEN
        SesionCajaResponse response = service.getSessionActual(USUARIO_ID);

        // THEN
        assertThat(response.estado()).isEqualTo("ABIERTA");
        assertThat(response.montoApertura()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    // ============================================================
    // SES-003-2: No open session
    // ============================================================

    @Test
    void getSessionActual_WithNoOpenSession_ShouldThrow() {
        // GIVEN the user has no open session
        when(sesionCajaRepository.findByUsuarioAperturaIdAndEstadoAndActivoTrue(USUARIO_ID, Estado.ABIERTA))
            .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> service.getSessionActual(USUARIO_ID))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("sesión abierta");
    }

    // ============================================================
    // SES-004-1: Payment-session link (service method)
    // ============================================================

    @Test
    void tieneSesionAbierta_WithOpenSession_ShouldReturnTrue() {
        // GIVEN the user has an open session
        when(sesionCajaRepository.existsByUsuarioAperturaIdAndEstadoAndActivoTrue(USUARIO_ID, Estado.ABIERTA))
            .thenReturn(true);

        // WHEN
        boolean result = service.tieneSesionAbierta(USUARIO_ID);

        // THEN
        assertThat(result).isTrue();
    }

    // ============================================================
    // SES-004-2: No open session for payment
    // ============================================================

    @Test
    void tieneSesionAbierta_WithNoOpenSession_ShouldReturnFalse() {
        // GIVEN the user has no open session
        when(sesionCajaRepository.existsByUsuarioAperturaIdAndEstadoAndActivoTrue(USUARIO_ID, Estado.ABIERTA))
            .thenReturn(false);

        // WHEN
        boolean result = service.tieneSesionAbierta(USUARIO_ID);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    void getOpenSessionEntity_WithOpenSession_ShouldReturnEntity() {
        // GIVEN the user has an open session
        SesionCaja session = createOpenSession(500.00, 0.00);
        when(sesionCajaRepository.findByUsuarioAperturaIdAndEstadoAndActivoTrue(USUARIO_ID, Estado.ABIERTA))
            .thenReturn(Optional.of(session));

        // WHEN
        SesionCaja result = service.getOpenSessionEntity(USUARIO_ID);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getEstado()).isEqualTo(Estado.ABIERTA);
    }

    @Test
    void getOpenSessionEntity_WithNoOpenSession_ShouldReturnNull() {
        // GIVEN the user has no open session
        when(sesionCajaRepository.findByUsuarioAperturaIdAndEstadoAndActivoTrue(USUARIO_ID, Estado.ABIERTA))
            .thenReturn(Optional.empty());

        // WHEN
        SesionCaja result = service.getOpenSessionEntity(USUARIO_ID);

        // THEN
        assertThat(result).isNull();
    }

    // ============================================================
    // Helpers
    // ============================================================

    private SesionCaja createOpenSession(double montoApertura, double totalVentas) {
        SesionCaja session = new SesionCaja();
        session.setId(1L);
        session.setCodigo("SES-TEST-0001");
        session.setUsuarioAperturaId(USUARIO_ID);
        session.setFechaApertura(NOW);
        session.setMontoApertura(new BigDecimal(String.valueOf(montoApertura)));
        session.setEstado(Estado.ABIERTA);
        session.setTotalVentas(new BigDecimal(String.valueOf(totalVentas)));
        return session;
    }
}

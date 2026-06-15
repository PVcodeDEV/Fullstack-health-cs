package com.clinica.caja.liquidacion.service;

import com.clinica.caja.liquidacion.dto.*;
import com.clinica.caja.liquidacion.entity.Liquidacion;
import com.clinica.caja.liquidacion.entity.PaymentLeg;
import com.clinica.caja.liquidacion.repository.LiquidacionRepository;
import com.clinica.caja.liquidacion.repository.PaymentLegRepository;
import com.clinica.caja.sesion.entity.SesionCaja;
import com.clinica.caja.sesion.service.SesionCajaService;
import com.clinica.caja.tipocambio.entity.TipoCambio;
import com.clinica.caja.tipocambio.repository.TipoCambioRepository;
import com.clinica.clinica.admision.entity.Cuenta;
import com.clinica.clinica.cuenta.dto.CargoAdicionalResponse;
import com.clinica.clinica.cuenta.service.CuentaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiquidacionServiceTest {

    @Mock
    private LiquidacionRepository liquidacionRepository;
    @Mock
    private PaymentLegRepository paymentLegRepository;
    @Mock
    private CuentaService cuentaService;
    @Mock
    private SesionCajaService sesionCajaService;
    @Mock
    private TipoCambioRepository tipoCambioRepository;

    private DescuentoValidator descuentoValidator;
    private LiquidacionService service;

    @Captor
    private ArgumentCaptor<Liquidacion> liquidacionCaptor;

    private static final Long USUARIO_ID = 1L;
    private static final Long CUENTA_ID = 100L;
    private static final Long SESION_ID = 50L;
    private static final Long TIPO_CAMBIO_ID = 10L;

    @BeforeEach
    void setUp() {
        descuentoValidator = new DescuentoValidator();
        service = new LiquidacionService(
            liquidacionRepository, paymentLegRepository,
            cuentaService, sesionCajaService,
            tipoCambioRepository, descuentoValidator);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private Cuenta createCuenta(String estado, BigDecimal totalCargos) {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(CUENTA_ID);
        cuenta.setPacienteId(1L);
        cuenta.setEstado(estado);
        cuenta.setTotalCargos(totalCargos);
        cuenta.setFechaApertura(LocalDateTime.now());
        return cuenta;
    }

    private SesionCaja createOpenSession() {
        SesionCaja session = new SesionCaja();
        session.setId(SESION_ID);
        session.setTotalVentas(BigDecimal.ZERO);
        session.setEstado(SesionCaja.Estado.ABIERTA);
        return session;
    }

    private TipoCambio createTipoCambio() {
        TipoCambio tc = new TipoCambio();
        tc.setId(TIPO_CAMBIO_ID);
        tc.setMonedaOrigen("USD");
        tc.setMonedaDestino("PEN");
        tc.setTipoCambio(new BigDecimal("3.75"));
        tc.setFecha(LocalDate.now());
        tc.setUsuarioId(USUARIO_ID);
        return tc;
    }

    private List<CargoAdicionalResponse> createCargos() {
        return List.of(
            new CargoAdicionalResponse(1L, CUENTA_ID, "Cirugía",
                new BigDecimal("3000.00"), "PROCEDIMIENTO", LocalDateTime.now(), true),
            new CargoAdicionalResponse(2L, CUENTA_ID, "Honorarios",
                new BigDecimal("1500.00"), "HONORARIOS", LocalDateTime.now(), true),
            new CargoAdicionalResponse(3L, CUENTA_ID, "Días cama",
                new BigDecimal("2700.00"), "HOSPITALARIO", LocalDateTime.now(), true)
        );
    }

    private void mockLiquidacionSave() {
        when(liquidacionRepository.save(any())).thenAnswer(invocation -> {
            Liquidacion liq = invocation.getArgument(0);
            liq.setId(1L);
            return liq;
        });
    }

    private void mockPaymentLegSave() {
        when(paymentLegRepository.save(any())).thenAnswer(invocation -> {
            PaymentLeg leg = invocation.getArgument(0);
            leg.setId((long) (Math.random() * 1000));
            return leg;
        });
    }

    // ============================================================
    // LIQ-002-1: Pre-liquidación generation
    // ============================================================

    @Test
    void preLiquidar_WithPendingCuenta_ShouldReturnPreview() {
        // GIVEN Cuenta CTA-001 has 3 cargos and estado PENDIENTE_COBRO
        Cuenta cuenta = createCuenta("PENDIENTE_COBRO", new BigDecimal("7200.00"));
        when(cuentaService.obtenerCuenta(CUENTA_ID)).thenReturn(cuenta);
        when(cuentaService.listarCargos(CUENTA_ID)).thenReturn(createCargos());

        // WHEN
        PreLiquidacionResponse response = service.preLiquidar(CUENTA_ID);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.cuentaId()).isEqualTo(CUENTA_ID);
        assertThat(response.items()).hasSize(3);
        assertThat(response.estado()).isEqualTo("PREVIEW");
        // subtotal = 3000 + 1500 + 2700 = 7200
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("7200.00"));
        // IGV = 7200 * 0.18 = 1296
        assertThat(response.igv()).isEqualByComparingTo(new BigDecimal("1296.00"));
        // total = 7200 + 1296 = 8496
        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("8496.00"));
    }

    // ============================================================
    // LIQ-002-2: Pre-liquidación for non-pending cuenta
    // ============================================================

    @Test
    void preLiquidar_WithNonPendingCuenta_ShouldThrow() {
        // GIVEN Cuenta CTA-002 has estado ABIERTO
        Cuenta cuenta = createCuenta("ABIERTO", new BigDecimal("500.00"));
        when(cuentaService.obtenerCuenta(CUENTA_ID)).thenReturn(cuenta);

        // WHEN/THEN
        assertThatThrownBy(() -> service.preLiquidar(CUENTA_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("pendiente de cobro");
    }

    // ============================================================
    // LIQ-001-2: Multi-method payment
    // ============================================================

    @Test
    void pagar_WithSplitPayment_ShouldAccept() {
        // GIVEN Cuenta with total 500.00
        Cuenta cuenta = createCuenta("PENDIENTE_COBRO", new BigDecimal("500.00"));
        when(cuentaService.obtenerCuenta(CUENTA_ID)).thenReturn(cuenta);
        when(sesionCajaService.getOpenSessionEntity(USUARIO_ID)).thenReturn(createOpenSession());
        mockLiquidacionSave();
        mockPaymentLegSave();
        doNothing().when(cuentaService).confirmarCobro(CUENTA_ID);

        var request = new PagoRequest(
            "PEN", null, BigDecimal.ZERO, null,
            List.of(
                new PagoLegRequest("EFECTIVO", new BigDecimal("200.00"), null),
                new PagoLegRequest("POS", new BigDecimal("300.00"), "VOUCHER-12345")
            )
        );

        // WHEN
        LiquidacionResponse response = service.pagar(CUENTA_ID, request, USUARIO_ID);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.estado()).isEqualTo("PAGADO");
        assertThat(response.montoTotal()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.pagos()).hasSize(2);

        verify(cuentaService).confirmarCobro(CUENTA_ID);
    }

    // ============================================================
    // LIQ-001-3: Split payment sum mismatch
    // ============================================================

    @Test
    void pagar_WithSumMismatch_ShouldThrow() {
        // GIVEN Cuenta with total 500.00
        Cuenta cuenta = createCuenta("PENDIENTE_COBRO", new BigDecimal("500.00"));
        when(cuentaService.obtenerCuenta(CUENTA_ID)).thenReturn(cuenta);
        when(sesionCajaService.getOpenSessionEntity(USUARIO_ID)).thenReturn(createOpenSession());

        // WHEN pagos sum = 400 != 500
        var request = new PagoRequest(
            "PEN", null, BigDecimal.ZERO, null,
            List.of(
                new PagoLegRequest("EFECTIVO", new BigDecimal("200.00"), null),
                new PagoLegRequest("POS", new BigDecimal("200.00"), "VOUCHER-123")
            )
        );

        // THEN
        assertThatThrownBy(() -> service.pagar(CUENTA_ID, request, USUARIO_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no coincide");
    }

    // ============================================================
    // LIQ-001-4: Referencia required for POS
    // ============================================================

    @Test
    void pagar_WithPosNoReferencia_ShouldThrow() {
        // GIVEN Cuenta with total 500.00
        Cuenta cuenta = createCuenta("PENDIENTE_COBRO", new BigDecimal("500.00"));
        when(cuentaService.obtenerCuenta(CUENTA_ID)).thenReturn(cuenta);
        when(sesionCajaService.getOpenSessionEntity(USUARIO_ID)).thenReturn(createOpenSession());

        var request = new PagoRequest(
            "PEN", null, BigDecimal.ZERO, null,
            List.of(
                new PagoLegRequest("POS", new BigDecimal("500.00"), null)
            )
        );

        // THEN
        assertThatThrownBy(() -> service.pagar(CUENTA_ID, request, USUARIO_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("referencia");
    }

    // ============================================================
    // LIQ-005-2: USD transaction with TipoCambio
    // ============================================================

    @Test
    void pagar_WithUsdAndTipoCambio_ShouldRecordConversion() {
        // GIVEN Cuenta with total 1000.00
        Cuenta cuenta = createCuenta("PENDIENTE_COBRO", new BigDecimal("1000.00"));
        when(cuentaService.obtenerCuenta(CUENTA_ID)).thenReturn(cuenta);
        when(sesionCajaService.getOpenSessionEntity(USUARIO_ID)).thenReturn(createOpenSession());
        when(tipoCambioRepository.findById(TIPO_CAMBIO_ID)).thenReturn(Optional.of(createTipoCambio()));
        mockLiquidacionSave();
        mockPaymentLegSave();
        doNothing().when(cuentaService).confirmarCobro(CUENTA_ID);

        var request = new PagoRequest(
            "USD", TIPO_CAMBIO_ID, BigDecimal.ZERO, null,
            List.of(
                new PagoLegRequest("EFECTIVO", new BigDecimal("1000.00"), null)
            )
        );

        // WHEN
        LiquidacionResponse response = service.pagar(CUENTA_ID, request, USUARIO_ID);

        // THEN
        assertThat(response.moneda()).isEqualTo("USD");
        assertThat(response.tipoCambioId()).isEqualTo(TIPO_CAMBIO_ID);
        assertThat(response.montoUSD()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.montoPEN()).isEqualByComparingTo(new BigDecimal("3750.00")); // 1000 * 3.75
    }

    // ============================================================
    // LIQ-005-3: USD transaction without TipoCambio
    // ============================================================

    @Test
    void pagar_WithUsdNoTipoCambio_ShouldThrow() {
        // GIVEN Cuenta with total 1000.00
        Cuenta cuenta = createCuenta("PENDIENTE_COBRO", new BigDecimal("1000.00"));
        when(cuentaService.obtenerCuenta(CUENTA_ID)).thenReturn(cuenta);
        when(sesionCajaService.getOpenSessionEntity(USUARIO_ID)).thenReturn(createOpenSession());

        var request = new PagoRequest(
            "USD", null, BigDecimal.ZERO, null,
            List.of(
                new PagoLegRequest("EFECTIVO", new BigDecimal("1000.00"), null)
            )
        );

        // THEN
        assertThatThrownBy(() -> service.pagar(CUENTA_ID, request, USUARIO_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("tipo de cambio");
    }

    // ============================================================
    // Non-pending Cuenta rejection
    // ============================================================

    @Test
    void pagar_WithNonPendingCuenta_ShouldThrow() {
        Cuenta cuenta = createCuenta("ABIERTO", new BigDecimal("500.00"));
        when(cuentaService.obtenerCuenta(CUENTA_ID)).thenReturn(cuenta);

        var request = new PagoRequest(
            "PEN", null, BigDecimal.ZERO, null,
            List.of(new PagoLegRequest("EFECTIVO", new BigDecimal("500.00"), null))
        );

        assertThatThrownBy(() -> service.pagar(CUENTA_ID, request, USUARIO_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("pendiente de cobro");
    }

    // ============================================================
    // No open session rejection
    // ============================================================

    @Test
    void pagar_WithNoOpenSession_ShouldThrow() {
        Cuenta cuenta = createCuenta("PENDIENTE_COBRO", new BigDecimal("500.00"));
        when(cuentaService.obtenerCuenta(CUENTA_ID)).thenReturn(cuenta);
        when(sesionCajaService.getOpenSessionEntity(USUARIO_ID)).thenReturn(null);

        var request = new PagoRequest(
            "PEN", null, BigDecimal.ZERO, null,
            List.of(new PagoLegRequest("EFECTIVO", new BigDecimal("500.00"), null))
        );

        assertThatThrownBy(() -> service.pagar(CUENTA_ID, request, USUARIO_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("sesión de caja");
    }
}

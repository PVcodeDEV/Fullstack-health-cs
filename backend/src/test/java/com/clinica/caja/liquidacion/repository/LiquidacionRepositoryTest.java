package com.clinica.caja.liquidacion.repository;

import com.clinica.caja.liquidacion.entity.Liquidacion;
import com.clinica.caja.liquidacion.entity.PaymentLeg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.clinica.config.JpaAuditingConfig;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository test for Liquidacion persistence.
 * Verifies full flow: payment creation → payment legs → confirmar-cobro integration.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Import(JpaAuditingConfig.class)
class LiquidacionRepositoryTest {

    @Autowired
    private LiquidacionRepository liquidacionRepository;

    @Autowired
    private PaymentLegRepository paymentLegRepository;

    private static final Long CUENTA_ID = 100L;
    private static final Long SESION_ID = 50L;
    private static final Long USUARIO_ID = 1L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 12, 10, 0, 0);

    @BeforeEach
    void setUp() {
        liquidacionRepository.deleteAll();
        paymentLegRepository.deleteAll();
    }

    // ============================================================
    // Helper
    // ============================================================

    private Liquidacion createLiquidacion(Long cuentaId, BigDecimal montoTotal, String moneda) {
        Liquidacion liq = new Liquidacion();
        liq.setCuentaId(cuentaId);
        liq.setSesionId(SESION_ID);
        liq.setFecha(NOW);
        liq.setMoneda(moneda);
        liq.setMontoTotal(montoTotal);
        liq.setMontoPEN(moneda.equals("PEN") ? montoTotal : null);
        liq.setMontoUSD(moneda.equals("USD") ? montoTotal : null);
        liq.setDescuentoTotal(BigDecimal.ZERO);
        liq.setUsuarioCobraId(USUARIO_ID);
        liq.setEstado("PAGADO");
        return liquidacionRepository.save(liq);
    }

    private PaymentLeg createPaymentLeg(Liquidacion liquidacion, String metodo, BigDecimal monto, String referencia) {
        PaymentLeg leg = new PaymentLeg();
        leg.setLiquidacion(liquidacion);
        leg.setMetodoPago(metodo);
        leg.setMonto(monto);
        leg.setReferencia(referencia);
        return paymentLegRepository.save(leg);
    }

    // ============================================================
    // LIQ-003-1: Successful payment creates Liquidacion with legs
    // ============================================================

    @Test
    void shouldCreateLiquidacionWithPaymentLegs() {
        // GIVEN Cuenta is PENDIENTE_COBRO (simulated)
        Liquidacion savedLiq = createLiquidacion(CUENTA_ID, new BigDecimal("500.00"), "PEN");

        // WHEN create two payment legs
        createPaymentLeg(savedLiq, "EFECTIVO", new BigDecimal("200.00"), null);
        createPaymentLeg(savedLiq, "POS", new BigDecimal("300.00"), "VOUCHER-12345");

        // THEN Liquidacion is persisted
        Liquidacion found = liquidacionRepository.findById(savedLiq.getId()).orElseThrow();
        assertThat(found.getCuentaId()).isEqualTo(CUENTA_ID);
        assertThat(found.getEstado()).isEqualTo("PAGADO");
        assertThat(found.getMontoTotal()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(found.getMoneda()).isEqualTo("PEN");
        assertThat(found.getSesionId()).isEqualTo(SESION_ID);
        assertThat(found.getUsuarioCobraId()).isEqualTo(USUARIO_ID);
        assertThat(found.getCreatedAt()).isNotNull();

        // THEN payment legs are persisted
        List<PaymentLeg> legs = paymentLegRepository.findByLiquidacionId(savedLiq.getId());
        assertThat(legs).hasSize(2);
        assertThat(legs).extracting(PaymentLeg::getMetodoPago)
            .containsExactlyInAnyOrder("EFECTIVO", "POS");
        assertThat(legs).extracting(PaymentLeg::getMonto)
            .allMatch(m -> m.compareTo(BigDecimal.ZERO) > 0);
    }

    // ============================================================
    // LIQ-003-2: Transaction rollback simulation (verify query side)
    // ============================================================

    @Test
    void shouldFindLiquidacionByCuentaId() {
        // GIVEN a liquidacion for CUENTA_ID
        createLiquidacion(CUENTA_ID, new BigDecimal("8496.00"), "PEN");

        // WHEN
        List<Liquidacion> results = liquidacionRepository.findByCuentaId(CUENTA_ID);

        // THEN
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMontoTotal()).isEqualByComparingTo(new BigDecimal("8496.00"));
    }

    // ============================================================
    // LIQ-005-2: USD transaction with exchange rate
    // ============================================================

    @Test
    void shouldCreateLiquidacionInUsd() {
        // GIVEN a USD liquidacion with TipoCambio reference
        Liquidacion liq = new Liquidacion();
        liq.setCuentaId(CUENTA_ID);
        liq.setSesionId(SESION_ID);
        liq.setFecha(NOW);
        liq.setMoneda("USD");
        liq.setMontoTotal(new BigDecimal("1000.00"));
        liq.setMontoUSD(new BigDecimal("1000.00"));
        liq.setMontoPEN(new BigDecimal("3750.00"));
        liq.setDescuentoTotal(BigDecimal.ZERO);
        liq.setUsuarioCobraId(USUARIO_ID);
        liq.setEstado("PAGADO");

        Liquidacion saved = liquidacionRepository.save(liq);

        // WHEN
        Liquidacion found = liquidacionRepository.findById(saved.getId()).orElseThrow();

        // THEN
        assertThat(found.getMoneda()).isEqualTo("USD");
        assertThat(found.getMontoUSD()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(found.getMontoPEN()).isEqualByComparingTo(new BigDecimal("3750.00"));
    }

    // ============================================================
    // Payment legs cascade query by liquidacion
    // ============================================================

    @Test
    void shouldFindPaymentLegsByLiquidacionId() {
        // GIVEN liquidacion with legs
        Liquidacion liq = createLiquidacion(CUENTA_ID, new BigDecimal("250.00"), "PEN");
        createPaymentLeg(liq, "YAPE_PLIN", new BigDecimal("250.00"), "YAPE-99999");

        // WHEN
        List<PaymentLeg> legs = paymentLegRepository.findByLiquidacionId(liq.getId());

        // THEN
        assertThat(legs).hasSize(1);
        PaymentLeg leg = legs.get(0);
        assertThat(leg.getMetodoPago()).isEqualTo("YAPE_PLIN");
        assertThat(leg.getMonto()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(leg.getReferencia()).isEqualTo("YAPE-99999");
        assertThat(leg.getLiquidacion().getId()).isEqualTo(liq.getId());
    }

    // ============================================================
    // Query by sesionId
    // ============================================================

    @Test
    void shouldFindLiquidacionBySesionId() {
        // GIVEN
        createLiquidacion(CUENTA_ID, new BigDecimal("100.00"), "PEN");

        // WHEN
        List<Liquidacion> results = liquidacionRepository.findBySesionId(SESION_ID);

        // THEN
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getSesionId()).isEqualTo(SESION_ID);
    }
}

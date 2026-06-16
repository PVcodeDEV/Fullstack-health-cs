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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles pre-liquidación generation and payment processing.
 */
@Service
@Transactional
public class LiquidacionService {

    private static final Logger log = LoggerFactory.getLogger(LiquidacionService.class);
    private static final BigDecimal IGV_PORCENTAJE = DescuentoValidator.getIgvPorcentaje();
    private static final Set<String> NON_CASH_METHODS = Set.of("POS", "YAPE_PLIN", "TRANSFERENCIA");

    private final LiquidacionRepository liquidacionRepository;
    private final PaymentLegRepository paymentLegRepository;
    private final CuentaService cuentaService;
    private final SesionCajaService sesionCajaService;
    private final TipoCambioRepository tipoCambioRepository;
    private final DescuentoValidator descuentoValidator;

    public LiquidacionService(LiquidacionRepository liquidacionRepository,
                              PaymentLegRepository paymentLegRepository,
                              CuentaService cuentaService,
                              SesionCajaService sesionCajaService,
                              TipoCambioRepository tipoCambioRepository,
                              DescuentoValidator descuentoValidator) {
        this.liquidacionRepository = liquidacionRepository;
        this.paymentLegRepository = paymentLegRepository;
        this.cuentaService = cuentaService;
        this.sesionCajaService = sesionCajaService;
        this.tipoCambioRepository = tipoCambioRepository;
        this.descuentoValidator = descuentoValidator;
    }

    /**
     * Generate a pre-liquidación (pre-bill) preview for patient review.
     * This is a read-only view — does not persist any record.
     *
     * @param cuentaId the Cuenta to pre-bill
     * @return PreLiquidacionResponse with charge details and totals
     * @throws EntityNotFoundException  if Cuenta not found
     * @throws IllegalStateException    if Cuenta not ready for billing
     */
    @Transactional(readOnly = true)
    public PreLiquidacionResponse preLiquidar(Long cuentaId) {
        Cuenta cuenta = cuentaService.obtenerCuenta(cuentaId);

        // LIQ-002-2: Only PENDIENTE_COBRO accounts can be pre-billed
        if (!"PENDIENTE_COBRO".equals(cuenta.getEstado())) {
            throw new IllegalStateException(
                "La cuenta no está pendiente de cobro. Estado actual: " + cuenta.getEstado());
        }

        List<CargoAdicionalResponse> cargos = cuentaService.listarCargos(cuentaId);

        List<PreLiquidacionItem> items = cargos.stream()
            .map(c -> new PreLiquidacionItem(
                c.id(), c.tipoCargo(), c.descripcion(), c.monto()))
            .toList();

        BigDecimal subtotal = cargos.stream()
            .map(CargoAdicionalResponse::monto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PreLiquidacionResponse.create(cuentaId, items, subtotal, IGV_PORCENTAJE);
    }

    /**
     * Process payment for a Cuenta.
     * Validates discount, creates Liquidacion + PaymentLegs,
     * updates SesionCaja totalVentas, and calls clinica's confirmar-cobro.
     *
     * @param cuentaId  the Cuenta to charge
     * @param request   payment details including legs and optional discount
     * @param usuarioId the cashier processing the payment
     * @return LiquidacionResponse with payment details
     * @throws EntityNotFoundException if Cuenta, SesionCaja, or TipoCambio not found
     * @throws IllegalStateException   if validation fails
     */
    public LiquidacionResponse pagar(Long cuentaId, PagoRequest request, Long usuarioId) {
        // Resolve Cuenta
        Cuenta cuenta = cuentaService.obtenerCuenta(cuentaId);
        if (!"PENDIENTE_COBRO".equals(cuenta.getEstado())) {
            throw new IllegalStateException(
                "La cuenta no está pendiente de cobro. Estado actual: " + cuenta.getEstado());
        }

        // Resolve open SesionCaja for the cashier
        SesionCaja sesion = sesionCajaService.getOpenSessionEntity(usuarioId);
        if (sesion == null) {
            throw new IllegalStateException("El usuario no tiene una sesión de caja abierta");
        }

        // Resolve total from Cuenta
        BigDecimal total = cuenta.getTotalCargos() != null
            ? cuenta.getTotalCargos()
            : BigDecimal.ZERO;

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("La cuenta no tiene cargos registrados");
        }

        // Resolve descuento
        BigDecimal descuento = request.descuento() != null ? request.descuento() : BigDecimal.ZERO;
        BigDecimal totalConDescuento = total.subtract(descuento);

        // Validate discount
        // Note: costoTotal is not available from Cuenta in MVP; pass null to skip cost-floor check
        descuentoValidator.validar(total, descuento, null, request.usuarioApruebaId());

        // Validate payment legs
        validarPagos(request, totalConDescuento);

        // Resolve moneda and exchange rate
        String moneda = request.moneda() != null ? request.moneda().toUpperCase() : "PEN";
        Long tipoCambioId = null;
        BigDecimal montoUSD = null;
        BigDecimal montoPEN = null;

        if ("USD".equals(moneda)) {
            // LIQ-005-3: Exchange rate required for USD
            if (request.tipoCambioId() == null) {
                throw new IllegalArgumentException(
                    "Se requiere tipo de cambio para transacciones en USD");
            }
            TipoCambio tc = tipoCambioRepository.findById(request.tipoCambioId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Tipo de cambio no encontrado con id: " + request.tipoCambioId()));
            tipoCambioId = tc.getId();
            montoUSD = totalConDescuento;
            montoPEN = totalConDescuento.multiply(tc.getTipoCambio())
                .setScale(2, RoundingMode.HALF_UP);
        } else {
            montoPEN = totalConDescuento;
        }

        // Create Liquidacion
        Liquidacion liq = new Liquidacion();
        liq.setCuentaId(cuentaId);
        liq.setSesionId(sesion.getId());
        liq.setFecha(LocalDateTime.now());
        liq.setMoneda(moneda);
        liq.setMontoTotal(totalConDescuento);
        liq.setMontoUSD(montoUSD);
        liq.setMontoPEN(montoPEN);
        liq.setTipoCambioId(tipoCambioId);
        liq.setDescuentoTotal(descuento);
        if (descuento.compareTo(BigDecimal.ZERO) > 0) {
            liq.setDescuentoPorcentaje(
                descuento.multiply(BigDecimal.valueOf(100))
                    .divide(total, 2, RoundingMode.HALF_UP));
        }
        liq.setUsuarioApruebaId(request.usuarioApruebaId());
        if (request.usuarioApruebaId() != null) {
            liq.setFechaAprobacion(LocalDateTime.now());
        }
        liq.setUsuarioCobraId(usuarioId);
        liq.setEstado("PAGADO");

        liq = liquidacionRepository.save(liq);

        // Create PaymentLegs
        List<PaymentLeg> legs = new ArrayList<>();
        for (PagoLegRequest pago : request.pagos()) {
            PaymentLeg leg = new PaymentLeg();
            leg.setLiquidacion(liq);
            leg.setMetodoPago(pago.metodoPago());
            leg.setMonto(pago.monto());
            leg.setReferencia(pago.referencia());
            leg = paymentLegRepository.save(leg);
            legs.add(leg);
        }

        // Update SesionCaja totalVentas
        BigDecimal currentTotal = sesion.getTotalVentas() != null ? sesion.getTotalVentas() : BigDecimal.ZERO;
        sesion.setTotalVentas(currentTotal.add(totalConDescuento));

        // Call clinica CuentaService.confirmarCobro (same JVM, same transaction)
        // This marks Cuenta as CERRADA, ends Hospitalizacion, releases bed
        cuentaService.confirmarCobro(cuentaId);

        log.debug("Liquidacion created id={}, cuentaId={}, total={}, moneda={}",
            liq.getId(), cuentaId, totalConDescuento, moneda);

        return LiquidacionResponse.fromEntity(liq, legs);
    }

    /**
     * Find recent liquidaciones by usuario who processed the payment.
     */
    @Transactional(readOnly = true)
    public List<LiquidacionResponse> findRecentByUsuario(Long usuarioId, int limit) {
        return liquidacionRepository.findRecentByUsuarioCobraId(usuarioId, limit)
                .stream()
                .map(liq -> {
                    List<PaymentLeg> legs = paymentLegRepository.findByLiquidacionId(liq.getId());
                    return LiquidacionResponse.fromEntity(liq, legs);
                })
                .toList();
    }

    /**
     * Find liquidaciones after a given date.
     */
    @Transactional(readOnly = true)
    public List<LiquidacionResponse> findByFechaAfter(LocalDateTime fecha) {
        return liquidacionRepository.findByFechaAfter(fecha)
                .stream()
                .map(liq -> {
                    List<PaymentLeg> legs = paymentLegRepository.findByLiquidacionId(liq.getId());
                    return LiquidacionResponse.fromEntity(liq, legs);
                })
                .toList();
    }

    /**
     * Validates payment legs:
     * - Sum of amounts must equal the total to collect
     * - Referencia required for non-Efectivo methods
     */
    private void validarPagos(PagoRequest request, BigDecimal totalEsperado) {
        BigDecimal sumaPagos = BigDecimal.ZERO;

        for (PagoLegRequest pago : request.pagos()) {
            String metodo = pago.metodoPago() != null ? pago.metodoPago().toUpperCase() : "";

            // LIQ-001-4: Referencia required for POS, YAPE/PLIN, Transferencia
            if (NON_CASH_METHODS.contains(metodo)
                && (pago.referencia() == null || pago.referencia().isBlank())) {
                throw new IllegalArgumentException(
                    "Se requiere referencia para pagos con " + metodo);
            }

            sumaPagos = sumaPagos.add(pago.monto());
        }

        // LIQ-001-3: Sum of payments must equal total
        if (sumaPagos.compareTo(totalEsperado) != 0) {
            throw new IllegalArgumentException(
                "La suma de los pagos (" + sumaPagos + ") no coincide con el total a cobrar ("
                    + totalEsperado + ")");
        }
    }
}

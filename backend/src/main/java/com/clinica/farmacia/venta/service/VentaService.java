package com.clinica.farmacia.venta.service;

import com.clinica.farmacia.caja.entity.SesionCaja;
import com.clinica.farmacia.caja.repository.SesionCajaRepository;
import com.clinica.farmacia.caja.type.EstadoSesion;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.lote.entity.MovimientoStock;
import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.lote.repository.MovimientoStockRepository;
import com.clinica.farmacia.lote.service.DescuentoService;
import com.clinica.farmacia.lote.type.TipoMovimiento;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.util.RoundingUtil;
import com.clinica.farmacia.venta.dto.DetalleVentaRequest;
import com.clinica.farmacia.venta.dto.DetalleVentaResponse;
import com.clinica.farmacia.venta.dto.VentaRequest;
import com.clinica.farmacia.venta.dto.VentaResponse;
import com.clinica.farmacia.venta.entity.DetalleVenta;
import com.clinica.farmacia.venta.entity.Venta;
import com.clinica.farmacia.venta.repository.DetalleVentaRepository;
import com.clinica.farmacia.venta.repository.VentaRepository;
import com.clinica.farmacia.venta.type.EstadoVenta;
import com.clinica.farmacia.venta.type.TipoLista;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal IGV_PCT = new BigDecimal("18");
    private static final BigDecimal MAX_DESC_PCT = new BigDecimal("20");

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final LoteRepository loteRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final SesionCajaRepository sesionCajaRepository;
    private final DescuentoService descuentoService;
    private final TransactionTemplate txTemplate;

    public VentaService(VentaRepository ventaRepository,
                        DetalleVentaRepository detalleVentaRepository,
                        LoteRepository loteRepository,
                        MovimientoStockRepository movimientoStockRepository,
                        SesionCajaRepository sesionCajaRepository,
                        DescuentoService descuentoService,
                        PlatformTransactionManager ptm) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.loteRepository = loteRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.sesionCajaRepository = sesionCajaRepository;
        this.descuentoService = descuentoService;

        this.txTemplate = new TransactionTemplate(ptm);
        this.txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Complete a sale: validate stock, apply discounts, decrement stock, persist Venta + Detalles.
     * Optimistic lock retry: up to 2 attempts (SC-08).
     */
    public VentaResponse completar(VentaRequest request, Long vendedorUsuarioId) {
        int maxAttempts = 2;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                return txTemplate.execute(status -> completarInternal(request, vendedorUsuarioId));
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                if (attempt == maxAttempts - 1) {
                    log.warn("Optimistic lock failure on final attempt for session {}", request.sesionCajaId());
                    throw new OptimisticLockException(
                        "Conflicto de stock al completar la venta. Intente nuevamente.");
                }
                log.warn("Optimistic lock failure on attempt {}/{} for session {}, retrying...",
                    attempt + 1, maxAttempts, request.sesionCajaId());
            }
        }
        throw new IllegalStateException("No se pudo completar la venta después de " + maxAttempts + " intentos");
    }

    /**
     * Internal completar logic — package-private for unit testability.
     * Validates, discounts, decrements stock, and persists.
     */
    VentaResponse completarInternal(VentaRequest request, Long vendedorUsuarioId) {
        // 1. Load SesionCaja
        SesionCaja sesion = sesionCajaRepository.findById(request.sesionCajaId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Sesión de caja no encontrada con id: " + request.sesionCajaId()));

        // 2. Validate session is ABIERTA
        if (sesion.getEstado() != EstadoSesion.ABIERTA) {
            throw new IllegalStateException("La sesión de caja no está abierta");
        }

        // 3. Compute next correlativo (per-session, starting at 1)
        Integer maxCorrelativo = ventaRepository.findMaxCorrelativoBySesionCajaId(request.sesionCajaId());
        Integer correlativo = (maxCorrelativo == null ? 0 : maxCorrelativo) + 1;

        // 4. Process each detalle
        List<DetalleVenta> detalles = new ArrayList<>();

        for (DetalleVentaRequest detReq : request.detalles()) {
            DetalleVenta detalle = procesarDetalle(detReq, request.tipoLista(), vendedorUsuarioId);
            detalles.add(detalle);
        }

        // 5. Build Venta
        Venta venta = new Venta();
        venta.setSesionCaja(sesion);
        venta.setCorrelativo(correlativo);
        venta.setClientePersonaId(request.clientePersonaId());
        venta.setTipoLista(request.tipoLista() != null ? request.tipoLista() : TipoLista.PUBLICO);
        venta.setEstado(EstadoVenta.COMPLETADA);
        venta.setConImpresion(true);
        venta.setVendedorUsuarioId(vendedorUsuarioId);
        venta.setObservaciones(request.observaciones());

        for (DetalleVenta d : detalles) {
            venta.addDetalle(d);
        }

        venta.calcularTotales();
        venta = ventaRepository.save(venta);

        // Update denormalized totals on the cash session
        sesion.agregarVenta(venta.getTotal());
        sesionCajaRepository.save(sesion);

        log.info("Venta completada: id={}, correlativo={}, sesion={}, total={}, items={}",
            venta.getId(), venta.getCorrelativo(), request.sesionCajaId(),
            venta.getTotal(), detalles.size());

        return VentaResponse.fromEntity(venta);
    }

    /**
     * Process a single line item: validate stock, apply discounts, decrement stock,
     * log stock movement.
     */
    private DetalleVenta procesarDetalle(DetalleVentaRequest detReq, TipoLista tipoLista, Long usuarioId) {
        // Load Lote with optimistic lock
        Lote lote = loteRepository.findById(detReq.loteId())
            .orElseThrow(() -> new EntityNotFoundException("Lote no encontrado con id: " + detReq.loteId()));

        // Validate lote is active
        if (!lote.getActivo()) {
            throw new IllegalArgumentException("El lote " + lote.getCodigoLote() + " está inactivo");
        }

        Producto producto = lote.getProducto();

        // Validate producto is active
        if (!producto.getActivo()) {
            throw new IllegalArgumentException("El producto " + producto.getCodigo() + " está inactivo");
        }

        // Validate stock
        if (detReq.cantidad() > lote.getStockActual()) {
            throw new IllegalArgumentException(
                "Stock insuficiente en lote " + lote.getCodigoLote()
                    + ": solicitado " + detReq.cantidad()
                    + ", disponible " + lote.getStockActual());
        }

        // Determine base price from tipoLista
        BigDecimal precioOriginal;
        if (tipoLista == TipoLista.MEDICO) {
            precioOriginal = producto.getPrecioVentaMedico();
        } else {
            precioOriginal = producto.getPrecioVentaPublico();
        }

        // Compute IGV and cost+IGV floor
        BigDecimal igvAmount = producto.getPrecioCosto()
            .multiply(IGV_PCT)
            .divide(CIEN, 4, RoundingMode.HALF_UP);
        BigDecimal costoMasIgv = producto.getPrecioCosto().add(igvAmount);

        // Apply lifecycle discount from DescuentoService
        BigDecimal descuentoLifecycle = descuentoService.calcularDescuento(
            lote, precioOriginal, costoMasIgv);

        // Add manual discount
        BigDecimal descuentoManual = detReq.descuentoManual() != null
            ? detReq.descuentoManual() : BigDecimal.ZERO;
        BigDecimal descuentoTotal = descuentoLifecycle.add(descuentoManual);

        // Clamp: max 20% of precioOriginal AND never below cost+IGV
        BigDecimal maxDescuentoPct = precioOriginal
            .multiply(MAX_DESC_PCT)
            .divide(CIEN, 4, RoundingMode.HALF_UP);
        BigDecimal maxDescuentoFisico = precioOriginal.subtract(costoMasIgv);
        if (maxDescuentoFisico.compareTo(BigDecimal.ZERO) < 0) {
            maxDescuentoFisico = BigDecimal.ZERO;
        }
        BigDecimal maxDescuento = maxDescuentoPct.min(maxDescuentoFisico);

        if (descuentoTotal.compareTo(maxDescuento) > 0) {
            descuentoTotal = maxDescuento;
        }

        // Compute final unit price (rounded to 0.10)
        BigDecimal precioUnitarioFinal = precioOriginal.subtract(descuentoTotal);
        if (precioUnitarioFinal.compareTo(BigDecimal.ZERO) < 0) {
            precioUnitarioFinal = BigDecimal.ZERO;
        }
        precioUnitarioFinal = RoundingUtil.redondearPrecio(precioUnitarioFinal);

        // Compute line subtotal (rounded to 2 decimals)
        BigDecimal subtotalLinea = precioUnitarioFinal
            .multiply(BigDecimal.valueOf(detReq.cantidad()));
        subtotalLinea = RoundingUtil.redondearMonto(subtotalLinea);

        // Build DetalleVenta
        DetalleVenta detalle = new DetalleVenta();
        detalle.setLote(lote);
        detalle.setCantidad(detReq.cantidad());
        detalle.setPrecioUnitario(precioUnitarioFinal);
        detalle.setPrecioOriginal(precioOriginal);
        detalle.setDescuentoAplicado(descuentoTotal);
        detalle.setSubtotal(subtotalLinea);

        // Decrement stock (triggers @Version optimistic lock on flush)
        lote.setStockActual(lote.getStockActual() - detReq.cantidad());
        loteRepository.save(lote);

        // Log MovimientoStock(SALIDA)
        MovimientoStock mov = new MovimientoStock();
        mov.setLote(lote);
        mov.setTipo(TipoMovimiento.SALIDA);
        mov.setCantidad(detReq.cantidad());
        mov.setMotivo("Venta #" + lote.getCodigoLote());
        mov.setUsuarioId(usuarioId);
        movimientoStockRepository.save(mov);

        return detalle;
    }

    /**
     * Find a sale by ID with full detalles (VEN-01 read).
     */
    public VentaResponse findById(Long id) {
        return ventaRepository.findByIdWithDetalles(id)
            .map(VentaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + id));
    }

    /**
     * List all sales for a cash session, ordered by correlativo ascending.
     */
    public List<VentaResponse> findBySesionCajaId(Long sesionCajaId) {
        return ventaRepository.findBySesionCajaIdOrderByCorrelativoAsc(sesionCajaId)
            .stream()
            .map(VentaResponse::fromEntity)
            .toList();
    }

    /**
     * List sales for a client, ordered by creation date descending.
     */
    public List<VentaResponse> findByClientePersonaId(Long personaId) {
        return ventaRepository.findByClientePersonaIdOrderByCreatedAtDesc(personaId)
            .stream()
            .map(VentaResponse::fromEntity)
            .toList();
    }

    /**
     * Soft-cancel a sale: set ANULADA, restore stock, log DEVOLUCION movements.
     * Implements SC-15 from spec (anulación).
     */
    public VentaResponse anular(Long ventaId, Long usuarioId) {
        Venta venta = ventaRepository.findByIdWithDetalles(ventaId)
            .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + ventaId));

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new IllegalStateException("La venta " + ventaId + " ya está anulada");
        }

        // Restore stock for each detalle
        for (DetalleVenta detalle : venta.getDetalles()) {
            Lote lote = detalle.getLote();
            lote.setStockActual(lote.getStockActual() + detalle.getCantidad());
            loteRepository.save(lote);

            // Log MovimientoStock(DEVOLUCION)
            MovimientoStock mov = new MovimientoStock();
            mov.setLote(lote);
            mov.setTipo(TipoMovimiento.DEVOLUCION);
            mov.setCantidad(detalle.getCantidad());
            mov.setMotivo("Anulación de venta #" + ventaId);
            mov.setVentaId(ventaId);
            mov.setUsuarioId(usuarioId);
            movimientoStockRepository.save(mov);
        }

        venta.setEstado(EstadoVenta.ANULADA);
        venta = ventaRepository.save(venta);

        log.info("Venta anulada: id={}, correlativo={}", venta.getId(), venta.getCorrelativo());

        return VentaResponse.fromEntity(venta);
    }
}

package com.clinica.farmacia.lote.service;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.lote.dto.TransferenciaRequest;
import com.clinica.farmacia.lote.dto.LoteResponse;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.lote.entity.MovimientoStock;
import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.lote.repository.MovimientoStockRepository;
import com.clinica.farmacia.lote.type.TipoMovimiento;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages stock transfers between warehouses.
 *
 * TRF-01: MovimientoStock supports TRANSFERENCIA type with almacenOrigen/Destino.
 * TRF-02: POST /api/v1/farmacia/transferencias — atomic transaction.
 * TRF-03: Reject if cantidad > stockActual of source Lote.
 * TRF-04: Generates one MovimientoStock(TRANSFERENCIA), decrements source,
 *         creates destination Lote.
 */
@Service
@Transactional
public class TransferenciaService {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaService.class);

    private final LoteRepository loteRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final AlmacenRepository almacenRepository;

    public TransferenciaService(LoteRepository loteRepository,
                                MovimientoStockRepository movimientoStockRepository,
                                AlmacenRepository almacenRepository) {
        this.loteRepository = loteRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.almacenRepository = almacenRepository;
    }

    /**
     * Transfers stock from a source lot to a destination warehouse.
     * Atomic: decrement source, create dest Lote, log MovimientoStock(TRANSFERENCIA).
     */
    public LoteResponse transferir(TransferenciaRequest request) {
        if (request.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        // Validate source lot exists and has enough stock
        Lote loteOrigen = loteRepository.findById(request.loteOrigenId())
            .orElseThrow(() -> new EntityNotFoundException("Lote de origen no encontrado con id: " + request.loteOrigenId()));

        // TRF-03: Reject if cantidad > stockActual
        if (request.cantidad() > loteOrigen.getStockActual()) {
            throw new IllegalArgumentException(
                "La cantidad a transferir (" + request.cantidad()
                    + ") excede el stock actual del lote (" + loteOrigen.getStockActual() + ")");
        }

        // Validate destination warehouse
        Almacen almacenDestino = almacenRepository.findById(request.almacenDestinoId())
            .orElseThrow(() -> new EntityNotFoundException("Almacén de destino no encontrado con id: " + request.almacenDestinoId()));

        // TRF-04: Decrement source lot stock
        loteOrigen.setStockActual(loteOrigen.getStockActual() - request.cantidad());
        loteOrigen = loteRepository.save(loteOrigen);

        // TRF-04: Create destination lot (same codigoLote, fechaVencimiento, precioCosto)
        Lote loteDestino = new Lote();
        loteDestino.setProducto(loteOrigen.getProducto());
        loteDestino.setCodigoLote(loteOrigen.getCodigoLote());
        loteDestino.setFechaVencimiento(loteOrigen.getFechaVencimiento());
        loteDestino.setStockInicial(request.cantidad());
        loteDestino.setStockActual(request.cantidad());
        loteDestino.setPrecioCosto(loteOrigen.getPrecioCosto());
        loteDestino.setAlmacen(almacenDestino);
        loteDestino = loteRepository.save(loteDestino);

        // TRF-04: Log MovimientoStock(TRANSFERENCIA) with both almacenOrigen and almacenDestino
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setLote(loteDestino);
        movimiento.setTipo(TipoMovimiento.TRANSFERENCIA);
        movimiento.setCantidad(request.cantidad());
        movimiento.setMotivo(request.motivo());
        movimiento.setVentaId(null);
        movimiento.setUsuarioId(null);
        movimiento.setAlmacenOrigen(loteOrigen.getAlmacen());
        movimiento.setAlmacenDestino(almacenDestino);
        movimientoStockRepository.save(movimiento);

        log.debug("Transferencia: {} unidades de lote {} (almacén {}) → almacén {}",
            request.cantidad(), loteOrigen.getCodigoLote(),
            loteOrigen.getAlmacen().getCodigo(), almacenDestino.getCodigo());

        return LoteResponse.fromEntity(loteDestino);
    }
}

package com.clinica.farmacia.lote.service;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.lote.dto.LoteRequest;
import com.clinica.farmacia.lote.dto.LoteResponse;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.lote.entity.MovimientoStock;
import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.lote.repository.MovimientoStockRepository;
import com.clinica.farmacia.lote.type.TipoMovimiento;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LoteService {

    private static final Logger log = LoggerFactory.getLogger(LoteService.class);

    private final LoteRepository loteRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;

    public LoteService(LoteRepository loteRepository,
                       MovimientoStockRepository movimientoStockRepository,
                       ProductoRepository productoRepository,
                       AlmacenRepository almacenRepository) {
        this.loteRepository = loteRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.productoRepository = productoRepository;
        this.almacenRepository = almacenRepository;
    }

    /**
     * Receive stock: atomically creates a Lote and a MovimientoStock(ENTRADA).
     * Implements STK-02, SC-07.
     */
    public LoteResponse recibir(LoteRequest request) {
        Producto producto = productoRepository.findById(request.productoId())
            .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + request.productoId()));

        Almacen almacen = almacenRepository.findById(request.almacenId())
            .orElseThrow(() -> new EntityNotFoundException("Almacén no encontrado con id: " + request.almacenId()));

        if (!producto.getActivo()) {
            throw new IllegalArgumentException("No se puede recibir stock para un producto inactivo");
        }

        // Create Lote
        Lote lote = new Lote();
        lote.setProducto(producto);
        lote.setCodigoLote(request.codigoLote());
        lote.setFechaVencimiento(request.fechaVencimiento());
        lote.setStockInicial(request.stockInicial());
        lote.setStockActual(request.stockInicial()); // stockActual starts equal to stockInicial
        lote.setPrecioCosto(request.precioCosto());
        lote.setAlmacen(almacen);
        lote = loteRepository.save(lote);

        // Create MovimientoStock(ENTRADA)
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setLote(lote);
        movimiento.setTipo(TipoMovimiento.ENTRADA);
        movimiento.setCantidad(request.stockInicial());
        movimiento.setMotivo(request.motivo() != null ? request.motivo() : "Recepción de stock");
        movimiento.setUsuarioId(request.usuarioId());
        movimiento.setVentaId(null);
        movimiento.setAlmacenOrigen(null);
        movimiento.setAlmacenDestino(null);
        movimientoStockRepository.save(movimiento);

        log.debug("Lote recibido: producto={}, lote={}, cantidad={}, almacén={}",
            producto.getCodigo(), lote.getCodigoLote(), request.stockInicial(), almacen.getCodigo());

        return LoteResponse.fromEntity(lote);
    }

    @Transactional(readOnly = true)
    public List<LoteResponse> findByProducto(Long productoId) {
        return loteRepository.findByProductoIdAndStockActualGreaterThanAndActivoTrue(
                productoId, 0)
            .stream()
            .map(LoteResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public LoteResponse findById(Long id) {
        return loteRepository.findById(id)
            .map(LoteResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Lote no encontrado con id: " + id));
    }
}

package com.clinica.farmacia.reposicion.service;

import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import com.clinica.farmacia.reposicion.dto.ProductoStockBajoProjection;
import com.clinica.farmacia.reposicion.dto.ReposicionDetalleResponse;
import com.clinica.farmacia.reposicion.dto.ReposicionGenerarRequest;
import com.clinica.farmacia.reposicion.dto.ReposicionResponse;
import com.clinica.farmacia.reposicion.entity.Reposicion;
import com.clinica.farmacia.reposicion.entity.ReposicionDetalle;
import com.clinica.farmacia.reposicion.repository.ReposicionDetalleRepository;
import com.clinica.farmacia.reposicion.repository.ReposicionRepository;
import com.clinica.farmacia.reposicion.type.EstadoReposicion;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for replenishment planning (lista de reposición).
 * Implements REP-01, SC-18, SC-19.
 */
@Service
@Transactional
public class ReposicionService {

    private static final Logger log = LoggerFactory.getLogger(ReposicionService.class);

    private final ReposicionRepository reposicionRepository;
    private final ReposicionDetalleRepository detalleRepository;
    private final ProductoRepository productoRepository;

    public ReposicionService(ReposicionRepository reposicionRepository,
                             ReposicionDetalleRepository detalleRepository,
                             ProductoRepository productoRepository) {
        this.reposicionRepository = reposicionRepository;
        this.detalleRepository = detalleRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Generate a replenishment list for products below the stock threshold.
     * If {@code critico} is true, uses stockCritico instead of stockMinimo.
     */
    public ReposicionResponse generar(ReposicionGenerarRequest request, Long usuarioId) {
        Integer umbral = request.critico() ? null : 0; // For stockMinimo, we query all then filter
        // Use stockMinimo as threshold when not critico; use stockCritico when critico is true

        // Build a map of productoId -> ProductoStockBajoProjection for products below threshold
        List<ProductoStockBajoProjection> productosBajoStock;

        if (request.critico()) {
            // critico=true: use stockCritico as threshold
            productosBajoStock = productoRepository.findProductosBajoStockCritico();
        } else {
            // Use stockMinimo as threshold — query with a threshold that ensures all
            // products with stockActual <= stockMinimo are included
            productosBajoStock = productoRepository.findProductosBajoStockMinimo();
        }

        // Filter by almacen if specified (join with lotes in the query itself)
        // The query already does this via the LEFT JOIN on Lote
        // If almacenId is specified, we need additional filtering
        if (request.almacenId() != null && !productosBajoStock.isEmpty()) {
            // Refetch filtered by almacen
            if (request.critico()) {
                productosBajoStock = productoRepository.findProductosBajoStockCriticoPorAlmacen(
                    request.almacenId());
            } else {
                productosBajoStock = productoRepository.findProductosBajoStockMinimoPorAlmacen(
                    request.almacenId());
            }
        }

        if (productosBajoStock.isEmpty()) {
            log.info("No se encontraron productos con stock bajo para generar reposición");
        }

        // Create Reposicion entity
        Reposicion reposicion = new Reposicion();
        reposicion.setGeneradaEn(LocalDateTime.now());
        reposicion.setUsuarioId(usuarioId);
        reposicion.setAlmacenId(request.almacenId() != null ? request.almacenId() : 0L);
        reposicion.setObservaciones(request.observaciones());
        reposicion.setEstado(EstadoReposicion.PENDIENTE);
        reposicion = reposicionRepository.save(reposicion);

        Reposicion finalReposicion = reposicion;

        // Create ReposicionDetalle for each product below threshold
        List<ReposicionDetalle> detalles = productosBajoStock.stream()
            .map(proj -> {
                Integer threshold = request.critico()
                    ? (proj.stockCritico() != null ? proj.stockCritico() : proj.stockMinimo())
                    : proj.stockMinimo();
                // cantidadSugerida = max(stockMinimo * 2 - stockActual, 0)
                int cantidadSugerida = Math.max(
                    proj.stockMinimo() * 2 - proj.stockActual().intValue(), 0);

                ReposicionDetalle detalle = new ReposicionDetalle();
                detalle.setReposicion(finalReposicion);
                detalle.setProductoId(proj.productoId());
                detalle.setStockActual(proj.stockActual().intValue());
                detalle.setStockMinimo(proj.stockMinimo());
                detalle.setStockCritico(proj.stockCritico());
                detalle.setCantidadSugerida(cantidadSugerida);
                return detalle;
            })
            .collect(Collectors.toList());

        detalleRepository.saveAll(detalles);
        reposicion.setDetalles(detalles);
        reposicion = reposicionRepository.save(reposicion);

        log.info("Reposición generada: id={}, productos={}, usuario={}",
            reposicion.getId(), detalles.size(), usuarioId);

        return buildResponse(reposicion);
    }

    /**
     * Find a reposicion by ID with its detalles.
     */
    @Transactional(readOnly = true)
    public ReposicionResponse findById(Long id) {
        Reposicion reposicion = reposicionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Reposición no encontrada con id: " + id));
        return buildResponse(reposicion);
    }

    /**
     * List reposiciones by estado with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ReposicionResponse> listar(EstadoReposicion estado, Pageable pageable) {
        return reposicionRepository.findByEstado(estado, pageable)
            .map(this::buildResponse);
    }

    /**
     * Mark a reposicion as PROCESADA.
     */
    public ReposicionResponse marcarProcesada(Long id) {
        Reposicion reposicion = reposicionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Reposición no encontrada con id: " + id));

        if (reposicion.getEstado() != EstadoReposicion.PENDIENTE) {
            throw new IllegalStateException(
                "La reposición " + id + " no está PENDIENTE (estado: " + reposicion.getEstado() + ")");
        }

        reposicion.setEstado(EstadoReposicion.PROCESADA);
        reposicion.setProcesadaEn(LocalDateTime.now());
        reposicion = reposicionRepository.save(reposicion);

        log.info("Reposición procesada: id={}", id);
        return buildResponse(reposicion);
    }

    /**
     * Mark a reposicion as DESCARTADA with a reason.
     */
    public ReposicionResponse descartar(Long id, String motivo) {
        Reposicion reposicion = reposicionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Reposición no encontrada con id: " + id));

        if (reposicion.getEstado() != EstadoReposicion.PENDIENTE) {
            throw new IllegalStateException(
                "La reposición " + id + " no está PENDIENTE (estado: " + reposicion.getEstado() + ")");
        }

        reposicion.setEstado(EstadoReposicion.DESCARTADA);
        if (motivo != null && !motivo.isBlank()) {
            String existingObs = reposicion.getObservaciones();
            reposicion.setObservaciones(
                (existingObs != null ? existingObs + " | " : "") + "DESCARTADO: " + motivo);
        }
        reposicion = reposicionRepository.save(reposicion);

        log.info("Reposición descartada: id={}, motivo={}", id, motivo);
        return buildResponse(reposicion);
    }

    /**
     * Build a full response with producto codes and descriptions.
     */
    private ReposicionResponse buildResponse(Reposicion reposicion) {
        List<ReposicionDetalle> detalles = reposicion.getDetalles();
        if (detalles == null || detalles.isEmpty()) {
            detalles = detalleRepository.findByReposicionId(reposicion.getId());
        }

        // Fetch product codes and descriptions for each detalle
        Map<Long, String[]> productoInfo = detalles.stream()
            .map(ReposicionDetalle::getProductoId)
            .distinct()
            .collect(Collectors.toMap(
                id -> id,
                id -> {
                    Producto p = productoRepository.findById(id).orElse(null);
                    if (p != null) {
                        String descripcion = p.getDescripcion() != null ? p.getDescripcion() : "";
                        return new String[]{p.getCodigo(), descripcion};
                    }
                    return new String[]{"?", "?"};
                }
            ));

        List<ReposicionDetalleResponse> detalleResponses = detalles.stream()
            .map(detalle -> {
                String[] info = productoInfo.get(detalle.getProductoId());
                String codigo = info != null ? info[0] : "?";
                String descripcion = info != null ? info[1] : "?";
                return ReposicionDetalleResponse.fromEntity(detalle, codigo, descripcion);
            })
            .toList();

        return ReposicionResponse.fromEntity(reposicion, detalleResponses);
    }
}

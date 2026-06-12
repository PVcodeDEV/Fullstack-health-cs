package com.clinica.caja.tarifario.service;

import com.clinica.caja.tarifario.dto.PaqueteRequest;
import com.clinica.caja.tarifario.dto.PaqueteResponse;
import com.clinica.caja.tarifario.dto.PriceChangeRequest;
import com.clinica.caja.tarifario.dto.PrecioResponse;
import com.clinica.caja.tarifario.dto.TarifarioItemRequest;
import com.clinica.caja.tarifario.dto.TarifarioItemResponse;
import com.clinica.caja.tarifario.entity.Paquete;
import com.clinica.caja.tarifario.entity.PaqueteDetalle;
import com.clinica.caja.tarifario.entity.Tarifario;
import com.clinica.caja.tarifario.entity.TarifarioItem;
import com.clinica.caja.tarifario.repository.PaqueteDetalleRepository;
import com.clinica.caja.tarifario.repository.PaqueteRepository;
import com.clinica.caja.tarifario.repository.TarifarioItemRepository;
import com.clinica.caja.tarifario.repository.TarifarioRepository;
import com.clinica.clinica.admision.repository.CuentaPaqueteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TarifarioService {

    private static final Logger log = LoggerFactory.getLogger(TarifarioService.class);

    private final TarifarioRepository tarifarioRepository;
    private final TarifarioItemRepository tarifarioItemRepository;
    private final PaqueteRepository paqueteRepository;
    private final PaqueteDetalleRepository paqueteDetalleRepository;
    private final PrecioCalculator precioCalculator;
    private final CuentaPaqueteRepository cuentaPaqueteRepository;

    public TarifarioService(TarifarioRepository tarifarioRepository,
                            TarifarioItemRepository tarifarioItemRepository,
                            PaqueteRepository paqueteRepository,
                            PaqueteDetalleRepository paqueteDetalleRepository,
                            PrecioCalculator precioCalculator,
                            CuentaPaqueteRepository cuentaPaqueteRepository) {
        this.tarifarioRepository = tarifarioRepository;
        this.tarifarioItemRepository = tarifarioItemRepository;
        this.paqueteRepository = paqueteRepository;
        this.paqueteDetalleRepository = paqueteDetalleRepository;
        this.precioCalculator = precioCalculator;
        this.cuentaPaqueteRepository = cuentaPaqueteRepository;
    }

    // --- Tarifario ---

    @Transactional(readOnly = true)
    public List<Tarifario> listarTarifarios() {
        return tarifarioRepository.findAllByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Tarifario obtenerTarifario(Long id) {
        return tarifarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Tarifario no encontrado con id: " + id));
    }

    public Tarifario crearTarifario(String nombre, String descripcion, Long aseguradoraId) {
        Tarifario entity = new Tarifario();
        entity.setNombre(nombre);
        entity.setDescripcion(descripcion);
        entity.setAseguradoraId(aseguradoraId);
        entity = tarifarioRepository.save(entity);
        log.debug("Tarifario created with id: {}", entity.getId());
        return entity;
    }

    // --- Tarifario Items ---

    @Transactional(readOnly = true)
    public TarifarioItemResponse findItemById(Long id) {
        return tarifarioItemRepository.findById(id)
            .map(TarifarioItemResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Tarifario item no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TarifarioItemResponse> listItemsByTarifario(Long tarifarioId) {
        return tarifarioItemRepository.findByTarifarioIdAndActivoTrue(tarifarioId)
            .stream()
            .map(TarifarioItemResponse::fromEntity)
            .toList();
    }

    public TarifarioItemResponse createItem(TarifarioItemRequest request) {
        Tarifario tarifario = tarifarioRepository.findById(request.tarifarioId())
            .orElseThrow(() -> new EntityNotFoundException("Tarifario no encontrado con id: " + request.tarifarioId()));

        // Calculate final price using injected PrecioCalculator (reads config dynamically)
        BigDecimal precioFinal = precioCalculator.calcularPrecioFinal(request.precioBase());

        TarifarioItem entity = new TarifarioItem();
        entity.setTarifario(tarifario);
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setDescripcion(request.descripcion());
        entity.setPrecioBase(request.precioBase());
        entity.setPrecioFinal(precioFinal);
        entity.setUnidadMedidaId(request.unidadMedidaId());
        entity.setFechaDesde(request.fechaDesde());
        entity.setFechaHasta(null); // new items are active by default

        try {
            entity = tarifarioItemRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            // TOCTOU prevention: unique constraint violation on concurrent create
            log.warn("Duplicate codigo detected on save (concurrent): {}", request.codigo());
            throw new IllegalArgumentException("Ya existe un item con el código: " + request.codigo());
        }

        log.debug("TarifarioItem created: codigo={}, precioFinal={}", entity.getCodigo(), precioFinal);
        return TarifarioItemResponse.fromEntity(entity);
    }

    /**
     * Price revision per TRF-002: creates a new row with updated price.
     * The old row gets fechaHasta set to the day before the new fechaDesde.
     */
    public TarifarioItemResponse priceChange(PriceChangeRequest request) {
        TarifarioItem currentItem = tarifarioItemRepository.findByCodigo(request.codigo())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró item con código: " + request.codigo()));

        // Cap old row's fechaHasta to day before new fechaDesde
        LocalDate oldEndDate = request.fechaDesde().minusDays(1);
        currentItem.setFechaHasta(oldEndDate);
        tarifarioItemRepository.save(currentItem);

        // Create new row with updated price (uses injected PrecioCalculator with dynamic config)
        BigDecimal precioFinal = precioCalculator.calcularPrecioFinal(request.nuevoPrecio());

        TarifarioItem newItem = new TarifarioItem();
        newItem.setTarifario(currentItem.getTarifario());
        newItem.setCodigo(request.codigo());
        newItem.setNombre(currentItem.getNombre());
        newItem.setDescripcion(currentItem.getDescripcion());
        newItem.setPrecioBase(request.nuevoPrecio());
        newItem.setPrecioFinal(precioFinal);
        newItem.setUnidadMedidaId(currentItem.getUnidadMedidaId());
        newItem.setFechaDesde(request.fechaDesde());
        newItem.setFechaHasta(null);

        newItem = tarifarioItemRepository.save(newItem);
        log.debug("Price change for codigo={}: old price ended {}, new price {} from {}",
            request.codigo(), oldEndDate, request.nuevoPrecio(), request.fechaDesde());
        return TarifarioItemResponse.fromEntity(newItem);
    }

    /**
     * Resolve effective price for a given codigo and date (TRF-006).
     */
    @Transactional(readOnly = true)
    public PrecioResponse resolvePrecio(String codigo, LocalDate fecha) {
        TarifarioItem item = tarifarioItemRepository.findEffectiveByCodigoAndFecha(codigo, fecha)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró precio activo para el código: " + codigo + " en fecha: " + fecha));

        return PrecioResponse.fromEntity(item);
    }

    // --- Paquetes ---

    @Transactional(readOnly = true)
    public PaqueteResponse findPaqueteById(Long id) {
        return paqueteRepository.findById(id)
            .map(this::toPaqueteResponse)
            .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PaqueteResponse> listPaquetes() {
        return paqueteRepository.findAllByActivoTrue()
            .stream()
            .map(this::toPaqueteResponse)
            .toList();
    }

    public PaqueteResponse createPaquete(PaqueteRequest request) {
        Paquete paquete = new Paquete();
        paquete.setCodigo(request.codigo());
        paquete.setNombre(request.nombre());
        paquete.setDescripcion(request.descripcion());
        paquete.setPrecioTotal(request.precioTotal());

        try {
            paquete = paqueteRepository.save(paquete);
        } catch (DataIntegrityViolationException e) {
            // TOCTOU prevention: unique constraint violation on concurrent create
            log.warn("Duplicate paquete codigo detected on save (concurrent): {}", request.codigo());
            throw new IllegalArgumentException("Ya existe un paquete con el código: " + request.codigo());
        }

        // Create detalles
        if (request.items() != null) {
            for (var item : request.items()) {
                TarifarioItem tarifarioItem = tarifarioItemRepository.findById(item.tarifarioItemId())
                    .orElseThrow(() -> new EntityNotFoundException(
                        "Tarifario item no encontrado con id: " + item.tarifarioItemId()));

                PaqueteDetalle detalle = new PaqueteDetalle();
                detalle.setPaquete(paquete);
                detalle.setTarifarioItem(tarifarioItem);
                detalle.setCantidad(item.cantidad());
                paqueteDetalleRepository.save(detalle);
            }
        }

        log.debug("Paquete created: codigo={}, items={}", paquete.getCodigo(),
            request.items() != null ? request.items().size() : 0);
        return toPaqueteResponse(paquete);
    }

    public PaqueteResponse softDeletePaquete(Long id) {
        Paquete paquete = paqueteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado con id: " + id));

        // TRF-004-2: reject deletion if referenced by any Cuenta
        if (cuentaPaqueteRepository.existsByPaqueteQuirurgicoId(id)) {
            throw new IllegalStateException(
                "No se puede eliminar el paquete: está referenciado por una o más cuentas activas");
        }

        paquete.markAsInactive();
        paquete = paqueteRepository.save(paquete);

        // Soft delete all detalles
        paqueteDetalleRepository.findByPaqueteId(id)
            .forEach(d -> {
                d.markAsInactive();
                paqueteDetalleRepository.save(d);
            });

        log.debug("Paquete soft-deleted: id={}", id);
        return toPaqueteResponse(paquete);
    }

    private PaqueteResponse toPaqueteResponse(Paquete paquete) {
        List<PaqueteDetalle> detalles = paqueteDetalleRepository.findByPaqueteId(paquete.getId());
        return PaqueteResponse.fromEntity(paquete, detalles);
    }
}

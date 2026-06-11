package com.clinica.farmacia.almacen.service;

import com.clinica.farmacia.almacen.dto.AlmacenRequest;
import com.clinica.farmacia.almacen.dto.AlmacenResponse;
import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.lote.repository.LoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AlmacenService {

    private static final Logger log = LoggerFactory.getLogger(AlmacenService.class);

    private final AlmacenRepository repository;
    private final LoteRepository loteRepository;

    public AlmacenService(AlmacenRepository repository,
                          LoteRepository loteRepository) {
        this.repository = repository;
        this.loteRepository = loteRepository;
    }

    @Transactional(readOnly = true)
    public List<AlmacenResponse> findAll() {
        return repository.findAllByActivoTrueOrderByNombre()
            .stream()
            .map(AlmacenResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public AlmacenResponse findById(Long id) {
        return repository.findById(id)
            .map(AlmacenResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Almacen no encontrado con id: " + id));
    }

    public AlmacenResponse create(AlmacenRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException("Ya existe un almacén con el código: " + request.codigo());
        }

        // Auto-singleton: if no default exists, force this one to be default
        if (!request.defaultWarehouse() && !repository.existsByDefaultWarehouseTrue()) {
            request = new AlmacenRequest(
                request.codigo(), request.nombre(), request.ubicacion(), true);
        }

        // Enforce exactly one default
        if (request.defaultWarehouse()) {
            clearExistingDefault();
        }

        Almacen entity = new Almacen();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setUbicacion(request.ubicacion());
        entity.setDefaultWarehouse(request.defaultWarehouse());

        entity = repository.save(entity);
        log.debug("Almacen created with id: {}", entity.getId());
        return AlmacenResponse.fromEntity(entity);
    }

    public AlmacenResponse update(Long id, AlmacenRequest request) {
        Almacen entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Almacen no encontrado con id: " + id));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException("Ya existe un almacén con el código: " + request.codigo());
        }

        // Auto-singleton: if removing the only default, keep it as default
        if (!request.defaultWarehouse() && entity.getDefaultWarehouse()
            && repository.countByDefaultWarehouseTrue() == 1) {
            request = new AlmacenRequest(
                request.codigo(), request.nombre(), request.ubicacion(), true);
        }

        if (request.defaultWarehouse() && !entity.getDefaultWarehouse()) {
            clearExistingDefault();
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setUbicacion(request.ubicacion());
        entity.setDefaultWarehouse(request.defaultWarehouse());

        entity = repository.save(entity);
        log.debug("Almacen updated with id: {}", entity.getId());
        return AlmacenResponse.fromEntity(entity);
    }

    public AlmacenResponse softDelete(Long id) {
        Almacen entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Almacen no encontrado con id: " + id));

        // 409 if there are active lotes with stock in this almacen
        if (loteRepository.existsByAlmacenIdAndStockActualGreaterThanAndActivoTrue(id, 0)) {
            throw new IllegalStateException(
                "No se puede eliminar el almacén " + id + " porque tiene lotes con stock activo");
        }

        entity.markAsInactive();
        entity = repository.save(entity);
        log.debug("Almacen soft-deleted with id: {}", entity.getId());
        return AlmacenResponse.fromEntity(entity);
    }

    private void clearExistingDefault() {
        repository.findByDefaultWarehouseTrue()
            .ifPresent(existing -> {
                existing.setDefaultWarehouse(false);
                repository.save(existing);
            });
    }
}

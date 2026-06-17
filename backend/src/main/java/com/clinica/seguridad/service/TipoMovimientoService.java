package com.clinica.seguridad.service;

import com.clinica.seguridad.dto.TipoMovimientoResponse;
import com.clinica.seguridad.entity.TipoMovimiento;
import com.clinica.seguridad.repository.TipoMovimientoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoMovimientoService {

    private static final Logger log = LoggerFactory.getLogger(TipoMovimientoService.class);

    private final TipoMovimientoRepository repository;

    public TipoMovimientoService(TipoMovimientoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TipoMovimientoResponse> findAll() {
        return repository.findAll().stream()
            .map(TipoMovimientoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public TipoMovimientoResponse findById(Long id) {
        TipoMovimiento entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoMovimiento no encontrado con id: " + id));
        return TipoMovimientoResponse.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public TipoMovimientoResponse findByCodigo(String codigo) {
        TipoMovimiento entity = repository.findByCodigo(codigo)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoMovimiento no encontrado con código: " + codigo));
        return TipoMovimientoResponse.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public List<TipoMovimientoResponse> findByModulo(String modulo) {
        return repository.findByModulo(modulo).stream()
            .map(TipoMovimientoResponse::fromEntity)
            .toList();
    }

    public TipoMovimientoResponse create(TipoMovimiento entity) {
        if (repository.existsByCodigo(entity.getCodigo())) {
            throw new IllegalArgumentException(
                "Ya existe un tipo de movimiento con código: " + entity.getCodigo());
        }

        entity = repository.save(entity);
        log.debug("TipoMovimiento created: {} ({})", entity.getCodigo(), entity.getModulo());
        return TipoMovimientoResponse.fromEntity(entity);
    }

    public TipoMovimientoResponse update(Long id, TipoMovimiento update) {
        TipoMovimiento entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoMovimiento no encontrado con id: " + id));

        // Check unique constraint if codigo changed
        if (!entity.getCodigo().equals(update.getCodigo())) {
            if (repository.existsByCodigo(update.getCodigo())) {
                throw new IllegalArgumentException(
                    "Ya existe un tipo de movimiento con código: " + update.getCodigo());
            }
        }

        entity.setCodigo(update.getCodigo());
        entity.setNombre(update.getNombre());
        entity.setModulo(update.getModulo());
        entity.setDescripcion(update.getDescripcion());
        entity = repository.save(entity);
        log.debug("TipoMovimiento updated: {} ({})", entity.getCodigo(), entity.getModulo());
        return TipoMovimientoResponse.fromEntity(entity);
    }

    public void toggleActivo(Long id) {
        TipoMovimiento entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoMovimiento no encontrado con id: " + id));
        entity.setActivo(!entity.getActivo());
        repository.save(entity);
        log.debug("TipoMovimiento {} toggled activo={}", id, entity.getActivo());
    }
}

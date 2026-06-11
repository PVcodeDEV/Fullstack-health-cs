package com.clinica.seguridad.service;

import com.clinica.seguridad.dto.PermisoResponse;
import com.clinica.seguridad.entity.Permiso;
import com.clinica.seguridad.repository.PermisoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PermisoService {

    private static final Logger log = LoggerFactory.getLogger(PermisoService.class);

    private final PermisoRepository repository;

    public PermisoService(PermisoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> findAll() {
        return repository.findAll().stream()
            .map(PermisoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> findByModulo(String modulo) {
        return repository.findByModulo(modulo).stream()
            .map(PermisoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public PermisoResponse findById(Long id) {
        Permiso entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Permiso no encontrado con id: " + id));
        return PermisoResponse.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public PermisoResponse findByCodigo(String codigo) {
        Permiso entity = repository.findByCodigo(codigo)
            .orElseThrow(() -> new EntityNotFoundException("Permiso no encontrado con código: " + codigo));
        return PermisoResponse.fromEntity(entity);
    }

    public PermisoResponse create(String codigo, String nombre, String modulo, String descripcion) {
        if (repository.findByCodigo(codigo).isPresent()) {
            throw new IllegalArgumentException("Ya existe un permiso con el código: " + codigo);
        }
        Permiso entity = new Permiso();
        entity.setCodigo(codigo);
        entity.setNombre(nombre);
        entity.setModulo(modulo);
        entity.setDescripcion(descripcion);
        entity = repository.save(entity);
        log.debug("Permiso created with id: {} and codigo: {}", entity.getId(), codigo);
        return PermisoResponse.fromEntity(entity);
    }

    public PermisoResponse update(Long id, String codigo, String nombre, String modulo, String descripcion) {
        Permiso entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Permiso no encontrado con id: " + id));

        if (!entity.getCodigo().equals(codigo) && repository.findByCodigo(codigo).isPresent()) {
            throw new IllegalArgumentException("Ya existe un permiso con el código: " + codigo);
        }
        entity.setCodigo(codigo);
        entity.setNombre(nombre);
        entity.setModulo(modulo);
        entity.setDescripcion(descripcion);
        entity = repository.save(entity);
        log.debug("Permiso updated with id: {}", entity.getId());
        return PermisoResponse.fromEntity(entity);
    }

    public void delete(Long id) {
        Permiso entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Permiso no encontrado con id: " + id));
        entity.markAsInactive();
        repository.save(entity);
        log.debug("Permiso soft-deleted with id: {}", id);
    }
}

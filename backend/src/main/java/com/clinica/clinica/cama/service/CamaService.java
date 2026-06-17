package com.clinica.clinica.cama.service;

import com.clinica.clinica.cama.dto.CamaRequest;
import com.clinica.clinica.cama.dto.CamaResponse;
import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.entity.EstadoCama;
import com.clinica.clinica.cama.repository.CamaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CamaService {

    private static final Logger log = LoggerFactory.getLogger(CamaService.class);

    private final CamaRepository repository;

    public CamaService(CamaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CamaResponse> findAll() {
        return repository.findAllByActivoTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CamaResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Cama no encontrada con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<CamaResponse> findByHabitacionId(Long habitacionId) {
        return repository.findByHabitacionId(habitacionId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CamaResponse> findDisponibles() {
        return repository.findByEstado(EstadoCama.DISPONIBLE).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CamaResponse> findDisponiblesByTipoHabitacion(Long tipoHabitacionId) {
        return repository.findByTipoHabitacionAndDisponible(tipoHabitacionId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CamaResponse create(CamaRequest request) {
        Cama entity = new Cama();
        entity.setHabitacionId(request.habitacionId());
        entity.setCodigo(request.codigo());
        entity.setEstado(EstadoCama.DISPONIBLE);
        entity = repository.save(entity);
        log.debug("Cama created with id: {}, codigo: {}", entity.getId(), entity.getCodigo());
        return toResponse(entity);
    }

    public CamaResponse cambiarEstado(Long id, String nuevoEstado) {
        Cama cama = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cama no encontrada con id: " + id));
        EstadoCama target = EstadoCama.valueOf(nuevoEstado.toUpperCase());
        EstadoCama current = cama.getEstado();

        if (current == target) {
            return toResponse(cama);
        }

        switch (current) {
            case DISPONIBLE -> {
                if (target == EstadoCama.OCUPADO) cama.ocupar();
                else if (target == EstadoCama.MANTENIMIENTO) cama.ponerEnMantenimiento();
                else throw new IllegalStateException("Transición inválida de " + current + " a " + target);
            }
            case OCUPADO -> {
                if (target == EstadoCama.DISPONIBLE) cama.liberar();
                else throw new IllegalStateException("No se puede cambiar de OCUPADO a " + target);
            }
            case MANTENIMIENTO -> {
                if (target == EstadoCama.DISPONIBLE) cama.disponibilizar();
                else throw new IllegalStateException("No se puede cambiar de MANTENIMIENTO a " + target);
            }
        }

        cama = repository.save(cama);
        log.debug("Cama estado cambiado: id={}, nuevoEstado={}", id, target);
        return toResponse(cama);
    }

    public CamaResponse update(Long id, CamaRequest request) {
        Cama entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cama no encontrada con id: " + id));
        entity.setHabitacionId(request.habitacionId());
        entity.setCodigo(request.codigo());
        entity = repository.save(entity);
        log.debug("Cama updated with id: {}", entity.getId());
        return toResponse(entity);
    }

    public CamaResponse softDelete(Long id) {
        Cama entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cama no encontrada con id: " + id));
        entity.markAsInactive();
        entity = repository.save(entity);
        log.debug("Cama soft-deleted with id: {}", entity.getId());
        return toResponse(entity);
    }

    private CamaResponse toResponse(Cama entity) {
        return new CamaResponse(
                entity.getId(),
                entity.getHabitacionId(),
                entity.getCodigo(),
                entity.getEstado().name(),
                entity.getActivo(),
                null // observaciones — not stored in entity currently
        );
    }
}

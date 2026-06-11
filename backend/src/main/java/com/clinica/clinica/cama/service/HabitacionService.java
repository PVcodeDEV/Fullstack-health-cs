package com.clinica.clinica.cama.service;

import com.clinica.clinica.cama.dto.HabitacionRequest;
import com.clinica.clinica.cama.dto.HabitacionResponse;
import com.clinica.clinica.cama.entity.Habitacion;
import com.clinica.clinica.cama.repository.HabitacionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HabitacionService {

    private static final Logger log = LoggerFactory.getLogger(HabitacionService.class);

    private final HabitacionRepository repository;

    public HabitacionService(HabitacionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<HabitacionResponse> findAll() {
        return repository.findAllByActivoTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitacionResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Habitacion no encontrada con id: " + id));
    }

    public HabitacionResponse create(HabitacionRequest request) {
        Habitacion entity = new Habitacion();
        entity.setCodigo(request.nombre().substring(0, Math.min(request.nombre().length(), 20)).toUpperCase());
        entity.setNombre(request.nombre());
        entity.setTipoHabitacionId(request.tipoHabitacionId());
        entity.setCapacidad(request.capacidad() != null ? request.capacidad() : 1);
        entity = repository.save(entity);
        log.debug("Habitacion created with id: {}", entity.getId());
        return toResponse(entity);
    }

    public HabitacionResponse update(Long id, HabitacionRequest request) {
        Habitacion entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Habitacion no encontrada con id: " + id));
        entity.setNombre(request.nombre());
        entity.setTipoHabitacionId(request.tipoHabitacionId());
        entity.setCapacidad(request.capacidad() != null ? request.capacidad() : 1);
        entity = repository.save(entity);
        log.debug("Habitacion updated with id: {}", entity.getId());
        return toResponse(entity);
    }

    public HabitacionResponse softDelete(Long id) {
        Habitacion entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Habitacion no encontrada con id: " + id));
        entity.markAsInactive();
        entity = repository.save(entity);
        log.debug("Habitacion soft-deleted with id: {}", entity.getId());
        return toResponse(entity);
    }

    private HabitacionResponse toResponse(Habitacion entity) {
        return new HabitacionResponse(
                entity.getId(),
                entity.getTipoHabitacionId(),
                null, // tipoHabitacionNombre — requires join if needed later
                entity.getNombre(),
                entity.getPiso() != null ? "Piso " + entity.getPiso() : null,
                entity.getCapacidad(),
                entity.getActivo()
        );
    }
}

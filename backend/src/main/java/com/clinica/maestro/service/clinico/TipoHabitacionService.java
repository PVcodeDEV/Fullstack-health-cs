package com.clinica.maestro.service.clinico;

import com.clinica.maestro.dto.clinico.TipoHabitacionRequest;
import com.clinica.maestro.dto.clinico.TipoHabitacionResponse;
import com.clinica.maestro.entity.clinico.TipoHabitacion;
import com.clinica.maestro.repository.clinico.TipoHabitacionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoHabitacionService {

    private final TipoHabitacionRepository repository;

    public TipoHabitacionService(TipoHabitacionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TipoHabitacionResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(TipoHabitacionResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public TipoHabitacionResponse findById(Long id) {
        return repository.findById(id)
            .map(TipoHabitacionResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoHabitacion not found with id: " + id));
    }

    public TipoHabitacionResponse create(TipoHabitacionRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoHabitacion con codigo: " + request.codigo());
        }

        var entity = new TipoHabitacion();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setTarifaBase(request.tarifaBase());
        entity.setCapacidad(request.capacidad());

        entity = repository.save(entity);
        return TipoHabitacionResponse.fromEntity(entity);
    }

    public TipoHabitacionResponse update(Long id, TipoHabitacionRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoHabitacion not found with id: " + id));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoHabitacion con codigo: " + request.codigo());
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setTarifaBase(request.tarifaBase());
        entity.setCapacidad(request.capacidad());

        entity = repository.save(entity);
        return TipoHabitacionResponse.fromEntity(entity);
    }

    public TipoHabitacionResponse softDelete(Long id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoHabitacion not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return TipoHabitacionResponse.fromEntity(entity);
    }
}

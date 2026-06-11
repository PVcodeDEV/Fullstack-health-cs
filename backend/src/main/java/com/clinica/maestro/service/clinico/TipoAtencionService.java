package com.clinica.maestro.service.clinico;

import com.clinica.maestro.dto.clinico.TipoAtencionRequest;
import com.clinica.maestro.dto.clinico.TipoAtencionResponse;
import com.clinica.maestro.entity.clinico.TipoAtencion;
import com.clinica.maestro.repository.clinico.TipoAtencionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoAtencionService {

    private final TipoAtencionRepository repository;

    public TipoAtencionService(TipoAtencionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TipoAtencionResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(TipoAtencionResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public TipoAtencionResponse findById(Long id) {
        return repository.findById(id)
            .map(TipoAtencionResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoAtencion not found with id: " + id));
    }

    public TipoAtencionResponse create(TipoAtencionRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoAtencion con codigo: " + request.codigo());
        }

        var entity = new TipoAtencion();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setRequiereHabitacion(request.requiereHabitacion() != null && request.requiereHabitacion());

        entity = repository.save(entity);
        return TipoAtencionResponse.fromEntity(entity);
    }

    public TipoAtencionResponse update(Long id, TipoAtencionRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoAtencion not found with id: " + id));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoAtencion con codigo: " + request.codigo());
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setRequiereHabitacion(request.requiereHabitacion() != null && request.requiereHabitacion());

        entity = repository.save(entity);
        return TipoAtencionResponse.fromEntity(entity);
    }

    public TipoAtencionResponse softDelete(Long id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoAtencion not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return TipoAtencionResponse.fromEntity(entity);
    }
}

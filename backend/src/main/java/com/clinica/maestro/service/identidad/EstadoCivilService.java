package com.clinica.maestro.service.identidad;

import com.clinica.maestro.dto.identidad.EstadoCivilRequest;
import com.clinica.maestro.dto.identidad.EstadoCivilResponse;
import com.clinica.maestro.entity.identidad.EstadoCivil;
import com.clinica.maestro.repository.identidad.EstadoCivilRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EstadoCivilService {

    private final EstadoCivilRepository repository;

    public EstadoCivilService(EstadoCivilRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EstadoCivilResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(EstadoCivilResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public EstadoCivilResponse findById(Long id) {
        return repository.findById(id)
            .map(EstadoCivilResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "EstadoCivil not found with id: " + id));
    }

    public EstadoCivilResponse create(EstadoCivilRequest request) {
        if (repository.existsByCodigoReniec(request.codigoReniec())) {
            throw new IllegalArgumentException(
                "Ya existe un EstadoCivil con codigo_reniec: " + request.codigoReniec());
        }

        var entity = new EstadoCivil();
        entity.setCodigoReniec(request.codigoReniec());
        entity.setNombre(request.nombre());

        entity = repository.save(entity);
        return EstadoCivilResponse.fromEntity(entity);
    }

    public EstadoCivilResponse update(Long id, EstadoCivilRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "EstadoCivil not found with id: " + id));

        if (!entity.getCodigoReniec().equals(request.codigoReniec())
            && repository.existsByCodigoReniec(request.codigoReniec())) {
            throw new IllegalArgumentException(
                "Ya existe un EstadoCivil con codigo_reniec: " + request.codigoReniec());
        }

        entity.setCodigoReniec(request.codigoReniec());
        entity.setNombre(request.nombre());

        entity = repository.save(entity);
        return EstadoCivilResponse.fromEntity(entity);
    }

    public EstadoCivilResponse softDelete(Long id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "EstadoCivil not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return EstadoCivilResponse.fromEntity(entity);
    }
}

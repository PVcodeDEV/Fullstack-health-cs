package com.clinica.maestro.service.clinico;

import com.clinica.maestro.dto.clinico.EspecialidadMedicaRequest;
import com.clinica.maestro.dto.clinico.EspecialidadMedicaResponse;
import com.clinica.maestro.entity.clinico.EspecialidadMedica;
import com.clinica.maestro.repository.clinico.EspecialidadMedicaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EspecialidadMedicaService {

    private final EspecialidadMedicaRepository repository;

    public EspecialidadMedicaService(EspecialidadMedicaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EspecialidadMedicaResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(EspecialidadMedicaResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public EspecialidadMedicaResponse findById(Long id) {
        return repository.findById(id)
            .map(EspecialidadMedicaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "EspecialidadMedica not found with id: " + id));
    }

    public EspecialidadMedicaResponse create(EspecialidadMedicaRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe una EspecialidadMedica con codigo: " + request.codigo());
        }

        var entity = new EspecialidadMedica();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setAbreviatura(request.abreviatura());

        entity = repository.save(entity);
        return EspecialidadMedicaResponse.fromEntity(entity);
    }

    public EspecialidadMedicaResponse update(Long id, EspecialidadMedicaRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "EspecialidadMedica not found with id: " + id));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe una EspecialidadMedica con codigo: " + request.codigo());
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setAbreviatura(request.abreviatura());

        entity = repository.save(entity);
        return EspecialidadMedicaResponse.fromEntity(entity);
    }

    public EspecialidadMedicaResponse softDelete(Long id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "EspecialidadMedica not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return EspecialidadMedicaResponse.fromEntity(entity);
    }
}

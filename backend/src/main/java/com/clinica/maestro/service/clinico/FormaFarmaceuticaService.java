package com.clinica.maestro.service.clinico;

import com.clinica.maestro.dto.clinico.FormaFarmaceuticaRequest;
import com.clinica.maestro.dto.clinico.FormaFarmaceuticaResponse;
import com.clinica.maestro.entity.clinico.FormaFarmaceutica;
import com.clinica.maestro.repository.clinico.FormaFarmaceuticaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FormaFarmaceuticaService {

    private final FormaFarmaceuticaRepository repository;

    public FormaFarmaceuticaService(FormaFarmaceuticaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<FormaFarmaceuticaResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(FormaFarmaceuticaResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public FormaFarmaceuticaResponse findById(Long id) {
        return repository.findById(id)
            .map(FormaFarmaceuticaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "FormaFarmaceutica not found with id: " + id));
    }

    public FormaFarmaceuticaResponse create(FormaFarmaceuticaRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe una FormaFarmaceutica con codigo: " + request.codigo());
        }

        var entity = new FormaFarmaceutica();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setRequierePreparacion(request.requierePreparacion() != null && request.requierePreparacion());

        entity = repository.save(entity);
        return FormaFarmaceuticaResponse.fromEntity(entity);
    }

    public FormaFarmaceuticaResponse update(Long id, FormaFarmaceuticaRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "FormaFarmaceutica not found with id: " + id));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe una FormaFarmaceutica con codigo: " + request.codigo());
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setRequierePreparacion(request.requierePreparacion() != null && request.requierePreparacion());

        entity = repository.save(entity);
        return FormaFarmaceuticaResponse.fromEntity(entity);
    }

    public FormaFarmaceuticaResponse softDelete(Long id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "FormaFarmaceutica not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return FormaFarmaceuticaResponse.fromEntity(entity);
    }
}

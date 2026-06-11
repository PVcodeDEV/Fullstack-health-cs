package com.clinica.maestro.service.organizacion;

import com.clinica.maestro.dto.organizacion.AseguradoraRequest;
import com.clinica.maestro.dto.organizacion.AseguradoraResponse;
import com.clinica.maestro.entity.organizacion.Aseguradora;
import com.clinica.maestro.repository.organizacion.AseguradoraRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AseguradoraService {

    private final AseguradoraRepository repository;

    public AseguradoraService(AseguradoraRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AseguradoraResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(AseguradoraResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public AseguradoraResponse findById(Integer id) {
        return repository.findById(id)
            .map(AseguradoraResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "Aseguradora not found with id: " + id));
    }

    public AseguradoraResponse create(AseguradoraRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un Aseguradora con codigo: " + request.codigo());
        }

        var entity = new Aseguradora();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setTipo(request.tipo());
        entity.setContratoVigente(request.contratoVigente());

        entity = repository.save(entity);
        return AseguradoraResponse.fromEntity(entity);
    }

    public AseguradoraResponse update(Integer id, AseguradoraRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Aseguradora not found with id: " + id));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un Aseguradora con codigo: " + request.codigo());
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setTipo(request.tipo());
        entity.setContratoVigente(request.contratoVigente());

        entity = repository.save(entity);
        return AseguradoraResponse.fromEntity(entity);
    }

    public AseguradoraResponse softDelete(Integer id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Aseguradora not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return AseguradoraResponse.fromEntity(entity);
    }
}

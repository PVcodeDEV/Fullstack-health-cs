package com.clinica.maestro.service.organizacion;

import com.clinica.maestro.dto.organizacion.AreaFuncionalRequest;
import com.clinica.maestro.dto.organizacion.AreaFuncionalResponse;
import com.clinica.maestro.entity.organizacion.AreaFuncional;
import com.clinica.maestro.repository.organizacion.AreaFuncionalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AreaFuncionalService {

    private final AreaFuncionalRepository repository;

    public AreaFuncionalService(AreaFuncionalRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AreaFuncionalResponse> findAll(Boolean esAreaFisica) {
        if (esAreaFisica != null) {
            return repository.findByEsAreaFisica(esAreaFisica)
                .stream()
                .map(AreaFuncionalResponse::fromEntity)
                .toList();
        }
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(AreaFuncionalResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public AreaFuncionalResponse findById(Integer id) {
        return repository.findById(id)
            .map(AreaFuncionalResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "AreaFuncional not found with id: " + id));
    }

    public AreaFuncionalResponse create(AreaFuncionalRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un AreaFuncional con codigo: " + request.codigo());
        }

        var entity = new AreaFuncional();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setEsAreaFisica(request.esAreaFisica());

        entity = repository.save(entity);
        return AreaFuncionalResponse.fromEntity(entity);
    }

    public AreaFuncionalResponse update(Integer id, AreaFuncionalRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "AreaFuncional not found with id: " + id));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un AreaFuncional con codigo: " + request.codigo());
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setEsAreaFisica(request.esAreaFisica());

        entity = repository.save(entity);
        return AreaFuncionalResponse.fromEntity(entity);
    }

    public AreaFuncionalResponse softDelete(Integer id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "AreaFuncional not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return AreaFuncionalResponse.fromEntity(entity);
    }
}

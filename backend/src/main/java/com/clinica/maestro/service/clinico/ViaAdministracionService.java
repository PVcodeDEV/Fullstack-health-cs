package com.clinica.maestro.service.clinico;

import com.clinica.maestro.dto.clinico.ViaAdministracionRequest;
import com.clinica.maestro.dto.clinico.ViaAdministracionResponse;
import com.clinica.maestro.entity.clinico.ViaAdministracion;
import com.clinica.maestro.repository.clinico.ViaAdministracionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ViaAdministracionService {

    private final ViaAdministracionRepository repository;

    public ViaAdministracionService(ViaAdministracionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ViaAdministracionResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(ViaAdministracionResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public ViaAdministracionResponse findById(Long id) {
        return repository.findById(id)
            .map(ViaAdministracionResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "ViaAdministracion not found with id: " + id));
    }

    public ViaAdministracionResponse create(ViaAdministracionRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe una ViaAdministracion con codigo: " + request.codigo());
        }

        var entity = new ViaAdministracion();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());

        entity = repository.save(entity);
        return ViaAdministracionResponse.fromEntity(entity);
    }

    public ViaAdministracionResponse update(Long id, ViaAdministracionRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "ViaAdministracion not found with id: " + id));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe una ViaAdministracion con codigo: " + request.codigo());
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());

        entity = repository.save(entity);
        return ViaAdministracionResponse.fromEntity(entity);
    }

    public ViaAdministracionResponse softDelete(Long id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "ViaAdministracion not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return ViaAdministracionResponse.fromEntity(entity);
    }
}

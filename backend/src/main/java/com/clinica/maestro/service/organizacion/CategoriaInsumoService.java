package com.clinica.maestro.service.organizacion;

import com.clinica.maestro.dto.organizacion.CategoriaInsumoRequest;
import com.clinica.maestro.dto.organizacion.CategoriaInsumoResponse;
import com.clinica.maestro.entity.organizacion.CategoriaInsumo;
import com.clinica.maestro.repository.organizacion.CategoriaInsumoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriaInsumoService {

    private final CategoriaInsumoRepository repository;

    public CategoriaInsumoService(CategoriaInsumoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaInsumoResponse> findAll(Integer categoriaPadreId) {
        if (categoriaPadreId != null) {
            return repository.findByCategoriaPadreId(categoriaPadreId)
                .stream()
                .map(CategoriaInsumoResponse::fromEntity)
                .toList();
        }
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(CategoriaInsumoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaInsumoResponse findById(Integer id) {
        return repository.findById(id)
            .map(CategoriaInsumoResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "CategoriaInsumo not found with id: " + id));
    }

    public CategoriaInsumoResponse create(CategoriaInsumoRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un CategoriaInsumo con codigo: " + request.codigo());
        }

        var entity = new CategoriaInsumo();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());

        if (request.categoriaPadreId() != null) {
            var padre = repository.findById(request.categoriaPadreId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "CategoriaInsumo padre not found with id: " + request.categoriaPadreId()));
            entity.setCategoriaPadre(padre);
        }

        entity = repository.save(entity);
        return CategoriaInsumoResponse.fromEntity(entity);
    }

    public CategoriaInsumoResponse update(Integer id, CategoriaInsumoRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "CategoriaInsumo not found with id: " + id));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un CategoriaInsumo con codigo: " + request.codigo());
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());

        if (request.categoriaPadreId() != null) {
            var padre = repository.findById(request.categoriaPadreId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "CategoriaInsumo padre not found with id: " + request.categoriaPadreId()));
            entity.setCategoriaPadre(padre);
        } else {
            entity.setCategoriaPadre(null);
        }

        entity = repository.save(entity);
        return CategoriaInsumoResponse.fromEntity(entity);
    }

    public CategoriaInsumoResponse softDelete(Integer id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "CategoriaInsumo not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return CategoriaInsumoResponse.fromEntity(entity);
    }
}

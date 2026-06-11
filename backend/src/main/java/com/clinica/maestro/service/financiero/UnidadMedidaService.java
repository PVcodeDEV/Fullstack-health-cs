package com.clinica.maestro.service.financiero;

import com.clinica.maestro.dto.financiero.UnidadMedidaRequest;
import com.clinica.maestro.dto.financiero.UnidadMedidaResponse;
import com.clinica.maestro.entity.financiero.UnidadMedida;
import com.clinica.maestro.repository.financiero.UnidadMedidaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UnidadMedidaService {

    private final UnidadMedidaRepository repository;

    public UnidadMedidaService(UnidadMedidaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<UnidadMedidaResponse> findAll() {
        return repository.findAllByOrderByCodigoSunatAsc()
            .stream()
            .map(UnidadMedidaResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public UnidadMedidaResponse findById(Integer id) {
        return repository.findById(id)
            .map(UnidadMedidaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "UnidadMedida not found with id: " + id));
    }

    public UnidadMedidaResponse create(UnidadMedidaRequest request) {
        if (repository.existsByCodigoSunat(request.codigoSunat())) {
            throw new IllegalArgumentException(
                "Ya existe una UnidadMedida con código SUNAT: " + request.codigoSunat());
        }
        var entity = new UnidadMedida();
        entity.setCodigoSunat(request.codigoSunat());
        entity.setNombre(request.nombre());
        entity.setAbreviatura(request.abreviatura());
        entity = repository.save(entity);
        return UnidadMedidaResponse.fromEntity(entity);
    }

    public UnidadMedidaResponse update(Integer id, UnidadMedidaRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "UnidadMedida not found with id: " + id));
        entity.setNombre(request.nombre());
        entity.setAbreviatura(request.abreviatura());
        entity = repository.save(entity);
        return UnidadMedidaResponse.fromEntity(entity);
    }

    public void softDelete(Integer id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "UnidadMedida not found with id: " + id));
        entity.markAsInactive();
        repository.save(entity);
    }
}

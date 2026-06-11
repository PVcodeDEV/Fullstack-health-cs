package com.clinica.maestro.service.financiero;

import com.clinica.maestro.dto.financiero.TipoMonedaRequest;
import com.clinica.maestro.dto.financiero.TipoMonedaResponse;
import com.clinica.maestro.entity.financiero.TipoMoneda;
import com.clinica.maestro.repository.financiero.TipoMonedaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoMonedaService {

    private final TipoMonedaRepository repository;

    public TipoMonedaService(TipoMonedaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TipoMonedaResponse> findAll() {
        return repository.findAllByOrderByCodigoSunatAsc()
            .stream()
            .map(TipoMonedaResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public TipoMonedaResponse findById(Integer id) {
        return repository.findById(id)
            .map(TipoMonedaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoMoneda not found with id: " + id));
    }

    public TipoMonedaResponse create(TipoMonedaRequest request) {
        if (repository.existsByCodigoSunat(request.codigoSunat())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoMoneda con código SUNAT: " + request.codigoSunat());
        }
        var entity = new TipoMoneda();
        entity.setCodigoSunat(request.codigoSunat());
        entity.setNombre(request.nombre());
        entity.setSimbolo(request.simbolo());
        entity = repository.save(entity);
        return TipoMonedaResponse.fromEntity(entity);
    }

    public TipoMonedaResponse update(Integer id, TipoMonedaRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoMoneda not found with id: " + id));
        entity.setNombre(request.nombre());
        entity.setSimbolo(request.simbolo());
        entity = repository.save(entity);
        return TipoMonedaResponse.fromEntity(entity);
    }

    public void softDelete(Integer id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoMoneda not found with id: " + id));
        entity.markAsInactive();
        repository.save(entity);
    }
}

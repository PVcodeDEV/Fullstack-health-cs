package com.clinica.maestro.service.financiero;

import com.clinica.maestro.dto.financiero.TipoComprobanteRequest;
import com.clinica.maestro.dto.financiero.TipoComprobanteResponse;
import com.clinica.maestro.entity.financiero.TipoComprobante;
import com.clinica.maestro.repository.financiero.TipoComprobanteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoComprobanteService {

    private final TipoComprobanteRepository repository;

    public TipoComprobanteService(TipoComprobanteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TipoComprobanteResponse> findAll() {
        return repository.findAllByOrderByCodigoSunatAsc()
            .stream()
            .map(TipoComprobanteResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public TipoComprobanteResponse findById(Integer id) {
        return repository.findById(id)
            .map(TipoComprobanteResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoComprobante not found with id: " + id));
    }

    public TipoComprobanteResponse create(TipoComprobanteRequest request) {
        if (repository.existsByCodigoSunat(request.codigoSunat())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoComprobante con código SUNAT: " + request.codigoSunat());
        }
        var entity = new TipoComprobante();
        entity.setCodigoSunat(request.codigoSunat());
        entity.setNombre(request.nombre());
        entity = repository.save(entity);
        return TipoComprobanteResponse.fromEntity(entity);
    }

    public TipoComprobanteResponse update(Integer id, TipoComprobanteRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoComprobante not found with id: " + id));
        entity.setNombre(request.nombre());
        entity = repository.save(entity);
        return TipoComprobanteResponse.fromEntity(entity);
    }

    public void softDelete(Integer id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoComprobante not found with id: " + id));
        entity.markAsInactive();
        repository.save(entity);
    }
}

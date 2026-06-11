package com.clinica.maestro.service.identidad;

import com.clinica.maestro.dto.identidad.TipoDocumentoIdentidadRequest;
import com.clinica.maestro.dto.identidad.TipoDocumentoIdentidadResponse;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoDocumentoIdentidadService {

    private final TipoDocumentoIdentidadRepository repository;

    public TipoDocumentoIdentidadService(TipoDocumentoIdentidadRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TipoDocumentoIdentidadResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(TipoDocumentoIdentidadResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public TipoDocumentoIdentidadResponse findById(Long id) {
        return repository.findById(id)
            .map(TipoDocumentoIdentidadResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoDocumentoIdentidad not found with id: " + id));
    }

    public TipoDocumentoIdentidadResponse create(TipoDocumentoIdentidadRequest request) {
        if (repository.existsByCodigoSunat(request.codigoSunat())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoDocumentoIdentidad con codigo_sunat: " + request.codigoSunat());
        }

        var entity = new TipoDocumentoIdentidad();
        entity.setCodigoSunat(request.codigoSunat());
        entity.setNombre(request.nombre());
        entity.setLongitudMinima(request.longitudMinima());
        entity.setLongitudMaxima(request.longitudMaxima());

        entity = repository.save(entity);
        return TipoDocumentoIdentidadResponse.fromEntity(entity);
    }

    public TipoDocumentoIdentidadResponse update(Long id, TipoDocumentoIdentidadRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoDocumentoIdentidad not found with id: " + id));

        if (!entity.getCodigoSunat().equals(request.codigoSunat())
            && repository.existsByCodigoSunat(request.codigoSunat())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoDocumentoIdentidad con codigo_sunat: " + request.codigoSunat());
        }

        entity.setCodigoSunat(request.codigoSunat());
        entity.setNombre(request.nombre());
        entity.setLongitudMinima(request.longitudMinima());
        entity.setLongitudMaxima(request.longitudMaxima());

        entity = repository.save(entity);
        return TipoDocumentoIdentidadResponse.fromEntity(entity);
    }

    public TipoDocumentoIdentidadResponse softDelete(Long id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoDocumentoIdentidad not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return TipoDocumentoIdentidadResponse.fromEntity(entity);
    }
}

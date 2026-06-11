package com.clinica.maestro.service.organizacion;

import com.clinica.maestro.dto.organizacion.TipoDocumentoClinicoRequest;
import com.clinica.maestro.dto.organizacion.TipoDocumentoClinicoResponse;
import com.clinica.maestro.entity.organizacion.TipoDocumentoClinico;
import com.clinica.maestro.repository.organizacion.TipoDocumentoClinicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoDocumentoClinicoService {

    private final TipoDocumentoClinicoRepository repository;

    public TipoDocumentoClinicoService(TipoDocumentoClinicoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TipoDocumentoClinicoResponse> findAll(Boolean requiereFirma) {
        if (requiereFirma != null) {
            return repository.findByRequiereFirma(requiereFirma)
                .stream()
                .map(TipoDocumentoClinicoResponse::fromEntity)
                .toList();
        }
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(TipoDocumentoClinicoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public TipoDocumentoClinicoResponse findById(Integer id) {
        return repository.findById(id)
            .map(TipoDocumentoClinicoResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoDocumentoClinico not found with id: " + id));
    }

    public TipoDocumentoClinicoResponse create(TipoDocumentoClinicoRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoDocumentoClinico con codigo: " + request.codigo());
        }

        var entity = new TipoDocumentoClinico();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setRequiereFirma(request.requiereFirma());

        entity = repository.save(entity);
        return TipoDocumentoClinicoResponse.fromEntity(entity);
    }

    public TipoDocumentoClinicoResponse update(Integer id, TipoDocumentoClinicoRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoDocumentoClinico not found with id: " + id));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoDocumentoClinico con codigo: " + request.codigo());
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setRequiereFirma(request.requiereFirma());

        entity = repository.save(entity);
        return TipoDocumentoClinicoResponse.fromEntity(entity);
    }

    public TipoDocumentoClinicoResponse softDelete(Integer id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoDocumentoClinico not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return TipoDocumentoClinicoResponse.fromEntity(entity);
    }
}

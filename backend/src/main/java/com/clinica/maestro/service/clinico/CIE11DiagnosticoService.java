package com.clinica.maestro.service.clinico;

import com.clinica.maestro.dto.clinico.CIE11DiagnosticoRequest;
import com.clinica.maestro.dto.clinico.CIE11DiagnosticoResponse;
import com.clinica.maestro.entity.clinico.CIE11Diagnostico;
import com.clinica.maestro.repository.clinico.CIE11DiagnosticoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CIE11DiagnosticoService {

    private final CIE11DiagnosticoRepository repository;

    public CIE11DiagnosticoService(CIE11DiagnosticoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CIE11DiagnosticoResponse> findAll() {
        return repository.findAllByOrderByFrecuenciaUsoDescCodigoAsc()
            .stream()
            .map(CIE11DiagnosticoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public CIE11DiagnosticoResponse findById(Long id) {
        return repository.findById(id)
            .map(CIE11DiagnosticoResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "CIE11Diagnostico not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<CIE11DiagnosticoResponse> search(String q) {
        if (q == null || q.isBlank()) {
            return findAll();
        }
        var byCode = repository.findByCodigoStartingWithIgnoreCaseOrderByFrecuenciaUsoDesc(q);
        var byDesc = repository.findByDescripcionContainingIgnoreCaseOrderByFrecuenciaUsoDesc(q);
        var merged = byCode.stream()
            .map(CIE11DiagnosticoResponse::fromEntity)
            .toList();
        var existingIds = merged.stream()
            .map(CIE11DiagnosticoResponse::id)
            .toList();
        var additional = byDesc.stream()
            .filter(e -> !existingIds.contains(e.getId()))
            .map(CIE11DiagnosticoResponse::fromEntity)
            .toList();
        return java.util.stream.Stream.concat(merged.stream(), additional.stream()).toList();
    }

    public CIE11DiagnosticoResponse create(CIE11DiagnosticoRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un diagnóstico CIE-11 con código: " + request.codigo());
        }

        var entity = new CIE11Diagnostico();
        entity.setCodigo(request.codigo());
        entity.setDescripcion(request.descripcion());
        entity.setCategoria(request.categoria());
        entity.setSexoAplicable(request.sexoAplicable() != null ? request.sexoAplicable() : "AMBOS");
        entity.setEdadMinima(request.edadMinima());
        entity.setEdadMaxima(request.edadMaxima());
        entity.setVersion(request.version() != null ? request.version() : "CIE-11");
        entity.setFrecuenciaUso(0);

        entity = repository.save(entity);
        return CIE11DiagnosticoResponse.fromEntity(entity);
    }
}

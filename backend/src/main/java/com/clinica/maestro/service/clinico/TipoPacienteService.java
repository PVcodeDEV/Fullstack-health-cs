package com.clinica.maestro.service.clinico;

import com.clinica.maestro.dto.clinico.TipoPacienteRequest;
import com.clinica.maestro.dto.clinico.TipoPacienteResponse;
import com.clinica.maestro.entity.clinico.TipoPaciente;
import com.clinica.maestro.repository.clinico.TipoPacienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoPacienteService {

    private final TipoPacienteRepository repository;

    public TipoPacienteService(TipoPacienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TipoPacienteResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(TipoPacienteResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public TipoPacienteResponse findById(Long id) {
        return repository.findById(id)
            .map(TipoPacienteResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoPaciente not found with id: " + id));
    }

    public TipoPacienteResponse create(TipoPacienteRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoPaciente con codigo: " + request.codigo());
        }

        var entity = new TipoPaciente();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());

        entity = repository.save(entity);
        return TipoPacienteResponse.fromEntity(entity);
    }

    public TipoPacienteResponse update(Long id, TipoPacienteRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoPaciente not found with id: " + id));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un TipoPaciente con codigo: " + request.codigo());
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());

        entity = repository.save(entity);
        return TipoPacienteResponse.fromEntity(entity);
    }

    public TipoPacienteResponse softDelete(Long id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoPaciente not found with id: " + id));

        entity.markAsInactive();
        entity = repository.save(entity);
        return TipoPacienteResponse.fromEntity(entity);
    }
}

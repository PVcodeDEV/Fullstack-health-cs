package com.clinica.maestro.service.ubigeo;

import com.clinica.maestro.dto.ubigeo.UbigeoDepartamentoRequest;
import com.clinica.maestro.dto.ubigeo.UbigeoDepartamentoResponse;
import com.clinica.maestro.entity.ubigeo.UbigeoDepartamento;
import com.clinica.maestro.repository.ubigeo.UbigeoDepartamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UbigeoDepartamentoService {

    private final UbigeoDepartamentoRepository repository;

    public UbigeoDepartamentoService(UbigeoDepartamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<UbigeoDepartamentoResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(UbigeoDepartamentoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public UbigeoDepartamentoResponse findById(String codigo) {
        return repository.findById(codigo)
            .map(UbigeoDepartamentoResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoDepartamento not found with codigo: " + codigo));
    }

    public UbigeoDepartamentoResponse create(UbigeoDepartamentoRequest request) {
        if (repository.existsById(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un UbigeoDepartamento con codigo: " + request.codigo());
        }

        var entity = new UbigeoDepartamento();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());

        entity = repository.save(entity);
        return UbigeoDepartamentoResponse.fromEntity(entity);
    }

    public UbigeoDepartamentoResponse update(String codigo, UbigeoDepartamentoRequest request) {
        var entity = repository.findById(codigo)
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoDepartamento not found with codigo: " + codigo));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsById(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un UbigeoDepartamento con codigo: " + request.codigo());
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());

        entity = repository.save(entity);
        return UbigeoDepartamentoResponse.fromEntity(entity);
    }

    public UbigeoDepartamentoResponse softDelete(String codigo) {
        var entity = repository.findById(codigo)
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoDepartamento not found with codigo: " + codigo));

        entity.markAsInactive();
        entity = repository.save(entity);
        return UbigeoDepartamentoResponse.fromEntity(entity);
    }
}

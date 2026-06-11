package com.clinica.maestro.service.ubigeo;

import com.clinica.maestro.dto.ubigeo.UbigeoProvinciaRequest;
import com.clinica.maestro.dto.ubigeo.UbigeoProvinciaResponse;
import com.clinica.maestro.entity.ubigeo.UbigeoDepartamento;
import com.clinica.maestro.entity.ubigeo.UbigeoProvincia;
import com.clinica.maestro.repository.ubigeo.UbigeoDepartamentoRepository;
import com.clinica.maestro.repository.ubigeo.UbigeoProvinciaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UbigeoProvinciaService {

    private final UbigeoProvinciaRepository repository;
    private final UbigeoDepartamentoRepository departamentoRepository;

    public UbigeoProvinciaService(UbigeoProvinciaRepository repository,
                                   UbigeoDepartamentoRepository departamentoRepository) {
        this.repository = repository;
        this.departamentoRepository = departamentoRepository;
    }

    @Transactional(readOnly = true)
    public List<UbigeoProvinciaResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(UbigeoProvinciaResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<UbigeoProvinciaResponse> findByDepartamento(String departamentoCodigo) {
        return repository.findByDepartamentoCodigoOrderByNombreAsc(departamentoCodigo)
            .stream()
            .map(UbigeoProvinciaResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public UbigeoProvinciaResponse findById(String codigo) {
        return repository.findById(codigo)
            .map(UbigeoProvinciaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoProvincia not found with codigo: " + codigo));
    }

    public UbigeoProvinciaResponse create(UbigeoProvinciaRequest request) {
        if (repository.existsById(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un UbigeoProvincia con codigo: " + request.codigo());
        }

        var departamento = departamentoRepository.findById(request.departamentoCodigo())
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoDepartamento not found with codigo: " + request.departamentoCodigo()));

        var entity = new UbigeoProvincia();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setDepartamento(departamento);

        entity = repository.save(entity);
        return UbigeoProvinciaResponse.fromEntity(entity);
    }

    public UbigeoProvinciaResponse update(String codigo, UbigeoProvinciaRequest request) {
        var entity = repository.findById(codigo)
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoProvincia not found with codigo: " + codigo));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsById(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un UbigeoProvincia con codigo: " + request.codigo());
        }

        var departamento = departamentoRepository.findById(request.departamentoCodigo())
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoDepartamento not found with codigo: " + request.departamentoCodigo()));

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setDepartamento(departamento);

        entity = repository.save(entity);
        return UbigeoProvinciaResponse.fromEntity(entity);
    }

    public UbigeoProvinciaResponse softDelete(String codigo) {
        var entity = repository.findById(codigo)
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoProvincia not found with codigo: " + codigo));

        entity.markAsInactive();
        entity = repository.save(entity);
        return UbigeoProvinciaResponse.fromEntity(entity);
    }
}

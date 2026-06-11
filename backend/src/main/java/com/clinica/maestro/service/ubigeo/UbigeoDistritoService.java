package com.clinica.maestro.service.ubigeo;

import com.clinica.maestro.dto.ubigeo.UbigeoDistritoRequest;
import com.clinica.maestro.dto.ubigeo.UbigeoDistritoResponse;
import com.clinica.maestro.entity.ubigeo.UbigeoDistrito;
import com.clinica.maestro.entity.ubigeo.UbigeoProvincia;
import com.clinica.maestro.repository.ubigeo.UbigeoDistritoRepository;
import com.clinica.maestro.repository.ubigeo.UbigeoProvinciaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UbigeoDistritoService {

    private final UbigeoDistritoRepository repository;
    private final UbigeoProvinciaRepository provinciaRepository;

    public UbigeoDistritoService(UbigeoDistritoRepository repository,
                                  UbigeoProvinciaRepository provinciaRepository) {
        this.repository = repository;
        this.provinciaRepository = provinciaRepository;
    }

    @Transactional(readOnly = true)
    public List<UbigeoDistritoResponse> findAll() {
        return repository.findAllByOrderByNombreAsc()
            .stream()
            .map(UbigeoDistritoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<UbigeoDistritoResponse> findByProvincia(String provinciaCodigo) {
        return repository.findByProvinciaCodigoOrderByNombreAsc(provinciaCodigo)
            .stream()
            .map(UbigeoDistritoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public UbigeoDistritoResponse findById(String codigo) {
        return repository.findById(codigo)
            .map(UbigeoDistritoResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoDistrito not found with codigo: " + codigo));
    }

    public UbigeoDistritoResponse create(UbigeoDistritoRequest request) {
        if (repository.existsById(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un UbigeoDistrito con codigo: " + request.codigo());
        }

        var provincia = provinciaRepository.findById(request.provinciaCodigo())
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoProvincia not found with codigo: " + request.provinciaCodigo()));

        var entity = new UbigeoDistrito();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setProvincia(provincia);

        entity = repository.save(entity);
        return UbigeoDistritoResponse.fromEntity(entity);
    }

    public UbigeoDistritoResponse update(String codigo, UbigeoDistritoRequest request) {
        var entity = repository.findById(codigo)
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoDistrito not found with codigo: " + codigo));

        if (!entity.getCodigo().equals(request.codigo())
            && repository.existsById(request.codigo())) {
            throw new IllegalArgumentException(
                "Ya existe un UbigeoDistrito con codigo: " + request.codigo());
        }

        var provincia = provinciaRepository.findById(request.provinciaCodigo())
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoProvincia not found with codigo: " + request.provinciaCodigo()));

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setProvincia(provincia);

        entity = repository.save(entity);
        return UbigeoDistritoResponse.fromEntity(entity);
    }

    public UbigeoDistritoResponse softDelete(String codigo) {
        var entity = repository.findById(codigo)
            .orElseThrow(() -> new EntityNotFoundException(
                "UbigeoDistrito not found with codigo: " + codigo));

        entity.markAsInactive();
        entity = repository.save(entity);
        return UbigeoDistritoResponse.fromEntity(entity);
    }
}

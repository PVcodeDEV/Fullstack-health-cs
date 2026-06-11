package com.clinica.seguridad.service;

import com.clinica.seguridad.dto.ConfiguracionApiRequest;
import com.clinica.seguridad.dto.ConfiguracionApiResponse;
import com.clinica.seguridad.entity.ConfiguracionApi;
import com.clinica.seguridad.repository.ConfiguracionApiRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ConfiguracionApiService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionApiService.class);

    private final ConfiguracionApiRepository repository;

    public ConfiguracionApiService(ConfiguracionApiRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ConfiguracionApiResponse> findAll() {
        return repository.findAll().stream()
            .map(ConfiguracionApiResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true, noRollbackFor = EntityNotFoundException.class)
    @Cacheable(value = "apiConfig", key = "#modulo + ':' + #clave")
    public ConfiguracionApiResponse findByModuloAndClave(String modulo, String clave) {
        ConfiguracionApi entity = repository.findByModuloAndClave(modulo, clave)
            .orElseThrow(() -> new EntityNotFoundException(
                "Configuracion no encontrada para módulo: " + modulo + ", clave: " + clave));
        return ConfiguracionApiResponse.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "apiConfig", key = "'modulo:' + #modulo")
    public List<ConfiguracionApiResponse> findByModulo(String modulo) {
        return repository.findByModulo(modulo).stream()
            .map(ConfiguracionApiResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public ConfiguracionApiResponse findById(Long id) {
        ConfiguracionApi entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Configuracion no encontrada con id: " + id));
        return ConfiguracionApiResponse.fromEntity(entity);
    }

    @CacheEvict(value = "apiConfig", allEntries = true)
    public ConfiguracionApiResponse create(ConfiguracionApiRequest request) {
        if (repository.existsByModuloAndClave(request.modulo(), request.clave())) {
            throw new IllegalArgumentException(
                "Ya existe una configuración para módulo: " + request.modulo()
                    + ", clave: " + request.clave());
        }

        ConfiguracionApi entity = new ConfiguracionApi();
        entity.setModulo(request.modulo());
        entity.setClave(request.clave());
        entity.setValor(request.valor());
        entity.setTipo(request.tipo() != null ? request.tipo() : "string");
        entity = repository.save(entity);
        log.debug("ConfiguracionApi created: {}/{}", entity.getModulo(), entity.getClave());
        return ConfiguracionApiResponse.fromEntity(entity);
    }

    @CacheEvict(value = "apiConfig", allEntries = true)
    public ConfiguracionApiResponse update(Long id, ConfiguracionApiRequest request) {
        ConfiguracionApi entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Configuracion no encontrada con id: " + id));

        // Check unique constraint if modulo+clave changed
        if (!entity.getModulo().equals(request.modulo())
            || !entity.getClave().equals(request.clave())) {
            if (repository.existsByModuloAndClave(request.modulo(), request.clave())) {
                throw new IllegalArgumentException(
                    "Ya existe una configuración para módulo: " + request.modulo()
                        + ", clave: " + request.clave());
            }
        }

        entity.setModulo(request.modulo());
        entity.setClave(request.clave());
        entity.setValor(request.valor());
        entity.setTipo(request.tipo() != null ? request.tipo() : "string");
        entity = repository.save(entity);
        log.debug("ConfiguracionApi updated: {}/{}", entity.getModulo(), entity.getClave());
        return ConfiguracionApiResponse.fromEntity(entity);
    }

    @CacheEvict(value = "apiConfig", allEntries = true)
    public void delete(Long id) {
        ConfiguracionApi entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Configuracion no encontrada con id: " + id));
        entity.markAsInactive();
        repository.save(entity);
        log.debug("ConfiguracionApi soft-deleted: {}/{}", entity.getModulo(), entity.getClave());
    }
}

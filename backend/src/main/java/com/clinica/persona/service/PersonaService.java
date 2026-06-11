package com.clinica.persona.service;

import com.clinica.maestro.entity.identidad.EstadoCivil;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.EstadoCivilRepository;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.dto.PersonaRequest;
import com.clinica.persona.dto.PersonaResponse;
import com.clinica.persona.dto.PersonaSearchResponse;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PersonaService {

    private static final Logger log = LoggerFactory.getLogger(PersonaService.class);

    private static final int STALE_DATA_YEARS = 1;

    private final PersonaRepository personaRepository;
    private final TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;
    private final EstadoCivilRepository estadoCivilRepository;
    private final Modulo11Validator modulo11Validator;
    private final List<ReniecClient> reniecClients;

    public PersonaService(PersonaRepository personaRepository,
                          TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository,
                          EstadoCivilRepository estadoCivilRepository,
                          Modulo11Validator modulo11Validator,
                          List<ReniecClient> reniecClients) {
        this.personaRepository = personaRepository;
        this.tipoDocumentoIdentidadRepository = tipoDocumentoIdentidadRepository;
        this.estadoCivilRepository = estadoCivilRepository;
        this.modulo11Validator = modulo11Validator;
        this.reniecClients = reniecClients;
    }

    @Transactional(readOnly = true)
    public List<PersonaSearchResponse> search(String numeroDocumento, String nombres, String apellidoPaterno) {
        if (numeroDocumento != null && !numeroDocumento.isBlank()) {
            // Exact document match
            return personaRepository.findByNumeroDocumento(numeroDocumento.trim())
                .filter(Persona::getActivo)
                .map(entity -> PersonaSearchResponse.fromEntity(entity))
                .stream()
                .toList();
        }

        if (nombres != null && !nombres.isBlank()) {
            return personaRepository.findByNombresContainingIgnoreCase(nombres.trim())
                .stream()
                .filter(Persona::getActivo)
                .map(PersonaSearchResponse::fromEntity)
                .toList();
        }

        if (apellidoPaterno != null && !apellidoPaterno.isBlank()) {
            return personaRepository.findByApellidoPaternoContainingIgnoreCase(apellidoPaterno.trim())
                .stream()
                .filter(Persona::getActivo)
                .map(PersonaSearchResponse::fromEntity)
                .toList();
        }

        // Default: return all active
        return personaRepository.findAllByActivoTrue()
            .stream()
            .map(PersonaSearchResponse::fromEntity)
            .toList();
    }

    public PersonaResponse findById(Long id) {
        Persona entity = personaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Persona not found with id: " + id));

        // Stale-data refresh: if DNI and fechaUltimaConsulta > 1 year ago, attempt API refresh
        if (esDni(entity) && datosDesactualizados(entity)) {
            log.debug("Stale data detected for persona {}, attempting API refresh", entity.getId());
            consultarApiYActualizar(entity);
        }

        return PersonaResponse.fromEntity(entity);
    }

    public PersonaResponse create(PersonaRequest request) {
        // Validate unique document number
        if (personaRepository.existsByNumeroDocumento(request.numeroDocumento().trim())) {
            throw new IllegalArgumentException(
                "Ya existe una persona con el número de documento: " + request.numeroDocumento());
        }

        // Resolve FK: tipoDocumentoIdentidad
        TipoDocumentoIdentidad tipoDocumento = tipoDocumentoIdentidadRepository.findById(request.tipoDocumentoId())
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoDocumentoIdentidad not found with id: " + request.tipoDocumentoId()));

        // Build entity
        Persona entity = new Persona();
        entity.setTipoDocumentoIdentidad(tipoDocumento);
        entity.setNumeroDocumento(request.numeroDocumento().trim());

        // Módulo 11 validation for DNI
        if (esDni(tipoDocumento)) {
            if (!modulo11Validator.validar(request.numeroDocumento().trim())) {
                throw new DniInvalidoException(
                    "El número de DNI ingresado no es válido (dígito verificador incorrecto)");
            }
        }

        // API auto-fill for DNI: try each ReniecClient in order
        if (esDni(tipoDocumento)) {
            var datosOpt = consultarApiEnCadena(request.numeroDocumento().trim());
            Persona finalEntity = entity; // effectively final for lambda
            datosOpt.ifPresent(datos -> {
                if (request.nombres() == null) finalEntity.setNombres(datos.nombres());
                if (request.apellidoPaterno() == null) finalEntity.setApellidoPaterno(datos.apellidoPaterno());
                if (request.apellidoMaterno() == null) finalEntity.setApellidoMaterno(datos.apellidoMaterno());
                if (request.direccion() == null) finalEntity.setDireccion(datos.direccion());
                if (request.sexo() == null) finalEntity.setSexo(datos.sexo());
                if (request.fechaNacimiento() == null) finalEntity.setFechaNacimiento(datos.fechaNacimiento());
                if (request.ubigeoDistrito() == null) finalEntity.setUbigeoDistrito(datos.ubigeoDistrito());
                finalEntity.setFechaUltimaConsulta(LocalDate.now());
                log.debug("DNI auto-fill completed for persona");
            });
        }

        // Merge user-supplied fields (overrides any auto-filled API data)
        mergePersonaFields(entity, request);
        entity.setActivo(true);

        entity = personaRepository.save(entity);
        log.debug("Persona created with id: {}", entity.getId());
        return PersonaResponse.fromEntity(entity);
    }

    public PersonaResponse update(Long id, PersonaRequest request) {
        Persona entity = personaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Persona not found with id: " + id));

        // Check duplicate documento (if changed)
        if (!entity.getNumeroDocumento().equals(request.numeroDocumento().trim())
            && personaRepository.existsByNumeroDocumento(request.numeroDocumento().trim())) {
            throw new IllegalArgumentException(
                "Ya existe una persona con el número de documento: " + request.numeroDocumento());
        }

        // Resolve FK: tipoDocumentoIdentidad
        TipoDocumentoIdentidad tipoDocumento = tipoDocumentoIdentidadRepository.findById(request.tipoDocumentoId())
            .orElseThrow(() -> new EntityNotFoundException(
                "TipoDocumentoIdentidad not found with id: " + request.tipoDocumentoId()));

        entity.setTipoDocumentoIdentidad(tipoDocumento);
        entity.setNumeroDocumento(request.numeroDocumento().trim());
        mergePersonaFields(entity, request);

        entity = personaRepository.save(entity);
        log.debug("Persona updated with id: {}", entity.getId());
        return PersonaResponse.fromEntity(entity);
    }

    public PersonaResponse softDelete(Long id) {
        Persona entity = personaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Persona not found with id: " + id));

        entity.markAsInactive();
        entity = personaRepository.save(entity);
        log.debug("Persona soft-deleted with id: {}", entity.getId());
        return PersonaResponse.fromEntity(entity);
    }

    /**
     * Checks if the given tipoDocumento is DNI (código SUNAT "01" or nombre "DNI").
     */
    private boolean esDni(TipoDocumentoIdentidad tdi) {
        return "01".equals(tdi.getCodigoSunat()) || "DNI".equalsIgnoreCase(tdi.getNombre());
    }

    /**
     * Checks if the entity's Persona has DNI as its document type.
     */
    private boolean esDni(Persona entity) {
        return entity.getTipoDocumentoIdentidad() != null
            && esDni(entity.getTipoDocumentoIdentidad());
    }

    /**
     * Returns true if fechaUltimaConsulta is null or older than STALE_DATA_YEARS.
     */
    private boolean datosDesactualizados(Persona entity) {
        return entity.getFechaUltimaConsulta() == null
            || entity.getFechaUltimaConsulta().isBefore(LocalDate.now().minusYears(STALE_DATA_YEARS));
    }

    /**
     * Tries each available ReniecClient in order until one returns data.
     * Never throws — returns empty if all clients fail.
     */
    private java.util.Optional<com.clinica.persona.service.api.PersonaDatos> consultarApiEnCadena(String dni) {
        for (ReniecClient client : reniecClients) {
            try {
                var result = client.consultarPorDni(dni);
                if (result.isPresent()) {
                    log.debug("API consult successful via {}", client.getClass().getSimpleName());
                    return result;
                }
            } catch (Exception e) {
                log.debug("API consult failed via {}: {}", client.getClass().getSimpleName(), e.getMessage());
            }
        }
        log.debug("All API clients failed for DNI consult");
        return java.util.Optional.empty();
    }

    /**
     * Attempts an API refresh for stale data and updates fechaUltimaConsulta.
     */
    private void consultarApiYActualizar(Persona entity) {
        consultarApiEnCadena(entity.getNumeroDocumento()).ifPresent(datos -> {
            if (datos.nombres() != null) entity.setNombres(datos.nombres());
            if (datos.apellidoPaterno() != null) entity.setApellidoPaterno(datos.apellidoPaterno());
            if (datos.apellidoMaterno() != null) entity.setApellidoMaterno(datos.apellidoMaterno());
            if (datos.direccion() != null) entity.setDireccion(datos.direccion());
            if (datos.sexo() != null) entity.setSexo(datos.sexo());
            if (datos.fechaNacimiento() != null) entity.setFechaNacimiento(datos.fechaNacimiento());
            if (datos.ubigeoDistrito() != null) entity.setUbigeoDistrito(datos.ubigeoDistrito());
            entity.setFechaUltimaConsulta(LocalDate.now());
            personaRepository.save(entity);
            log.debug("Stale data refresh completed for persona {}", entity.getId());
        });
    }

    private void mergePersonaFields(Persona entity, PersonaRequest request) {
        if (request.nombres() != null) entity.setNombres(request.nombres());
        if (request.apellidoPaterno() != null) entity.setApellidoPaterno(request.apellidoPaterno());
        if (request.apellidoMaterno() != null) entity.setApellidoMaterno(request.apellidoMaterno());
        if (request.fechaNacimiento() != null) entity.setFechaNacimiento(request.fechaNacimiento());
        if (request.sexo() != null) entity.setSexo(request.sexo());
        if (request.direccion() != null) entity.setDireccion(request.direccion());
        if (request.ubigeoDistrito() != null) entity.setUbigeoDistrito(request.ubigeoDistrito());
        if (request.telefono() != null) entity.setTelefono(request.telefono());
        if (request.email() != null) entity.setEmail(request.email());

        // Resolve estadoCivil FK
        if (request.estadoCivilId() != null) {
            EstadoCivil estadoCivil = estadoCivilRepository.findById(request.estadoCivilId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "EstadoCivil not found with id: " + request.estadoCivilId()));
            entity.setEstadoCivil(estadoCivil);
        } else {
            entity.setEstadoCivil(null);
        }
    }
}

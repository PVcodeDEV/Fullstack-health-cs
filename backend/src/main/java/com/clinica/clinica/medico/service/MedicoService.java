package com.clinica.clinica.medico.service;

import com.clinica.clinica.medico.dto.MedicoRequest;
import com.clinica.clinica.medico.dto.MedicoResponse;
import com.clinica.clinica.medico.entity.Medico;
import com.clinica.clinica.medico.repository.MedicoRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MedicoService {

    private static final Logger log = LoggerFactory.getLogger(MedicoService.class);

    private final MedicoRepository medicoRepository;
    private final PersonaRepository personaRepository;
    private final TrabajadorRepository trabajadorRepository;

    public MedicoService(MedicoRepository medicoRepository,
                         PersonaRepository personaRepository,
                         TrabajadorRepository trabajadorRepository) {
        this.medicoRepository = medicoRepository;
        this.personaRepository = personaRepository;
        this.trabajadorRepository = trabajadorRepository;
    }

    @Transactional(readOnly = true)
    public List<MedicoResponse> findAll() {
        return medicoRepository.findAllByActivoTrue()
            .stream()
            .map(MedicoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public MedicoResponse findById(Long id) {
        return medicoRepository.findById(id)
            .map(MedicoResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Medico not found with id: " + id));
    }

    public MedicoResponse create(MedicoRequest request) {
        // Validate unique persona
        if (medicoRepository.existsByPersonaId(request.personaId())) {
            throw new IllegalArgumentException(
                "Ya existe un médico con la persona id: " + request.personaId());
        }

        // Validate unique CMP
        if (medicoRepository.existsByCmp(request.cmp())) {
            throw new IllegalArgumentException(
                "Ya existe un médico con el CMP: " + request.cmp());
        }

        // Validate unique trabajador (if provided)
        if (request.trabajadorId() != null && medicoRepository.existsByTrabajadorId(request.trabajadorId())) {
            throw new IllegalArgumentException(
                "Ya existe un médico con el trabajador id: " + request.trabajadorId());
        }

        // Resolve FK: persona (always required)
        Persona persona = personaRepository.findById(request.personaId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Persona not found with id: " + request.personaId()));

        var entity = new Medico();
        entity.setPersona(persona);

        // Resolve FK: trabajador (optional — null for external doctors)
        if (request.trabajadorId() != null) {
            Trabajador trabajador = trabajadorRepository.findById(request.trabajadorId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Trabajador not found with id: " + request.trabajadorId()));
            entity.setTrabajador(trabajador);
        }

        entity.setCmp(request.cmp());
        entity.setEspecialidadId(request.especialidadId());
        entity.setEsEspecialista(request.esEspecialista() != null && request.esEspecialista());
        entity.setActivo(true);

        entity = medicoRepository.save(entity);
        log.debug("Medico created with id: {}", entity.getId());
        return MedicoResponse.fromEntity(entity);
    }

    public MedicoResponse update(Long id, MedicoRequest request) {
        Medico entity = medicoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Medico not found with id: " + id));

        // Check duplicate persona (if changed)
        if (!entity.getPersona().getId().equals(request.personaId())
            && medicoRepository.existsByPersonaId(request.personaId())) {
            throw new IllegalArgumentException(
                "Ya existe un médico con la persona id: " + request.personaId());
        }

        // Check duplicate CMP (if changed)
        if (!entity.getCmp().equals(request.cmp())
            && medicoRepository.existsByCmp(request.cmp())) {
            throw new IllegalArgumentException(
                "Ya existe un médico con el CMP: " + request.cmp());
        }

        // Check duplicate trabajador (if changed and provided)
        if (request.trabajadorId() != null
            && (entity.getTrabajador() == null || !entity.getTrabajador().getId().equals(request.trabajadorId()))
            && medicoRepository.existsByTrabajadorId(request.trabajadorId())) {
            throw new IllegalArgumentException(
                "Ya existe un médico con el trabajador id: " + request.trabajadorId());
        }

        // Resolve FK: persona
        Persona persona = personaRepository.findById(request.personaId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Persona not found with id: " + request.personaId()));
        entity.setPersona(persona);

        // Resolve FK: trabajador (optional)
        if (request.trabajadorId() != null) {
            Trabajador trabajador = trabajadorRepository.findById(request.trabajadorId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Trabajador not found with id: " + request.trabajadorId()));
            entity.setTrabajador(trabajador);
        } else {
            entity.setTrabajador(null);
        }

        entity.setCmp(request.cmp());
        entity.setEspecialidadId(request.especialidadId());
        entity.setEsEspecialista(request.esEspecialista() != null && request.esEspecialista());

        entity = medicoRepository.save(entity);
        log.debug("Medico updated with id: {}", entity.getId());
        return MedicoResponse.fromEntity(entity);
    }

    public MedicoResponse softDelete(Long id) {
        Medico entity = medicoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Medico not found with id: " + id));

        entity.markAsInactive();
        entity = medicoRepository.save(entity);
        log.debug("Medico soft-deleted with id: {}", entity.getId());
        return MedicoResponse.fromEntity(entity);
    }
}

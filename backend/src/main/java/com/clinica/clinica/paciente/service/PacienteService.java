package com.clinica.clinica.paciente.service;

import com.clinica.clinica.paciente.dto.PacienteRequest;
import com.clinica.clinica.paciente.dto.PacienteResponse;
import com.clinica.clinica.paciente.entity.Paciente;
import com.clinica.clinica.paciente.repository.PacienteRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PacienteService {

    private static final Logger log = LoggerFactory.getLogger(PacienteService.class);

    private final PacienteRepository pacienteRepository;
    private final PersonaRepository personaRepository;

    public PacienteService(PacienteRepository pacienteRepository, PersonaRepository personaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.personaRepository = personaRepository;
    }

    @Transactional(readOnly = true)
    public List<PacienteResponse> findAll() {
        return pacienteRepository.findAllByActivoTrue()
            .stream()
            .map(PacienteResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public PacienteResponse findById(Long id) {
        return pacienteRepository.findById(id)
            .map(PacienteResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Paciente not found with id: " + id));
    }

    public PacienteResponse create(PacienteRequest request) {
        // Validate unique persona
        if (pacienteRepository.existsByPersonaId(request.personaId())) {
            throw new IllegalArgumentException(
                "Ya existe un paciente con la persona id: " + request.personaId());
        }

        // Resolve FK: persona
        Persona persona = personaRepository.findById(request.personaId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Persona not found with id: " + request.personaId()));

        var entity = new Paciente();
        entity.setPersona(persona);
        entity.setTipoPaciente(request.tipoPaciente());
        entity.setNroHistoriaClinica(request.nroHistoriaClinica());
        entity.setGrupoSanguineo(request.grupoSanguineo());
        entity.setAlergias(request.alergias());
        entity.setContactoEmergenciaNombre(request.contactoEmergenciaNombre());
        entity.setContactoEmergenciaTelefono(request.contactoEmergenciaTelefono());
        entity.setActivo(true);

        entity = pacienteRepository.save(entity);
        log.debug("Paciente created with id: {}", entity.getId());
        return PacienteResponse.fromEntity(entity);
    }

    public PacienteResponse update(Long id, PacienteRequest request) {
        Paciente entity = pacienteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Paciente not found with id: " + id));

        // Check duplicate persona (if changed)
        if (!entity.getPersona().getId().equals(request.personaId())
            && pacienteRepository.existsByPersonaId(request.personaId())) {
            throw new IllegalArgumentException(
                "Ya existe un paciente con la persona id: " + request.personaId());
        }

        // Resolve FK: persona
        Persona persona = personaRepository.findById(request.personaId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Persona not found with id: " + request.personaId()));

        entity.setPersona(persona);
        entity.setTipoPaciente(request.tipoPaciente());
        entity.setNroHistoriaClinica(request.nroHistoriaClinica());
        entity.setGrupoSanguineo(request.grupoSanguineo());
        entity.setAlergias(request.alergias());
        entity.setContactoEmergenciaNombre(request.contactoEmergenciaNombre());
        entity.setContactoEmergenciaTelefono(request.contactoEmergenciaTelefono());

        entity = pacienteRepository.save(entity);
        log.debug("Paciente updated with id: {}", entity.getId());
        return PacienteResponse.fromEntity(entity);
    }

    public PacienteResponse softDelete(Long id) {
        Paciente entity = pacienteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Paciente not found with id: " + id));

        entity.markAsInactive();
        entity = pacienteRepository.save(entity);
        log.debug("Paciente soft-deleted with id: {}", entity.getId());
        return PacienteResponse.fromEntity(entity);
    }
}

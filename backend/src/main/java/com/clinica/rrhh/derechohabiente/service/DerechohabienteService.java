package com.clinica.rrhh.derechohabiente.service;

import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.derechohabiente.dto.DerechohabienteRequest;
import com.clinica.rrhh.derechohabiente.dto.DerechohabienteResponse;
import com.clinica.rrhh.derechohabiente.entity.Derechohabiente;
import com.clinica.rrhh.derechohabiente.repository.DerechohabienteRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.TipoRelacionDerechohabiente;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DerechohabienteService {

    private static final Logger log = LoggerFactory.getLogger(DerechohabienteService.class);

    private final DerechohabienteRepository derechohabienteRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final PersonaRepository personaRepository;

    public DerechohabienteService(DerechohabienteRepository derechohabienteRepository,
                                  TrabajadorRepository trabajadorRepository,
                                  PersonaRepository personaRepository) {
        this.derechohabienteRepository = derechohabienteRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.personaRepository = personaRepository;
    }

    public DerechohabienteResponse create(DerechohabienteRequest request) {
        Trabajador trabajador = trabajadorRepository.findById(request.trabajadorId())
            .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con id: " + request.trabajadorId()));
        Persona persona = personaRepository.findById(request.personaId())
            .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id: " + request.personaId()));

        var entity = new Derechohabiente();
        entity.setTrabajador(trabajador);
        entity.setPersona(persona);
        entity.setRelacion(TipoRelacionDerechohabiente.valueOf(request.relacion()));
        entity.setFechaInicio(request.fechaInicio());

        // Auto-calculate fechaFin for HIJO
        if (request.relacion().equals("HIJO")) {
            entity.setFechaFin(request.fechaInicio().plusYears(18));
        } else {
            entity.setFechaFin(request.fechaFin());
        }

        entity.setEstado("ACTIVO");
        entity = derechohabienteRepository.save(entity);
        log.debug("Derechohabiente creado id={}, relacion={}", entity.getId(), request.relacion());
        return DerechohabienteResponse.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public List<DerechohabienteResponse> findByTrabajadorId(Long trabajadorId) {
        return derechohabienteRepository.findByTrabajadorIdOrderByFechaInicioDesc(trabajadorId)
            .stream().map(DerechohabienteResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public DerechohabienteResponse findById(Long id) {
        return derechohabienteRepository.findById(id)
            .map(DerechohabienteResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Derechohabiente no encontrado con id: " + id));
    }

    /**
     * Inactivates all active derechohabientes for a given trabajador.
     * Called when a contrato is resolved.
     */
    public void inactivarPorTrabajador(Long trabajadorId) {
        List<Derechohabiente> activos = derechohabienteRepository
            .findByTrabajadorIdAndEstadoOrderByFechaInicioDesc(trabajadorId, "ACTIVO");
        for (Derechohabiente d : activos) {
            d.setEstado("INACTIVO");
            derechohabienteRepository.save(d);
        }
        if (!activos.isEmpty()) {
            log.debug("Derechohabientes inactivados para trabajadorId={}: {}", trabajadorId, activos.size());
        }
    }

    public DerechohabienteResponse inactivar(Long id) {
        Derechohabiente entity = derechohabienteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Derechohabiente no encontrado con id: " + id));
        entity.setEstado("INACTIVO");
        entity = derechohabienteRepository.save(entity);
        log.debug("Derechohabiente inactivado id={}", id);
        return DerechohabienteResponse.fromEntity(entity);
    }
}

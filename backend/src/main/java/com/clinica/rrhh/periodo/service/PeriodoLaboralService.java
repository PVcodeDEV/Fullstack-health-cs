package com.clinica.rrhh.periodo.service;

import com.clinica.rrhh.periodo.dto.PeriodoLaboralResponse;
import com.clinica.rrhh.periodo.entity.PeriodoLaboral;
import com.clinica.rrhh.periodo.repository.PeriodoLaboralRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PeriodoLaboralService {

    private static final Logger log = LoggerFactory.getLogger(PeriodoLaboralService.class);

    private final PeriodoLaboralRepository periodoLaboralRepository;
    private final TrabajadorRepository trabajadorRepository;

    public PeriodoLaboralService(PeriodoLaboralRepository periodoLaboralRepository,
                                 TrabajadorRepository trabajadorRepository) {
        this.periodoLaboralRepository = periodoLaboralRepository;
        this.trabajadorRepository = trabajadorRepository;
    }

    public PeriodoLaboralResponse registrarIngreso(Long trabajadorId, LocalDate fechaInicio, boolean esReingreso) {
        Trabajador trabajador = trabajadorRepository.findById(trabajadorId)
            .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con id: " + trabajadorId));

        // Cerrar periodo activo anterior si existe
        periodoLaboralRepository.findByTrabajadorIdAndActivoTrue(trabajadorId)
            .ifPresent(prev -> {
                prev.setActivo(false);
                periodoLaboralRepository.save(prev);
                log.debug("Periodo laboral anterior id={} cerrado automáticamente", prev.getId());
            });

        PeriodoLaboral pl = new PeriodoLaboral();
        pl.setTrabajador(trabajador);
        pl.setFechaInicio(fechaInicio);
        pl.setEsReingreso(esReingreso);
        pl = periodoLaboralRepository.save(pl);
        log.debug("Periodo laboral creado id={} para trabajadorId={}", pl.getId(), trabajadorId);
        return PeriodoLaboralResponse.fromEntity(pl);
    }

    public PeriodoLaboralResponse registrarCese(Long periodoId, LocalDate fechaCese, String motivo) {
        PeriodoLaboral entity = periodoLaboralRepository.findById(periodoId)
            .orElseThrow(() -> new EntityNotFoundException("Periodo laboral no encontrado con id: " + periodoId));

        entity.setFechaCese(fechaCese);
        entity.setMotivoCese(motivo);
        entity.setActivo(false);
        entity = periodoLaboralRepository.save(entity);
        log.debug("Periodo laboral id={} registrado cese", entity.getId());
        return PeriodoLaboralResponse.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public PeriodoLaboralResponse findById(Long id) {
        return periodoLaboralRepository.findById(id)
            .map(PeriodoLaboralResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Periodo laboral no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PeriodoLaboralResponse> findByTrabajadorId(Long trabajadorId) {
        return periodoLaboralRepository.findByTrabajadorIdOrderByFechaInicioDesc(trabajadorId)
            .stream()
            .map(PeriodoLaboralResponse::fromEntity)
            .toList();
    }
}

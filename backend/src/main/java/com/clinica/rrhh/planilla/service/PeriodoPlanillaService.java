package com.clinica.rrhh.planilla.service;

import com.clinica.rrhh.planilla.dto.PeriodoPlanillaRequest;
import com.clinica.rrhh.planilla.dto.PeriodoPlanillaResponse;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planilla.repository.PlanillaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PeriodoPlanillaService {

    private static final Logger log = LoggerFactory.getLogger(PeriodoPlanillaService.class);

    private final PeriodoPlanillaRepository periodoPlanillaRepository;
    private final PlanillaRepository planillaRepository;

    public PeriodoPlanillaService(PeriodoPlanillaRepository periodoPlanillaRepository,
                                   PlanillaRepository planillaRepository) {
        this.periodoPlanillaRepository = periodoPlanillaRepository;
        this.planillaRepository = planillaRepository;
    }

    public PeriodoPlanillaResponse create(PeriodoPlanillaRequest request) {
        if (periodoPlanillaRepository.existsByAnioAndMes(request.anio(), request.mes())) {
            throw new IllegalArgumentException(
                "Ya existe un periodo para " + request.anio() + "-" + request.mes());
        }

        var entity = new PeriodoPlanilla();
        entity.setAnio(request.anio());
        entity.setMes(request.mes());
        entity.setFechaInicio(request.fechaInicio());
        entity.setFechaFin(request.fechaFin());
        entity.setEstado("ABIERTO");
        entity = periodoPlanillaRepository.save(entity);
        log.debug("Periodo planilla creado: {}-{}", request.anio(), request.mes());
        return PeriodoPlanillaResponse.fromEntity(entity);
    }

    public PeriodoPlanillaResponse cerrar(Long id) {
        PeriodoPlanilla entity = periodoPlanillaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Periodo no encontrado: " + id));

        if (!"ABIERTO".equals(entity.getEstado())) {
            throw new IllegalStateException("El periodo no está ABIERTO");
        }

        if (!planillaRepository.existsByPeriodoPlanillaId(id)) {
            throw new IllegalStateException("No se puede cerrar un periodo sin planilla generada");
        }

        entity.setEstado("CERRADO");
        entity = periodoPlanillaRepository.save(entity);
        log.debug("Periodo planilla cerrado: {}-{}", entity.getAnio(), entity.getMes());
        return PeriodoPlanillaResponse.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public List<PeriodoPlanillaResponse> findAll() {
        return periodoPlanillaRepository.findAllByOrderByAnioDescMesDesc()
            .stream().map(PeriodoPlanillaResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public PeriodoPlanillaResponse findById(Long id) {
        return periodoPlanillaRepository.findById(id)
            .map(PeriodoPlanillaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Periodo no encontrado: " + id));
    }
}

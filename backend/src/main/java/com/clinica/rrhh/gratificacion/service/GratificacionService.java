package com.clinica.rrhh.gratificacion.service;

import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.gratificacion.dto.GratificacionResponse;
import com.clinica.rrhh.gratificacion.entity.Gratificacion;
import com.clinica.rrhh.gratificacion.repository.GratificacionRepository;
import com.clinica.rrhh.planilla.config.PlanillaProperties;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.type.EstadoContrato;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class GratificacionService {

    private static final Logger log = LoggerFactory.getLogger(GratificacionService.class);
    private static final BigDecimal GRATIF_PEQUENA_EMPRESA = new BigDecimal("0.5");
    private static final BigDecimal BONIF_EXTRAORDINARIA_TASA = new BigDecimal("0.09");
    private static final BigDecimal ASIGNACION_FAMILIAR_PCT = new BigDecimal("0.10");

    private final GratificacionRepository gratificacionRepository;
    private final PeriodoPlanillaRepository periodoPlanillaRepository;
    private final ContratoRepository contratoRepository;
    private final PlanillaProperties properties;

    public GratificacionService(GratificacionRepository gratificacionRepository,
                                PeriodoPlanillaRepository periodoPlanillaRepository,
                                ContratoRepository contratoRepository,
                                PlanillaProperties properties) {
        this.gratificacionRepository = gratificacionRepository;
        this.periodoPlanillaRepository = periodoPlanillaRepository;
        this.contratoRepository = contratoRepository;
        this.properties = properties;
    }

    public List<GratificacionResponse> calcular(Long periodoPlanillaId) {
        PeriodoPlanilla periodo = periodoPlanillaRepository.findById(periodoPlanillaId)
            .orElseThrow(() -> new EntityNotFoundException("Periodo no encontrado: " + periodoPlanillaId));

        // Determine semester from mes
        String semestre;
        LocalDate semestreInicio;
        LocalDate semestreFin;
        int mes = periodo.getMes();
        if (mes == 6) {
            semestre = "ENERO-JUNIO";
            semestreInicio = LocalDate.of(periodo.getAnio(), 1, 1);
            semestreFin = LocalDate.of(periodo.getAnio(), 6, 30);
        } else if (mes == 12) {
            semestre = "JULIO-DICIEMBRE";
            semestreInicio = LocalDate.of(periodo.getAnio(), 7, 1);
            semestreFin = LocalDate.of(periodo.getAnio(), 12, 31);
        } else {
            throw new IllegalArgumentException(
                "Periodo inválido para gratificación. Debe ser mes 6 (Junio) o 12 (Diciembre). Mes actual: " + mes);
        }

        // Find active contratos during this semester
        List<Contrato> contratos = contratoRepository.findAll().stream()
            .filter(c -> c.getEstado() == EstadoContrato.ACTIVO)
            .filter(c -> !c.getFechaInicio().isAfter(semestreFin))
            .toList();

        if (contratos.isEmpty()) {
            log.warn("No hay contratos activos para gratificación periodoId={}", periodoPlanillaId);
            return List.of();
        }

        BigDecimal rmv = BigDecimal.valueOf(properties.getRmv());
        List<GratificacionResponse> resultados = new ArrayList<>();

        for (Contrato contrato : contratos) {
            Trabajador trabajador = contrato.getTrabajador();
            LocalDate inicio = contrato.getFechaInicio();

            // Calculate meses computables in semester
            int meses = calcularMesesComputables(inicio, semestreInicio, semestreFin);
            if (meses < 1) continue;

            // Remuneración computable = sueldo base + asignación familiar
            BigDecimal remuneracion = contrato.getRemuneracion();
            if (trabajador.getCantidadHijos() != null && trabajador.getCantidadHijos() >= 1) {
                remuneracion = remuneracion.add(rmv.multiply(ASIGNACION_FAMILIAR_PCT));
            }

            // Gratificación (Pequeña Empresa REMYPE)
            BigDecimal gratificacion;
            if (meses >= 6) {
                gratificacion = remuneracion.multiply(GRATIF_PEQUENA_EMPRESA)
                    .setScale(2, RoundingMode.HALF_UP);
            } else {
                gratificacion = remuneracion.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(meses));
            }

            // Bonificación Extraordinaria (9%)
            BigDecimal bonif = gratificacion.multiply(BONIF_EXTRAORDINARIA_TASA)
                .setScale(2, RoundingMode.HALF_UP);

            BigDecimal total = gratificacion.add(bonif);

            // Upsert
            Gratificacion entity = gratificacionRepository
                .findByPeriodoPlanillaIdAndTrabajadorId(periodoPlanillaId, trabajador.getId())
                .orElseGet(Gratificacion::new);

            entity.setPeriodoPlanilla(periodo);
            entity.setTrabajador(trabajador);
            entity.setContrato(contrato);
            entity.setSemestre(semestre);
            entity.setMesesComputables(meses);
            entity.setRemuneracionComputable(remuneracion);
            entity.setGratificacion(gratificacion);
            entity.setBonificacionExtraordinaria(bonif);
            entity.setTotal(total);
            entity.setEstado("CALCULADO");

            entity = gratificacionRepository.save(entity);
            resultados.add(GratificacionResponse.fromEntity(entity));
        }

        log.debug("Gratificación calculada para periodoId={}, semestre={}, trabajadores={}",
            periodoPlanillaId, semestre, resultados.size());
        return resultados;
    }

    /**
     * Calcula los meses computables en un semestre.
     * Regla Pequeña Empresa: día 1-14 = mes completo, 15+ = desde mes siguiente.
     */
    int calcularMesesComputables(LocalDate inicio, LocalDate semestreInicio, LocalDate semestreFin) {
        // If contrato started before the semester, count from semester start
        LocalDate effectiveStart;
        if (inicio.isBefore(semestreInicio)) {
            effectiveStart = semestreInicio;
        } else {
            effectiveStart = inicio;
        }

        // Day 1-14: count the month. Day 15+: start from next month.
        if (effectiveStart.getDayOfMonth() >= 15) {
            effectiveStart = effectiveStart.plusMonths(1).withDayOfMonth(1);
        } else {
            effectiveStart = effectiveStart.withDayOfMonth(1);
        }

        // We can only count months that are WITHIN the semester
        if (effectiveStart.isAfter(semestreFin)) {
            return 0;
        }

        // Count months from effectiveStart to semestreFin
        int meses = 0;
        LocalDate cursor = effectiveStart;
        while (!cursor.isAfter(semestreFin)) {
            meses++;
            cursor = cursor.plusMonths(1);
        }

        return Math.min(meses, 6);
    }

    @Transactional(readOnly = true)
    public List<GratificacionResponse> findAll() {
        return gratificacionRepository.findAll().stream()
            .map(GratificacionResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public GratificacionResponse findById(Long id) {
        return gratificacionRepository.findById(id)
            .map(GratificacionResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Gratificación no encontrada: " + id));
    }
}

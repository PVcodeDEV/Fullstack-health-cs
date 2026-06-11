package com.clinica.rrhh.cts.service;

import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.cts.dto.DepositoCtsResponse;
import com.clinica.rrhh.cts.entity.DepositoCts;
import com.clinica.rrhh.cts.repository.DepositoCtsRepository;
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
import java.util.Optional;

@Service
@Transactional
public class CtsService {

    private static final Logger log = LoggerFactory.getLogger(CtsService.class);
    private static final BigDecimal ASIGNACION_FAMILIAR_PCT = new BigDecimal("0.10");
    private static final int DAYS_PER_MONTH = 30;
    private static final int DAYS_PER_YEAR = 360;

    private final DepositoCtsRepository depositoCtsRepository;
    private final PeriodoPlanillaRepository periodoPlanillaRepository;
    private final ContratoRepository contratoRepository;
    private final GratificacionRepository gratificacionRepository;
    private final PlanillaProperties properties;

    public CtsService(DepositoCtsRepository depositoCtsRepository,
                      PeriodoPlanillaRepository periodoPlanillaRepository,
                      ContratoRepository contratoRepository,
                      GratificacionRepository gratificacionRepository,
                      PlanillaProperties properties) {
        this.depositoCtsRepository = depositoCtsRepository;
        this.periodoPlanillaRepository = periodoPlanillaRepository;
        this.contratoRepository = contratoRepository;
        this.gratificacionRepository = gratificacionRepository;
        this.properties = properties;
    }

    public List<DepositoCtsResponse> calcular(Long periodoPlanillaId) {
        PeriodoPlanilla periodo = periodoPlanillaRepository.findById(periodoPlanillaId)
            .orElseThrow(() -> new EntityNotFoundException("Periodo no encontrado: " + periodoPlanillaId));

        // Derive semester from mes
        String semestre;
        LocalDate semestreInicio;
        LocalDate semestreFin;
        int mes = periodo.getMes();
        int anio = periodo.getAnio();

        if (mes == 5) {
            // Periodo MAYO → semestre NOVIEMBRE (mayo–octubre)
            semestre = "MAYO-OCTUBRE";
            semestreInicio = LocalDate.of(anio, 5, 1);
            semestreFin = LocalDate.of(anio, 10, 31);
        } else if (mes == 11) {
            // Periodo NOVIEMBRE → semestre MAYO (noviembre–abril)
            semestre = "NOVIEMBRE-ABRIL";
            semestreInicio = LocalDate.of(anio, 11, 1);
            semestreFin = LocalDate.of(anio + 1, 4, 30);
        } else {
            throw new IllegalArgumentException(
                "Periodo inválido para CTS. Debe ser mes 5 (Mayo) o 11 (Noviembre). Mes actual: " + mes);
        }

        // Find active contratos during this semester
        List<Contrato> contratos = contratoRepository.findAll().stream()
            .filter(c -> c.getEstado() == EstadoContrato.ACTIVO)
            .filter(c -> !c.getFechaInicio().isAfter(semestreFin))
            .toList();

        if (contratos.isEmpty()) {
            log.warn("No hay contratos activos para CTS periodoId={}", periodoPlanillaId);
            return List.of();
        }

        BigDecimal rmv = BigDecimal.valueOf(properties.getRmv());
        List<DepositoCtsResponse> resultados = new ArrayList<>();

        for (Contrato contrato : contratos) {
            Trabajador trabajador = contrato.getTrabajador();
            LocalDate inicio = contrato.getFechaInicio();

            // Calculate días computables (30-day truncamiento)
            int dias = calcularDiasComputables(inicio, semestreInicio, semestreFin);
            if (dias < 1) continue;

            // Remuneración base
            BigDecimal sueldoBase = contrato.getRemuneracion();

            // Asignación familiar (10% RMV if ≥ 1 child)
            BigDecimal asigFamiliar = BigDecimal.ZERO;
            if (trabajador.getCantidadHijos() != null && trabajador.getCantidadHijos() >= 1) {
                asigFamiliar = rmv.multiply(ASIGNACION_FAMILIAR_PCT)
                    .setScale(2, RoundingMode.HALF_UP);
            }

            // Average gratificación (last 2 records, 1/6 each)
            List<Gratificacion> gratifRecords = gratificacionRepository
                .findByTrabajadorIdOrderByCreatedAtDesc(trabajador.getId())
                .stream()
                .limit(2)
                .toList();

            BigDecimal promGratif;
            BigDecimal promBonif;
            if (gratifRecords.isEmpty()) {
                promGratif = BigDecimal.ZERO;
                promBonif = BigDecimal.ZERO;
            } else {
                BigDecimal sumGratif = BigDecimal.ZERO;
                BigDecimal sumBonif = BigDecimal.ZERO;
                for (Gratificacion g : gratifRecords) {
                    sumGratif = sumGratif.add(
                        Optional.ofNullable(g.getGratificacion()).orElse(BigDecimal.ZERO));
                    sumBonif = sumBonif.add(
                        Optional.ofNullable(g.getBonificacionExtraordinaria()).orElse(BigDecimal.ZERO));
                }
                int count = gratifRecords.size();
                promGratif = sumGratif.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
                promBonif = sumBonif.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
            }

            // Remuneración computable
            BigDecimal rc = sueldoBase.add(asigFamiliar).add(promGratif).add(promBonif);

            // CTS monto: (RC / 360) × días computables
            BigDecimal montoCts = rc.divide(BigDecimal.valueOf(DAYS_PER_YEAR), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(dias))
                .setScale(2, RoundingMode.HALF_UP);

            // Upsert
            DepositoCts entity = depositoCtsRepository
                .findByPeriodoPlanillaIdAndTrabajadorId(periodoPlanillaId, trabajador.getId())
                .orElseGet(DepositoCts::new);

            entity.setPeriodoPlanilla(periodo);
            entity.setTrabajador(trabajador);
            entity.setContrato(contrato);
            entity.setSemestre(semestre);
            entity.setDiasComputables(dias);
            entity.setRemuneracionComputable(rc);
            entity.setPromedioGratificacion(promGratif);
            entity.setPromedioBonificacion(promBonif);
            entity.setMontoCts(montoCts);
            entity.setEstado("CALCULADO");

            entity = depositoCtsRepository.save(entity);
            resultados.add(DepositoCtsResponse.fromEntity(entity));
        }

        log.debug("CTS calculado para periodoId={}, semestre={}, trabajadores={}",
            periodoPlanillaId, semestre, resultados.size());
        return resultados;
    }

    /**
     * Calcula los días computables en un semestre.
     * Regla Pequeña Empresa: día 1-14 = mes completo (30 días), 15+ = desde mes siguiente.
     * Cada mes completo equivale a 30 días (truncamiento).
     */
    int calcularDiasComputables(LocalDate inicio, LocalDate semestreInicio, LocalDate semestreFin) {
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

        meses = Math.min(meses, 6);
        return meses * DAYS_PER_MONTH;
    }

    @Transactional(readOnly = true)
    public List<DepositoCtsResponse> findAll() {
        return depositoCtsRepository.findAll().stream()
            .map(DepositoCtsResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public DepositoCtsResponse findById(Long id) {
        return depositoCtsRepository.findById(id)
            .map(DepositoCtsResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("CTS no encontrado: " + id));
    }
}

package com.clinica.rrhh.vacacion.service;

import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.EstadoContrato;
import com.clinica.rrhh.vacacion.dto.*;
import com.clinica.rrhh.vacacion.entity.VacacionGoce;
import com.clinica.rrhh.vacacion.entity.VacacionRecord;
import com.clinica.rrhh.vacacion.repository.VacacionGoceRepository;
import com.clinica.rrhh.vacacion.repository.VacacionRecordRepository;
import com.clinica.rrhh.planilla.config.PlanillaProperties;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class VacacionService {

    private static final Logger log = LoggerFactory.getLogger(VacacionService.class);
    private static final int DIAS_PEQUENA_EMPRESA = 15;
    private static final BigDecimal ASIGNACION_FAMILIAR_PCT = new BigDecimal("0.10");

    private final VacacionRecordRepository recordRepository;
    private final VacacionGoceRepository goceRepository;
    private final ContratoRepository contratoRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final PlanillaProperties properties;
    private final Clock clock;

    public VacacionService(VacacionRecordRepository recordRepository,
                           VacacionGoceRepository goceRepository,
                           ContratoRepository contratoRepository,
                           TrabajadorRepository trabajadorRepository,
                           PlanillaProperties properties,
                           Clock clock) {
        this.recordRepository = recordRepository;
        this.goceRepository = goceRepository;
        this.contratoRepository = contratoRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Calculate vacation records for all workers who have completed 12 months
     * of service. Idempotent — will not duplicate existing records.
     * Also marks expired records as PERDIDO.
     *
     * @param diasReduccion full months of unjustified absence or unpaid leave
     *                      (each month reduces 1/12 = 1.25 days)
     */
    public List<VacacionRecordResponse> calcular(Integer diasReduccion) {
        if (diasReduccion == null) diasReduccion = 0;

        // Auto-expiry: mark records past expiration as PERDIDO
        List<VacacionRecord> activos = recordRepository.findByEstado("ACTIVO");
        LocalDate hoy = LocalDate.now(clock);
        for (VacacionRecord rec : activos) {
            if (rec.getFechaExpiracion().isBefore(hoy)) {
                rec.setEstado("PERDIDO");
                rec.setDiasPendientes(BigDecimal.ZERO);
                recordRepository.save(rec);
                log.debug("VacacionRecord {} expirado (PERDIDO)", rec.getId());
            }
        }

        // Find active contratos with 12+ months of service
        List<Contrato> contratos = contratoRepository.findAll().stream()
            .filter(c -> c.getEstado() == EstadoContrato.ACTIVO)
            .filter(c -> ChronoUnit.MONTHS.between(c.getFechaInicio(), hoy) >= 12)
            .toList();

        List<VacacionRecordResponse> results = new ArrayList<>();

        for (Contrato contrato : contratos) {
            Trabajador trabajador = contrato.getTrabajador();
            LocalDate fechaInicioRecord = contrato.getFechaInicio();
            LocalDate fechaFinRecord = fechaInicioRecord.plusYears(1);

            // Skip if record already exists for this period
            if (recordRepository.existsByTrabajadorIdAndFechaInicio(trabajador.getId(), fechaInicioRecord)) {
                recordRepository.findByTrabajadorIdAndFechaInicio(trabajador.getId(), fechaInicioRecord)
                    .ifPresent(r -> results.add(VacacionRecordResponse.fromEntity(r)));
                continue;
            }

            // If 12+ months reduction, derecho is extinguished — skip
            if (diasReduccion >= 12) {
                log.debug("Derecho extinguido para trabajadorId={}: {} meses de reducción",
                    trabajador.getId(), diasReduccion);
                continue;
            }

            LocalDate fechaExpiracion = fechaFinRecord.plusYears(1);

            // Dias pendientes = 15 - (diasReduccion × 1.25), min 0
            BigDecimal reduccion = BigDecimal.valueOf(diasReduccion)
                .multiply(new BigDecimal("1.25"))
                .setScale(2, RoundingMode.HALF_UP);
            BigDecimal pendientes = BigDecimal.valueOf(DIAS_PEQUENA_EMPRESA)
                .subtract(reduccion);
            if (pendientes.compareTo(BigDecimal.ZERO) < 0) {
                pendientes = BigDecimal.ZERO;
            }

            VacacionRecord record = new VacacionRecord();
            record.setTrabajador(trabajador);
            record.setContrato(contrato);
            record.setFechaInicio(fechaInicioRecord);
            record.setFechaFin(fechaFinRecord);
            record.setDiasDerecho(DIAS_PEQUENA_EMPRESA);
            record.setDiasReduccion(diasReduccion);
            record.setDiasPendientes(pendientes);
            record.setEstado("ACTIVO");
            record.setFechaExpiracion(fechaExpiracion);

            record = recordRepository.save(record);
            results.add(VacacionRecordResponse.fromEntity(record));
            log.debug("VacacionRecord creado: trabajadorId={}, periodo={} a {}, pendientes={}",
                trabajador.getId(), fechaInicioRecord, fechaFinRecord, pendientes);
        }

        return results;
    }

    /**
     * Program a vacation goce period. Validates minimum 7 days and available balance.
     */
    public VacacionGoceResponse programar(@Valid ProgramarRequest request) {
        // Find trabajador
        Trabajador trabajador = trabajadorRepository.findById(request.trabajadorId())
            .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado: " + request.trabajadorId()));

        // Find the active record
        List<VacacionRecord> records = recordRepository.findByTrabajadorIdOrderByFechaInicioDesc(request.trabajadorId());
        VacacionRecord record = records.stream()
            .filter(r -> "ACTIVO".equals(r.getEstado()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Trabajador " + request.trabajadorId() + " no tiene registro vacacional activo"));

        // Validate available days
        if (record.getDiasPendientes().compareTo(BigDecimal.valueOf(request.dias())) < 0) {
            throw new IllegalArgumentException(
                "Días solicitados (" + request.dias() + ") exceden días pendientes (" + record.getDiasPendientes() + ")");
        }

        // Calculate end date
        LocalDate fechaFin = request.fechaInicio().plusDays(request.dias() - 1);

        // Remuneración = sueldo base (from contrato) + asignación familiar
        BigDecimal remuneracion = record.getContrato() != null
            ? record.getContrato().getRemuneracion()
            : BigDecimal.ZERO;
        if (trabajador.getCantidadHijos() != null && trabajador.getCantidadHijos() >= 1) {
            remuneracion = remuneracion.add(
                BigDecimal.valueOf(properties.getRmv()).multiply(ASIGNACION_FAMILIAR_PCT)
                    .setScale(2, RoundingMode.HALF_UP));
        }

        VacacionGoce goce = new VacacionGoce();
        goce.setRecord(record);
        goce.setFechaInicio(request.fechaInicio());
        goce.setFechaFin(fechaFin);
        goce.setDias(request.dias());
        goce.setRemuneracion(remuneracion);
        goce.setEstado("PROGRAMADO");

        goce = goceRepository.save(goce);
        log.debug("VacacionGoce programado: recordId={}, inicio={}, dias={}",
            record.getId(), request.fechaInicio(), request.dias());

        return VacacionGoceResponse.fromEntity(goce);
    }

    /**
     * Start a programmed vacation (EN_CURSO).
     */
    public VacacionGoceResponse iniciar(Long goceId) {
        VacacionGoce goce = goceRepository.findById(goceId)
            .orElseThrow(() -> new EntityNotFoundException("Goce no encontrado: " + goceId));

        if (!"PROGRAMADO".equals(goce.getEstado())) {
            throw new IllegalStateException(
                "Goce " + goceId + " en estado " + goce.getEstado() + ", no se puede iniciar");
        }

        goce.setEstado("EN_CURSO");
        goce = goceRepository.save(goce);
        log.debug("VacacionGoce {} iniciado (EN_CURSO)", goceId);
        return VacacionGoceResponse.fromEntity(goce);
    }

    /**
     * Complete a vacation (COMPLETADO). Updates the record's pending days.
     */
    public VacacionGoceResponse completar(Long goceId) {
        VacacionGoce goce = goceRepository.findById(goceId)
            .orElseThrow(() -> new EntityNotFoundException("Goce no encontrado: " + goceId));

        if (!"EN_CURSO".equals(goce.getEstado())) {
            throw new IllegalStateException(
                "Goce " + goceId + " en estado " + goce.getEstado() + ", no se puede completar");
        }

        goce.setEstado("COMPLETADO");
        goce = goceRepository.save(goce);

        // Update record pending days
        VacacionRecord record = goce.getRecord();
        int gozados = goceRepository.findByRecordIdAndEstado(record.getId(), "COMPLETADO")
            .stream().mapToInt(VacacionGoce::getDias).sum();
        BigDecimal pendientes = BigDecimal.valueOf(record.getDiasDerecho())
            .subtract(BigDecimal.valueOf(record.getDiasReduccion()).multiply(new BigDecimal("1.25")))
            .subtract(BigDecimal.valueOf(gozados))
            .setScale(2, RoundingMode.HALF_UP);
        if (pendientes.compareTo(BigDecimal.ZERO) < 0) {
            pendientes = BigDecimal.ZERO;
        }
        record.setDiasPendientes(pendientes);

        if (record.getDiasPendientes().compareTo(BigDecimal.ZERO) == 0) {
            record.setEstado("COMPLETADO");
        }
        recordRepository.save(record);

        log.debug("VacacionGoce {} completado. Record {}: {} días pendientes",
            goceId, record.getId(), record.getDiasPendientes());
        return VacacionGoceResponse.fromEntity(goce);
    }

    /**
     * Cancel a programmed or in-progress vacation (CANCELADO).
     */
    public VacacionGoceResponse cancelar(Long goceId) {
        VacacionGoce goce = goceRepository.findById(goceId)
            .orElseThrow(() -> new EntityNotFoundException("Goce no encontrado: " + goceId));

        if (!"PROGRAMADO".equals(goce.getEstado()) && !"EN_CURSO".equals(goce.getEstado())) {
            throw new IllegalStateException(
                "Goce " + goceId + " en estado " + goce.getEstado() + ", no se puede cancelar");
        }

        goce.setEstado("CANCELADO");
        goce = goceRepository.save(goce);
        log.debug("VacacionGoce {} cancelado", goceId);
        return VacacionGoceResponse.fromEntity(goce);
    }

    @Transactional(readOnly = true)
    public List<VacacionRecordResponse> findRecordsByTrabajador(Long trabajadorId) {
        return recordRepository.findByTrabajadorIdOrderByFechaInicioDesc(trabajadorId)
            .stream().map(VacacionRecordResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<VacacionGoceResponse> findGocesByRecord(Long recordId) {
        return goceRepository.findByRecordIdOrderByFechaInicioAsc(recordId)
            .stream().map(VacacionGoceResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public VacacionRecordResponse findRecordById(Long id) {
        return recordRepository.findById(id)
            .map(VacacionRecordResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Record no encontrado: " + id));
    }
}

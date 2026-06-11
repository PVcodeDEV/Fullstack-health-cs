package com.clinica.rrhh.planilla.service;

import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.maestro.entity.rrhh.AfpTasaHistorica;
import com.clinica.maestro.entity.rrhh.ConceptoPlanilla;
import com.clinica.maestro.repository.rrhh.AfpRepository;
import com.clinica.maestro.repository.rrhh.ConceptoPlanillaRepository;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.planilla.config.PlanillaProperties;
import com.clinica.rrhh.planilla.dto.PlanillaResponse;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.entity.Planilla;
import com.clinica.rrhh.planilla.entity.PlanillaDetalle;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planilla.repository.PlanillaDetalleRepository;
import com.clinica.rrhh.planilla.repository.PlanillaRepository;
import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import com.clinica.rrhh.pension.repository.InformacionPensionariaRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlanillaLiquidacionService {

    private static final Logger log = LoggerFactory.getLogger(PlanillaLiquidacionService.class);
    private static final BigDecimal ONP_RATE = new BigDecimal("0.13");
    private static final BigDecimal ESSALUD_RATE = new BigDecimal("0.09");
    private static final BigDecimal ASIGNACION_FAMILIAR_PCT = new BigDecimal("0.10");

    private final PeriodoPlanillaRepository periodoPlanillaRepository;
    private final PlanillaRepository planillaRepository;
    private final PlanillaDetalleRepository planillaDetalleRepository;
    private final ContratoRepository contratoRepository;
    private final InformacionPensionariaRepository pensionRepository;
    private final AfpRepository afpRepository;
    private final ConceptoPlanillaRepository conceptoRepository;
    private final Renta5taCalculator renta5taCalculator;
    private final PlanillaProperties properties;

    public PlanillaLiquidacionService(PeriodoPlanillaRepository periodoPlanillaRepository,
                                       PlanillaRepository planillaRepository,
                                       PlanillaDetalleRepository planillaDetalleRepository,
                                       ContratoRepository contratoRepository,
                                       InformacionPensionariaRepository pensionRepository,
                                       AfpRepository afpRepository,
                                       ConceptoPlanillaRepository conceptoRepository,
                                       Renta5taCalculator renta5taCalculator,
                                       PlanillaProperties properties) {
        this.periodoPlanillaRepository = periodoPlanillaRepository;
        this.planillaRepository = planillaRepository;
        this.planillaDetalleRepository = planillaDetalleRepository;
        this.contratoRepository = contratoRepository;
        this.pensionRepository = pensionRepository;
        this.afpRepository = afpRepository;
        this.conceptoRepository = conceptoRepository;
        this.renta5taCalculator = renta5taCalculator;
        this.properties = properties;
    }

    public PlanillaResponse generar(Long periodoPlanillaId) {
        PeriodoPlanilla periodo = periodoPlanillaRepository.findById(periodoPlanillaId)
            .orElseThrow(() -> new EntityNotFoundException("Periodo no encontrado: " + periodoPlanillaId));

        if (!"ABIERTO".equals(periodo.getEstado())) {
            throw new IllegalStateException("El periodo no está ABIERTO");
        }

        if (planillaRepository.existsByPeriodoPlanillaId(periodoPlanillaId)) {
            throw new IllegalArgumentException("Ya existe una planilla para este periodo");
        }

        Map<String, ConceptoPlanilla> conceptos = conceptoRepository.findAllByActivoTrueOrderByOrden()
            .stream().collect(Collectors.toMap(ConceptoPlanilla::getCodigo, c -> c));

        List<Contrato> contratosActivos = contratoRepository.findAll().stream()
            .filter(c -> c.getEstado() == EstadoContrato.ACTIVO)
            .toList();

        Planilla planilla = new Planilla();
        planilla.setPeriodoPlanilla(periodo);
        planilla.setFechaLiquidacion(LocalDate.now());

        java.util.List<PlanillaDetalle> detalles = new java.util.ArrayList<>();
        for (Contrato contrato : contratosActivos) {
            PlanillaDetalle detalle = liquidarTrabajador(contrato, periodo, conceptos);
            detalles.add(detalle);
        }

        // Compute totals
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalDescuentos = BigDecimal.ZERO;
        BigDecimal totalAportes = BigDecimal.ZERO;
        BigDecimal totalNeto = BigDecimal.ZERO;
        for (PlanillaDetalle d : detalles) {
            totalIngresos = totalIngresos.add(d.getTotalIngresos());
            totalDescuentos = totalDescuentos.add(d.getTotalDescuentos());
            totalAportes = totalAportes.add(d.getTotalAportes());
            totalNeto = totalNeto.add(d.getNeto());
        }

        planilla.setTotalIngresos(totalIngresos);
        planilla.setTotalDescuentos(totalDescuentos);
        planilla.setTotalAportes(totalAportes);
        planilla.setTotalNeto(totalNeto);
        planilla.setCantidadTrabajadores(detalles.size());
        planilla.setEstado(detalles.isEmpty() ? "BORRADOR" : "LIQUIDADO");

        // Persist planilla header FIRST so it gets an ID
        planilla = planillaRepository.save(planilla);

        // Now persist each detalle with the saved planilla reference
        for (PlanillaDetalle detalle : detalles) {
            detalle.setPlanilla(planilla);
            planillaDetalleRepository.save(detalle);
        }
        log.debug("Planilla generada id={}, periodo={}-{}, trabajadores={}",
            planilla.getId(), periodo.getAnio(), periodo.getMes(), detalles.size());

        return PlanillaResponse.fromEntity(planilla);
    }

    private PlanillaDetalle liquidarTrabajador(Contrato contrato, PeriodoPlanilla periodo,
                                                Map<String, ConceptoPlanilla> conceptos) {
        Trabajador trabajador = contrato.getTrabajador();
        BigDecimal sueldoBase = contrato.getRemuneracion();
        BigDecimal rmv = BigDecimal.valueOf(properties.getRmv());

        BigDecimal ingresos = sueldoBase;
        BigDecimal descuentos = BigDecimal.ZERO;
        BigDecimal aportes = BigDecimal.ZERO;

        // Asignación familiar (10% RMV if >= 1 hijo)
        BigDecimal asignacionFamiliar = BigDecimal.ZERO;
        if (trabajador.getCantidadHijos() != null && trabajador.getCantidadHijos() >= 1) {
            asignacionFamiliar = rmv.multiply(ASIGNACION_FAMILIAR_PCT).setScale(2, RoundingMode.HALF_UP);
            ingresos = ingresos.add(asignacionFamiliar);
        }

        // Pension deduction (AFP or ONP)
        Optional<InformacionPensionaria> pensionOpt = pensionRepository.findByTrabajadorId(trabajador.getId());
        BigDecimal descuentoPension = BigDecimal.ZERO;
        BigDecimal aporteEssalud = BigDecimal.ZERO;

        if (pensionOpt.isPresent()) {
            InformacionPensionaria pension = pensionOpt.get();
            Afp afp = pension.getAfp();

            if ("ONP".equals(afp.getCodigo())) {
                descuentoPension = sueldoBase.multiply(ONP_RATE).setScale(2, RoundingMode.HALF_UP);
            } else {
                Optional<AfpTasaHistorica> tasaOpt = afpRepository.findCurrentRateByAfpId(afp.getId());
                if (tasaOpt.isPresent()) {
                    AfpTasaHistorica tasa = tasaOpt.get();
                    BigDecimal tasaComision = tasa.getTasa();
                    BigDecimal prima = tasa.getPrimaSeguro();
                    descuentoPension = sueldoBase.multiply(tasaComision.add(prima))
                        .setScale(2, RoundingMode.HALF_UP);
                } else {
                    log.warn("No se encontró tasa vigente para AFP {} (trabajadorId={})",
                        afp.getCodigo(), trabajador.getId());
                }
            }

            // EsSalud: 9% employer aporte
            aporteEssalud = sueldoBase.multiply(ESSALUD_RATE).setScale(2, RoundingMode.HALF_UP);
        }

        descuentos = descuentos.add(descuentoPension);
        aportes = aportes.add(aporteEssalud);

        // Renta 5ta Categoría
        BigDecimal renta5ta = renta5taCalculator.calcular(sueldoBase, BigDecimal.ZERO, 0);
        descuentos = descuentos.add(renta5ta);

        // Build conceptos JSON
        StringBuilder json = new StringBuilder("[");
        appendConcepto(json, "BASICO", sueldoBase, conceptos);
        if (asignacionFamiliar.compareTo(BigDecimal.ZERO) > 0) {
            json.append(",");
            appendConcepto(json, "ASIGNACION_FAMILIAR", asignacionFamiliar, conceptos);
        }
        if (descuentoPension.compareTo(BigDecimal.ZERO) > 0) {
            json.append(",");
            String codigo = pensionOpt.isPresent() && "ONP".equals(pensionOpt.get().getAfp().getCodigo())
                ? "ONP_DESCUENTO" : "AFP_OBLIGATORIO";
            appendConcepto(json, codigo, descuentoPension, conceptos);
        }
        if (aporteEssalud.compareTo(BigDecimal.ZERO) > 0) {
            json.append(",");
            appendConcepto(json, "ESSALUD_APORTE", aporteEssalud, conceptos);
        }
        if (renta5ta.compareTo(BigDecimal.ZERO) > 0) {
            json.append(",");
            appendConcepto(json, "RENTA_5TA", renta5ta, conceptos);
        }
        json.append("]");

        BigDecimal neto = ingresos.subtract(descuentos);

        PlanillaDetalle detalle = new PlanillaDetalle();
        detalle.setTrabajador(trabajador);
        detalle.setContrato(contrato);
        detalle.setSueldoBase(sueldoBase);
        detalle.setAsignacionFamiliar(asignacionFamiliar);
        detalle.setDiasLaborados(30);
        detalle.setTotalIngresos(ingresos);
        detalle.setTotalDescuentos(descuentos);
        detalle.setTotalAportes(aportes);
        detalle.setNeto(neto);
        detalle.setConceptosJson(json.toString());

        return detalle;
    }

    private void appendConcepto(StringBuilder json, String codigo, BigDecimal monto,
                                 Map<String, ConceptoPlanilla> conceptos) {
        ConceptoPlanilla c = conceptos.get(codigo);
        json.append("{\"codigo\":\"").append(codigo)
            .append("\",\"nombre\":\"").append(c != null ? c.getNombre() : "")
            .append("\",\"tipo\":\"").append(c != null ? c.getTipo() : "")
            .append("\",\"monto\":").append(monto)
            .append("}");
    }
}

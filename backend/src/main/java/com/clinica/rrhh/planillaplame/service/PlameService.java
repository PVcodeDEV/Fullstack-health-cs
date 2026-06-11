package com.clinica.rrhh.planillaplame.service;

import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.maestro.entity.rrhh.AfpTasaHistorica;
import com.clinica.maestro.repository.rrhh.AfpRepository;
import com.clinica.rrhh.cts.entity.DepositoCts;
import com.clinica.rrhh.cts.repository.DepositoCtsRepository;
import com.clinica.rrhh.gratificacion.entity.Gratificacion;
import com.clinica.rrhh.gratificacion.repository.GratificacionRepository;
import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import com.clinica.rrhh.pension.repository.InformacionPensionariaRepository;
import com.clinica.rrhh.planilla.config.PlanillaProperties;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.entity.Planilla;
import com.clinica.rrhh.planilla.entity.PlanillaDetalle;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planilla.repository.PlanillaDetalleRepository;
import com.clinica.rrhh.planilla.repository.PlanillaRepository;
import com.clinica.rrhh.planilla.service.Renta5taCalculator;
import com.clinica.rrhh.planillaplame.dto.ArchivoPlanillaResponse;
import com.clinica.rrhh.planillaplame.entity.ArchivoPlanilla;
import com.clinica.rrhh.planillaplame.repository.ArchivoPlanillaRepository;
import com.clinica.rrhh.vacacion.entity.VacacionGoce;
import com.clinica.rrhh.vacacion.repository.VacacionGoceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlameService {

    private static final Logger log = LoggerFactory.getLogger(PlameService.class);

    private static final BigDecimal ONP_RATE = new BigDecimal("0.13");
    private static final BigDecimal ESSALUD_RATE = new BigDecimal("0.09");
    private static final String DEFAULT_HORAS_ORDINARIAS = "240";
    private static final String DEFAULT_MINUTOS_ORDINARIOS = "0";
    private static final String DEFAULT_HORAS_SOBRETIEMPO = "0";
    private static final String DEFAULT_MINUTOS_SOBRETIEMPO = "0";

    private final PeriodoPlanillaRepository periodoPlanillaRepository;
    private final PlanillaRepository planillaRepository;
    private final PlanillaDetalleRepository planillaDetalleRepository;
    private final GratificacionRepository gratificacionRepository;
    private final DepositoCtsRepository depositoCtsRepository;
    private final VacacionGoceRepository vacacionGoceRepository;
    private final InformacionPensionariaRepository pensionRepository;
    private final AfpRepository afpRepository;
    private final ArchivoPlanillaRepository archivoRepository;
    private final Renta5taCalculator renta5taCalculator;
    private final PlanillaProperties properties;

    public PlameService(PeriodoPlanillaRepository periodoPlanillaRepository,
                        PlanillaRepository planillaRepository,
                        PlanillaDetalleRepository planillaDetalleRepository,
                        GratificacionRepository gratificacionRepository,
                        DepositoCtsRepository depositoCtsRepository,
                        VacacionGoceRepository vacacionGoceRepository,
                        InformacionPensionariaRepository pensionRepository,
                        AfpRepository afpRepository,
                        ArchivoPlanillaRepository archivoRepository,
                        Renta5taCalculator renta5taCalculator,
                        PlanillaProperties properties) {
        this.periodoPlanillaRepository = periodoPlanillaRepository;
        this.planillaRepository = planillaRepository;
        this.planillaDetalleRepository = planillaDetalleRepository;
        this.gratificacionRepository = gratificacionRepository;
        this.depositoCtsRepository = depositoCtsRepository;
        this.vacacionGoceRepository = vacacionGoceRepository;
        this.pensionRepository = pensionRepository;
        this.afpRepository = afpRepository;
        this.archivoRepository = archivoRepository;
        this.renta5taCalculator = renta5taCalculator;
        this.properties = properties;
    }

    /**
     * Generates 5 SUNAT PLAME files (.rem, .jor, .snl, .or5, .toc) for the given
     * CERRADO period. Upserts one tb_archivos_planilla row per file type.
     *
     * @return list of 5 ArchivoPlanillaResponse metadata records
     */
    public List<ArchivoPlanillaResponse> generar(Long periodoPlanillaId) {
        PeriodoPlanilla periodo = periodoPlanillaRepository.findById(periodoPlanillaId)
                .orElseThrow(() -> new EntityNotFoundException("Periodo no encontrado: " + periodoPlanillaId));

        if (!"CERRADO".equals(periodo.getEstado())) {
            throw new IllegalStateException("El periodo debe estar CERRADO para generar PLAME");
        }

        // Find liquidated Planilla for this period
        Planilla planilla = planillaRepository.findByPeriodoPlanillaId(periodoPlanillaId)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe planilla liquidada para el periodo " + periodoPlanillaId));
        List<PlanillaDetalle> detalles = planillaDetalleRepository.findByPlanillaId(planilla.getId());

        if (detalles.isEmpty()) {
            log.warn("Planilla vacía — no hay trabajadores para generar PLAME en periodoId={}", periodoPlanillaId);
        }

        // Fetch period benefits data
        Map<Long, Gratificacion> gratifMap = gratificacionRepository.findByPeriodoPlanillaId(periodoPlanillaId)
                .stream().collect(Collectors.toMap(g -> g.getTrabajador().getId(), g -> g,
                        (a, b) -> a));

        Map<Long, DepositoCts> ctsMap = depositoCtsRepository.findByPeriodoPlanillaId(periodoPlanillaId)
                .stream().collect(Collectors.toMap(c -> c.getTrabajador().getId(), c -> c,
                        (a, b) -> a));

        // Fetch completed vacations ending within this period
        List<VacacionGoce> vacaciones = vacacionGoceRepository
                .findByEstadoAndFechaFinBetween("COMPLETADO", periodo.getFechaInicio(), periodo.getFechaFin());
        Map<Long, List<VacacionGoce>> vacacionesPorTrabajador = vacaciones.stream()
                .collect(Collectors.groupingBy(v -> v.getRecord().getTrabajador().getId()));

        String rucEmpleador = properties.getRucEmpleador();

        // Build file contents
        StringBuilder remSb = new StringBuilder();
        StringBuilder jorSb = new StringBuilder();
        StringBuilder snlSb = new StringBuilder();
        StringBuilder or5Sb = new StringBuilder();
        StringBuilder tocSb = new StringBuilder();

        for (PlanillaDetalle detalle : detalles) {
            var trabajador = detalle.getTrabajador();
            var persona = trabajador.getPersona();
            String tipoDoc = obtenerCodigoSunat(persona.getTipoDocumentoIdentidad().getCodigoSunat());
            String nroDoc = persona.getNumeroDocumento() != null ? persona.getNumeroDocumento() : "";
            Long trabId = trabajador.getId();
            BigDecimal sueldoBase = Optional.ofNullable(detalle.getSueldoBase()).orElse(BigDecimal.ZERO);

            // --- .rem (Estructura 18): one line per concept per worker ---
            // 0121 — Remuneración o jornal básico
            appendRemLine(remSb, tipoDoc, nroDoc, "0121", sueldoBase);

            // 0201 — Asignación Familiar
            BigDecimal asigFamiliar = Optional.ofNullable(detalle.getAsignacionFamiliar()).orElse(BigDecimal.ZERO);
            if (asigFamiliar.compareTo(BigDecimal.ZERO) > 0) {
                appendRemLine(remSb, tipoDoc, nroDoc, "0201", asigFamiliar);
            }

            // 0401 — Gratificación (if exists for this period)
            Gratificacion gratif = gratifMap.get(trabId);
            if (gratif != null) {
                BigDecimal gratifValor = Optional.ofNullable(gratif.getTotal()).orElse(BigDecimal.ZERO);
                if (gratifValor.compareTo(BigDecimal.ZERO) > 0) {
                    appendRemLine(remSb, tipoDoc, nroDoc, "0401", gratifValor);
                }
            }

            // 0904 — CTS (if exists for this period)
            DepositoCts cts = ctsMap.get(trabId);
            if (cts != null) {
                BigDecimal ctsValor = Optional.ofNullable(cts.getMontoCts()).orElse(BigDecimal.ZERO);
                if (ctsValor.compareTo(BigDecimal.ZERO) > 0) {
                    appendRemLine(remSb, tipoDoc, nroDoc, "0904", ctsValor);
                }
            }

            // 0118 — Remuneración vacacional (completed vacations in period)
            List<VacacionGoce> vacsTrab = vacacionesPorTrabajador.get(trabId);
            if (vacsTrab != null && !vacsTrab.isEmpty()) {
                BigDecimal totalVac = vacsTrab.stream()
                        .map(v -> Optional.ofNullable(v.getRemuneracion()).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (totalVac.compareTo(BigDecimal.ZERO) > 0) {
                    appendRemLine(remSb, tipoDoc, nroDoc, "0118", totalVac);
                }
            }

            // Pension regime and deductions
            Optional<InformacionPensionaria> pensionOpt = pensionRepository.findByTrabajadorId(trabId);
            boolean isAfp = false;
            BigDecimal afpDescuento = BigDecimal.ZERO;
            BigDecimal onpDescuento = BigDecimal.ZERO;

            if (pensionOpt.isPresent()) {
                InformacionPensionaria pension = pensionOpt.get();
                Afp afp = pension.getAfp();

                if ("ONP".equals(afp.getCodigo())) {
                    // 0607 — SNP / ONP
                    onpDescuento = sueldoBase.multiply(ONP_RATE).setScale(2, RoundingMode.HALF_UP);
                    appendRemLine(remSb, tipoDoc, nroDoc, "0607", onpDescuento);
                } else {
                    // AFP — 0608 SPP Aportación Obligatoria
                    isAfp = true;
                    Optional<AfpTasaHistorica> tasaOpt = afpRepository.findCurrentRateByAfpId(afp.getId());
                    if (tasaOpt.isPresent()) {
                        AfpTasaHistorica tasa = tasaOpt.get();
                        BigDecimal tasaComision = tasa.getTasa();
                        BigDecimal prima = tasa.getPrimaSeguro();
                        afpDescuento = sueldoBase.multiply(tasaComision.add(prima))
                                .setScale(2, RoundingMode.HALF_UP);
                    } else {
                        log.warn("No se encontró tasa vigente para AFP {} (trabajadorId={})",
                                afp.getCodigo(), trabId);
                    }
                    if (afpDescuento.compareTo(BigDecimal.ZERO) > 0) {
                        appendRemLine(remSb, tipoDoc, nroDoc, "0608", afpDescuento);
                    }
                }

                // 0804 — EsSalud Seguro Regular (9% employer aporte)
                BigDecimal essalud = sueldoBase.multiply(ESSALUD_RATE).setScale(2, RoundingMode.HALF_UP);
                appendRemLine(remSb, tipoDoc, nroDoc, "0804", essalud);
            }

            // 0605 — Renta Quinta Categoría
            BigDecimal renta5ta = renta5taCalculator.calcular(sueldoBase, BigDecimal.ZERO, 0);
            if (renta5ta.compareTo(BigDecimal.ZERO) > 0) {
                appendRemLine(remSb, tipoDoc, nroDoc, "0605", renta5ta);
            }

            // --- .jor (Estructura 14): hours ---
            jorSb.append(tipoDoc).append('|')
                    .append(nroDoc).append('|')
                    .append(DEFAULT_HORAS_ORDINARIAS).append('|')
                    .append(DEFAULT_MINUTOS_ORDINARIOS).append('|')
                    .append(DEFAULT_HORAS_SOBRETIEMPO).append('|')
                    .append(DEFAULT_MINUTOS_SOBRETIEMPO).append('\n');

            // --- .snl (Estructura 15): suspensions — empty for now ---
            // (No suspension tracking in v1; file stays empty)

            // --- .or5 (Estructura 12): otras rentas 5ta — empty for now ---
            // (No multi-employer tracking in v1; file stays empty)

            // --- .toc (Estructura 26): conditions ---
            String indicadorPension;
            if (pensionOpt.isEmpty()) {
                indicadorPension = "0"; // ninguno
            } else if (isAfp) {
                indicadorPension = "1"; // AFP
            } else {
                indicadorPension = "2"; // ONP
            }

            tocSb.append(tipoDoc).append('|')
                    .append(nroDoc).append('|')
                    .append(indicadorPension).append('|')
                    .append("0").append('|')   // IndicadorSeguroVida
                    .append("0").append('|')   // IndicadorFDSA
                    .append("1")               // Domiciliado
                    .append('\n');
        }

        // Upsert each file type
        List<ArchivoPlanillaResponse> results = new ArrayList<>();
        results.add(upsertArchivo(periodo, "REM", remSb.toString(), rucEmpleador));
        results.add(upsertArchivo(periodo, "JOR", jorSb.toString(), rucEmpleador));
        results.add(upsertArchivo(periodo, "SNL", snlSb.toString(), rucEmpleador));
        results.add(upsertArchivo(periodo, "OR5", or5Sb.toString(), rucEmpleador));
        results.add(upsertArchivo(periodo, "TOC", tocSb.toString(), rucEmpleador));

        log.debug("PLAME generado para periodo {}: {} trabajadores, 5 archivos",
                periodoPlanillaId, detalles.size());
        return results;
    }

    @Transactional(readOnly = true)
    public List<ArchivoPlanillaResponse> getArchivos(Long periodoPlanillaId) {
        return archivoRepository.findByPeriodoPlanillaId(periodoPlanillaId)
                .stream()
                .map(ArchivoPlanillaResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArchivoPlanilla getArchivoParaDescarga(Long archivoId) {
        return archivoRepository.findById(archivoId)
                .orElseThrow(() -> new EntityNotFoundException("Archivo no encontrado: " + archivoId));
    }

    @Transactional(readOnly = true)
    public ArchivoPlanilla getArchivoPorPeriodoTipo(Long periodoPlanillaId, String tipo) {
        return archivoRepository.findByPeriodoPlanillaIdAndTipo(periodoPlanillaId, tipo)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Archivo PLAME tipo " + tipo + " no encontrado para periodo " + periodoPlanillaId));
    }

    @Transactional(readOnly = true)
    public List<ArchivoPlanilla> getArchivosPorPeriodo(Long periodoPlanillaId) {
        return archivoRepository.findByPeriodoPlanillaId(periodoPlanillaId);
    }

    private ArchivoPlanillaResponse upsertArchivo(PeriodoPlanilla periodo, String tipo,
                                                   String contenido, String rucEmpleador) {
        ArchivoPlanilla archivo = archivoRepository
                .findByPeriodoPlanillaIdAndTipo(periodo.getId(), tipo)
                .orElseGet(ArchivoPlanilla::new);
        archivo.setPeriodoPlanilla(periodo);
        archivo.setTipo(tipo);
        archivo.setContenido(contenido);
        archivo.setHash(sha256(contenido));
        archivo.setGeneradoPor("SYSTEM");
        archivo = archivoRepository.save(archivo);
        return ArchivoPlanillaResponse.fromEntity(archivo);
    }

    private void appendRemLine(StringBuilder sb, String tipoDoc, String nroDoc,
                                String codigoConcepto, BigDecimal monto) {
        String montoStr = monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
        sb.append(tipoDoc).append('|')
                .append(nroDoc).append('|')
                .append(codigoConcepto).append('|')
                .append(montoStr).append('|')
                .append(montoStr).append('\n');
    }

    private String sha256(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    static String obtenerCodigoSunat(String codigoTipoDoc) {
        return switch (codigoTipoDoc) {
            case "01" -> "1";   // DNI
            case "04" -> "4";   // CE
            case "06" -> "6";   // RUC
            case "07" -> "7";   // Pasaporte
            default -> "0";
        };
    }
}

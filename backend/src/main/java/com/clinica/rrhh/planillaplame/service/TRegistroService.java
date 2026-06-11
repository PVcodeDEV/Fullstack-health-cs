package com.clinica.rrhh.planillaplame.service;

import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import com.clinica.rrhh.pension.repository.InformacionPensionariaRepository;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planillaplame.dto.ArchivoPlanillaResponse;
import com.clinica.rrhh.planillaplame.dto.TRegistroEventoResponse;
import com.clinica.rrhh.planillaplame.entity.ArchivoPlanilla;
import com.clinica.rrhh.planillaplame.entity.TRegistroEvento;
import com.clinica.rrhh.planillaplame.repository.ArchivoPlanillaRepository;
import com.clinica.rrhh.planillaplame.repository.TRegistroEventoRepository;
import com.clinica.rrhh.type.EstadoContrato;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TRegistroService {

    private static final Logger log = LoggerFactory.getLogger(TRegistroService.class);

    private final PeriodoPlanillaRepository periodoPlanillaRepository;
    private final ContratoRepository contratoRepository;
    private final InformacionPensionariaRepository pensionRepository;
    private final TRegistroEventoRepository eventoRepository;
    private final ArchivoPlanillaRepository archivoRepository;

    public TRegistroService(PeriodoPlanillaRepository periodoPlanillaRepository,
                            ContratoRepository contratoRepository,
                            InformacionPensionariaRepository pensionRepository,
                            TRegistroEventoRepository eventoRepository,
                            ArchivoPlanillaRepository archivoRepository) {
        this.periodoPlanillaRepository = periodoPlanillaRepository;
        this.contratoRepository = contratoRepository;
        this.pensionRepository = pensionRepository;
        this.eventoRepository = eventoRepository;
        this.archivoRepository = archivoRepository;
    }

    public ArchivoPlanillaResponse generar(Long periodoPlanillaId) {
        PeriodoPlanilla periodo = periodoPlanillaRepository.findById(periodoPlanillaId)
                .orElseThrow(() -> new EntityNotFoundException("Periodo no encontrado: " + periodoPlanillaId));

        if (!"CERRADO".equals(periodo.getEstado())) {
            throw new IllegalStateException("El periodo debe estar CERRADO para generar T-Registro");
        }

        List<TRegistroEvento> eventos = scanEventos(periodo);
        List<TRegistroEvento> saved = upsertEventos(eventos, periodo);
        String contenido = formatearTxt(saved);
        String hash = sha256(contenido);

        ArchivoPlanilla archivo = archivoRepository
                .findByPeriodoPlanillaIdAndTipo(periodo.getId(), "T_REGISTRO")
                .orElseGet(ArchivoPlanilla::new);
        archivo.setPeriodoPlanilla(periodo);
        archivo.setTipo("T_REGISTRO");
        archivo.setContenido(contenido);
        archivo.setHash(hash);
        archivo.setGeneradoPor("SYSTEM");
        archivo = archivoRepository.save(archivo);

        log.debug("T-Registro generado para periodo {}", periodoPlanillaId);
        return ArchivoPlanillaResponse.fromEntity(archivo);
    }

    @Transactional(readOnly = true)
    public List<TRegistroEventoResponse> getEventos(Long periodoPlanillaId) {
        return eventoRepository.findByPeriodoPlanillaIdOrderByFechaEventoAsc(periodoPlanillaId)
                .stream()
                .map(TRegistroEventoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArchivoPlanilla getArchivoParaDescarga(Long archivoId) {
        return archivoRepository.findById(archivoId)
                .orElseThrow(() -> new EntityNotFoundException("Archivo no encontrado: " + archivoId));
    }

    private List<TRegistroEvento> scanEventos(PeriodoPlanilla periodo) {
        List<TRegistroEvento> eventos = new ArrayList<>();
        LocalDate inicio = periodo.getFechaInicio();
        LocalDate fin = periodo.getFechaFin();

        // ALTA: contratos creados en el periodo
        for (Contrato c : contratoRepository.findByFechaInicioBetween(inicio, fin)) {
            eventos.add(crearEvento(c, "ALTA", c.getFechaInicio(), null));
        }

        // BAJA: contratos terminados en el periodo
        for (Contrato c : contratoRepository.findByFechaFinBetween(inicio, fin)) {
            if (c.getFechaFin() != null) {
                String detalle = c.getMotivoCese() != null
                        ? "{\"motivo\":\"" + c.getMotivoCese() + "\"}"
                        : null;
                eventos.add(crearEvento(c, "BAJA", c.getFechaFin(), detalle));
            }
        }

        // SUSPENSION: contratos suspendidos en el periodo
        for (Contrato c : contratoRepository.findByEstadoAndUpdatedAtBetween(
                EstadoContrato.SUSPENDIDO, inicio.atStartOfDay(), fin.atTime(23, 59, 59))) {
            eventos.add(crearEvento(c, "SUSPENSION", c.getUpdatedAt().toLocalDate(), null));
        }

        // VARIACION: cambios pensionarios en el periodo
        for (InformacionPensionaria p : pensionRepository.findByUpdatedAtBetween(
                inicio.atStartOfDay(), fin.atTime(23, 59, 59))) {
            eventos.add(crearEventoPorPension(p, "VARIACION", p.getUpdatedAt().toLocalDate()));
        }

        return eventos;
    }

    private TRegistroEvento crearEvento(Contrato contrato, String tipo, LocalDate fecha, String detalle) {
        TRegistroEvento e = new TRegistroEvento();
        e.setTrabajador(contrato.getTrabajador());
        e.setContrato(contrato);
        e.setTipoEvento(tipo);
        e.setFechaEvento(fecha);
        e.setEstado("GENERADO");
        e.setDetalleJson(detalle);
        return e;
    }

    private TRegistroEvento crearEventoPorPension(InformacionPensionaria pension, String tipo, LocalDate fecha) {
        TRegistroEvento e = new TRegistroEvento();
        e.setTrabajador(pension.getTrabajador());
        e.setTipoEvento(tipo);
        e.setFechaEvento(fecha);
        e.setEstado("GENERADO");
        e.setDetalleJson("{\"tipo\":\"pension\"}");
        return e;
    }

    private List<TRegistroEvento> upsertEventos(List<TRegistroEvento> eventos, PeriodoPlanilla periodo) {
        List<TRegistroEvento> saved = new ArrayList<>();
        for (TRegistroEvento e : eventos) {
            e.setPeriodoPlanilla(periodo);
            saved.add(eventoRepository.save(e));
        }
        return saved;
    }

    private String formatearTxt(List<TRegistroEvento> eventos) {
        StringBuilder sb = new StringBuilder();
        for (TRegistroEvento e : eventos) {
            String tipoDoc = obtenerCodigoSunat(e.getTrabajador().getPersona()
                    .getTipoDocumentoIdentidad().getCodigoSunat());
            String nroDoc = e.getTrabajador().getPersona().getNumeroDocumento();
            sb.append(tipoDoc).append('|')
                    .append(nroDoc != null ? nroDoc : "").append('|')
                    .append(e.getTipoEvento()).append('|')
                    .append(e.getFechaEvento().format(DateTimeFormatter.ofPattern("yyyyMMdd"))).append('|')
                    .append(e.getDetalleJson() != null ? e.getDetalleJson() : "")
                    .append('\n');
        }
        return sb.toString();
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

    private static String obtenerCodigoSunat(String codigoTipoDoc) {
        return switch (codigoTipoDoc) {
            case "01" -> "1";   // DNI
            case "04" -> "4";   // CE
            case "06" -> "6";   // RUC
            case "07" -> "7";   // Pasaporte
            default -> "0";
        };
    }
}

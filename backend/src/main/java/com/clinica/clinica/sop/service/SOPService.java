package com.clinica.clinica.sop.service;

import com.clinica.clinica.sop.dto.*;
import com.clinica.clinica.sop.entity.*;
import com.clinica.clinica.sop.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class SOPService {

    private static final Logger log = LoggerFactory.getLogger(SOPService.class);

    private final ReporteQuirurgicoRepository reporteRepository;
    private final URPARegistroRepository urpaRegistroRepository;

    public SOPService(ReporteQuirurgicoRepository reporteRepository,
                      URPARegistroRepository urpaRegistroRepository) {
        this.reporteRepository = reporteRepository;
        this.urpaRegistroRepository = urpaRegistroRepository;
    }

    public ReporteQuirurgicoResponse crearReporte(ReporteQuirurgicoRequest request) {
        ReporteQuirurgico reporte = new ReporteQuirurgico();
        reporte.setHospitalizacionId(request.hospitalizacionId());
        reporte.setCirujanoId(request.cirujanoId());
        reporte.setAnestesiologoId(request.anestesiologoId());
        reporte.setDiagnosticoPre(request.diagnosticoPreoperatorio());
        reporte.setProcedimientoRealizado(request.procedimientoRealizado());
        reporte.setHallazgos(request.hallazgos());
        reporte.setComplicaciones(request.complicaciones());
        reporte.setFechaCirugia(request.fechaCirugia());
        reporte.setHoraInicio(request.horaInicio());
        reporte.setHoraFin(request.horaFin());
        reporte.setEstado(request.estado() != null ? request.estado() : "BORRADOR");
        reporte = reporteRepository.save(reporte);

        log.debug("Reporte quirúrgico creado id={}", reporte.getId());
        return toReporteResponse(reporte);
    }

    public ReporteQuirurgicoResponse completarReporte(Long reporteId) {
        ReporteQuirurgico reporte = reporteRepository.findById(reporteId)
            .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado con id: " + reporteId));

        if (!"BORRADOR".equals(reporte.getEstado())) {
            throw new IllegalStateException("Solo se pueden completar reportes en estado BORRADOR");
        }

        reporte.setEstado("COMPLETADO");
        reporte = reporteRepository.save(reporte);
        log.debug("Reporte quirúrgico completado id={}", reporte.getId());
        return toReporteResponse(reporte);
    }

    public URPARegistroResponse registrarURPA(Long reporteId, URPARegistroRequest request) {
        ReporteQuirurgico reporte = reporteRepository.findById(reporteId)
            .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado con id: " + reporteId));

        URPARegistro urpa = new URPARegistro();
        urpa.setReporteId(reporteId);
        urpa.setFechaIngresoURPA(LocalDateTime.now());
        urpa.setCondicionIngreso(request.condicionIngreso());
        urpa.setEscalaAldreteIngreso(request.escalaAldreteIngreso());
        urpa.setObservaciones(request.observaciones());
        urpa = urpaRegistroRepository.save(urpa);

        log.debug("URPA registrado id={} para reporteId={}", urpa.getId(), reporteId);
        return new URPARegistroResponse(
            urpa.getId(), urpa.getReporteId(),
            urpa.getFechaIngresoURPA(), urpa.getFechaSalidaURPA(),
            urpa.getCondicionIngreso(), urpa.getCondicionSalida(),
            urpa.getEscalaAldreteIngreso(), urpa.getEscalaAldreteSalida(),
            urpa.getObservaciones()
        );
    }

    private ReporteQuirurgicoResponse toReporteResponse(ReporteQuirurgico r) {
        return new ReporteQuirurgicoResponse(
            r.getId(), r.getHospitalizacionId(),
            r.getCirujanoId(), null, r.getAnestesiologoId(), null,
            r.getDiagnosticoPre(), r.getProcedimientoRealizado(),
            r.getHallazgos(), r.getComplicaciones(), r.getFechaCirugia(),
            r.getHoraInicio(), r.getHoraFin(), r.getEstado(),
            r.getCreatedAt()
        );
    }
}

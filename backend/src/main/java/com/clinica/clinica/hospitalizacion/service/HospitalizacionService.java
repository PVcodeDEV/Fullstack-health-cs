package com.clinica.clinica.hospitalizacion.service;

import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.entity.EstadoCama;
import com.clinica.clinica.cama.repository.CamaRepository;
import com.clinica.clinica.hospitalizacion.dto.*;
import com.clinica.clinica.hospitalizacion.entity.*;
import com.clinica.clinica.hospitalizacion.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class HospitalizacionService {

    private static final Logger log = LoggerFactory.getLogger(HospitalizacionService.class);

    private final HospitalizacionRepository hospitalizacionRepository;
    private final CambioHabitacionRepository cambioHabitacionRepository;
    private final NotaEvolucionRepository notaEvolucionRepository;
    private final SolicitudMedicamentoRepository solicitudMedicamentoRepository;
    private final AltaMedicaRepository altaMedicaRepository;
    private final CamaRepository camaRepository;

    public HospitalizacionService(HospitalizacionRepository hospitalizacionRepository,
                                  CambioHabitacionRepository cambioHabitacionRepository,
                                  NotaEvolucionRepository notaEvolucionRepository,
                                  SolicitudMedicamentoRepository solicitudMedicamentoRepository,
                                  AltaMedicaRepository altaMedicaRepository,
                                  CamaRepository camaRepository) {
        this.hospitalizacionRepository = hospitalizacionRepository;
        this.cambioHabitacionRepository = cambioHabitacionRepository;
        this.notaEvolucionRepository = notaEvolucionRepository;
        this.solicitudMedicamentoRepository = solicitudMedicamentoRepository;
        this.altaMedicaRepository = altaMedicaRepository;
        this.camaRepository = camaRepository;
    }

    public CambioHabitacionResponse cambiarCama(Long hospitalizacionId, CambioHabitacionRequest request, Long usuarioId) {
        Hospitalizacion hosp = hospitalizacionRepository.findById(hospitalizacionId)
            .orElseThrow(() -> new EntityNotFoundException("Hospitalización no encontrada con id: " + hospitalizacionId));

        Cama camaOrigen = camaRepository.findById(hosp.getCamaId())
            .orElseThrow(() -> new EntityNotFoundException("Cama origen no encontrada con id: " + hosp.getCamaId()));
        Cama camaDestino = camaRepository.findById(request.camaDestinoId())
            .orElseThrow(() -> new EntityNotFoundException("Cama destino no encontrada con id: " + request.camaDestinoId()));

        if (!camaDestino.isDisponible()) {
            throw new IllegalStateException("La cama destino no está disponible");
        }

        // Use cama state machine
        camaOrigen.liberar();
        camaDestino.ocupar();
        camaRepository.save(camaOrigen);
        camaRepository.save(camaDestino);

        hosp.setCamaId(request.camaDestinoId());
        hospitalizacionRepository.save(hosp);

        CambioHabitacion cambio = new CambioHabitacion();
        cambio.setHospitalizacionId(hospitalizacionId);
        cambio.setCamaOrigenId(camaOrigen.getId());
        cambio.setCamaDestinoId(camaDestino.getId());
        cambio.setUsuarioId(usuarioId);
        cambio.setFechaCambio(LocalDateTime.now());
        cambio.setMotivo(request.motivo());
        cambio = cambioHabitacionRepository.save(cambio);

        log.debug("Cambio de cama registrado: hospId={}, origen={}, destino={}", hospitalizacionId, camaOrigen.getCodigo(), camaDestino.getCodigo());

        return new CambioHabitacionResponse(
            cambio.getId(), cambio.getHospitalizacionId(),
            camaOrigen.getCodigo(), camaDestino.getCodigo(),
            cambio.getFechaCambio(), cambio.getMotivo(), String.valueOf(usuarioId)
        );
    }

    public NotaEvolucionResponse registrarNota(Long hospitalizacionId, NotaEvolucionRequest request, Long usuarioId) {
        Hospitalizacion hosp = hospitalizacionRepository.findById(hospitalizacionId)
            .orElseThrow(() -> new EntityNotFoundException("Hospitalización no encontrada con id: " + hospitalizacionId));

        NotaEvolucion nota = new NotaEvolucion();
        nota.setHospitalizacionId(hospitalizacionId);
        nota.setFechaHora(LocalDateTime.now());
        nota.setUsuarioId(usuarioId);
        nota.setDescripcion(request.descripcion());
        nota.setPlan(request.plan());
        nota.setTipo(request.tipo() != null ? request.tipo() : "EVOLUCION");
        nota.setSignosVitales(request.signosVitales());
        nota = notaEvolucionRepository.save(nota);

        log.debug("Nota registrada id={} para hospitalizacionId={}", nota.getId(), hospitalizacionId);
        return new NotaEvolucionResponse(
            nota.getId(), nota.getHospitalizacionId(), nota.getFechaHora(),
            String.valueOf(usuarioId), nota.getDescripcion(), nota.getPlan(),
            nota.getTipo(), nota.getSignosVitales()
        );
    }

    public SolicitudMedicamentoResponse solicitarMedicamento(Long hospitalizacionId, SolicitudMedicamentoRequest request, Long usuarioId) {
        Hospitalizacion hosp = hospitalizacionRepository.findById(hospitalizacionId)
            .orElseThrow(() -> new EntityNotFoundException("Hospitalización no encontrada con id: " + hospitalizacionId));

        SolicitudMedicamento sm = new SolicitudMedicamento();
        sm.setHospitalizacionId(hospitalizacionId);
        sm.setMedicamentoId(request.medicamentoId());
        sm.setDosis(request.dosis());
        sm.setFrecuencia(request.frecuencia());
        sm.setViaAdministracionId(request.viaAdministracionId());
        sm.setFechaInicio(request.fechaInicio());
        sm.setFechaFin(request.fechaFin());
        sm.setEstado("PENDIENTE");
        sm.setUsuarioId(usuarioId);
        sm = solicitudMedicamentoRepository.save(sm);

        log.debug("Solicitud medicamento id={} para hospitalizacionId={}", sm.getId(), hospitalizacionId);
        return new SolicitudMedicamentoResponse(
            sm.getId(), sm.getHospitalizacionId(), sm.getMedicamentoId(),
            null, sm.getDosis(), sm.getFrecuencia(), sm.getViaAdministracionId(),
            sm.getFechaInicio(), sm.getFechaFin(), sm.getEstado(), String.valueOf(usuarioId)
        );
    }

    public AltaMedicaResponse darAlta(Long hospitalizacionId, AltaMedicaRequest request) {
        Hospitalizacion hosp = hospitalizacionRepository.findById(hospitalizacionId)
            .orElseThrow(() -> new EntityNotFoundException("Hospitalización no encontrada con id: " + hospitalizacionId));

        if (!"HOSPITALIZADO".equals(hosp.getEstado())) {
            throw new IllegalStateException("La hospitalización no está activa");
        }

        // Registrar alta
        AltaMedica alta = new AltaMedica();
        alta.setHospitalizacionId(hospitalizacionId);
        alta.setFechaAlta(LocalDateTime.now());
        alta.setTipoAlta(request.tipoAlta());
        alta.setDiagnosticoFinal(request.diagnosticoFinal());
        alta.setMedicoId(request.medicoId());
        alta.setObservaciones(request.observaciones());
        alta = altaMedicaRepository.save(alta);

        // Actualizar hospitalización
        hosp.setEstado("ALTA");
        hosp.setFechaAlta(alta.getFechaAlta());
        hospitalizacionRepository.save(hosp);

        log.debug("Alta médica registrada id={} para hospitalizacionId={}", alta.getId(), hospitalizacionId);
        return new AltaMedicaResponse(
            alta.getId(), alta.getHospitalizacionId(), alta.getFechaAlta(),
            alta.getTipoAlta(), alta.getDiagnosticoFinal(),
            String.valueOf(request.medicoId()), alta.getObservaciones()
        );
    }
}

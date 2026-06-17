package com.clinica.clinica.admision.service;

import com.clinica.clinica.admision.dto.*;
import com.clinica.clinica.admision.entity.*;
import com.clinica.clinica.admision.repository.*;
import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.entity.EstadoCama;
import com.clinica.clinica.cama.repository.CamaRepository;
import com.clinica.clinica.hospitalizacion.entity.Hospitalizacion;
import com.clinica.clinica.hospitalizacion.repository.HospitalizacionRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdmisionService {

    private static final Logger log = LoggerFactory.getLogger(AdmisionService.class);

    private final CuentaRepository cuentaRepository;
    private final CuentaPaqueteRepository cuentaPaqueteRepository;
    private final SolicitudHospitalizacionRepository solicitudRepository;
    private final AdmisionDiagnosticoRepository diagnosticoRepository;
    private final PersonaRepository personaRepository;
    private final CamaRepository camaRepository;
    private final HospitalizacionRepository hospitalizacionRepository;

    public AdmisionService(CuentaRepository cuentaRepository,
                           CuentaPaqueteRepository cuentaPaqueteRepository,
                           SolicitudHospitalizacionRepository solicitudRepository,
                           AdmisionDiagnosticoRepository diagnosticoRepository,
                           PersonaRepository personaRepository,
                           CamaRepository camaRepository,
                           HospitalizacionRepository hospitalizacionRepository) {
        this.cuentaRepository = cuentaRepository;
        this.cuentaPaqueteRepository = cuentaPaqueteRepository;
        this.solicitudRepository = solicitudRepository;
        this.diagnosticoRepository = diagnosticoRepository;
        this.personaRepository = personaRepository;
        this.camaRepository = camaRepository;
        this.hospitalizacionRepository = hospitalizacionRepository;
    }

    @Transactional(readOnly = true)
    public List<Persona> buscarPaciente(String query) {
        if (query == null || query.isBlank()) return List.of();
        var byDni = personaRepository.findByNumeroDocumento(query);
        if (byDni.isPresent()) return List.of(byDni.get());
        List<Persona> results = new ArrayList<>();
        results.addAll(personaRepository.findByNombresContainingIgnoreCase(query));
        results.addAll(personaRepository.findByApellidoPaternoContainingIgnoreCase(query));
        return results;
    }

    @Transactional(readOnly = true)
    public List<CuentaPaquete> getPaquetesActivos() {
        return cuentaPaqueteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CuentaPaquete getPaqueteById(Long id) {
        return cuentaPaqueteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado: " + id));
    }

    public Cuenta crearCuentaConSolicitud(Long pacienteId, Long paqueteId, Long camaId) {
        Persona paciente = personaRepository.findById(pacienteId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado: " + pacienteId));

        Cuenta cuenta = new Cuenta();
        cuenta.setPacienteId(pacienteId);
        cuenta.setPaqueteQuirurgicoId(paqueteId);
        cuenta.setFechaApertura(LocalDateTime.now());
        cuenta.setEstado("ABIERTA");
        cuenta = cuentaRepository.save(cuenta);
        log.debug("Cuenta created id={}, paciente={}, paquete={}", cuenta.getId(), pacienteId, paqueteId);

        SolicitudHospitalizacion sol = new SolicitudHospitalizacion();
        sol.setCuentaId(cuenta.getId());
        sol.setTipoHabitacionId(paqueteId);
        sol.setEstado("PENDIENTE");
        sol.setFechaSolicitud(LocalDateTime.now());
        solicitudRepository.save(sol);

        if (camaId != null) {
            Cama cama = camaRepository.findById(camaId)
                    .orElseThrow(() -> new EntityNotFoundException("Cama no encontrada: " + camaId));
            cama.ocupar();
            camaRepository.save(cama);

            sol.setEstado("ASIGNADA");
            solicitudRepository.save(sol);
        }

        return cuenta;
    }

    public void registrarDiagnostico(Long cuentaId, String codigoCIE11, String descripcion, String tipo) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada: " + cuentaId));

        AdmisionDiagnostico diag = new AdmisionDiagnostico();
        diag.setCuentaId(cuentaId);
        diag.setCodigoCIE11(codigoCIE11);
        diag.setTipo(tipo != null ? tipo : "PRINCIPAL");
        diagnosticoRepository.save(diag);
        log.debug("Diagnostico registrado para cuentaId={}, CIE-11={}", cuentaId, codigoCIE11);
    }

    public CuentaResponse crearCuenta(CuentaRequest request) {
        Persona paciente = personaRepository.findById(request.pacienteId())
            .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado con id: " + request.pacienteId()));

        Cuenta cuenta = new Cuenta();
        cuenta.setPacienteId(request.pacienteId());
        if (request.medicoId() != null) {
            cuenta.setPaqueteQuirurgicoId(request.medicoId()); // store as reference
        }
        if (request.tipoSeguroId() != null) {
            cuenta.setTipoHabitacionId(request.tipoSeguroId()); // store as reference
        }
        cuenta.setFechaApertura(LocalDateTime.now());
        cuenta.setEstado("ABIERTA");
        cuenta = cuentaRepository.save(cuenta);
        log.debug("Cuenta created id={}, paciente={}", cuenta.getId(), request.pacienteId());

        // Auto-generate solicitud if paqueteId is present
        if (request.paqueteId() != null) {
            SolicitudHospitalizacion sol = new SolicitudHospitalizacion();
            sol.setCuentaId(cuenta.getId());
            sol.setTipoHabitacionId(request.paqueteId());
            sol.setEstado("PENDIENTE");
            sol.setFechaSolicitud(LocalDateTime.now());
            solicitudRepository.save(sol);
            log.debug("Solicitud auto-generated for cuenta id={}, paqueteId={}", cuenta.getId(), request.paqueteId());
        }

        return toCuentaResponse(cuenta, paciente);
    }

    public Hospitalizacion asignarCama(AsignarCamaRequest request) {
        Cama cama = camaRepository.findById(request.camaId())
            .orElseThrow(() -> new EntityNotFoundException("Cama no encontrada con id: " + request.camaId()));

        if (!cama.isDisponible()) {
            throw new IllegalStateException("La cama " + cama.getCodigo() + " no está disponible (estado: " + cama.getEstado() + ")");
        }

        SolicitudHospitalizacion solicitud = null;
        if (request.solicitudHospitalizacionId() != null) {
            solicitud = solicitudRepository.findById(request.solicitudHospitalizacionId())
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada con id: " + request.solicitudHospitalizacionId()));
            solicitud.setEstado("ASIGNADA");
            solicitudRepository.save(solicitud);
        }

        // Use cama state machine
        cama.ocupar();
        camaRepository.save(cama);

        Hospitalizacion hosp = new Hospitalizacion();
        hosp.setSolicitudId(solicitud != null ? solicitud.getId() : 0L);
        hosp.setCuentaId(solicitud != null ? solicitud.getCuentaId() : 0L);
        if (solicitud != null) {
            Cuenta cuenta = cuentaRepository.findById(solicitud.getCuentaId()).orElse(null);
            hosp.setPacienteId(cuenta != null ? cuenta.getPacienteId() : 0L);
        } else {
            hosp.setPacienteId(0L);
            log.warn("No se pudo determinar pacienteId: solicitud no especificada");
        }
        hosp.setCamaId(request.camaId());
        hosp.setFechaIngreso(LocalDateTime.now());
        hosp.setEstado("HOSPITALIZADO");
        hosp = hospitalizacionRepository.save(hosp);

        log.debug("Hospitalizacion created id={}, camaId={}", hosp.getId(), request.camaId());
        return hosp;
    }

    public AdmisionDiagnosticoResponse registrarDiagnostico(Long cuentaId, AdmisionDiagnosticoRequest request) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
            .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada con id: " + cuentaId));

        AdmisionDiagnostico diag = new AdmisionDiagnostico();
        diag.setCuentaId(cuentaId);
        diag.setCodigoCIE11(request.codigoCie11());
        diag.setTipo(request.tipoDiagnostico() != null ? request.tipoDiagnostico() : "PRINCIPAL");
        diag = diagnosticoRepository.save(diag);

        log.debug("Diagnostico registrado id={} para cuentaId={}", diag.getId(), cuentaId);
        return new AdmisionDiagnosticoResponse(
            diag.getId(), diag.getCuentaId(), diag.getCodigoCIE11(),
            null, diag.getTipo(), request.descripcion()
        );
    }

    private CuentaResponse toCuentaResponse(Cuenta cuenta, Persona paciente) {
        return new CuentaResponse(
            cuenta.getId(), cuenta.getPacienteId(),
            paciente.getNombres() + " " + paciente.getApellidoPaterno() + " " + paciente.getApellidoMaterno(),
            null, null,
            String.valueOf(cuenta.getTipoHabitacionId()),
            null,
            cuenta.getFechaApertura(), cuenta.getEstado(),
            null
        );
    }
}

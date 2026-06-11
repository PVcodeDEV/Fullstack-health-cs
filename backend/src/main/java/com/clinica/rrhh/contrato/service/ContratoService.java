package com.clinica.rrhh.contrato.service;

import com.clinica.maestro.entity.rrhh.TipoContrato;
import com.clinica.maestro.repository.rrhh.TipoContratoRepository;
import com.clinica.rrhh.contrato.dto.ContratoRequest;
import com.clinica.rrhh.contrato.dto.ContratoResponse;
import com.clinica.rrhh.contrato.dto.ContratoUpdateRequest;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.derechohabiente.service.DerechohabienteService;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.EstadoContrato;
import com.clinica.rrhh.type.TipoJornada;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContratoService {

    private static final Logger log = LoggerFactory.getLogger(ContratoService.class);

    private final ContratoRepository contratoRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final TipoContratoRepository tipoContratoRepository;
    private final DerechohabienteService derechohabienteService;

    public ContratoService(ContratoRepository contratoRepository,
                           TrabajadorRepository trabajadorRepository,
                           TipoContratoRepository tipoContratoRepository,
                           DerechohabienteService derechohabienteService) {
        this.contratoRepository = contratoRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.tipoContratoRepository = tipoContratoRepository;
        this.derechohabienteService = derechohabienteService;
    }

    public ContratoResponse create(ContratoRequest request) {
        Trabajador trabajador = trabajadorRepository.findById(request.trabajadorId())
            .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con id: " + request.trabajadorId()));

        TipoContrato tipoContrato = tipoContratoRepository.findById(request.tipoContratoId())
            .orElseThrow(() -> new EntityNotFoundException("Tipo de contrato no encontrado con id: " + request.tipoContratoId()));

        // Validate DETERMINADO needs fechaFin
        if ("DETERMINADO".equals(tipoContrato.getCodigo()) && request.fechaFin() == null) {
            throw new IllegalArgumentException("Contrato DETERMINADO requiere fecha de fin");
        }

        // Auto-expire previous ACTIVE contract
        contratoRepository.findByTrabajadorIdAndEstado(request.trabajadorId(), EstadoContrato.ACTIVO)
            .ifPresent(prev -> {
                prev.setEstado(EstadoContrato.VENCIDO);
                contratoRepository.save(prev);
                log.debug("Contrato anterior id={} vencido automáticamente", prev.getId());
            });

        Contrato entity = new Contrato();
        entity.setTrabajador(trabajador);
        entity.setTipoContrato(tipoContrato);
        entity.setFechaInicio(request.fechaInicio());
        entity.setFechaFin(request.fechaFin());
        entity.setPeriodoPruebaMeses(request.periodoPruebaMeses());
        entity.setRemuneracion(request.remuneracion());
        entity.setJornada(request.jornada() != null ? TipoJornada.valueOf(request.jornada()) : TipoJornada.REGULAR);
        entity.setEstado(EstadoContrato.ACTIVO);

        entity = contratoRepository.save(entity);
        log.debug("Contrato creado id={} para trabajadorId={}", entity.getId(), request.trabajadorId());
        return ContratoResponse.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public ContratoResponse findById(Long id) {
        return contratoRepository.findById(id)
            .map(ContratoResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ContratoResponse> findAll() {
        return contratoRepository.findAll().stream()
            .map(ContratoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ContratoResponse> findByTrabajadorId(Long trabajadorId) {
        return contratoRepository.findByTrabajadorIdOrderByFechaInicioDesc(trabajadorId)
            .stream()
            .map(ContratoResponse::fromEntity)
            .toList();
    }

    public ContratoResponse resolver(Long id, String motivoCese) {
        Contrato entity = contratoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado con id: " + id));

        if (entity.getEstado() != EstadoContrato.ACTIVO && entity.getEstado() != EstadoContrato.SUSPENDIDO) {
            throw new IllegalStateException(
                "No se puede resolver un contrato en estado " + entity.getEstado());
        }

        entity.setEstado(EstadoContrato.RESUELTO);
        entity.setMotivoCese(motivoCese);
        entity = contratoRepository.save(entity);

        // Cascade: inactivar derechohabientes del trabajador
        derechohabienteService.inactivarPorTrabajador(entity.getTrabajador().getId());

        log.debug("Contrato id={} resuelto", entity.getId());
        return ContratoResponse.fromEntity(entity);
    }

    public ContratoResponse suspender(Long id) {
        Contrato entity = contratoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado con id: " + id));

        if (entity.getEstado() != EstadoContrato.ACTIVO) {
            throw new IllegalStateException(
                "No se puede suspender un contrato en estado " + entity.getEstado());
        }

        entity.setEstado(EstadoContrato.SUSPENDIDO);
        entity = contratoRepository.save(entity);
        log.debug("Contrato id={} suspendido", entity.getId());
        return ContratoResponse.fromEntity(entity);
    }

    public ContratoResponse update(Long id, ContratoUpdateRequest request) {
        Contrato entity = contratoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado con id: " + id));

        if (entity.getEstado() == EstadoContrato.RESUELTO) {
            throw new IllegalStateException("No se puede modificar un contrato RESUELTO");
        }

        TipoContrato tipoContrato = tipoContratoRepository.findById(request.tipoContratoId())
            .orElseThrow(() -> new EntityNotFoundException("Tipo de contrato no encontrado con id: " + request.tipoContratoId()));

        // Validate DETERMINADO needs fechaFin
        if ("DETERMINADO".equals(tipoContrato.getCodigo()) && request.fechaFin() == null) {
            throw new IllegalArgumentException("Contrato DETERMINADO requiere fecha de fin");
        }

        entity.setTipoContrato(tipoContrato);
        entity.setFechaInicio(request.fechaInicio());
        entity.setFechaFin(request.fechaFin());
        entity.setPeriodoPruebaMeses(request.periodoPruebaMeses());
        entity.setRemuneracion(request.remuneracion());
        entity.setJornada(request.jornada() != null ? TipoJornada.valueOf(request.jornada()) : TipoJornada.REGULAR);

        entity = contratoRepository.save(entity);
        log.debug("Contrato id={} actualizado", id);
        return ContratoResponse.fromEntity(entity);
    }

    public ContratoResponse reactivar(Long id) {
        Contrato entity = contratoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado con id: " + id));

        if (entity.getEstado() != EstadoContrato.SUSPENDIDO) {
            throw new IllegalStateException(
                "No se puede reactivar un contrato en estado " + entity.getEstado());
        }

        entity.setEstado(EstadoContrato.ACTIVO);
        entity = contratoRepository.save(entity);
        log.debug("Contrato id={} reactivado", entity.getId());
        return ContratoResponse.fromEntity(entity);
    }
}

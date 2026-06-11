package com.clinica.rrhh.trabajador.service;

import com.clinica.maestro.entity.rrhh.TipoColegiatura;
import com.clinica.maestro.repository.rrhh.TipoColegiaturaRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.periodo.entity.PeriodoLaboral;
import com.clinica.rrhh.periodo.repository.PeriodoLaboralRepository;
import com.clinica.rrhh.trabajador.dto.TrabajadorRequest;
import com.clinica.rrhh.trabajador.dto.TrabajadorResponse;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.RegimenLaboral;
import com.clinica.rrhh.type.TipoTrabajador;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TrabajadorService {

    private static final Logger log = LoggerFactory.getLogger(TrabajadorService.class);

    private static final List<String> PROFESIONES_SALUD = List.of("MEDICO", "ENFERMERA", "NUTRICIONISTA");

    private final TrabajadorRepository trabajadorRepository;
    private final PersonaRepository personaRepository;
    private final TipoColegiaturaRepository tipoColegiaturaRepository;
    private final ContratoRepository contratoRepository;
    private final PeriodoLaboralRepository periodoLaboralRepository;

    public TrabajadorService(TrabajadorRepository trabajadorRepository,
                             PersonaRepository personaRepository,
                             TipoColegiaturaRepository tipoColegiaturaRepository,
                             ContratoRepository contratoRepository,
                             PeriodoLaboralRepository periodoLaboralRepository) {
        this.trabajadorRepository = trabajadorRepository;
        this.personaRepository = personaRepository;
        this.tipoColegiaturaRepository = tipoColegiaturaRepository;
        this.contratoRepository = contratoRepository;
        this.periodoLaboralRepository = periodoLaboralRepository;
    }

    @Transactional(readOnly = true)
    public List<TrabajadorResponse> findAll() {
        return trabajadorRepository.findAllByActivoTrue()
            .stream()
            .map(TrabajadorResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public TrabajadorResponse findById(Long id) {
        return trabajadorRepository.findById(id)
            .map(TrabajadorResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con id: " + id));
    }

    public TrabajadorResponse create(TrabajadorRequest request) {
        // Validate unique persona
        if (trabajadorRepository.existsByPersonaId(request.personaId())) {
            throw new IllegalArgumentException(
                "Ya existe un trabajador con la persona id: " + request.personaId());
        }

        // Validate unique codigoTrabajador
        if (trabajadorRepository.existsByCodigoTrabajador(request.codigoTrabajador())) {
            throw new IllegalArgumentException(
                "Ya existe un trabajador con el código: " + request.codigoTrabajador());
        }

        // Validate colegiatura for health professionals
        validarColegiatura(request.tipo(), request.tipoColegiaturaId(), request.nroColegiatura());

        // Resolve FK: persona
        Persona persona = personaRepository.findById(request.personaId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Persona no encontrada con id: " + request.personaId()));

        // Resolve FK: tipoColegiatura (optional)
        TipoColegiatura tipoColegiatura = null;
        if (request.tipoColegiaturaId() != null) {
            tipoColegiatura = tipoColegiaturaRepository.findById(request.tipoColegiaturaId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Tipo de colegiatura no encontrado con id: " + request.tipoColegiaturaId()));
        }

        var entity = new Trabajador();
        entity.setPersona(persona);
        entity.setCodigoTrabajador(request.codigoTrabajador());
        entity.setFechaIngreso(request.fechaIngreso());
        entity.setTipo(request.tipo() != null ? TipoTrabajador.valueOf(request.tipo()) : null);
        entity.setRegimenLaboral(request.regimenLaboral() != null ? RegimenLaboral.valueOf(request.regimenLaboral()) : null);
        entity.setCargo(request.cargo());
        entity.setAreaFuncionalId(request.areaFuncionalId());
        entity.setBanco(request.banco());
        entity.setCuentaSueldo(request.cuentaSueldo());
        entity.setCci(request.cci());
        entity.setContactoNombre(request.contactoNombre());
        entity.setContactoTelefono(request.contactoTelefono());
        entity.setCantidadHijos(request.cantidadHijos() != null ? request.cantidadHijos() : 0);
        entity.setNroColegiatura(request.nroColegiatura());
        entity.setTipoColegiatura(tipoColegiatura);
        entity.setDiscapacidad(request.discapacidad() != null && request.discapacidad());
        entity.setSindicalizado(request.sindicalizado() != null && request.sindicalizado());
        entity.setActivo(true);

        entity = trabajadorRepository.save(entity);
        log.debug("Trabajador created with id: {}", entity.getId());

        // Auto-create initial PeriodoLaboral
        PeriodoLaboral pl = new PeriodoLaboral();
        pl.setTrabajador(entity);
        pl.setFechaInicio(request.fechaIngreso() != null ? request.fechaIngreso() : LocalDate.now());
        pl.setEsReingreso(false);
        periodoLaboralRepository.save(pl);
        log.debug("Periodo laboral inicial creado para trabajadorId={}", entity.getId());

        return TrabajadorResponse.fromEntity(entity);
    }

    public TrabajadorResponse update(Long id, TrabajadorRequest request) {
        Trabajador entity = trabajadorRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con id: " + id));

        // Check duplicate persona (if changed)
        if (!entity.getPersona().getId().equals(request.personaId())
            && trabajadorRepository.existsByPersonaId(request.personaId())) {
            throw new IllegalArgumentException(
                "Ya existe un trabajador con la persona id: " + request.personaId());
        }

        // Check duplicate codigoTrabajador (if changed)
        if (!entity.getCodigoTrabajador().equals(request.codigoTrabajador())
            && trabajadorRepository.existsByCodigoTrabajador(request.codigoTrabajador())) {
            throw new IllegalArgumentException(
                "Ya existe un trabajador con el código: " + request.codigoTrabajador());
        }

        // Resolve FK: persona
        Persona persona = personaRepository.findById(request.personaId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Persona no encontrada con id: " + request.personaId()));

        // Resolve FK: tipoColegiatura (optional)
        TipoColegiatura tipoColegiatura = null;
        if (request.tipoColegiaturaId() != null) {
            tipoColegiatura = tipoColegiaturaRepository.findById(request.tipoColegiaturaId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Tipo de colegiatura no encontrado con id: " + request.tipoColegiaturaId()));
        }

        entity.setPersona(persona);
        entity.setCodigoTrabajador(request.codigoTrabajador());
        entity.setFechaIngreso(request.fechaIngreso());
        entity.setTipo(request.tipo() != null ? TipoTrabajador.valueOf(request.tipo()) : null);
        entity.setRegimenLaboral(request.regimenLaboral() != null ? RegimenLaboral.valueOf(request.regimenLaboral()) : null);
        entity.setCargo(request.cargo());
        entity.setAreaFuncionalId(request.areaFuncionalId());
        entity.setBanco(request.banco());
        entity.setCuentaSueldo(request.cuentaSueldo());
        entity.setCci(request.cci());
        entity.setContactoNombre(request.contactoNombre());
        entity.setContactoTelefono(request.contactoTelefono());
        entity.setCantidadHijos(request.cantidadHijos() != null ? request.cantidadHijos() : 0);
        entity.setNroColegiatura(request.nroColegiatura());
        entity.setTipoColegiatura(tipoColegiatura);
        entity.setDiscapacidad(request.discapacidad() != null && request.discapacidad());
        entity.setSindicalizado(request.sindicalizado() != null && request.sindicalizado());

        // Validate colegiatura after all set* calls (resolve tipo: prefer request, fallback to existing)
        String tipoAntiguo = entity.getTipo() != null ? entity.getTipo().name() : null;
        String resolvedTipo = request.tipo() != null ? request.tipo() : tipoAntiguo;
        validarColegiatura(resolvedTipo, request.tipoColegiaturaId(), request.nroColegiatura());

        entity = trabajadorRepository.save(entity);
        log.debug("Trabajador updated with id: {}", entity.getId());
        return TrabajadorResponse.fromEntity(entity);
    }

    public TrabajadorResponse softDelete(Long id) {
        Trabajador entity = trabajadorRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con id: " + id));

        entity.markAsInactive();
        entity = trabajadorRepository.save(entity);
        log.debug("Trabajador soft-deleted with id: {}", entity.getId());
        return TrabajadorResponse.fromEntity(entity);
    }

    private void validarColegiatura(String tipo, Long tipoColegiaturaId, String nroColegiatura) {
        if (tipo != null && PROFESIONES_SALUD.contains(tipo)) {
            if (tipoColegiaturaId == null || nroColegiatura == null || nroColegiatura.isBlank()) {
                throw new IllegalArgumentException(
                    "El tipo y número de colegiatura son obligatorios para " + tipo);
            }
        }
    }
}

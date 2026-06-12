package com.clinica.entidad.service;

import com.clinica.entidad.dto.EmpresaRequest;
import com.clinica.entidad.dto.EmpresaResponse;
import com.clinica.entidad.dto.SunatRucResponse;
import com.clinica.entidad.entity.Empresa;
import com.clinica.entidad.entity.Empresa.Estado;
import com.clinica.entidad.entity.Empresa.Rol;
import com.clinica.entidad.entity.Empresa.TipoRuc;
import com.clinica.entidad.entity.SunatConsultaLog;
import com.clinica.entidad.repository.EmpresaRepository;
import com.clinica.entidad.repository.SunatConsultaLogRepository;
import com.clinica.persona.repository.PersonaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmpresaService {

    private static final Logger log = LoggerFactory.getLogger(EmpresaService.class);

    private final EmpresaRepository empresaRepository;
    private final SunatConsultaLogRepository sunatLogRepository;
    private final PersonaRepository personaRepository;
    private final RucValidator rucValidator;
    private final SunatRucClient sunatRucClient;

    public EmpresaService(EmpresaRepository empresaRepository,
                          SunatConsultaLogRepository sunatLogRepository,
                          PersonaRepository personaRepository,
                          RucValidator rucValidator,
                          SunatRucClient sunatRucClient) {
        this.empresaRepository = empresaRepository;
        this.sunatLogRepository = sunatLogRepository;
        this.personaRepository = personaRepository;
        this.rucValidator = rucValidator;
        this.sunatRucClient = sunatRucClient;
    }

    // --- CRUD ---

    @Transactional(readOnly = true)
    public EmpresaResponse findById(Long id) {
        return empresaRepository.findById(id)
            .map(EmpresaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con id: " + id));
    }

    @Transactional(readOnly = true)
    public EmpresaResponse findByRuc(String ruc) {
        return empresaRepository.findByRuc(ruc)
            .map(EmpresaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con RUC: " + ruc));
    }

    @Transactional(readOnly = true)
    public Page<EmpresaResponse> search(String q, Rol rol, Estado estado, Pageable pageable) {
        return empresaRepository.findByFilters(rol, estado, q, pageable)
            .map(EmpresaResponse::fromEntity);
    }

    public EmpresaResponse create(EmpresaRequest request) {
        // Validate RUC modulo 11
        if (!rucValidator.validar(request.ruc())) {
            throw new IllegalArgumentException("RUC inválido: el dígito verificador no coincide");
        }

        // Detect tipoRuc from first digit
        TipoRuc tipoRuc = request.ruc().startsWith("1") ? TipoRuc.RUC_10 : TipoRuc.RUC_20;

        // Build entity
        Empresa entity = new Empresa();
        entity.setRuc(request.ruc());
        entity.setTipoRuc(tipoRuc);
        entity.setEstado(Estado.ACTIVO);
        entity.setRol(Rol.CLIENTE); // Default per ENT-002
        entity.setTelefono(request.telefono());
        entity.setEmail(request.email());

        if (tipoRuc == TipoRuc.RUC_20) {
            // RUC 20: razonSocial and direccionFiscal required
            if (request.razonSocial() == null || request.razonSocial().isBlank()) {
                throw new IllegalArgumentException("Razón social es obligatoria para RUC 20");
            }
            if (request.direccionFiscal() == null || request.direccionFiscal().isBlank()) {
                throw new IllegalArgumentException("Dirección fiscal es obligatoria para RUC 20");
            }
            entity.setRazonSocial(request.razonSocial());
            entity.setDireccionFiscal(request.direccionFiscal());
            entity.setUbigeo(request.ubigeo());
        } else {
            // RUC 10: optional fields, auto-link to Persona by DNI (RUC digits 2-9)
            entity.setRazonSocial(request.razonSocial()); // may be null, from SUNAT apenomdenunciado
            entity.setDireccionFiscal(request.direccionFiscal());
            entity.setUbigeo(request.ubigeo());

            // Auto-link to Persona by DNI (digits 2-9 of RUC 10)
            String dniPart = request.ruc().substring(2, 10);
            personaRepository.findByNumeroDocumento(dniPart)
                .ifPresent(persona -> entity.setPersonaId(persona.getId()));
        }

        try {
            Empresa saved = empresaRepository.save(entity);
            log.debug("Empresa created with id: {}, ruc: {}", saved.getId(), saved.getRuc());
            return EmpresaResponse.fromEntity(saved);
        } catch (DataIntegrityViolationException e) {
            // TOCTOU prevention: unique constraint violation on concurrent create
            log.warn("Duplicate RUC detected on save (concurrent): {}", request.ruc());
            throw new IllegalArgumentException("Ya existe una empresa con el RUC: " + request.ruc());
        }
    }

    public EmpresaResponse update(Long id, EmpresaRequest request) {
        Empresa entity = empresaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con id: " + id));

        // RUC cannot be changed (ENT-004)
        // Update editable fields
        if (request.razonSocial() != null) {
            entity.setRazonSocial(request.razonSocial());
        }
        if (request.direccionFiscal() != null) {
            entity.setDireccionFiscal(request.direccionFiscal());
        }
        entity.setUbigeo(request.ubigeo());
        entity.setTelefono(request.telefono());
        entity.setEmail(request.email());

        // Allow manual personaId link/unlink (ENT-005)
        entity.setPersonaId(request.personaId());

        entity = empresaRepository.save(entity);
        log.debug("Empresa updated with id: {}", entity.getId());
        return EmpresaResponse.fromEntity(entity);
    }

    public EmpresaResponse softDelete(Long id) {
        Empresa entity = empresaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con id: " + id));

        entity.setEstado(Estado.INACTIVO);
        entity.markAsInactive();
        entity = empresaRepository.save(entity);
        log.debug("Empresa soft-deleted with id: {}", entity.getId());
        return EmpresaResponse.fromEntity(entity);
    }

    // --- SUNAT Consult ---

    /**
     * Consult SUNAT RUC API and log the query.
     */
    public SunatRucResponse consultarSunat(String ruc, String ipOrigen, Long usuarioId) {
        Optional<SunatRucResponse> response = sunatRucClient.consultar(ruc);

        boolean exito = response.isPresent();
        String respuestaRaw = response.map(SunatRucResponse::toString)
            .orElse(null);

        // Truncate raw response to 2000 chars
        if (respuestaRaw != null && respuestaRaw.length() > 2000) {
            respuestaRaw = respuestaRaw.substring(0, 2000);
        }

        // Log the consultation
        SunatConsultaLog logEntry = new SunatConsultaLog();
        logEntry.setRuc(ruc);
        logEntry.setFecha(LocalDateTime.now());
        logEntry.setIpOrigen(ipOrigen);
        logEntry.setUsuarioId(usuarioId);
        logEntry.setRespuestaRaw(respuestaRaw);
        logEntry.setExito(exito);
        sunatLogRepository.save(logEntry);

        return response.orElse(
            new SunatRucResponse(ruc, null, null, null, null, false)
        );
    }

    // --- Role Promotion (ENT-002) ---

    /**
     * Promote role when Empresa is used in CLIENTE context.
     * Idempotent: CLIENTE → CLIENTE, PROVEEDOR → AMBOS, AMBOS → AMBOS.
     */
    public void promoteToCliente(Long empresaId) {
        Empresa entity = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con id: " + empresaId));

        Rol oldRol = entity.getRol();
        if (oldRol == Rol.PROVEEDOR) {
            entity.setRol(Rol.AMBOS);
        }
        // CLIENTE → CLIENTE (no change), AMBOS → AMBOS (no change)

        if (oldRol != entity.getRol()) {
            empresaRepository.save(entity);
            log.debug("Role promoted from {} to AMBOS for empresa id={} (cliente context)", oldRol, empresaId);
        }
    }

    /**
     * Promote role when Empresa is used in PROVEEDOR context.
     * Idempotent: PROVEEDOR → PROVEEDOR, CLIENTE → AMBOS, AMBOS → AMBOS.
     */
    public void promoteToProveedor(Long empresaId) {
        Empresa entity = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con id: " + empresaId));

        Rol oldRol = entity.getRol();
        if (oldRol == Rol.CLIENTE) {
            entity.setRol(Rol.AMBOS);
        }
        // PROVEEDOR → PROVEEDOR (no change), AMBOS → AMBOS (no change)

        if (oldRol != entity.getRol()) {
            empresaRepository.save(entity);
            log.debug("Role promoted from {} to AMBOS for empresa id={} (proveedor context)", oldRol, empresaId);
        }
    }

    // --- Persona Link (ENT-005) ---

    /**
     * Manually link or unlink a Persona to an Empresa.
     */
    public void linkPersona(Long empresaId, Long personaId) {
        Empresa entity = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con id: " + empresaId));

        entity.setPersonaId(personaId);
        empresaRepository.save(entity);
        log.debug("Persona {} linked to empresa id={}", personaId, empresaId);
    }

    public void unlinkPersona(Long empresaId) {
        linkPersona(empresaId, null);
    }
}

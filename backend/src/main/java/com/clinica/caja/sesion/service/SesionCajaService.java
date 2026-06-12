package com.clinica.caja.sesion.service;

import com.clinica.caja.sesion.dto.SesionCajaCerrarRequest;
import com.clinica.caja.sesion.dto.SesionCajaRequest;
import com.clinica.caja.sesion.dto.SesionCajaResponse;
import com.clinica.caja.sesion.entity.SesionCaja;
import com.clinica.caja.sesion.entity.SesionCaja.Estado;
import com.clinica.caja.sesion.repository.ClinicaSesionCajaRepository;
import com.clinica.config.CajaSesionProperties;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service("clinicaSesionCajaService")
@Transactional
public class SesionCajaService {

    private static final Logger log = LoggerFactory.getLogger(SesionCajaService.class);

    private final ClinicaSesionCajaRepository sesionCajaRepository;
    private final CajaSesionProperties properties;

    public SesionCajaService(ClinicaSesionCajaRepository sesionCajaRepository,
                             CajaSesionProperties properties) {
        this.sesionCajaRepository = sesionCajaRepository;
        this.properties = properties;
    }

    /**
     * Open a new cash session for the given user.
     *
     * @param request   contains montoApertura
     * @param usuarioId the authenticated cashier
     * @param now       server timestamp
     * @return the created session response
     * @throws IllegalStateException if the user already has an open session
     */
    public SesionCajaResponse abrirSesion(SesionCajaRequest request, Long usuarioId, LocalDateTime now) {
        // SES-001-2: reject double-open
        if (sesionCajaRepository.existsByUsuarioAperturaIdAndEstadoAndActivoTrue(usuarioId, Estado.ABIERTA)) {
            log.warn("Double-open attempt by user={}: already has an open session", usuarioId);
            throw new IllegalStateException("El usuario ya tiene una sesión abierta");
        }

        SesionCaja entity = new SesionCaja();
        entity.setCodigo(generarCodigo(now));
        entity.setUsuarioAperturaId(usuarioId);
        entity.setFechaApertura(now);
        entity.setMontoApertura(request.montoApertura());
        entity.setEstado(Estado.ABIERTA);
        entity.setTotalVentas(BigDecimal.ZERO);

        entity = sesionCajaRepository.save(entity);
        log.debug("Session opened: id={}, codigo={}, user={}, monto={}",
            entity.getId(), entity.getCodigo(), usuarioId, request.montoApertura());
        return SesionCajaResponse.fromEntity(entity);
    }

    /**
     * Close an open cash session.
     *
     * @param sesionId  the session to close
     * @param request   contains montoCierre
     * @param usuarioId user closing the session (may be ADMIN closing another's session)
     * @param now       server timestamp
     * @return the closed session response with discrepancy flag
     * @throws EntityNotFoundException if session not found
     * @throws IllegalStateException   if session is already closed
     */
    public SesionCajaResponse cerrarSesion(Long sesionId, SesionCajaCerrarRequest request,
                                            Long usuarioId, LocalDateTime now) {
        SesionCaja entity = sesionCajaRepository.findById(sesionId)
            .orElseThrow(() -> new EntityNotFoundException("Sesión no encontrada con id: " + sesionId));

        if (entity.getEstado() == Estado.CERRADA) {
            throw new IllegalStateException("La sesión ya está cerrada");
        }

        entity.cerrar(request.montoCierre(), usuarioId, now);
        entity = sesionCajaRepository.save(entity);

        boolean discrepanciaWarning = entity.getDiferencia().abs()
            .compareTo(properties.toleranciaDiferencia()) > 0;

        if (discrepanciaWarning) {
            log.warn("Session {} closed with discrepancy: diferencia={}, tolerance={}",
                entity.getId(), entity.getDiferencia(), properties.toleranciaDiferencia());
        }

        log.debug("Session closed: id={}, codigo={}, diferencia={}, discrepancia={}",
            entity.getId(), entity.getCodigo(), entity.getDiferencia(), discrepanciaWarning);
        return SesionCajaResponse.fromEntity(entity, discrepanciaWarning);
    }

    /**
     * Get the current open session for the given user.
     *
     * @param usuarioId the authenticated cashier
     * @return the open session
     * @throws EntityNotFoundException if no open session exists
     */
    @Transactional(readOnly = true)
    public SesionCajaResponse getSessionActual(Long usuarioId) {
        return sesionCajaRepository
            .findByUsuarioAperturaIdAndEstadoAndActivoTrue(usuarioId, Estado.ABIERTA)
            .map(SesionCajaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró una sesión abierta para el usuario: " + usuarioId));
    }

    /**
     * Find a session by ID.
     */
    @Transactional(readOnly = true)
    public SesionCajaResponse findById(Long id) {
        return sesionCajaRepository.findById(id)
            .map(SesionCajaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Sesión no encontrada con id: " + id));
    }

    /**
     * Check if a user has an open session (used by LiquidacionService).
     */
    @Transactional(readOnly = true)
    public boolean tieneSesionAbierta(Long usuarioId) {
        return sesionCajaRepository.existsByUsuarioAperturaIdAndEstadoAndActivoTrue(usuarioId, Estado.ABIERTA);
    }

    /**
     * Get the open session entity for a user (used by LiquidacionService to link).
     *
     * @return the open SesionCaja or null if none
     */
    @Transactional(readOnly = true)
    public SesionCaja getOpenSessionEntity(Long usuarioId) {
        return sesionCajaRepository
            .findByUsuarioAperturaIdAndEstadoAndActivoTrue(usuarioId, Estado.ABIERTA)
            .orElse(null);
    }

    /**
     * Generate a unique session code: SES-{timestamp}-{random 4-digit suffix}.
     */
    private String generarCodigo(LocalDateTime now) {
        String ts = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        int suffix = (int) (Math.random() * 9000) + 1000;
        return "SES-" + ts + "-" + suffix;
    }
}

package com.clinica.farmacia.caja.service;

import com.clinica.farmacia.caja.dto.SesionCajaAbrirRequest;
import com.clinica.farmacia.caja.dto.SesionCajaCerrarRequest;
import com.clinica.farmacia.caja.dto.SesionCajaResponse;
import com.clinica.farmacia.caja.entity.SesionCaja;
import com.clinica.farmacia.caja.repository.SesionCajaRepository;
import com.clinica.farmacia.caja.type.EstadoSesion;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing cash sessions (SesionCaja).
 * Implements CAJ-01, CAJ-02, CAJ-03, SC-13, SC-14.
 */
@Service
@Transactional
public class SesionCajaService {

    private static final Logger log = LoggerFactory.getLogger(SesionCajaService.class);

    private final SesionCajaRepository repository;

    public SesionCajaService(SesionCajaRepository repository) {
        this.repository = repository;
    }

    /**
     * Open a new cash session for the given user.
     * Validates: montoApertura >= 0, no other open session for this user.
     */
    public SesionCajaResponse abrir(SesionCajaAbrirRequest request, Long usuarioId) {
        if (request.montoApertura().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto de apertura no puede ser negativo");
        }

        // Validate no open session exists for this user
        if (repository.existsByUsuarioIdAndEstado(usuarioId, EstadoSesion.ABIERTA)) {
            throw new IllegalStateException(
                "El usuario " + usuarioId + " ya tiene una sesión de caja abierta");
        }

        SesionCaja sesion = new SesionCaja();
        sesion.abrir(usuarioId, request.almacenId(), request.montoApertura(), request.observaciones());
        sesion = repository.save(sesion);

        log.info("Sesión de caja abierta: id={}, usuario={}, almacén={}, monto={}",
            sesion.getId(), usuarioId, request.almacenId(), request.montoApertura());

        return SesionCajaResponse.fromEntity(sesion);
    }

    /**
     * Close an open cash session.
     * Recomputes montoCierreEsperado = montoApertura + sum(ventas.total),
     * then computes diferencia = montoCierreReal - montoCierreEsperado.
     */
    public SesionCajaResponse cerrar(Long sesionId, SesionCajaCerrarRequest request) {
        SesionCaja sesion = repository.findById(sesionId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Sesión de caja no encontrada con id: " + sesionId));

        if (sesion.getEstado() != EstadoSesion.ABIERTA) {
            throw new IllegalStateException(
                "La sesión de caja " + sesionId + " no está abierta (estado: " + sesion.getEstado() + ")");
        }

        sesion.cerrar(request.montoCierreReal(), request.observaciones());
        sesion = repository.save(sesion);

        log.info("Sesión de caja cerrada: id={}, esperado={}, real={}, diferencia={}",
            sesionId, sesion.getMontoCierreEsperado(),
            sesion.getMontoCierreReal(), sesion.getDiferenciaCierre());

        return SesionCajaResponse.fromEntity(sesion);
    }

    /**
     * Find the current open session for a user, if any.
     */
    @Transactional(readOnly = true)
    public Optional<SesionCajaResponse> findOpenByUsuario(Long usuarioId) {
        return repository.findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(
                usuarioId, EstadoSesion.ABIERTA)
            .map(SesionCajaResponse::fromEntity);
    }

    /**
     * Find a session by ID.
     */
    @Transactional(readOnly = true)
    public SesionCajaResponse findById(Long id) {
        return repository.findById(id)
            .map(SesionCajaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "Sesión de caja no encontrada con id: " + id));
    }

    /**
     * List sessions by estado.
     */
    @Transactional(readOnly = true)
    public List<SesionCajaResponse> listByEstado(EstadoSesion estado) {
        return repository.findByEstadoOrderByFechaAperturaDesc(estado)
            .stream()
            .map(SesionCajaResponse::fromEntity)
            .toList();
    }

    /**
     * List all sessions.
     */
    @Transactional(readOnly = true)
    public List<SesionCajaResponse> listAll() {
        return repository.findAll()
            .stream()
            .map(SesionCajaResponse::fromEntity)
            .toList();
    }

    /**
     * List sessions in a date range.
     */
    @Transactional(readOnly = true)
    public List<SesionCajaResponse> listByDateRange(LocalDateTime desde, LocalDateTime hasta) {
        return repository.findByFechaAperturaBetweenOrderByFechaAperturaDesc(desde, hasta)
            .stream()
            .map(SesionCajaResponse::fromEntity)
            .toList();
    }

    /**
     * Update the denormalized totalVentas and montoCierreEsperado after a sale.
     * Called by VentaService after successful completar.
     */
    public void registrarVenta(Long sesionId, java.math.BigDecimal montoVenta) {
        SesionCaja sesion = repository.findById(sesionId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Sesión de caja no encontrada con id: " + sesionId));
        sesion.agregarVenta(montoVenta);
        repository.save(sesion);
    }
}

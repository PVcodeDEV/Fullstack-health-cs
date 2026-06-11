package com.clinica.clinica.cuenta.service;

import com.clinica.clinica.admision.entity.Cuenta;
import com.clinica.clinica.admision.repository.CuentaRepository;
import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.repository.CamaRepository;
import com.clinica.clinica.cuenta.dto.CargoAdicionalRequest;
import com.clinica.clinica.cuenta.dto.CargoAdicionalResponse;
import com.clinica.clinica.cuenta.entity.CargoAdicional;
import com.clinica.clinica.cuenta.repository.CargoAdicionalRepository;
import com.clinica.clinica.hospitalizacion.entity.Hospitalizacion;
import com.clinica.clinica.hospitalizacion.repository.HospitalizacionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CuentaService {

    private static final Logger log = LoggerFactory.getLogger(CuentaService.class);

    private final CuentaRepository cuentaRepository;
    private final CargoAdicionalRepository cargoRepository;
    private final HospitalizacionRepository hospitalizacionRepository;
    private final CamaRepository camaRepository;

    public CuentaService(CuentaRepository cuentaRepository,
                         CargoAdicionalRepository cargoRepository,
                         HospitalizacionRepository hospitalizacionRepository,
                         CamaRepository camaRepository) {
        this.cuentaRepository = cuentaRepository;
        this.cargoRepository = cargoRepository;
        this.hospitalizacionRepository = hospitalizacionRepository;
        this.camaRepository = camaRepository;
    }

    public CargoAdicionalResponse agregarCargo(CargoAdicionalRequest request) {
        CargoAdicional cargo = new CargoAdicional();
        // Map hospitalizacionId to cuentaId for the existing entity
        cargo.setCuentaId(request.hospitalizacionId());
        cargo.setDescripcion(request.descripcion());
        cargo.setMonto(request.monto());
        cargo.setTipo(request.tipoCargo() != null ? request.tipoCargo() : "GENERAL");
        cargo.setFechaRegistro(LocalDateTime.now());
        cargo = cargoRepository.save(cargo);

        log.debug("Cargo adicional registrado id={}, monto={}", cargo.getId(), cargo.getMonto());
        return new CargoAdicionalResponse(
            cargo.getId(), cargo.getCuentaId(), cargo.getDescripcion(),
            cargo.getMonto(), cargo.getTipo(), cargo.getCreatedAt(), cargo.getActivo()
        );
    }

    public void confirmarCobro(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
            .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada con id: " + cuentaId));
        // Find hospitalizacion by cuentaId
        Hospitalizacion hosp = hospitalizacionRepository.findByCuentaId(cuentaId)
            .orElseThrow(() -> new EntityNotFoundException("Hospitalización no encontrada para cuenta: " + cuentaId));
        // Liberar cama
        Cama cama = camaRepository.findById(hosp.getCamaId())
            .orElseThrow(() -> new EntityNotFoundException("Cama no encontrada"));
        cama.liberar();
        camaRepository.save(cama);
        // Actualizar estados
        hosp.setEstado("FINALIZADO");
        hospitalizacionRepository.save(hosp);
        cuenta.setEstado("CERRADA");
        cuentaRepository.save(cuenta);
        log.debug("Cobro confirmado para cuentaId={}, cama liberada", cuentaId);
    }

    @Transactional(readOnly = true)
    public Cuenta obtenerCuenta(Long cuentaId) {
        return cuentaRepository.findById(cuentaId)
            .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada con id: " + cuentaId));
    }

    @Transactional(readOnly = true)
    public List<CargoAdicionalResponse> listarCargos(Long cuentaId) {
        return cargoRepository.findByCuentaId(cuentaId).stream()
            .map(c -> new CargoAdicionalResponse(
                c.getId(), c.getCuentaId(), c.getDescripcion(),
                c.getMonto(), c.getTipo(), c.getCreatedAt(), c.getActivo()
            ))
            .toList();
    }
}

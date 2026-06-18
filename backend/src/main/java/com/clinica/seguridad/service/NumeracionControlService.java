package com.clinica.seguridad.service;

import com.clinica.seguridad.dto.NumeracionControlResponse;
import com.clinica.seguridad.entity.NumeracionControl;
import com.clinica.seguridad.repository.NumeracionControlRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

/**
 * Servicio centralizado de numeración correlativa sin saltos.
 * Cada llamada a nextCorrelativo() incrementa y retorna bajo FOR UPDATE.
 */
@Service
@Transactional
public class NumeracionControlService {

    private static final Logger log = LoggerFactory.getLogger(NumeracionControlService.class);

    private final NumeracionControlRepository repository;

    public NumeracionControlService(NumeracionControlRepository repository) {
        this.repository = repository;
    }

    /**
     * Retorna el siguiente correlativo formateado para una entidad/serie/año actual.
     * El método es synchronized para seguridad adicional sobre FOR UPDATE (garantiza
     * serialización incluso en escenarios de timeout de conexión).
     * <p>
     * Formato: {prefijo}{correlativo_padded}
     * Ejemplo: "HC-000011" o "000006"
     *
     * @param entidad nombre de la entidad (COMPROBANTE, HC, VENTA, etc.)
     * @param serie   serie numérica (001, 002, etc.)
     * @return String con el correlativo formateado
     * @throws IllegalStateException si la entrada está inactiva o no existe
     */
    public synchronized String nextCorrelativo(String entidad, String serie) {
        int anio = Year.now().getValue();

        NumeracionControl control = repository.findByEntidadAndSerieAndAnioWithLock(entidad, serie, anio)
            .orElseThrow(() -> new IllegalStateException(
                "No existe configuración de numeración para entidad=" + entidad
                    + ", serie=" + serie + ", anio=" + anio));

        if (!control.getActivo()) {
            throw new IllegalStateException(
                "La numeración para entidad=" + entidad + ", serie=" + serie + " está inactiva");
        }

        long next = control.getCorrelativoActual() + 1;
        control.setCorrelativoActual(next);
        repository.save(control);

        String prefix = control.getPrefijo() != null ? control.getPrefijo() : "";
        String formatted = String.format("%s%0" + control.getLongitudCorrelativo() + "d",
            prefix, next);

        log.debug("nextCorrelativo({}, {}) = {} (anio={})", entidad, serie, formatted, anio);
        return formatted;
    }

    @Transactional(readOnly = true)
    public List<NumeracionControlResponse> findAll() {
        return repository.findAll().stream()
            .map(NumeracionControlResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public NumeracionControlResponse findById(Long id) {
        NumeracionControl entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "NumeracionControl no encontrada con id: " + id));
        return NumeracionControlResponse.fromEntity(entity);
    }

    public NumeracionControlResponse create(NumeracionControl entity) {
        if (repository.existsByEntidadAndSerieAndAnio(
                entity.getEntidad(), entity.getSerie(), entity.getAnio())) {
            throw new IllegalArgumentException(
                "Ya existe una entrada de numeración para entidad=" + entity.getEntidad()
                    + ", serie=" + entity.getSerie() + ", anio=" + entity.getAnio());
        }

        if (entity.getCorrelativoActual() == null) {
            entity.setCorrelativoActual(0L);
        }
        if (entity.getLongitudCorrelativo() == 0) {
            entity.setLongitudCorrelativo(6);
        }

        entity = repository.save(entity);
        log.debug("NumeracionControl created: {}/{}/{}", entity.getEntidad(), entity.getSerie(), entity.getAnio());
        return NumeracionControlResponse.fromEntity(entity);
    }

    public NumeracionControlResponse update(Long id, NumeracionControl update) {
        NumeracionControl entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "NumeracionControl no encontrada con id: " + id));

        // Check unique constraint if entidad/serie/anio changed
        if (!entity.getEntidad().equals(update.getEntidad())
            || !entity.getSerie().equals(update.getSerie())
            || entity.getAnio() != update.getAnio()) {
            if (repository.existsByEntidadAndSerieAndAnio(
                    update.getEntidad(), update.getSerie(), update.getAnio())) {
                throw new IllegalArgumentException(
                    "Ya existe una entrada de numeración para entidad=" + update.getEntidad()
                        + ", serie=" + update.getSerie() + ", anio=" + update.getAnio());
            }
        }

        entity.setEntidad(update.getEntidad());
        entity.setSerie(update.getSerie());
        entity.setCorrelativoActual(update.getCorrelativoActual());
        entity.setPrefijo(update.getPrefijo());
        entity.setLongitudCorrelativo(update.getLongitudCorrelativo());
        entity.setAnio(update.getAnio());
        entity = repository.save(entity);
        log.debug("NumeracionControl updated: {}/{}/{}", entity.getEntidad(), entity.getSerie(), entity.getAnio());
        return NumeracionControlResponse.fromEntity(entity);
    }

    public void toggleActivo(Long id) {
        NumeracionControl entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "NumeracionControl no encontrada con id: " + id));
        entity.setActivo(!entity.getActivo());
        repository.save(entity);
        log.debug("NumeracionControl {} toggled activo={}", id, entity.getActivo());
    }
}

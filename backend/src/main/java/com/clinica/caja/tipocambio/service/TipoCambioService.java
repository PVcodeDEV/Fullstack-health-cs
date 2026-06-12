package com.clinica.caja.tipocambio.service;

import com.clinica.caja.tipocambio.dto.TipoCambioRequest;
import com.clinica.caja.tipocambio.dto.TipoCambioResponse;
import com.clinica.caja.tipocambio.entity.TipoCambio;
import com.clinica.caja.tipocambio.repository.TipoCambioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoCambioService {

    private static final Logger log = LoggerFactory.getLogger(TipoCambioService.class);

    private final TipoCambioRepository tipoCambioRepository;

    public TipoCambioService(TipoCambioRepository tipoCambioRepository) {
        this.tipoCambioRepository = tipoCambioRepository;
    }

    /**
     * Create a new exchange rate record.
     */
    public TipoCambioResponse create(TipoCambioRequest request) {
        TipoCambio entity = new TipoCambio();
        entity.setMonedaOrigen(request.monedaOrigen().toUpperCase());
        entity.setMonedaDestino(request.monedaDestino().toUpperCase());
        entity.setTipoCambio(request.tipoCambio());
        entity.setFecha(request.fecha());
        entity.setUsuarioId(request.usuarioId());

        entity = tipoCambioRepository.save(entity);
        log.debug("TipoCambio created: {} {}→{} = {} on {}",
            entity.getId(), entity.getMonedaOrigen(), entity.getMonedaDestino(),
            entity.getTipoCambio(), entity.getFecha());
        return TipoCambioResponse.fromEntity(entity);
    }

    /**
     * List all exchange rate records.
     */
    @Transactional(readOnly = true)
    public List<TipoCambioResponse> list() {
        return tipoCambioRepository.findAll().stream()
            .map(TipoCambioResponse::fromEntity)
            .toList();
    }

    /**
     * Get the latest exchange rate for a given currency pair.
     */
    @Transactional(readOnly = true)
    public TipoCambioResponse getLatest(String monedaOrigen, String monedaDestino) {
        return tipoCambioRepository.findLatestByMonedas(
                monedaOrigen.toUpperCase(), monedaDestino.toUpperCase())
            .map(TipoCambioResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró tipo de cambio para " + monedaOrigen + "/" + monedaDestino));
    }
}

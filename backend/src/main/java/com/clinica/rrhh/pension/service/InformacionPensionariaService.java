package com.clinica.rrhh.pension.service;

import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.maestro.repository.rrhh.AfpRepository;
import com.clinica.rrhh.pension.dto.InformacionPensionariaRequest;
import com.clinica.rrhh.pension.dto.InformacionPensionariaResponse;
import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import com.clinica.rrhh.pension.repository.InformacionPensionariaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InformacionPensionariaService {

    private static final Logger log = LoggerFactory.getLogger(InformacionPensionariaService.class);

    private final InformacionPensionariaRepository informacionPensionariaRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final AfpRepository afpRepository;

    public InformacionPensionariaService(InformacionPensionariaRepository informacionPensionariaRepository,
                                          TrabajadorRepository trabajadorRepository,
                                          AfpRepository afpRepository) {
        this.informacionPensionariaRepository = informacionPensionariaRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.afpRepository = afpRepository;
    }

    @Transactional(readOnly = true)
    public InformacionPensionariaResponse getByTrabajadorId(Long trabajadorId) {
        return informacionPensionariaRepository.findByTrabajadorId(trabajadorId)
            .map(InformacionPensionariaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                "Información pensionaria no encontrada para trabajador id: " + trabajadorId));
    }

    public InformacionPensionariaResponse upsert(Long trabajadorId, InformacionPensionariaRequest request) {
        Trabajador trabajador = trabajadorRepository.findById(trabajadorId)
            .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con id: " + trabajadorId));

        Afp afp = afpRepository.findById(request.afpId())
            .orElseThrow(() -> new EntityNotFoundException("AFP no encontrada con id: " + request.afpId()));

        boolean isOnp = "ONP".equals(afp.getCodigo());
        String cuspp;
        String comisionTipo;

        if (isOnp) {
            // ONP: comisionTipo = null, cuspp = DNI del trabajador
            comisionTipo = null;
            cuspp = trabajador.getPersona().getNumeroDocumento();
        } else {
            // AFP: validate cuspp (12-digit numeric)
            if (request.cuspp() == null || request.cuspp().isBlank()) {
                throw new IllegalArgumentException("CUSPP es obligatorio para AFP");
            }
            if (request.cuspp().length() != 12 || !request.cuspp().matches("\\d+")) {
                throw new IllegalArgumentException("CUSPP debe tener 12 dígitos");
            }
            if (request.comisionTipo() == null || request.comisionTipo().isBlank()) {
                throw new IllegalArgumentException("Tipo de comisión es obligatorio para AFP");
            }
            cuspp = request.cuspp();
            comisionTipo = request.comisionTipo();
        }

        // Upsert: find existing or create new
        InformacionPensionaria info = informacionPensionariaRepository.findByTrabajadorId(trabajadorId)
            .orElseGet(InformacionPensionaria::new);

        info.setTrabajador(trabajador);
        info.setAfp(afp);
        info.setCuspp(cuspp);
        info.setComisionTipo(comisionTipo);
        info.setSctr(request.sctr() != null ? request.sctr() : false);
        info.setFechaAfiliacion(request.fechaAfiliacion());
        info.setDocumentoReferencia(request.documentoReferencia());
        if (info.getEstado() == null) {
            info.setEstado("ACTIVO");
        }

        info = informacionPensionariaRepository.save(info);
        log.debug("Información pensionaria {} para trabajadorId={}", 
            info.getId() != null ? "actualizada" : "creada", trabajadorId);
        return InformacionPensionariaResponse.fromEntity(info);
    }
}

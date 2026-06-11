package com.clinica.rrhh.trabajador.dto;

import com.clinica.rrhh.periodo.dto.PeriodoLaboralResponse;
import com.clinica.rrhh.trabajador.entity.Trabajador;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record TrabajadorResponse(
    Long id,
    Long personaId,
    String personaNombres,
    String personaApellidoPaterno,
    String personaNumeroDocumento,
    String codigoTrabajador,
    LocalDate fechaIngreso,
    String tipo,
    String regimenLaboral,
    String cargo,
    Long areaFuncionalId,
    String banco,
    String cuentaSueldo,
    String cci,
    String contactoNombre,
    String contactoTelefono,
    Integer cantidadHijos,
    String nroColegiatura,
    Long tipoColegiaturaId,
    String tipoColegiaturaNombre,
    Boolean discapacidad,
    Boolean sindicalizado,
    List<PeriodoLaboralResponse> periodosLaborales,
    Boolean activo
) {
    @Override
    public final String toString() {
        return "TrabajadorResponse{id=" + id
            + ", personaId=" + personaId
            + ", codigoTrabajador=" + codigoTrabajador
            + ", fechaIngreso=" + fechaIngreso
            + ", tipo=" + tipo
            + ", regimenLaboral=" + regimenLaboral
            + ", cargo=" + cargo
            + ", areaFuncionalId=" + areaFuncionalId
            + ", banco=" + banco
            + ", contactoNombre=" + contactoNombre
            + ", cantidadHijos=" + cantidadHijos
            + ", nroColegiatura=" + nroColegiatura
            + ", tipoColegiaturaId=" + tipoColegiaturaId
            + ", tipoColegiaturaNombre=" + tipoColegiaturaNombre
            + ", discapacidad=" + discapacidad
            + ", sindicalizado=" + sindicalizado
            + ", periodosLaborales=" + (periodosLaborales != null ? periodosLaborales.size() + " entries" : "null")
            + ", activo=" + activo
            + "}";
    }

    public static TrabajadorResponse fromEntity(Trabajador entity) {
        return new TrabajadorResponse(
            entity.getId(),
            entity.getPersona() != null ? entity.getPersona().getId() : null,
            entity.getPersona() != null ? entity.getPersona().getNombres() : null,
            entity.getPersona() != null ? entity.getPersona().getApellidoPaterno() : null,
            entity.getPersona() != null ? entity.getPersona().getNumeroDocumento() : null,
            entity.getCodigoTrabajador(),
            entity.getFechaIngreso(),
            entity.getTipo() != null ? entity.getTipo().name() : null,
            entity.getRegimenLaboral() != null ? entity.getRegimenLaboral().name() : null,
            entity.getCargo(),
            entity.getAreaFuncionalId(),
            entity.getBanco(),
            entity.getCuentaSueldo(),
            entity.getCci(),
            entity.getContactoNombre(),
            entity.getContactoTelefono(),
            entity.getCantidadHijos(),
            entity.getNroColegiatura(),
            entity.getTipoColegiatura() != null ? entity.getTipoColegiatura().getId() : null,
            entity.getTipoColegiatura() != null ? entity.getTipoColegiatura().getNombre() : null,
            entity.getDiscapacidad(),
            entity.getSindicalizado(),
            entity.getPeriodosLaborales() != null
                ? entity.getPeriodosLaborales().stream().map(PeriodoLaboralResponse::fromEntity).toList()
                : Collections.emptyList(),
            entity.getActivo()
        );
    }
}

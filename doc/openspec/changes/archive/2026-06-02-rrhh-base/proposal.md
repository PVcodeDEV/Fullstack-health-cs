# Proposal: rrhh-base

## Intent

Foundation for RRHH module. Expands Trabajador for REMYPE, migrates CMP from Medico, adds Contrato + Derechohabiente — enabling worker, contract, and beneficiary tracking per Peruvian labor law.

## Scope

### In Scope
- Trabajador: tipoTrabajador, regimenLaboral, REMYPE fechas, bancario, emergencia, hijos, colegiatura, situacionEspecial
- CMP migration: Medico.cmp → Trabajador.nroColegiatura (backfill, med_cmp deprecated)
- Contrato: tipo, fechas, periodoPrueba, remuneracion, jornada, estado, renovaciones
- Derechohabiente: Persona FK + Trabajador FK, relación, vigencia, estado
- Java enums: TipoTrabajador, RegimenLaboral, SituacionEspecial, TipoJornada, TipoRelacionDerechohabiente, EstadoContrato
- Maestro catalogs: TipoContrato, TipoColegiatura
- Expanded Trabajador DTOs/Service/Controller + sub-resource endpoints
- Contrato + Derechohabiente full CRUD (DTO→Controller)
- Flyway V20: ALTER tb_trabajadores, CREATE tb_contratos, tb_derechohabientes, catálogos
- Tests (unit + integration)
- DataInitializer: rrhh permisos

### Out of Scope
- Planilla, CTS, Gratificaciones, Vacaciones, T-Registro, Asistencia, Utilidades, Documentos

## Capabilities

### New Capabilities
- `rrhh-trabajador`: Expanded Trabajador CRUD with sub-resource endpoints
- `rrhh-contrato`: Contrato lifecycle with estado state machine
- `rrhh-derechohabiente`: Beneficiary CRUD linked to Trabajador + Persona

### Modified Capabilities
None — CMP migration is internal refactor, API unchanged.

## Approach

Fields on Trabajador directly (no separate Colegiatura entity). Single-table Contrato with estado for renewals. Derechohabiente references Persona for age rules (hijos ≤18, ≤24 if studying). CMP backfilled via V20; med_cmp kept deprecated. Follow existing package-per-module layering.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `rrhh/trabajador/entity/` | Modified | +12 fields |
| `rrhh/trabajador/dto/service/controller/` | Modified | New fields + sub-resources |
| `rrhh/type/` | New | 6 enums |
| `rrhh/contrato/` | New | Full module |
| `rrhh/derechohabiente/` | New | Full module |
| `clinica/medico/entity/` | Modified | CMP delegates to Trabajador |
| `db/migration/V20__rrhh_base.sql` | New | DDL + backfill |
| `seguridad/bootstrap/DataInitializer.java` | Modified | +6 rrhh permisos |
| `maestro/` | New | TipoContrato, TipoColegiatura |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| CMP migration breaks Medico | Med | Keep med_cmp, deprecated |
| Derechohabiente FK constraint | Low | Validate Persona exists |
| Contrato date inversion | Low | Service-level validation |

## Rollback

Drop V20 columns/tables with `ALTER TABLE DROP COLUMN` + `DROP TABLE`. Restore original DTOs. med_cmp preserved — no data loss.

## Dependencies

- `modulo-persona` (Persona entity for Derechohabiente FK)

## Success Criteria

- [ ] All CRUD tests pass for Trabajador, Contrato, Derechohabiente
- [ ] Medico.getCmp() works via Trabajador — existing endpoints unchanged
- [ ] V20 applies and rollbacks cleanly on H2 + PostgreSQL
- [ ] Contrato estado transitions enforce domain rules (RESUELTO terminal)

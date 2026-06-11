# Proposal: RRHH Planilla — Vacaciones (Pequeña Empresa REMYPE)

## Intent

Peruvian Pequeña Empresa REMYPE grants 15 days paid vacation per full year of service. Without this module, the clinic cannot track earned accruals, schedule descanso, or comply with record-keeping — creating labor liability.

## Scope

### In Scope
- `VacacionRecord` — tracks earned derecho: worker, periodoRecord (12mo), diasCorrespondientes (15), estado (PENDIENTE/PROGRAMADO/PERDIDO)
- `VacacionGoce` — actual descanso: record FK, fechaInicio/Fin, estado (PROGRAMADO/EN_GOCE/COMPLETADO)
- Service — record creation on contrato anniversary, programming window, reduction for inasistencias/licencias, lifecycle state machine
- POST `registrar-record`, `programar`, `iniciar`, `completar`; GET list + by worker
- V28 migration: `tb_vacacion_record`, `tb_vacacion_goce`
- Tests per layering convention

### Out of Scope
- Blocking planilla when EN_GOCE, bulk scheduling, vacaciones truncas (<1yr), indemnización, self-service portal

## Capabilities

### New Capabilities
- `rrhh-vacacion`: Vacation accrual and descanso — 15-day annual REMYPE factor, 12mo record period, programar/iniciar/completar lifecycle, proportional reduction for licencias sin goce

### Modified Capabilities
None

## Approach

Two entities under `com.clinica.rrhh.vacacion`:
1. **VacacionRecord**: created at 12mo contrato anniversary. diasCorrespondientes = 15 minus (inasistenciasMes × 1.25). Estado machine: PENDIENTE → PROGRAMADO → PERDIDO.
2. **VacacionGoce**: FK to record, fechaInicio/Fin, diasEfectivos. Estado: PROGRAMADO → EN_GOCE → COMPLETADO.
3. Remuneración vacacional = sueldo at descanso start (from Contrato).
4. Caps: descanso within 12mo of record's fechaFinRecord. Expired → PERDIDO.
5. Multiple Goces per Record allowed (fractional goce).

Security: `rrhh:editar` writes, `rrhh:ver` reads. ~400-500 lines → recommend chained PRs.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `rrhh/vacacion/{entity,repository,service,dto,controller}/` | New | Full layered module |
| `db/migration/V28__rrhh_vacacion.sql` | New | DDL for 2 new tables |
| `rrhh-planilla` | None | Pure tracking, no planilla concept yet |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Record anniversary ≠ contrato start (suspensión perfecta) | Med | Validate against Licencia tables; unit test edge cases |
| Fractional goce (15 days split) | Low | Allow multiple Goces per Record; enforce sum ≤ diasCorrespondientes |
| ~500 lines may exceed review budget | High | Chain PRs: (1) entity+migration, (2) service, (3) controller+tests |

## Rollback

Drop V28: `DROP TABLE tb_vacacion_goce, tb_vacacion_record`. No concepts seeded. Existing modules unaffected.

## Dependencies

- `rrhh-base` — Trabajador, Contrato (remuneracion, fechas)
- `rrhh-contrato` — for licencia/suspensión lookups during reduction

## Success Criteria

- [ ] POST registrar-record for 12mo contrato → PENDIENTE with 15 días
- [ ] POST programar → VacacionGoce created; POST iniciar → EN_GOCE; POST completar → COMPLETADO
- [ ] 1 full mes inasistencia → record reduced to 13.75 días
- [ ] Record expired 12mo post fechaFinRecord → auto-PERDIDO
- [ ] V28 applies and rollbacks cleanly on H2 + PostgreSQL

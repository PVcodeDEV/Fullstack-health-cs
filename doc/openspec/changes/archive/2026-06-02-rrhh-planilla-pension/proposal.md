# Proposal: RRHH Planilla — Pension System Foundation

## Intent

Pension regime (AFP/ONP) is a prerequisite for ALL payroll deductions. Without knowing a worker's pension system, you can't calculate descuento pension, ESSALUD, or net pay. This PR establishes the pension data foundation — AFP catalog, historical rates, and per-worker pension info.

## Scope

### In Scope
- AFP catalog (`tb_afps`) in maestro — seed 4 AFPs (Prima, Profuturo, Habitat, Integra) + ONP
- Historical AFP rate tracking (`tb_afp_tasas_historicas`)
- Worker pension info (`tb_info_pensionaria`) — 1:1 with Trabajador, stores CUSPP, regime, comision tipo, SCTR, fechaAfiliacion
- Commission types: FLUJO (% of salary) and MIXTA (% + fixed fee)
- `GET /api/v1/afps` — public list for dropdowns
- `GET/PUT /api/v1/trabajadores/{id}/informacion-pensionaria` — read/upsert
- Flyway V23: 3 new tables + seed data with current SBS rates (2026)
- Tests: @DataJpaTest, @WebMvcTest, Mockito service, integration flow

### Out of Scope
- Payroll period calculation, ESSALUD (9%), Renta 5ta categoría
- CTS, gratificaciones, vacaciones, PLAME export
- Pension rate calculation engine (deferred to PR#2)
- SCTR pension premium calculation

## Capabilities

### New Capabilities
- `rrhh-pension`: Worker pension system — AFP catalog entity, historical rate tracking, pension info CRUD (get + upsert per worker)

### Modified Capabilities
None

## Approach

AFP catalog follows existing maestro pattern (BaseEntity, `codigo`+`nombre`). ONP stored as special tb_afps entry with null comision/prima. AfpTasaHistorica tracks SBS rate changes over time via `vigenciaDesde/vigenciaHasta`. InformacionPensionaria has unique constraint on trabajador_id — upsert semantics via `save()`. Follow existing layering: entity → repository → service → dto → controller. Permissions: `rrhh:ver` reads, `rrhh:editar` writes.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `maestro/entity/rrhh/Afp.java` | New | Catalog (codigo, nombre, activo) |
| `maestro/entity/rrhh/AfpTasaHistorica.java` | New | Historical rates (tipoComision, tasa, prima, vigencia) |
| `maestro/repository/controller/service/rrhh/Afp*` | New | CRUD for AFP catalog |
| `rrhh/pension/entity/InformacionPensionaria.java` | New | Worker pension (trabajadorId, cuspp, afpId, comisionTipo, sctr, fechaAfiliacion) |
| `rrhh/pension/repository/service/dto/controller/` | New | Full layer for pension info |
| `db/migration/V23__rrhh_pension.sql` | New | DDL + seed |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| AFP rates outdated at deploy | Low | Historical table; seed current SBS rates for 2026 |
| CUSPP format validation | Low | 12-digit numeric constraint in service layer + entity |
| ONP has no comision fields | Low | Nullable commission columns; service discriminates by codigo |

## Rollback

Drop V23: `DROP TABLE tb_info_pensionaria, tb_afp_tasas_historicas, tb_afps`. No data loss — rrhh-base tables unchanged.

## Dependencies

- `rrhh-base` (archived) — Trabajador entity required for InformacionPensionaria FK

## Success Criteria

- [ ] `GET /api/v1/afps` returns 5 entries with current commission rates
- [ ] Pension info upsert creates new record on first call, updates on second
- [ ] `@PreAuthorize("hasAuthority('rrhh:ver')")` enforced on reads, `rrhh:editar` on writes
- [ ] V23 applies and rollbacks on H2 + PostgreSQL

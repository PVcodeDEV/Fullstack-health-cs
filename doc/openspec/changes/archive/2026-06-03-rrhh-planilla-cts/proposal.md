# Proposal: RRHH Planilla — CTS (Compensación por Tiempo de Servicios)

## Intent

Peruvian labor law (Pequeña Empresa REMYPE) obligates CTS deposit in May and November — 15 days of remuneration per full year. Without this PR, the clinic cannot calculate the November 2026 CTS deposit and faces labor liability.

## Scope

### In Scope
- `tb_depositos_cts` entity — per-worker, per-semester: anio, periodo (MAYO/NOVIEMBRE), remuneracionComputable, gratificacionProporcional (1/6 avg of last 2 gratificaciones), montoDeposito, estado
- CTS generation service — compute for PeriodoPlanilla (May or November), truncamiento 30-day rule, REMYPE factor (15 days/year), average gratificación lookup
- Seed CTS (APORTE) concept in ConceptoPlanilla if needed for reporting
- POST `/api/v1/cts/calcular?periodoPlanillaId=X`, GET list + GET by ID
- V27 migration: DDL
- Tests: @DataJpaTest, Mockito service (truncamiento, gratif promedio edge cases), @WebMvcTest controller

### Out of Scope
- Actual disbursement/deposit in financial entity (solo cálculo)
- Editing or deleting CTS records (append-only)
- Intereses CTS (Ley 30478 — future PR)
- Retiro CTS (worker withdrawal flow)
- Vacaciones (PR#6), PLAME (PR#7)

## Capabilities

### New Capabilities
- `rrhh-cts`: CTS legal calculation for Pequeña Empresa REMYPE — semester-based with truncamiento 30-day rule, 1/6 average gratification in remuneración computable, 15-day annual accrual factor, append-only deposit records

### Modified Capabilities
None

## Approach

`CtsService` for a given PeriodoPlanilla (mes=5 → Noviembre, mes=11 → Mayo):
1. Derive semestre (Mayo: noviembre-abril, Noviembre: mayo-octubre)
2. Fetch active contratos with days in that semester
3. **Días computables**: per month worked, count 30 days (truncamiento). Entry day 1-14 = full month, day 15+ = start next month
4. **Remuneración computable**: sueldo base + asignación familiar (if ≥1 hijo) + 1/6 gratificación promedio de las 2 últimas + 1/6 bonif extraordinaria
5. **Gratificación promedio**: read last 2 Gratificacion records per worker (from `rrhh-gratificacion`). If <2 records, use available ones
6. **CTS (Pequeña Empresa REMYPE)**: (remuneracionComputable / 360) × díasLaborados (15-day annual accrual embedded in 360-day rate)
7. Persist each deposit record (PENDIENTE estado)
8. Idempotent: re-calculating updates existing records for same periodo

Package: `com.clinica.rrhh.cts` (5-layer structure). Permissions: `rrhh:editar` for POST, `rrhh:ver` for GETs.

Need to add `gratificacion-proporcional` to `application.yml` or use direct repository access to `tb_gratificaciones`. Prefer repository access — no new config values needed.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `rrhh/cts/{entity,repository,service,dto,controller}/` | New | Full layered module |
| `db/migration/V27__rrhh_cts.sql` | New | DDL for `tb_depositos_cts` |
| `maestro/service/concepto/ConceptoPlanillaService.java` | Modified | Seed CTS concept if needed |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Gratificación promedio requires past data; worker with 0 gratificaciones | Medium | 0 gratif → skip 1/6 addition; test edge case |
| Truncamiento 30-day diasLaborados ≠ mesesComputables (CTS vs gratif use different units) | Medium | Unit-test: contrato inicia 14/01 → 30d, 15/01 → 0d for January |
| Dependency on `rrhh-gratificacion` module (GratificacionRepository access) | Low | Inject GratificacionRepository via constructor; module lives in same `rrhh` package family |

## Rollback

Drop V27: `DROP TABLE tb_depositos_cts`. Purge CTS concept from ConceptoPlanilla if seeded. Existing planilla and gratificación tables unaffected.

## Dependencies

- `rrhh-planilla` (PR#2) — PeriodoPlanilla
- `rrhh-base` — Trabajador, Contrato.remuneracion, cantidadHijos
- `rrhh-gratificacion` (PR#3) — Gratificacion records for 1/6 promedio
- `application.yml` — rmv: 1130, regime already defined

## Success Criteria

- [ ] POST calcular with May period creates records; worker with 6 months full → CTS = (RC / 360) × 180
- [ ] Worker hired 20/01 → January excluded (truncamiento: day 15+ misses January)
- [ ] Worker hired 10/01 → January counts as 30 days (day 1-14 rule)
- [ ] Worker with 2 gratificaciones → 1/6 promedio included in RC
- [ ] Worker with 0 gratificaciones → RC excludes gratificación promedio (0 added)
- [ ] V27 applies and rollbacks cleanly on H2 + PostgreSQL

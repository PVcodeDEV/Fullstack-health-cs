# Proposal: RRHH Planilla — Gratificación Legal

## Intent

Peruvian labor law (Pequeña Empresa REMYPE) obligates gratificación in July & December — ½ month salary per worker with ≥1 month service. Without this PR, the clinic cannot legally pay the July 2026 gratificación and faces labor liability.

## Scope

### In Scope
- `tb_gratificaciones` entity — per-worker, per-semester: periodoLabel, trabajadorId, mesesComputables, remuneracionComputable, gratificacion, bonifExtraordinaria, total, estado
- Gratificación generation service — compute for PeriodoPlanilla (June or December), proportional logic, truncated-month rule, 9% extraordinary bonus (Ley 30334)
- Seed GRATIFICACION (INGRESO) + BONIF_EXTRAORDINARIA (APORTE) in ConceptoPlanilla
- POST `/api/v1/gratificaciones/calcular?periodoPlanillaId=X`, GET list + GET by ID
- V26 migration: DDL + conceptos seed
- Tests: @DataJpaTest, Mockito service, @WebMvcTest controller

### Out of Scope
- CTS (PR#4), Vacaciones (PR#5), PLAME (PR#6)
- Payment/disbursement flow
- Editing or deleting gratificación records (append-only)

## Capabilities

### New Capabilities
- `rrhh-gratificacion`: Gratificación legal — semester-based calculation with proportional months, 9% extraordinary bonus, zero pension/renta deductions, append-only records

### Modified Capabilities
None

## Approach

`GratificacionService` for a given PeriodoPlanilla:
1. Determine semester (Enero-Junio or Julio-Diciembre) from planilla mes
2. Fetch active contratos with ≥1 month in that semester
3. **Meses computables**: count full months from contrato.inicio; day 1-14 = full month, 15+ = start next month
4. **Remun. computable**: sueldo base + asignación familiar if aplica
5. **Gratificación (Pequeña Empresa REMYPE)**: ≥6 meses = ½ × remuneracion; <6 = (remuneracion / 12) × meses
6. **Bonif. Extraordinaria**: 9% of gratificación (employer aporte, not deduction)
7. **No descuentos**: zero AFP/ONP/Renta 5ta
8. Persist each worker record + register concepts

Idempotent: re-calculating updates existing records for that semester.

Package: `com.clinica.rrhh.gratificacion` (5-layer structure). Permissions: `rrhh:editar` for POST, `rrhh:ver` for GETs.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `rrhh/gratificacion/{entity,repository,service,dto,controller}/` | New | Full layered module |
| `maestro/service/concepto/ConceptoPlanillaService.java` | Modified | Seed GRATIFICACION + BONIF_EXTRAORDINARIA |
| `db/migration/V26__rrhh_gratificacion.sql` | New | DDL + conceptos seed |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Truncated month boundary wrong (day 14 vs 15) | Low | Unit-test edge cases: inicio 14/01 vs 15/01 |
| Semestre mismatch (mes 6 = Jun → Ene-Jun) | Low | Map PeriodoPlanilla.mes → semester; validate only mes 6 or 12 |

## Rollback

Drop V26: `DROP TABLE tb_gratificaciones`. Purge GRATIFICACION/BONIF_EXTRAORDINARIA from ConceptoPlanilla. Existing planilla tables unaffected.

## Dependencies

- `rrhh-planilla` (PR#2) — PeriodoPlanilla, ConceptoPlanillaService
- `rrhh-base` — Trabajador, Contrato.remuneracion, cantidadHijos
- `application.yml` — rmv: 1130 (asignación familiar base), regime factor (pequeña-empresa → ½ gratif, 15d CTS, 15d vac)

## Success Criteria

- [ ] POST calcular with June period creates records, months 1-6 → ½ sueldo + 9% bonus
- [ ] Worker hired 20/01 → months Feb-Jun = 5 → proportional = (remuneracion / 12) × 5
- [ ] Worker hired 10/01 → counts Jan-Jun = 6 (full month rule) → ½ sueldo
- [ ] Zero AFP/ONP/Renta 5ta in gratificación output
- [ ] V26 applies and rollbacks cleanly on H2 + PostgreSQL

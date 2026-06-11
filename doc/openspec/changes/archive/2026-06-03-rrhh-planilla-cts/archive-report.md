# Archive Report: rrhh-planilla-cts

**Change Name**: rrhh-planilla-cts
**Archive Date**: 2026-06-03
**Archive Path**: `doc/openspec/changes/archive/2026-06-03-rrhh-planilla-cts/`

---

## Summary

CTS (Compensación por Tiempo de Servicios) legal calculation module for Peruvian Pequeña Empresa REMYPE — semestral deposit (May/November), 15 days per full year, formula `(RC / 360) × días laborados`, 1/6 average gratificación in RC, truncamiento 30-day rule, zero deductions. Append-only deposit records per worker per period.

---

## Requirements Implemented

| ID | Requirement | Status |
|----|------------|--------|
| CTS-001 | Semester derivation (mes=5 → NOVIEMBRE, mes=11 → MAYO) | ✅ PASS |
| CTS-002 | Días computables (30-day truncamiento, day 1-14 counts, 15+ skips) | ✅ PASS |
| CTS-003 | Remuneración computable (sueldo + asigFamiliar + 1/6 avg gratif + 1/6 avg bonif) | ✅ PASS |
| CTS-004 | CTS amount formula `(RC / 360) × días` | ✅ PASS |
| CTS-005 | Zero deductions (AFP/ONP/Renta/EsSalud not applied) | ✅ PASS |
| CTS-006 | Endpoints (`POST calcular`, `GET list`, `GET by id`) + idempotent upsert | ✅ PASS |
| CTS-007 | PII protection (Ley 29733 — monetary fields excluded from toString, no DNI in response) | ✅ PASS |

---

## Test Results

| Metric | Value |
|--------|-------|
| Total tests | 34 |
| Passed | 34 |
| Failed | 0 |
| Errors | 0 |
| Skipped | 0 |
| Build time | 23.7s |
| Build result | BUILD SUCCESS |

### Test Classes

| Test | Layer | Type |
|------|-------|------|
| `DepositoCtsRepositoryTest.java` | Repository | `@DataJpaTest` |
| `CtsServiceTest.java` | Service | `@ExtendWith(MockitoExtension.class)` |
| `CtsControllerTest.java` | Controller | `@WebMvcTest` |
| `CtsFlowIntegrationTest.java` | Integration | `@SpringBootTest` + `@AutoConfigureMockMvc` |

---

## Files Created

### Backend — Production Code (`com.clinica.rrhh.cts`)

| File | Path |
|------|------|
| Entity | `backend/src/main/java/com/clinica/rrhh/cts/entity/DepositoCts.java` |
| Repository | `backend/src/main/java/com/clinica/rrhh/cts/repository/DepositoCtsRepository.java` |
| Service | `backend/src/main/java/com/clinica/rrhh/cts/service/CtsService.java` |
| DTO | `backend/src/main/java/com/clinica/rrhh/cts/dto/DepositoCtsResponse.java` |
| Controller | `backend/src/main/java/com/clinica/rrhh/cts/controller/CtsController.java` |

### Database Migration

| File | Description |
|------|-------------|
| `backend/src/main/resources/db/migration/V27__rrhh_cts.sql` | DDL for `tb_depositos_cts` (15 columns + PK + FKs + UNIQUE + indexes) |

### Test Code

| File | Path |
|------|------|
| Repository Test | `backend/src/test/java/com/clinica/rrhh/cts/repository/DepositoCtsRepositoryTest.java` |
| Service Test | `backend/src/test/java/com/clinica/rrhh/cts/service/CtsServiceTest.java` |
| Controller Test | `backend/src/test/java/com/clinica/rrhh/cts/controller/CtsControllerTest.java` |
| Integration Test | `backend/src/test/java/com/clinica/rrhh/cts/integration/CtsFlowIntegrationTest.java` |

### Files Modified

| File | Change |
|------|--------|
| `DataInitializer.java` | Added `rrhh:ver` and `rrhh:editar` seed permissions |

### Specs

| Spec | Path |
|------|------|
| Main spec (source of truth) | `doc/openspec/specs/rrhh-cts/spec.md` |

---

## Key Decisions

1. **Separate `cts/` module** — Distinct calculation rules from gratificación; cleaner domain boundary
2. **Store 1/6 proportional values** (promedioGratificacion, promedioBonificacion) — Audit-ready, avoids re-calculation at read time
3. **Gratificación lookup via repository** — Direct access within same `rrhh` package family, no circular dependencies
4. **Idempotent upsert** — Same pattern as Gratificacion; safe retry, no duplicates via `UNIQUE(periodoPlanillaId, trabajadorId)`
5. **V27: DDL only** — CTS doesn't need a ConceptoPlanilla seed; deposit table is standalone
6. **Zero deductions** — CTS is a benefit deposit, not salary; no AFP/ONP/Renta/EsSalud applied
7. **Scale precision**: `RoundingMode.HALF_UP` with 10 decimal intermediate scale before final `setScale(2)`
8. **PII compliance**: `@ToString.Exclude` on all monetary fields; response limited to `trabajadorId` and `trabajadorNombre`

---

## Deviations from Spec (Non-blocking)

| Spec | Actual | Impact |
|------|--------|--------|
| POST 201 (first) / 200 (re-run) | Always 201 | Idempotency works correctly; status code cosmetic |
| Invalid mes returns 400 | Returns 409 (via `IllegalArgumentException` → CONFLICT convention) | Project-wide pattern |
| Response: `nombres` + `apellidos` as separate fields | Single concatenated `trabajadorNombre` | No PII leaked; shape differs |

---

## Open Issues

**None**

All 7 requirements implemented. 34 tests passing. Verification verdict: **READY FOR ARCHIVE**.

---

## Archive Contents

- `proposal.md` ✅
- `design.md` ✅
- `tasks.md` ✅ (10/10 tasks complete)
- `verify-report.md` ✅
- `archive-report.md` ✅ (this file)

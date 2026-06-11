# Archive Report: rrhh-planilla-vacacion

**Change**: RRHH Planilla — Vacaciones (Pequeña Empresa REMYPE)
**Archive Date**: 2026-06-04
**Status**: READY FOR ARCHIVE

---

## Summary

| Metric | Result |
|--------|--------|
| **Requirements** | 7 (VAC-001 through VAC-007) |
| **Tasks completed** | 13 of 13 (100%) |
| **Tests run** | 59 |
| **Tests passed** | 59 |
| **Tests failed** | 0 |
| **Build** | SUCCESS |

---

## Requirements Implemented

| ID | Description | Status |
|----|-------------|--------|
| VAC-001 | Record at 12-month anniversary | WARNING — `estado=ACTIVO` (design override), spec says `PENDIENTE` |
| VAC-002 | Goce lifecycle (PROGRAMADO → EN_CURSO → COMPLETADO) | PASS |
| VAC-003 | Remuneración vacacional captured at programación | PASS |
| VAC-004 | Reduction for inasistencias (1/12 per month) | PASS |
| VAC-005 | Loss at expiry (auto-PERDIDO) | PASS |
| VAC-006 | Endpoints and idempotency | WARNING — URL path deviations, 201 vs 200 on idempotent re-run |
| VAC-007 | PII protection (Ley 29733) | PASS |

---

## Files Created/Modified

### Backend Source (9 files)

| File | Action |
|------|--------|
| `backend/src/main/java/com/clinica/rrhh/vacacion/entity/VacacionRecord.java` | Create |
| `backend/src/main/java/com/clinica/rrhh/vacacion/entity/VacacionGoce.java` | Create |
| `backend/src/main/java/com/clinica/rrhh/vacacion/repository/VacacionRecordRepository.java` | Create |
| `backend/src/main/java/com/clinica/rrhh/vacacion/repository/VacacionGoceRepository.java` | Create |
| `backend/src/main/java/com/clinica/rrhh/vacacion/service/VacacionService.java` | Create |
| `backend/src/main/java/com/clinica/rrhh/vacacion/dto/VacacionRecordResponse.java` | Create |
| `backend/src/main/java/com/clinica/rrhh/vacacion/dto/VacacionGoceResponse.java` | Create |
| `backend/src/main/java/com/clinica/rrhh/vacacion/dto/ProgramarRequest.java` | Create |
| `backend/src/main/java/com/clinica/rrhh/vacacion/controller/VacacionController.java` | Create |

### Backend Tests (5 files)

| File | Action |
|------|--------|
| `backend/src/test/java/com/clinica/rrhh/vacacion/repository/VacacionRecordRepositoryTest.java` | Create |
| `backend/src/test/java/com/clinica/rrhh/vacacion/repository/VacacionGoceRepositoryTest.java` | Create |
| `backend/src/test/java/com/clinica/rrhh/vacacion/service/VacacionServiceTest.java` | Create |
| `backend/src/test/java/com/clinica/rrhh/vacacion/controller/VacacionControllerTest.java` | Create |
| `backend/src/test/java/com/clinica/rrhh/vacacion/integration/VacacionFlowIntegrationTest.java` | Create |

### Database Migration (1 file)

| File | Action |
|------|--------|
| `backend/src/main/resources/db/migration/V28__rrhh_vacaciones.sql` | Create |

### Specification (1 file)

| File | Action |
|------|--------|
| `doc/openspec/specs/rrhh-vacacion/spec.md` | Create (main spec) |

---

## Key Decisions

| Decision | Rationale |
|----------|-----------|
| Two entities (Record + Goce) | Mirrors domain; each state machine is clean |
| Module `rrhh.vacacion` | Follows CTS/gratif pattern, distinct domain |
| `diasReduccion` in `calcular()` param | No Licencia table exists yet; explicit param, evolve later |
| Auto-expiry in `calcular()` (no scheduler) | Lazy evaluation, same pass, no new infra |
| V28 DDL only | No conceptos needed |
| `@PreAuthorize` auth | Class-level `rrhh:ver`, writes override with `rrhh:editar` |

---

## Open Issues (Warnings — No Critical Issues)

| ID | Description | Priority |
|----|-------------|----------|
| VAC-001-STATE | Spec says `estado=PENDIENTE`, code uses `ACTIVO` (design override) | LOW |
| VAC-006-TRABAJADOR | `POST /calcular` has no `trabajadorId` param — processes ALL workers | MEDIUM |
| VAC-006-IDEMPOTENT | Idempotent re-run returns 201 instead of 200 | LOW |
| VAC-006-URL | URL path deviations from spec/design | LOW |
| VAC-006-HTTPSTATUS | Business rule violations return 409 CONFLICT, spec says 400 | LOW |
| VAC-007-TOSTRING | `VacacionRecord` includes day counts in toString (spec strictness) | LOW |

---

## Test Coverage Breakdown

| Layer | Tests | Status |
|-------|-------|--------|
| Repository (VacacionRecordRepositoryTest) | 5 | ✅ ALL PASS |
| Repository (VacacionGoceRepositoryTest) | 5 | ✅ ALL PASS |
| Service (VacacionServiceTest) | 28 | ✅ ALL PASS |
| Controller (VacacionControllerTest) | 19 | ✅ ALL PASS |
| Integration (VacacionFlowIntegrationTest) | 1 | ✅ PASS |
| **Total (vacacion)** | **58** | **58 PASS, 0 FAIL** |
| **Shared context** | **1** | **1 PASS** |
| **Grand Total** | **59** | **59 PASS, 0 FAIL** |

---

## Archive Contents

- proposal.md ✅
- design.md ✅
- tasks.md ✅ (13/13 tasks complete)
- verify-report.md ✅
- archive-report.md ✅ (this file)

---

## SDD Cycle Complete

This change has been fully planned, implemented, verified, and archived.
Ready for the next change.

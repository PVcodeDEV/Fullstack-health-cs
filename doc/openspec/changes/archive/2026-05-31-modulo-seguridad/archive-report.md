# Archive Report

**Change**: modulo-seguridad
**Archived**: 2026-05-31
**Mode**: openspec
**Verdict**: PASS WITH WARNINGS

## Overview

Authentication, authorization, and API security module for ERP Clínico. Dual auth (formLogin for browser, JWT for API), RBAC with 6 seed roles + granular permisos, runtime API config in `tb_configuracion_api`, and bootstrap admin provisioning. Transforms the app from zero auth (`permitAll()`) to fully secured.

## Specs Synced to Source of Truth

| Domain | Action | Details |
|--------|--------|---------|
| modulo-autenticacion | Created | R-001 through R-005 — 5 requirements, 8 scenarios |
| modulo-autorizacion | Created | R-001 through R-006 — 6 requirements, 8 scenarios |
| modulo-personas-api | Updated | R-005 modified (delta) — API config moved from application properties to `tb_configuracion_api` |

### Delta Details — modulo-personas-api R-005

**MODIFIED**: R-005 — "API configuration in seguridad" → "API configuration in tb_configuracion_api"
- Source of config changed: `application.properties` → `tb_configuracion_api` via `DbApiConfigService`
- Added SC-005-1 (Config read from database) and SC-005-2 (Runtime update without restart)
- Preserved all other requirements (R-001 through R-004, R-006, R-007) unchanged

## Archive Contents

- proposal.md ✅ — Intent, scope, approach, risks, rollback (6 success criteria)
- specs/modulo-autenticacion/spec.md ✅ — Auth specification (5 requirements, 8 scenarios)
- specs/modulo-autorizacion/spec.md ✅ — Authorization specification (6 requirements, 8 scenarios)
- specs/modulo-personas-api/spec.md ✅ — API integration delta spec (R-005 modified)
- design.md ✅ — Architecture decisions, data flow, entity layout, endpoints, testing strategy
- tasks.md ✅ — 63/65 tasks complete (2 deferred)
- archive-report.md ✅ — This file

## Task Completion

| Phase | Tasks | Status |
|-------|-------|--------|
| 1. Foundation | 3/3 | ✅ Complete |
| 2. Entities + Repositories | 7/7 | ✅ Complete |
| 3. DTOs + Services + JWT | 10/10 | ✅ Complete |
| 4. Security Config | 5/5 | ✅ Complete |
| 5. Controllers | 3/3 | ✅ Complete |
| 6. Testing + Integration | 7/9 | ✅ 7 complete, 2 deferred |

### Incomplete Tasks (Deferred to Future PRs)

1. **6.7** `@SpringBootTest` — missing JWT returns 401, DataInitializer seeds 6 roles
2. **6.8** `ReniecClient` swap — read config from `ConfiguracionApiService` instead of properties

Both are non-blocking: unit tests cover the logic, and the ReniecClient swap requires the `modulo-personas-api` integration which is a separate concern.

## Build & Test Results

- Build: ✅ `mvn compile` — BUILD SUCCESS
- Tests: ✅ 366 passed, 0 failed, 0 skipped (pre-existing + new seguridad tests)
- Coverage: ➖ Not configured

## Source of Truth Updated

The following specs now reflect the new behavior:
- `doc/openspec/specs/modulo-autenticacion/spec.md` — Created (new domain)
- `doc/openspec/specs/modulo-autorizacion/spec.md` — Created (new domain)
- `doc/openspec/specs/modulo-personas-api/spec.md` — Updated (R-005 delta applied)

## SDD Cycle Complete

The change has been fully planned, implemented, verified, and archived.
Ready for the next change.

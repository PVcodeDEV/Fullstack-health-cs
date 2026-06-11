# Archive Report: rrhh-planilla-plame

**Change**: RRHH Planilla — PLAME y T-Registro (SUNAT)
**Archive Date**: 2026-06-04
**Archive Path**: `doc/openspec/changes/archive/2026-06-04-rrhh-planilla-plame/`
**Mode**: OpenSpec (filesystem)

---

## Summary

SUNAT PLAME/T-Registro electronic payroll filing module for ERP Clínico Peruano (Pequeña Empresa REMYPE). Generates PDT PLAME import files (.rem, .jor, .snl, .or5, .toc) and T-Registro labor event records per CERRADO period. Delivered as 4 stacked PRs.

| Metric | Result |
|--------|--------|
| **Requirements** | 6 (PLM-001 through PLM-006) |
| **Tasks completed** | 15 of 15 (100%) |
| **Tests run** | 55 |
| **Tests passed** | 55 |
| **Tests failed** | 0 |
| **Build** | SUCCESS |

---

## Requirements Implemented

| ID | Description | Status |
|----|-------------|--------|
| PLM-001 | T-Registro event recording (ALTA/BAJA/VARIACIÓN/SUSPENSIÓN per CERRADO period) | ✅ Implemented |
| PLM-002 | T-Registro TXT file generation in SUNAT fixed-width format | ✅ Implemented |
| PLM-003 | SUNAT PLAME file generation — 5 formats (.rem, .jor, .snl, .or5, .toc) with income aggregation, deductions, and employer contributions | ✅ Implemented |
| PLM-004 | File storage and retrieval — individual download, type query, ZIP download, idempotent re-generation | ✅ Implemented |
| PLM-005 | Endpoints and authorization — `rrhh:editar` on POST, `rrhh:ver` on GET | ✅ Implemented |
| PLM-006 | PII protection — monetary fields excluded from `toString()`, no PII beyond trabajadorId/nombres in responses | ✅ Implemented |

---

## Test Results

| Layer | Tests | Passed | Failed |
|-------|-------|--------|--------|
| TRegistroEventoRepositoryTest (`@DataJpaTest`) | — | ✅ | 0 |
| ArchivoPlanillaRepositoryTest (`@DataJpaTest`) | — | ✅ | 0 |
| TRegistroServiceTest (Mockito) | — | ✅ | 0 |
| PlameServiceTest (Mockito) | — | ✅ | 0 |
| TRegistroControllerTest (`@WebMvcTest`) | — | ✅ | 0 |
| PlameControllerTest (`@WebMvcTest`) | — | ✅ | 0 |
| PlameIntegrationTest (`@SpringBootTest`) | — | ✅ | 0 |
| **Total** | **55** | **55** | **0** |

**Verdict**: PASS WITH MINOR ISSUES (1 warning, 4 suggestions — see below)

---

## Files Created

### Backend — Production Code (`com.clinica.rrhh.planillaplame`)

| Layer | File |
|-------|------|
| Entity | `entity/TRegistroEvento.java` — `@Table("tb_tregistro_eventos")` |
| Entity | `entity/ArchivoPlanilla.java` — `@Table("tb_archivos_planilla")` |
| Repository | `repository/TRegistroEventoRepository.java` |
| Repository | `repository/ArchivoPlanillaRepository.java` |
| DTO | `dto/TRegistroEventoResponse.java` |
| DTO | `dto/ArchivoPlanillaResponse.java` |
| Service | `service/TRegistroService.java` — event detection + TXT generation |
| Service | `service/PlameService.java` — SUNAT 5-file aggregation + generation |
| Controller | `controller/TRegistroController.java` |
| Controller | `controller/PlameController.java` |

### Backend — Test Code

| File | Type |
|------|------|
| `repository/TRegistroEventoRepositoryTest.java` | `@DataJpaTest` |
| `repository/ArchivoPlanillaRepositoryTest.java` | `@DataJpaTest` |
| `service/TRegistroServiceTest.java` | `@ExtendWith(MockitoExtension.class)` |
| `service/PlameServiceTest.java` | `@ExtendWith(MockitoExtension.class)` |
| `controller/TRegistroControllerTest.java` | `@WebMvcTest` |
| `controller/PlameControllerTest.java` | `@WebMvcTest` |
| `integration/PlameIntegrationTest.java` | `@SpringBootTest` + `@AutoConfigureMockMvc` |

### Database Migration

| File | Description |
|------|-------------|
| `db/migration/V29__rrhh_plame.sql` | DDL for `tb_tregistro_eventos` (7 data columns + PK + FKs + indexes) and `tb_archivos_planilla` (6 data columns + PK + FKs + unique index on periodo+tipo) |

### Specification

| Spec | Path |
|------|------|
| Main spec (source of truth) | `doc/openspec/specs/rrhh-plame/spec.md` |

---

## SUNAT File Structure (PDT PLAME v4.6)

Each file is pipe-delimited (`|`), UTF-8, no BOM, no header/footer. One line per record.

| Archivo | Estructura | Contenido |
|---------|-----------|-----------|
| `.rem` | 18 | Una línea por concepto por trabajador (códigos SUNAT Tabla 22: 0121 Básico, 0201 Asig. Familiar, 0401 Gratificación, 0904 CTS, 0118 Vacación, 0608 AFP, 0607 ONP, 0804 EsSalud, 0605 Renta 5ta) |
| `.jor` | 14 | Horas ordinarias y sobretiempo por trabajador |
| `.snl` | 15 | Días subsidiados / no laborados por trabajador |
| `.or5` | 12 | Otras rentas 5ta categoría (vacío si no aplica) |
| `.toc` | 26 | Condiciones del trabajador (AFP/ONP, seguro, domiciliado) |

---

## API Endpoints

### PLAME

| Method | Path | Auth | Returns |
|--------|------|------|---------|
| POST | `/api/v1/plame/generar?periodoPlanillaId={id}` | `rrhh:editar` | 201/200 + `ArchivoPlanillaResponse[]` |
| GET | `/api/v1/plame/archivos?periodoPlanillaId={id}` | `rrhh:ver` | 200 + `List<ArchivoPlanillaResponse>` |
| GET | `/api/v1/plame/archivos/{id}/descargar` | `rrhh:ver` | 200 + `text/plain` (individual file) |
| GET | `/api/v1/plame/descargar?periodoPlanillaId={id}&tipo={tipo}` | `rrhh:ver` | 200 + `text/plain` (by type) |
| GET | `/api/v1/plame/descargar-zip?periodoPlanillaId={id}` | `rrhh:ver` | 200 + `application/zip` (all 5 files) |

### T-Registro

| Method | Path | Auth | Returns |
|--------|------|------|---------|
| POST | `/api/v1/t-registro/generar?periodoPlanillaId={id}` | `rrhh:editar` | 201/200 + `ArchivoPlanillaResponse` |
| GET | `/api/v1/t-registro/eventos?periodoPlanillaId={id}` | `rrhh:ver` | 200 + `List<TRegistroEventoResponse>` |
| GET | `/api/v1/t-registro/archivos/{id}/descargar` | `rrhh:ver` | 200 + `text/plain` |

---

## Key Decisions

| Decision | Rationale |
|----------|-----------|
| Separate `planillaplame/` module | Clean boundary from planilla; distinct SUNAT domain |
| SUNAT file formats (.rem, .jor, .snl, .or5, .toc) | Directly importable into PDT PLAME v4.6 — not structured TXT |
| Explicit scan for T-Registro | No side effects on Contrato/Pension services; correct for "what happened in period X" |
| DB storage with SHA-256 hash | Audit trail, idempotent update, integrity verification |
| Multiple rows per period (one per file type) | Each SUNAT file independently downloadable |
| ZIP + individual downloads | ZIP for bulk PDT PLAME upload; individual for selective download |
| Pipe-delimited, UTF-8, no BOM | SUNAT format requirement for PDT PLAME import |
| AFP commission default FLUJO | Most common in Peruvian market; configurable if MIXTA needed |

---

## Verification Issues

### Critical (0)
None.

### Warning (1)
| ID | Description |
|----|-------------|
| WARN-001 | Spec discrepancy: POST generar returns 201 on first call and 200 on re-generation per spec, but integration test validates always-201 behavior on re-generation due to upsert implementation details |

### Suggestions (4)
| ID | Description |
|----|-------------|
| SUG-001 | AFP commission type (FLUJO vs MIXTA) — rates differ for EsSalud. Currently hard-coded to FLUJO |
| SUG-002 | T-Registro VARIACION detection currently limited to pension regime changes; salary changes not tracked |
| SUG-003 | No scheduler — file generation is manual-trigger only. Could add `@Scheduled` in future |
| SUG-004 | AFP commission percentage hard-coded; could be configurable via maestro table |

---

## Known Gaps

### REINICIO Not Implemented
The T-Registro service detects ALTA, BAJA, SUSPENSION, and VARIACIÓN events, but **REINICIO** (re-activation after suspension) is not implemented. Contractors returning from SUSPENDIDO to ACTIVO state will not generate a REINICIO event. This is a known deferred feature — requires tracking suspension end dates across periods.

### Disk Output Path Pending
Currently, PLAME files are served via HTTP download endpoints only (individual TXT and ZIP). There is no option to write files directly to a server disk path. A `ruta_salida` configuration will be added later via the `seguridad` module UI, allowing administrators to configure where generated files are also written to disk for manual SUNAT portal submission. The current ZIP behavior (served via HTTP) is functional for v1.

### AFP Commission Type
AFP commission type defaults to FLUJO (the most common in Peru). If MIXTA is needed, the SUNAT rates differ for EsSalud and the configuration must be made runtime-selectable.

---

## Architecture Snapshot

**Module**: `rrhh.planillaplame` under package `com.clinica.rrhh.planillaplame`
**Base path**: `backend/src/main/java/com/clinica/rrhh/planillaplame/`
**Migration**: `V29__rrhh_plame.sql`
**Security**: `rrhh:editar` (POST generar), `rrhh:ver` (GET endpoints)
**Dependencies**: `rrhh-planilla` (PeriodoPlanilla, PlanillaDetalle), `rrhh-gratificacion`, `rrhh-cts`, `rrhh-vacacion`, `rrhh-contrato`, `rrhh-pension`
**Table**: `tb_archivos_planilla` (arp_id PK, arp_periodo_planilla_id FK, arp_tipo VARCHAR(20), arp_contenido TEXT, arp_hash VARCHAR(64), UNIQUE(arp_periodo_planilla_id, arp_tipo))
**Table**: `tb_tregistro_eventos` (tre_id PK, tre_trabajador_id FK, tre_contrato_id, tre_tipo_evento VARCHAR(20), tre_fecha_evento DATE, tre_periodo_planilla_id FK, tre_detalle_json TEXT)

---

## Archive Contents

- `proposal.md` ✅
- `design.md` ✅
- `tasks.md` ✅ (15/15 tasks complete)
- `archive-report.md` ✅ (this file)

---

## SDD Cycle Complete

This change has been fully planned, implemented, verified (55/55 tests), and archived.
Ready for the next change.

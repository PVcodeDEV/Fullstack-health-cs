# RRHH PLAME Specification

## Purpose

SUNAT electronic filing for Pequeña Empresa REMYPE: T-Registro TXT (labor events per worker per period) and PLAME SUNAT import files (.rem, .jor, .snl, .or5, .toc) for PDT PLAME v4.6. On-demand, per-CERRADO-period generation.

## Requirements

### Requirement: PLM-001 — T-Registro event recording

The system MUST record labor events (ALTA, BAJA, VARIACIÓN, SUSPENSIÓN) in `tb_tregistro_eventos` per CERRADO period, derived from contrato creation/termination and pension regime changes. Each event MUST capture tipo, trabajadorId, fechaEvento, and period.

#### Scenario: S-PLM-001-1 — New hires and terminations detected

- GIVEN CERRADO period with 1 new contrato and 1 termination
- WHEN T-Registro eventos generated for that period
- THEN 2 eventos created: ALTA for new hire, BAJA for termination

#### Scenario: S-PLM-001-2 — No contrato changes

- GIVEN CERRADO period with no contrato or pension changes
- WHEN T-Registro eventos generated
- THEN empty event list returned

### Requirement: PLM-002 — T-Registro file generation

The system MUST generate a TXT file in SUNAT T-Registro fixed-width format for a given CERRADO period, containing all recorded eventos from PLM-001.

#### Scenario: S-PLM-002-1 — Generate with events

- GIVEN CERRADO period with 2 eventos recorded
- WHEN POST `/api/v1/t-registro/generar?periodoPlanillaId=X`
- THEN returns 201; TXT contains ALTA and BAJA lines in SUNAT format

#### Scenario: S-PLM-002-2 — No events in period

- GIVEN CERRADO period with zero eventos
- WHEN POST `/api/v1/t-registro/generar`
- THEN returns 200 with header + footer only (no detail lines)

### Requirement: PLM-003 — SUNAT PLAME file generation

The system MUST aggregate per-worker income (base, gratificación, CTS, vacaciones), deductions (AFP/ONP, Renta 5ta), and employer contributions (EsSalud) from existing modules, then generate the 5 SUNAT file formats for PDT PLAME import: `.rem` (Estructura 18), `.jor` (Estructura 14), `.snl` (Estructura 15), `.or5` (Estructura 12), `.toc` (Estructura 26). Each file is pipe-delimited, UTF-8, one line per record.

#### Scenario: S-PLM-003-1 — CERRADO period with data

- GIVEN CERRADO PeriodoPlanilla, 2 workers with planilla + gratif + CTS data
- WHEN POST `/api/v1/plame/generar?periodoPlanillaId=X`
- THEN returns 201; 5 ArchivoPlanilla records created (tipos REM, JOR, SNL, OR5, TOC)
- AND `.rem` file contains concept lines per worker (Básico, Asignación Familiar, AFP/ONP, EsSalud, etc.)
- AND `.jor` file has one line per worker with horas ordinarias
- AND `.toc` file has pension indicator for each worker

#### Scenario: S-PLM-003-2 — ABIERTO period rejected

- GIVEN ABIERTO PeriodoPlanilla
- WHEN POST `/api/v1/plame/generar`
- THEN returns 409 (period must be CERRADO)

#### Scenario: S-PLM-003-3 — No worker data

- GIVEN CERRADO period with no workers or no generated planilla
- WHEN POST `/api/v1/plame/generar`
- THEN returns 200; all 5 files generated with zero workers (no detail lines, empty files)

### Requirement: PLM-004 — File storage and retrieval

The system MUST persist files in `tb_archivos_planilla` (arp_tipo, arp_periodo_planilla_id, arp_contenido as TEXT). Re-generation SHALL update the existing record for the same tipo+periodo. GET SHALL return file content as downloadable TXT, individually or as ZIP.

#### Scenario: S-PLM-004-1 — Download individual file

- GIVEN PLAME files generated for 2026-ENERO
- WHEN GET `/api/v1/plame/archivos/{id}/descargar`
- THEN returns 200 with Content-Type text/plain; charset=UTF-8
- AND Content-Disposition: attachment; filename="{ruc}.rem"

#### Scenario: S-PLM-004-2 — Download specific type via query

- GIVEN PLAME files generated for 2026-ENERO
- WHEN GET `/api/v1/plame/descargar?periodoPlanillaId=1&tipo=JOR`
- THEN returns 200 with Content-Disposition: attachment; filename="{ruc}.jor"

#### Scenario: S-PLM-004-3 — Download all as ZIP

- GIVEN PLAME files generated for 2026-ENERO
- WHEN GET `/api/v1/plame/descargar-zip?periodoPlanillaId=1`
- THEN returns 200 with Content-Type application/zip
- AND Content-Disposition: attachment; filename="{ruc}-2026-ENERO.zip"
- AND ZIP contains 5 files: {ruc}.rem, {ruc}.jor, {ruc}.snl, {ruc}.or5, {ruc}.toc

#### Scenario: S-PLM-004-4 — Re-generation updates all files

- GIVEN existing PLAME files for 2026-ENERO (5 rows)
- WHEN POST `/api/v1/plame/generar` again after payroll data changes
- THEN returns 200; all 5 records updated (no duplicates)

### Requirement: PLM-005 — Endpoints and authorization

POST generar: `rrhh:editar`. GET descargar and GET eventos: `rrhh:ver`. `@PreAuthorize` on all endpoints.

#### Scenario: S-PLM-005-1 — Authorized write

- GIVEN user with `rrhh:editar`, CERRADO period
- WHEN POST `/api/v1/plame/generar`
- THEN returns 201

#### Scenario: S-PLM-005-2 — Unauthorized read

- GIVEN user without `rrhh:ver`
- WHEN GET `/api/v1/plame/1/descargar`
- THEN returns 403

### Requirement: PLM-006 — PII protection

Monetary and worker PII MUST be excluded from entity `toString()` and API responses MUST NOT expose PII beyond trabajadorId, nombres, apellidos (Ley 29733).

#### Scenario: S-PLM-006-1 — toString excludes monetary data

- GIVEN ArchivoPlanilla or TRegistroEvento entity
- WHEN calling `toString()`
- THEN monetary fields and worker PII are excluded

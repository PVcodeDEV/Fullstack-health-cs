# Proposal: RRHH Planilla — PLAME y T-Registro (SUNAT)

## Intent

Peruvian Pequeña Empresa REMYPE must file PLAME (TXT 0601) within 10 business days and T-Registro labor events within 5 business days each month. Without this module, the clinic cannot meet SUNAT electronic filing obligations and faces regulatory penalties.

## Scope

### In Scope
- PLAME TXT generation (formato 0601) — aggregated from PlanillaDetalle, Gratificacion, Cts, Vacacion data
- T-Registro event records (alta/baja/variación/suspensión) per worker per CERRADO period
- REST: `POST /api/v1/plame/generar`, `GET /api/v1/plame/{id}/descargar`, `GET/POST /api/v1/t-registro/eventos`
- V28 migration: `tb_plame_archivos`, `tb_tregistro_eventos`
- Tests: file format generation, encoding, event recording, validations

### Out of Scope
- Automatic/scheduled generation (manual trigger only)
- Real-time T-Registro event detection (period-based only)
- SUNAT portal submission (file download only)
- Frontend UI
- Electronic pay slips / boletas de pago
- Retención 4ta categoría

## Capabilities

### New Capabilities
- `rrhh-planilla-plame`: PLAME 0601 TXT generation + T-Registro labor event records for SUNAT filing — on-demand, per-closed-period

### Modified Capabilities
None

## Approach

`PlameService` for a CERRADO PeriodoPlanilla:
1. Aggregate per-worker income (base, gratif, CTS, vacaciones) and deductions (AFP/ONP, Renta 5ta, EsSalud) from existing modules
2. Format as SUNAT fixed-width TXT (formato 0601), UTF-8 encoded
3. Detect T-Registro events from contrato/cese/variación history per period
4. Persist file as `PlameArchivo` (bytea) + events as `TRegistroEvento`
5. Idempotent: re-generating updates existing record for same period

Package: `com.clinica.rrhh.planillaplame` (5-layer). Permissions: `rrhh:editar` on POST, `rrhh:ver` on GETs.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `rrhh/planillaplame/*/` | New | Full layered module (entity/repository/service/dto/controller) |
| `db/migration/V28__rrhh_planilla_plame.sql` | New | DDL for `tb_plame_archivos`, `tb_tregistro_eventos` |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| SUNAT 0601 format interpretation gaps | Med | FormatValidator with explicit field-by-field rules |
| UTF-8 BOM encoding issues | Low | Unit-test raw bytes for expected encoding |
| T-Registro event detection misses edge cases | Med | Period-based manual generation; real-time detection deferred |

## Rollback

Drop V28. Delete generated file data. Existing planilla/gratif/CTS/vacaciones tables unaffected.

## Dependencies

- `rrhh-planilla` — PeriodoPlanilla + PlanillaDetalle per period
- `rrhh-gratificacion`, `rrhh-cts`, `rrhh-vacacion` — benefit data for PLAME
- `rrhh-contrato`, `rrhh-pension` — worker history for T-Registro events
- SUNAT 0601 technical layout (external reference)

## Success Criteria

- [ ] POST generar with CERRADO period produces valid 0601 TXT with correct UTF-8 encoding
- [ ] T-Registro events created per worker per period from contrato state
- [ ] GET descargar returns valid downloadable bytes
- [ ] V28 applies and rollbacks cleanly on H2 + PostgreSQL

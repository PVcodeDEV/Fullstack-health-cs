# Proposal: Módulo Personas

## Intent

Centralize person management shared by Paciente, Trabajador, Medico, and Cliente roles. Without this, each module duplicates person records, causing data inconsistency and PII compliance risks (Ley 29733).

## Scope

### In Scope
- `com.clinica.persona` module: entity, repository, service, dto, controller
- Role tables as composition FK: tb_pacientes, tb_trabajadores, tb_medicos, tb_clientes
- Modulo11Validator as shared `@Service`
- API client interfaces: SunatApiClient (free, no token) + SecureApiClient (paid, token)
- API strategy config stored in `seguridad` (URL + optional token)
- Fill rules: DNI→API, CE/Pasaporte→manual
- V10 migration (tb_personas), V11 (role tables)
- 2 new specs: persona, api-integracion-personas

### Out of Scope
- RENIEC API integration (future)
- Digital signature for Medico (seguridad module)
- Actual HTTP client implementation (interfaces + config only)
- UI/Thymeleaf views for person CRUD

## Capabilities

### New Caps
- `persona`: Person CRUD, módulo 11, fill rules per doc type, role composition FK
- `api-integracion-personas`: API interfaces (SUNAT free, Secure paid), provider selection

### Modified Caps
- `maestro-documentos-identidad`: R-005 — módulo 11 moves to persona; maestro provides ref data only

## Approach

New `com.clinica.persona` package following module layering. `Persona` entity with composition to role tables (no inheritance). `Modulo11Validator` as injectable `@Service`. API clients implement `ReniecClient` interface, selected by `seguridad` config properties (URL + token per provider). Flyway V10 for base table, V11 for role tables.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `com.clinica.persona` | New | Full module (entity→controller) |
| `com.clinica.maestro` | Modified | Módulo 11 extracted from maestro |
| `com.clinica.seguridad` | Modified | API config properties added |
| `com.clinica.clinica` | Modified | tb_pacientes FK → tb_personas |
| `com.clinica.rrhh` | Modified | tb_trabajadores + tb_medicos FK → tb_personas |
| `com.clinica.caja` | Modified | tb_clientes FK → tb_personas |
| `db/migration` | New | V10 (tb_personas) + V11 (role tables) |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| API config in seguridad creates cross-module coupling | Medium | Define config interface in persona; seguridad provides impl |
| Role table migrations out of sync between modules | Low | Single V11 file for all role tables |

## Rollback

Flyway undo V10/V11. Delete `com.clinica.persona` package. Restore módulo 11 in maestro. Remove API config from seguridad.

## Dependencies

- `maestro-documentos-identidad`: TipoDocumentoIdentidad + EstadoCivil
- `maestro-ubigeo`: UbigeoDistrito FK
- `maestro-catalogos-organizacion`: AreaFuncional FK

## Success Criteria

- [ ] Persona CRUD validates DNI check digit via Modulo11Validator before persist
- [ ] Role records correctly FK to tb_personas (no orphan roles)
- [ ] Fill rules enforced: DNI auto-fills names from API, CE/Pasaporte requires manual input
- [ ] V10 + V11 Flyway migrations apply cleanly on H2 and PostgreSQL

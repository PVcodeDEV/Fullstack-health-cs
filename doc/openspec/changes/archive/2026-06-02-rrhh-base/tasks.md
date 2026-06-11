# Tasks: RRHH Base

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~2000–3000 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 → PR 2 → PR 3 → PR 4 → PR 5 |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes (resolved: stacked-to-main, PR 2)
Chained PRs recommended: Yes (resolved: stacked-to-main)
Chain strategy: stacked-to-main (PR 2: Business Logic)
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | V20 migration + enums + entities + repos (data layer) | PR 1 | Foundation: catalogs, ALTER Trabajador, Contrato, PeriodoLaboral |
| 2 | DTOs + Services + CMP backward compat | PR 2 | Business logic; depends on PR 1 entities |
| 3 | Controllers + sub-resource endpoints + @PreAuthorize | PR 3 | HTTP layer; depends on PR 2 services |
| 4 | Derechohabiente module + V21 | PR 4 | Independent module; depends on PR 1–2 |
| 5 | Security permisos + tests (unit/integration) | PR 5 | Depends on PR 3–4 for controllers |

## Phase 1: Foundation (PR 1)

- [x] 1.1 Create `V20__rrhh_base.sql`: catalogs (`tb_tipos_contrato` `tco_`, `tb_tipos_colegiatura` `tcl_`) + seed + ALTER `tb_trabajadores` (12 cols) + CMP backfill + CREATE `tb_periodos_laborales` (`pla_`) + CREATE `tb_contratos` (`con_`) + indexes
- [x] 1.2 Create enums in `rrhh/type/`: `TipoTrabajador`, `RegimenLaboral`, `TipoJornada`, `EstadoContrato`, `TipoRelacionDerechohabiente`, `SituacionEspecial`
- [x] 1.3 Create `maestro/entity/rrhh/TipoContrato.java` + `TipoColegiatura.java` + repositories (no service/controller needed)
- [x] 1.4 Modify `Trabajador.java`: add 12 new fields (`tra_tipo`, `tra_regimen_laboral`, etc.), `@OneToMany` → `PeriodoLaboral`, `@ManyToOne` → `TipoColegiatura`
- [x] 1.5 Create `rrhh/contrato/entity/Contrato.java`: `con_` prefix, estado state machine, FK → Trabajador + TipoContrato
- [x] 1.6 Create `rrhh/periodo/entity/PeriodoLaboral.java`: `pla_` prefix, FK → Trabajador, unique active constraint
- [x] 1.7 Create `ContratoRepository.java` + `PeriodoLaboralRepository.java`
- [x] 1.8 Modify `TrabajadorRepository.java`: add query methods for new filter needs

## Phase 2: Business Logic (PR 2)

- [x] 2.1 Expand `TrabajadorRequest.java` + `TrabajadorResponse.java`: add all 15 new fields, `@ToString.Exclude` for PII
- [x] 2.2 Create `ContratoRequest.java` + `ContratoResponse.java`: tipo validation, `@ToString.Exclude` on remuneracion
- [x] 2.3 Create `PeriodoLaboralResponse.java`
- [x] 2.4 Expand `TrabajadorService.java`: colegiatura validation (MEDICO/ENFERMERA/NUTRICIONISTA requires CMP), new field mapping
- [x] 2.5 Create `ContratoService.java`: full CRUD + estado state machine + single-active enforcement (auto-VENCIDO previous)
- [x] 2.6 Create `PeriodoLaboralService.java`: manage reingresos, single-active-period enforcement
- [x] 2.7 Modify `Medico.java`: `getCmp()` delegates to `trabajador.getNroColegiatura()`

## Phase 3: Controllers (PR 3)

- [x] 3.1 Expand `TrabajadorController.java`: add sub-resource endpoints `/{id}/contratos`, `/{id}/periodos`, `/{id}/reingreso` + `@PreAuthorize` + inject ContratoService, PeriodoLaboralService + Create `ReingresoRequest`
- [x] 3.2 Create `ContratoController.java`: CRUD + `/{id}/resolver`, `/{id}/suspender`, `/{id}/reactivar`, `@PreAuthorize("rrhh:ver")` / `("rrhh:editar")`
- [x] 3.3 Create `PeriodoLaboralController.java`: `/{id}` findById, `/{id}/cese` registrarCese + `@PreAuthorize` + Create `CeseRequest` + Add `findById` to `PeriodoLaboralService` + Add `findAll` to `ContratoService`

## Phase 4: Derechohabiente (PR 4)

- [x] 4.1 Create `V21__rrhh_derechohabientes.sql`: CREATE `tb_derechohabientes` (`der_` prefix, FK → Trabajador + Persona) + indexes
- [x] 4.2 Create `rrhh/derechohabiente/entity/Derechohabiente.java` + repository
- [x] 4.3 Create `DerechohabienteRequest.java` + `DerechohabienteResponse.java`
- [x] 4.4 Create `DerechohabienteService.java`: CRUD + HIJO auto-fechaFin (18 years) + `inactivarPorTrabajador()` cascade
- [x] 4.5 Create `DerechohabienteController.java`: sub-resource endpoints under `/{id}/derechohabientes`, `@PreAuthorize("derechohabiente:*")`
- [x] 4.6 Modify `ContratoService.java`: inject `DerechohabienteService`, cascade `inactivarPorTrabajador()` in `resolver()`

## Phase 5: Security + Tests (PR 5)

- [x] 5.1 Add `@PreAuthorize` to `TrabajadorController`, `ContratoController`, `PeriodoLaboralController`, `DerechohabienteController` — verified: all controllers already have class-level `hasAuthority('rrhh:ver')` and write methods with `hasAuthority('rrhh:editar')`. No changes needed.
- [x] 5.2 Seed 4 rrhh permisos in `DataInitializer.java`: `rrhh:ver`, `rrhh:editar`, `rrhh:contrato:gestionar`, `rrhh:derechohabiente:gestionar`
- [x] 5.3 `@DataJpaTest` for `ContratoRepository` + `PeriodoLaboralRepository` + `DerechohabienteRepository` — 3 test files, 12 tests total, all pass
- [x] 5.4 Service unit tests (Mockito): ContratoServiceTest (12 tests), PeriodoLaboralServiceTest (8 tests), DerechohabienteServiceTest (9 tests) — all pass
- [x] 5.5 `@WebMvcTest` for new controllers: ContratoControllerTest (9 tests), PeriodoLaboralControllerTest (4 tests), DerechohabienteControllerTest (6 tests) — status codes, @Valid rejection
- [x] 5.6 Already covered — @PreAuthorize verification done in 5.1 (verificación no-code)
- [x] 5.7 Integration: `RrhhFlowIntegrationTest` — full flow: create trabajador → contrato ACTIVE → derechohabiente HIJO (auto 18y fechaFin) → resolver contrato → verify DER INACTIVO cascade → reingreso → new contrato. 1 test, passes.

## Implementation Order

PR 1 (Foundation) primero — todo depende de entidades y V20. PR 2 (Services) con lógica de negocio. PR 3 (Controllers) expone HTTP. PR 4 (Derechohabiente) puede ir después de PR 2. PR 5 (Tests) al final para probar toda la cadena.

## Next Step

**Decision needed**: Este cambio está muy por encima de las 400 líneas. Se recomiendan PRs encadenados. Elegir estrategia: **stacked-to-main** (cada PR a main en orden), **feature-branch-chain** (rama tracker rrhh-base, PRs hijos contra rama anterior), o **size:exception** (PR único con aprobación del mantenedor). Una vez elegida, `sdd-apply` puede empezar con Unit 1 (PR 1).

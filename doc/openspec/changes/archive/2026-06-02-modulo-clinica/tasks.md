# Tasks: Módulo Clínica

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~3000–4500 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 → PR 2 → PR 3 → PR 4 → PR 5 |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: feature-branch-chain
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Flyway V14–V19 + all entities + all repositories + Cama domain (6 sub-packages) | PR 1 | Data layer only; no business logic |
| 2 | All DTOs + all Services + HCE FirmaDigital + Cuenta extraction boundary + package-info | PR 2 | Business logic layer; depends on PR 1 entities |
| 3 | All controllers + @PreAuthorize per subdomain | PR 3 | HTTP layer; depends on PR 2 services |
| 4 | PacienteController + MedicoController @PreAuthorize + DataInitializer ~25 permisos | PR 4 | Security cross-cutting; independent from PR 3 |
| 5 | Repository @DataJpaTest + Service Mockito + Controller @WebMvcTest + Integration | PR 5 | Tests across all sub-packages |

## Phase 1: Foundation

- [x] 1.1 Create V14 `tb_camas`, `tb_habitaciones` + seed rooms
- [x] 1.2 Create V15 `tb_cuentas`, `tb_cuentas_paquetes`, `tb_solicitudes_hospitalizacion`, `tb_admision_diagnosticos`
- [x] 1.3 Create V16 `tb_hospitalizaciones`, `tb_cambios_habitacion`, `tb_notas_evolucion`, `tb_solicitudes_medicamento`, `tb_altas_medicas`
- [x] 1.4 Create V17 `tb_reportes_quirurgicos`, `tb_registros_urpa`
- [x] 1.5 Create V18 `tb_documentos_clinicos` (BYTEA) + `tb_firmas_digitales`
- [x] 1.6 Create V19 `tb_cargos_adicionales`
- [x] 1.7 Create entities + repositories for `cama/`, `admision/`, `hospitalizacion/`, `sop/`, `hce/`, `cuenta/`
- [x] 1.8 Create `Cama` state machine domain logic (disponible ↔ ocupado ↔ mantenimiento)

## Phase 2: Business Logic

- [x] 2.1 Create DTOs (Request/Response records) for all 6 sub-packages
- [x] 2.2 Create `AdmisionService` — account creation, paquete selection, solicitud auto-gen, bed assignment, HC creation, diagnosis registration
- [x] 2.3 Create `HospitalizacionService` — confirm ingreso, room change, nursing notes, evolution notes, medication requests, alta medica
- [x] 2.4 Create `SOPService` — report creation (draft/complete), URPA records
- [x] 2.5 Create `HCEService` — document CRUD, FirmaDigital generation (SHA-256), signature verification, audit log
- [x] 2.6 Create `CuentaService` — additional charges, pendiente-cobro, confirmar-cobro, account query
- [x] 2.7 Create `CuentaProjection` sealed interface + `package-info.java` extraction plan
- [x] 2.8 Patient search endpoint in AdmisionService (ILIKE by nombres/apellidos, exact DNI/HC)

## Phase 3: Controllers + Security

- [x] 3.1 Create `CamaController` + `HabitacionController` with `@PreAuthorize("cama:*")`
- [x] 3.2 Create `AdmisionController` (patient search, cuentas, solicitudes, asignar cama, diagnosticos) + `@PreAuthorize("admision:*")`
- [x] 3.3 Create `HospitalizacionController` (confirm ingreso, cambiar cama, notas, solicitudes medicamento, alta) + `@PreAuthorize("hospitalizacion:*")`
- [x] 3.4 Create `SOPController` (reportes operatorios, URPA) + `@PreAuthorize("sop:*")`
- [x] 3.5 Create `HCEController` (documentos, firma verification) + `@PreAuthorize("hce:*")`
- [x] 3.6 Create `CuentaController` (cargos, confirmar cobro) + `@PreAuthorize("cuenta:*")`

## Phase 4: Modified Files + Permisos

- [x] 4.1 Add `@PreAuthorize("hasAuthority('paciente:*')")` to PacienteController
- [x] 4.2 Add `@PreAuthorize("hasAuthority('medico:*')")` to MedicoController
- [x] 4.3 Seed ~25 permisos in DataInitializer (`{subdomain}:{accion}` format for crear/editar/eliminar/ver × 5 subdominios)
- [x] 4.4 `@EnableMethodSecurity` already present in SecurityConfig — no change needed

## Phase 5: Testing

- [x] 5.1 `@DataJpaTest` for each sub-package — custom queries, state transitions, FK constraints
- [x] 5.2 Service unit tests (Mockito) — Admision flow, bed state transitions, alta flow, HCE signature, cuenta charges
- [x] 5.3 `@WebMvcTest` per controller — HTTP status, `@Valid` validation, `@PreAuthorize` 403
- [x] 5.4 Integration test — full admission → discharge → cobro flow

## Implementation Order

PR 1 (Foundation) first — everything depends on entities. PR 2 (Services) next with business logic. PR 3 (Controllers) exposes HTTP layer. PR 4 (Security cross-cutting) is independent and can merge in parallel with PR 3. PR 5 (Tests) last to prove the chain.

## Next Step

**Decision needed**: This change is ~3000–4500 lines, well above the 400-line review budget. Chained PRs are recommended. Please choose a chain strategy: **stacked-to-main** (each PR merges to main in order), **feature-branch-chain** (tracker branch accumulates, child PRs target previous PR branch), or **size:exception** (single PR with maintainer approval). Once chosen, `sdd-apply` can begin with Unit 1 (PR 1).

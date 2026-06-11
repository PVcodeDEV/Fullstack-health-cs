# Tasks: RRHH Planilla — Pension System Foundation

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~350–450 |
| 400-line budget risk | Medium |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | stacked-to-main |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: stacked-to-main
400-line budget risk: Medium

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Pension foundation (migration + entities + repos + services + controllers + tests) | PR 1 | Single PR, main branch; stays under 400-line budget |

## Phase 1: Foundation (V23 + Entities + Repositories)

- [x] 1.1 Create `db/migration/V23__rrhh_pension.sql` — 3 tables (`tb_afps`, `tb_afp_tasas_historicas`, `tb_informacion_pensionaria`) + seed (5 AFP rows with current SBS 2026 rates) + indexes + constraints
- [x] 1.2 Create `maestro/entity/rrhh/Afp.java` — extends BaseEntity, columns: `afp_id`, `afp_codigo`, `afp_nombre`, `afp_descripcion`, `afp_activo`
- [x] 1.3 Create `maestro/entity/rrhh/AfpTasaHistorica.java` — extends BaseEntity, columns: `ath_id`, `ath_afp_id` (FK), `ath_tipo_comision`, `ath_tasa`, `ath_prima_seguro`, `ath_vigencia_desde`, `ath_vigencia_hasta`
- [x] 1.4 Create `rrhh/pension/entity/InformacionPensionaria.java` — `inf_id`, `inf_trabajador_id` (unique FK), `inf_cuspp`, `inf_afp_id` (FK), `inf_comision_tipo`, `inf_sctr`, `inf_fecha_afiliacion`, `inf_estado`. `@ToString.Exclude` on cuspp for PII
- [x] 1.5 Create `maestro/repository/rrhh/AfpRepository.java` — `findAllByActivoTrueOrderByNombre()`
- [x] 1.6 Create `rrhh/pension/repository/InformacionPensionariaRepository.java` — `findByTrabajadorId()`, `existsByTrabajadorId()`

## Phase 2: Services + DTOs

- [x] 2.1 Create `maestro/dto/AfpResponse.java` — record with id, codigo, nombre
- [x] 2.2 Create `rrhh/pension/dto/InformacionPensionariaRequest.java` — @Valid with @NotNull afpId, @Size cuspp; ONP validation handled in service layer
- [x] 2.3 Create `rrhh/pension/dto/InformacionPensionariaResponse.java` — record with afpId, comisionTipo, cuspp (toString excluded), sctr, fechaAfiliacion, estado
- [x] 2.4 Create `maestro/service/AfpService.java` — `findAll()` maps to AfpResponse list
- [x] 2.5 Create `rrhh/pension/service/InformacionPensionariaService.java` — `getByTrabajadorId()` returns response or 404; `upsert()` validates CUSPP (12 digits AFP, auto-populate DNI for ONP), null-safe ONP handling, find-or-save semantics

## Phase 3: Controllers + Tests

- [x] 3.1 Create `maestro/controller/AfpController.java` — `GET /api/v1/afps`, no auth required
- [x] 3.2 Create `rrhh/pension/controller/InformacionPensionariaController.java` — `GET /api/v1/trabajadores/{id}/informacion-pensionaria` (@PreAuthorize rrhh:ver), `PUT /api/v1/trabajadores/{id}/informacion-pensionaria` (@PreAuthorize rrhh:editar)
- [x] 3.3 Write `@DataJpaTest` for AfpRepository and InformacionPensionariaRepository (find by activo, find by trabajadorId, unique constraint)
- [x] 3.4 Write Mockito tests for `InformacionPensionariaService` (ONP null fields, AFP cuspp 12-digit validation, upsert creates vs updates, EntityNotFoundException)
- [x] 3.5 Write `@WebMvcTest` for AfpController (GET returns 5 entries) and InformacionPensionariaController (GET 200/403/404, PUT upsert creates/updates/rejects invalid cuspp)
- [x] 3.6 Write integration test `RrhhPensionFlowIntegrationTest` — create trabajador → upsert pension info → verify GET returns correct data → second upsert updates in-place

# Tasks: Maestro — Catálogos Base

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

| Field | Value |
|-------|-------|
| Estimated changed lines | 5000–8000 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Delivery strategy | ask-on-risk |

### Suggested Work Units
| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Foundation + Identidad | PR 1 | BaseEntity, 5 identity entities, Flyway V1–V3 |
| 2 | Clinical catalogs | PR 2 | CIE-10 + 6 clinical entities, Flyway V4–V5 |
| 3 | Financial + Organization | PR 3 | 3 SUNAT + 4 org entities, Flyway V6–V7 |
| 4 | Seeds + Tests | PR 4 | Ubigeo V8 + CIE-10 V9 + full test suite |

## Phase 1: Foundation
- [x] 1.1 Create `BaseEntity.java` — `@MappedSuperclass` with audit fields and `@AttributeOverrides`
- [x] 1.2 Add `@EnableJpaAuditing` to `ClinicaApplication.java`
- [x] 1.3 Create `maestro/{entity,repository,service,dto,controller}/` subdirs

## Phase 2: Identidad (5 entities)
- [x] 2.1 Entity+repo+service+DTO+controller for `TipoDocumentoIdentidad`
- [x] 2.2 Same for `EstadoCivil`
- [x] 2.3 Same for `UbigeoDepartamento`, `UbigeoProvincia`, `UbigeoDistrito` (FK chain)
- [x] 2.4 Flyway V1–V3: schema + seed for all 5 tables

## Phase 3: Catálogos Clínicos (7 entities)
- [x] 3.1 Entity+repo+service+DTO+controller for `CIE10Diagnostico` (PUT off, PATCH-only)
- [x] 3.2 Same for `EspecialidadMedica`, `TipoPaciente`, `TipoAtencion`, `ViaAdministracion`, `FormaFarmaceutica`, `TipoHabitacion`
- [x] 3.3 Flyway V4–V5: schema + seed + GIN trigram on cie_descripcion (skip CONCURRENTLY in H2)

## Phase 4: Financieros SUNAT (3 entities)
- [x] 4.1 Entity+repo+service+DTO+controller for `TipoComprobante` (String PK)
- [x] 4.2 Same for `TipoMoneda` (String PK)
- [x] 4.3 Same for `UnidadMedida` (String PK)
- [x] 4.4 Flyway V6: schema + SUNAT seed data

## Phase 5: Organización (4 entities)
- [x] 5.1 Entity+repo+service+DTO+controller for `AreaFuncional`
- [x] 5.2 Same for `Aseguradora`
- [x] 5.3 Same for `CategoriaInsumo` (self-ref FK, nullable)
- [x] 5.4 Same for `TipoDocumentoClinico`
- [x] 5.5 Flyway V7: schema + seed

## Phase 6: Bulk Seeds
- [x] 6.1 Flyway V8: `seed_ubigeo.sql` — 1874 districts with parent FK chain
- [x] 6.2 Flyway V9: `seed_cie11.sql` — CIE-11 codes (batch INSERT with explicit cols)

## Phase 7: Testing
- [x] 7.1 `@DataJpaTest` per batch: CRUD, unique constraints, FK integrity
- [x] 7.2 Service Mockito tests: codigo uniqueness, soft delete, inactive filter
- [x] 7.3 `@WebMvcTest` per batch: HTTP status, DTO validation, response shape
- [x] 7.4 `@SpringBootTest` integration: migrate→seed→read→write per batch

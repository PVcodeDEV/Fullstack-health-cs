# Design: RRHH Planilla — Pension System Foundation

## Technical Approach

Extend the maestro catalog pattern (tb_afps + tb_afp_tasas_historicas) and add a new `rrhh.pension` module with a 1:1 pension info record per Trabajador. Upsert semantics via PUT — no separate create. ONP treated as a catalog entry with codigo='ONP' and service-layer special handling. Follows existing layering: entity → repository → service → dto → controller.

## Architecture Decisions

### ADR-1: One pension info per worker (upsert pattern)

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Separate POST/PUT | More endpoints, orphan risk | **PUT = upsert** — single row per trabajador, client sends full payload |
| Partial PATCH | Complex merge logic, idempotency issues | Rejected — full replacement is simpler and auditable |

### ADR-2: AFP rates as historical table

| Option | Tradeoff | Decision |
|--------|----------|----------|
| `application.yml` constants | Leaks domain into config, no audit trail | **Rejected** — rates change every 6mo |
| `tb_afp_tasas_historicas` | DB overhead, seed management | **Chosen** — audit trail, retroactive calc support |

### ADR-3: ONP as catalog entry with service-layer rules

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Boolean `isOnp` on pension info | Branching hell, two systems to maintain | **Rejected** |
| codigo='ONP' in tb_afps | All consumers must handle ONP edge case | **Chosen** — pension type is inherent to the selection |

### ADR-4: CUSPP validation at service layer

| Option | Tradeoff | Decision |
|--------|----------|----------|
| DB constraint (CHECK + nullable) | Inflexible for ONP null case | **Rejected** |
| Service-layer `@AssertTrue` or manual | Testable, flexible, ONP-aware | **Chosen** — 12-digit numeric for AFP, null for ONP |

## Data Flow

```
GET /api/v1/afps
  AfpController → AfpService → AfpRepository → tb_afps
  └─> Returns AfpResponse[] (id, codigo, nombre)

GET /api/v1/trabajadores/{id}/informacion-pensionaria
  InformacionPensionariaController → InformacionPensionariaService
  → InformacionPensionariaRepository → tb_informacion_pensionaria
  └─> Returns InformacionPensionariaResponse

PUT /api/v1/trabajadores/{id}/informacion-pensionaria
  Controller (validates @Valid request) → Service
  ├─> ONP? → null comisionTipo, cuspp = trabajador.DNI
  └─> AFP? → validate cuspp (12 digits), comisionTipo required
  → Upsert: findByTrabajadorId → save()
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `maestro/entity/rrhh/Afp.java` | Create | AFP catalog entity (codigo, nombre, activo) |
| `maestro/entity/rrhh/AfpTasaHistorica.java` | Create | Historical rate entity (tipoComision, tasa, prima, vigencia) |
| `maestro/repository/rrhh/AfpRepository.java` | Create | JpaRepository + findAllByActivoTrueOrderByNombre |
| `maestro/service/rrhh/AfpService.java` | Create | findAll() returns AfpResponse list |
| `maestro/dto/rrhh/AfpResponse.java` | Create | id, codigo, nombre record |
| `maestro/controller/AfpController.java` | Create | GET /api/v1/afps — public |
| `rrhh/pension/entity/InformacionPensionaria.java` | Create | 1:1 pension info (cuspp, afp, comisionTipo, sctr) |
| `rrhh/pension/repository/InformacionPensionariaRepository.java` | Create | findByTrabajadorId, existsByTrabajadorId |
| `rrhh/pension/dto/InformacionPensionariaRequest.java` | Create | Upsert request with @Valid annotations |
| `rrhh/pension/dto/InformacionPensionariaResponse.java` | Create | Response record, toString excludes cuspp (PII) |
| `rrhh/pension/service/InformacionPensionariaService.java` | Create | upsert + getByTrabajadorId, ONP/AFP validation |
| `rrhh/pension/controller/InformacionPensionariaController.java` | Create | GET/PUT endpoints with @PreAuthorize |
| `db/migration/V23__rrhh_pension.sql` | Create | DDL + seed (3 tables, 5 AFP rows, indexes) |

## Interfaces / Contracts

```java
// Public endpoints (no auth)
GET    /api/v1/afps                                    → AfpResponse[]

// Protected endpoints
GET    /api/v1/trabajadores/{id}/informacion-pensionaria → InformacionPensionariaResponse  @rrhh:ver
PUT    /api/v1/trabajadores/{id}/informacion-pensionaria → InformacionPensionariaResponse  @rrhh:editar
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | Service validation: ONP null fields, AFP cuspp format, upsert creates vs updates | Mockito `@ExtendWith(MockitoExtension.class)` |
| Unit | CUSPP excluded from `toString()` | Assert string doesn't contain value |
| Unit | EntityNotFoundException when trabajador missing | Mock repository to return empty |
| Controller | GET returns 200 with data, 403 without authority, 404 if missing | `@WebMvcTest` + `TestMethodSecurityConfig` |
| Controller | PUT upsert creates new, updates existing, rejects invalid cuspp | MockMvc + @WithMockUser |
| Repository | Find by trabajadorId, unique constraint violation | `@DataJpaTest` + `@AutoConfigureTestDatabase` |

## Migration / Rollout

No migration required — this is a greenfield addition. Rollback: `DROP TABLE tb_informacion_pensionaria, tb_afp_tasas_historicas, tb_afps`.

## Open Questions

- [ ] Verify current SBS commission rates for seed data (2026 values)
- [ ] Confirm TestMethodSecurityConfig location for pension controller tests

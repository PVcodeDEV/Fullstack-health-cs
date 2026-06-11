# Design: RRHH Planilla — Gratificación Legal

## Technical Approach

New `gratificacion` module under `com.clinica.rrhh` following the existing 5-layer pattern. A single `GratificacionService.calcular()` fetches ACTIVE contratos valid for the semester, computes meses computables (proportional rule), applies Pequeña Empresa factor (½ sueldo) + 9% bonif. extraordinaria (Ley 30334), and upserts records per worker. Zero deductions — by law, gratificación has no AFP/ONP/Renta 5ta.

## Architecture Decisions

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Separate module vs. extending PlanillaService | Separate: clear domain boundary, no coupling to liquidation logic | **Separate `gratificacion/` module** |
| Upsert vs. insert-only for re-calculation | Upsert: idempotent, no duplicates on retry | **Upsert** (ADR-11) |
| Hard-code 0.5 factor vs. config | Config implies runtime change; factor is legal constant | **Hard-code as constant** (ADR-13) |
| V26: combined DDL + seed vs. split | Combined: fewer migrations, simpler | **Single V26** (ADR-10) |

## Data Flow

```
POST /calcular?periodoPlanillaId=X
  │
  ├─→ GratificacionService.calcular()
  │     ├─ Load PeriodoPlanilla → derive semester (mes 6/12) + date range
  │     ├─ Load ACTIVE contratos
  │     ├─ For each contrato:
  │     │     ├─ mesesComputables (day ≤14 → full month, 15+ → next)
  │     │     ├─ remuneracionComputable = sueldo + asignación familiar (10% RMV if ≥1 hijo)
  │     │     ├─ gratificación = (meses≥6) ? rem * 0.5 : (rem/12) * meses
  │     │     ├─ bonifExtra = gratificación * 0.09
  │     │     └─ upsert Gratificacion
  │     └─ Return List<GratificacionResponse>
  │
  └─→ 201 Created
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `backend/src/main/resources/db/migration/V26__rrhh_gratificacion.sql` | Create | DDL for `tb_gratificaciones` + seed GRATIFICACION / BONIF_EXTRAORDINARIA |
| `gratificacion/entity/Gratificacion.java` | Create | JPA entity, extends BaseEntity, `tb_gratificaciones`, `gra_` prefix |
| `gratificacion/repository/GratificacionRepository.java` | Create | JPA repository with finders |
| `gratificacion/dto/GratificacionResponse.java` | Create | Record with `fromEntity()`, toString excludes monetary fields |
| `gratificacion/service/GratificacionService.java` | Create | `@Service @Transactional` — core calculation and upsert logic |
| `gratificacion/controller/GratificacionController.java` | Create | 3 endpoints: POST /calcular, GET /, GET /{id} |

All files under `backend/src/main/java/com/clinica/rrhh/gratificacion/`.

## Interfaces / Contracts

```java
// API
POST   /api/v1/gratificaciones/calcular?periodoPlanillaId={id}  → 201 + GratificacionResponse[]
GET    /api/v1/gratificaciones                                   → 200 + GratificacionResponse[]
GET    /api/v1/gratificaciones/{id}                              → 200 + GratificacionResponse
// Security: POST requires rrhh:editar, GETs require rrhh:ver

// GratificacionResponse record
public record GratificacionResponse(
    Long id, Long periodoPlanillaId, String periodoLabel,
    Long trabajadorId, String trabajadorNombre, Long contratoId,
    String semestre, Integer mesesComputables,
    BigDecimal remuneracionComputable,
    BigDecimal gratificacion, BigDecimal bonificacionExtraordinaria,
    BigDecimal total, String estado
) {
    @Override public final String toString() { /* excludes monetary fields */ }
    static GratificacionResponse fromEntity(Gratificacion e) { ... }
}

// No request DTO — only query param.
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Repository | `@DataJpaTest` — basic CRUD, findByPeriodoPlanillaId, upsert path | `@DataJpaTest @AutoConfigureTestDatabase` |
| Service (Mockito) | Full semester (6m → ½ sueldo), proportional (5m), mid-month entry (day 15+), zero months (skip), idempotent recalculation | `@ExtendWith(MockitoExtension.class)`, mock repos |
| Controller | 201 on create, 200 on list/get, 403 without rrhh:editar | `@WebMvcTest` with `@WithMockUser` |
| Integration | Optional: full flow with existing test infrastructure | `@SpringBootTest` |

## Migration / Rollout

No migration required beyond V26. Rollback: `DROP TABLE tb_gratificaciones; DELETE FROM tb_conceptos_planilla WHERE cpl_codigo IN ('GRATIFICACION', 'BONIF_EXTRAORDINARIA');`

## Open Questions

None.

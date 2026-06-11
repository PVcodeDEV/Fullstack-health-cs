# Design: RRHH Planilla — CTS (Compensación por Tiempo de Servicios)

## Technical Approach

New `cts` module under `com.clinica.rrhh` following the existing 5-layer pattern. `CtsService.calcular()` derives the semester from `PeriodoPlanilla.mes` (5 → MAYO-OCTUBRE, 11 → NOVIEMBRE-ABRIL), iterates ACTIVE contratos, computes días computables (30-day truncamiento), computes RC (sueldo + asignación familiar + 1/6 avg gratif + 1/6 avg bonif), applies Pequeña Empresa formula `(RC / 360) × días`, and upserts per worker. Zero deductions — same as gratificación, it's a deposit, not a salary payment.

## Architecture Decisions

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Separate `cts/` module vs. extending GratificacionService | Different calculation rules, domain boundary | **Separate `cts/` module** |
| Store raw avg gratif vs. 1/6 proportional in entity | 1/6 already divided is audit-ready, avoids re-calculation at read | **Store 1/6 proportional** (promedioGratificacion, promedioBonificacion) |
| Gratificación lookup via repository vs. service | Repository: direct access, no circular deps (same package family) | **Inject GratificacionRepository** |
| Idempotent upsert vs. insert-only | Upsert: safe retry, no duplicates, same as Gratificacion pattern | **Upsert** |
| V27: DDL only vs. DDL + seed | CTS doesn't need a ConceptoPlanilla — it's a separate deposit table | **DDL only** |

## Data Flow

```
POST /api/v1/cts/calcular?periodoPlanillaId=X
  │
  ├─→ CtsService.calcular()
  │     ├─ Load PeriodoPlanilla → validate mes ∈ {5, 11}
  │     ├─ Derive semestre label + date range
  │     ├─ Load ACTIVE contratos overlapping semester
  │     ├─ For each contrato:
  │     │     ├─ díasComputables (30-day months, day ≤14 → count, 15+ → skip)
  │     │     ├─ Last 2 gratificaciones (gratifRepository)
  │     │     ├─ promGratif = avg(gratif[0..1]) / 6
  │     │     ├─ promBonif  = avg(bonif[0..1]) / 6
  │     │     ├─ asigFamiliar = (hijos ≥ 1) ? RMV × 0.10 : 0
  │     │     ├─ RC = sueldo + asigFamiliar + promGratif + promBonif
  │     │     ├─ montoCts = (RC / 360) × díasComputables
  │     │     └─ upsert DepositoCts (find + set or new)
  │     └─ Return List<CtsResponse>
  │
  └─→ 201 Created
```

## File Changes

| File | Action |
|------|--------|
| `db/migration/V27__rrhh_cts.sql` | Create |
| `rrhh/cts/entity/DepositoCts.java` | Create |
| `rrhh/cts/repository/DepositoCtsRepository.java` | Create |
| `rrhh/cts/dto/CtsResponse.java` | Create |
| `rrhh/cts/service/CtsService.java` | Create |
| `rrhh/cts/controller/CtsController.java` | Create |

All new files under `backend/src/main/java/com/clinica/rrhh/cts/`.

## Entity Model

```
tb_depositos_cts
├── dct_id                          BIGSERIAL PK
├── dct_periodo_planilla_id         BIGINT FK → tb_periodos_planilla
├── dct_trabajador_id               BIGINT FK → tb_trabajadores
├── dct_contrato_id                 BIGINT FK → tb_contratos
├── dct_semestre                    VARCHAR(20)   "MAYO-OCTUBRE" | "NOVIEMBRE-ABRIL"
├── dct_dias_computables            INTEGER       NOT NULL
├── dct_remuneracion_computable     NUMERIC(10,2)
├── dct_promedio_gratificacion      NUMERIC(10,2)  -- 1/6 avg gratif (0 if none)
├── dct_promedio_bonificacion       NUMERIC(10,2)  -- 1/6 avg bonif (0 if none)
├── dct_monto_cts                   NUMERIC(10,2)
├── dct_estado                      VARCHAR(20)   DEFAULT 'CALCULADO'
├── dct_activo                      BOOLEAN       DEFAULT TRUE
├── dct_created_at                  TIMESTAMP
├── dct_updated_at                  TIMESTAMP
└── UNIQUE (dct_periodo_planilla_id, dct_trabajador_id)
```

Entity extends `BaseEntity` with `@AttributeOverride` for `dct_` prefix (`dct_created_at`, `dct_updated_at`, `dct_activo`). Same `@ToString(callSuper = true, onlyExplicitlyIncluded = true)` with monetary fields excluded (Ley 29733).

## Interfaces / Contracts

```java
POST   /api/v1/cts/calcular?periodoPlanillaId={id}  → 201 + CtsResponse[]
GET    /api/v1/cts                                   → 200 + CtsResponse[]
GET    /api/v1/cts/{id}                              → 200 + CtsResponse
// POST: @PreAuthorize("hasAuthority('rrhh:editar')")
// GETs: @PreAuthorize("hasAuthority('rrhh:ver')") on class level
```

`CtsResponse` record — same shape as `GratificacionResponse` with CTS-specific fields. `toString()` excludes all monetary fields.

## Service Algorithm (CtsService.calcular)

1. **Validate**: `mes == 5` (NOVIEMBRE, semestre mayo–octubre) or `mes == 11` (MAYO, semestre noviembre–abril). Otherwise throw `IllegalArgumentException`.
2. **Derive range**: mes=5 → `semestreInicio = anio-05-01`, `semestreFin = anio-10-31`; mes=11 → `semestreInicio = anio-11-01`, `semestreFin = (anio+1)-04-30`.
3. **Fetch contratos**: `contratoRepository.findAll()` → filter `ACTIVO` + `fechaInicio <= semestreFin`.
4. **For each contrato**:
   - `días` = days computables (same truncamiento as gratificación, but output is days = months × 30, not months).
   - `gratifRecords` = `gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(trabajadorId)` → take first 2.
   - `promGratif` = average of up to 2 gratif amounts ÷ 6, or 0 if none.
   - `promBonif` = average of up to 2 bonif amounts ÷ 6, or 0 if none.
   - `asigFam` = (`trabajador.cantidadHijos >= 1`) ? `RMV × 0.10` : 0.
   - `RC` = `contrato.remuneracion + asigFam + promGratif + promBonif`.
   - `montoCts` = `RC / 360 × días`, scale 2, `RoundingMode.HALF_UP`.
   - Upsert: `findByPeriodoPlanillaIdAndTrabajadorId(...)` → set fields → `save()`.

## Testing Strategy

| Layer | What | Approach |
|-------|------|----------|
| Repository | CRUD, findByPeriodoPlanillaId, upsert path | `@DataJpaTest`, H2 |
| Service | Full semester (180d → RC/2), mid-month (15+), day 1-14, 0/1/2 gratifs, idempotent, invalid mes, empty contratos | `@ExtendWith(MockitoExtension.class)` |
| Controller | 201 create, 200 list/get, 403 no editar, 409 invalid mes | `@WebMvcTest` + `@WithMockUser` |

## Migration / Rollout

V27: DDL only. Rollback: `DROP TABLE IF EXISTS tb_depositos_cts;`

## Open Questions

None.

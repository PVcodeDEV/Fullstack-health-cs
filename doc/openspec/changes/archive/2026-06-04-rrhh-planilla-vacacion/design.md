# Design: RRHH Planilla — Vacaciones (Pequeña Empresa REMYPE)

## Technical Approach

New `vacacion` module under `com.clinica.rrhh` following the exact 5-layer pattern as CTS. Two entities — **VacacionRecord** (derecho adquirido at 12mo anniversary) and **VacacionGoce** (effective descanso with state machine). `calcular` scans active contratos, creates records for 12mo anniversaries (idempotent upsert). Goce lifecycle via state transitions. Remuneración captured at `programar` time.

## Architecture Decisions

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Single entity vs. Record + Goce | Single: fewer tables, but mixes accrual tracking with usage lifecycle | **Two entities** — mirror domain (spec VAC-001/002), each state machine is clean |
| In `rrhh.vacacion` vs. extending `planilla` | Planilla: mixed concerns. Separate: follows CTS/gratif pattern | **`rrhh.vacacion`** — distinct domain, same package family |
| Reduction: manual param vs. automatic from Licencia | No Licencia table exists yet; automatic requires new entity | **Accept `diasReduccion` in `calcular`** — explicit param, evolve later |
| Auto-expiry: scheduler vs. in `calcular` | Scheduler: clean but needs infra. In `calcular`: lazy evaluation, no new infra | **In `calcular`** — same pass; mark expired records before creating new ones |
| V28: DDL only vs. seed conceptos | Vacación is tracking, not a planilla concepto yet | **DDL only** — no conceptos needed |

## State Machine

```
VacacionRecord:                VacacionGoce:
  ACTIVO ──────────────────►     PROGRAMADO ──► EN_CURSO ──► COMPLETADO
    │  ├──► COMPLETADO              │                              ▲
    │  ├──► PERDIDO                 └───────── CANCELADO ──────────┘
    │  └──► INDEMNIZADO
    │        (out of scope v1)
```

## Data Flow

```
POST /api/v1/vacaciones/calcular?trabajadorId=X
  │
  └─→ VacacionService.calcular()
        ├─ Load Contrato (validate ACTIVE, 12mo elapsed)
        ├─ Find-or-create VacacionRecord
        │     ├─ fechaInicio = contrato.fechaInicio (or last record fechaFin + 1d)
        │     ├─ fechaFin = fechaInicio + 12 months
        │     ├─ diasDerecho = 15 - (diasReduccion / 1.25 meses)
        │     └─ estado = ACTIVO
        ├─ Check expiry for existing records (fechaExpiracion < today → PERDIDO)
        └─ Return VacacionRecordResponse

POST /api/v1/vacaciones/goces/programar
  │
  ├─→ Validate: dias ≤ record.diasPendientes, dias ≥ 7
  ├─→ Capture remuneracion from Contrato (sueldo + asigFamiliar)
  └─→ Create VacacionGoce (PROGRAMADO)

POST /api/v1/vacaciones/goces/{id}/iniciar
  └─→ Validate: estado == PROGRAMADO, fecha matches
      → estado = EN_CURSO

POST /api/v1/vacaciones/goces/{id}/completar
  └─→ Validate: estado == EN_CURSO
      → estado = COMPLETADO
      → recalculate record.diasPendientes

POST /api/v1/vacaciones/goces/{id}/cancelar
  └─→ Validate: estado == PROGRAMADO
      → estado = CANCELADO
      → free up diasPendientes
```

## Entity Model

```
tb_vacaciones_record                    tb_vacaciones_goces
├── vcr_id BIGSERIAL PK                 ├── vgo_id BIGSERIAL PK
├── vcr_trabajador_id BIGINT FK         ├── vgo_record_id BIGINT FK
├── vcr_contrato_id BIGINT FK           ├── vgo_fecha_inicio DATE
├── vcr_fecha_inicio DATE               ├── vgo_fecha_fin DATE
├── vcr_fecha_fin DATE                  ├── vgo_dias INTEGER
├── vcr_dias_derecho INTEGER (15)       ├── vgo_remuneracion NUMERIC(10,2)
├── vcr_dias_reduccion INTEGER (0)      ├── vgo_estado VARCHAR(20) 'PROGRAMADO'
├── vcr_dias_pendientes INTEGER         ├── vgo_activo BOOLEAN
├── vcr_estado VARCHAR(20) 'ACTIVO'     ├── vgo_created_at TIMESTAMP
├── vcr_fecha_expiracion DATE           └── vgo_updated_at TIMESTAMP
├── vcr_activo BOOLEAN
├── vcr_created_at TIMESTAMP
└── vcr_updated_at TIMESTAMP
```

Both entities extend `BaseEntity` with `@AttributeOverride` for `vcr_`/`vgo_` prefixes. Monetary/PII fields excluded from `toString()` (Ley 29733).

## File Changes

| File | Action |
|------|--------|
| `db/migration/V28__rrhh_vacacion.sql` | Create |
| `rrhh/vacacion/entity/VacacionRecord.java` | Create |
| `rrhh/vacacion/entity/VacacionGoce.java` | Create |
| `rrhh/vacacion/repository/VacacionRecordRepository.java` | Create |
| `rrhh/vacacion/repository/VacacionGoceRepository.java` | Create |
| `rrhh/vacacion/dto/VacacionRecordResponse.java` | Create |
| `rrhh/vacacion/dto/VacacionGoceResponse.java` | Create |
| `rrhh/vacacion/dto/ProgramarGoceRequest.java` | Create |
| `rrhh/vacacion/service/VacacionService.java` | Create |
| `rrhh/vacacion/controller/VacacionController.java` | Create |

All under `backend/src/main/java/com/clinica/rrhh/vacacion/`.

## Interfaces / Contracts

```java
POST   /api/v1/vacaciones/calcular?trabajadorId={id}&diasReduccion={n}
       → 201 + VacacionRecordResponse        // crear/actualizar record
POST   /api/v1/vacaciones/goces/programar    → 201 + VacacionGoceResponse
       // body: { recordId, fechaInicio, dias }
POST   /api/v1/vacaciones/goces/{id}/iniciar → 200 + VacacionGoceResponse
POST   /api/v1/vacaciones/goces/{id}/completar → 200 + VacacionGoceResponse
POST   /api/v1/vacaciones/goces/{id}/cancelar → 200 + VacacionGoceResponse
GET    /api/v1/vacaciones/records            → 200 + List<VacacionRecordResponse>
       // ?trabajadorId={id} filter
GET    /api/v1/vacaciones/records/{id}       → 200 + VacacionRecordResponse
GET    /api/v1/vacaciones/goces?recordId={id} → 200 + List<VacacionGoceResponse>

// Security: @PreAuthorize("hasAuthority('rrhh:ver')") class-level
//           @PreAuthorize("hasAuthority('rrhh:editar')") on writes
```

## Testing Strategy

| Layer | What | Approach |
|-------|------|----------|
| Repository | CRUD, find by trabajador/record, edge cases | `@DataJpaTest`, H2 |
| Service | calcular (12mo anniversary, idempotent, reduction, expiry), programar (min 7d, exceeds balance), iniciar/completar/cancelar transitions, invalid transitions | `@ExtendWith(MockitoExtension.class)` |
| Controller | 201 calcular, 201 programar, 200 transitions, 400 validation/invalid state, 403 auth | `@WebMvcTest` + `@WithMockUser` |
| Integration | Full flow: calcular → programar → iniciar → completar | `@SpringBootTest` + `@AutoConfigureMockMvc` |

## Migration / Rollout

V28: DDL only. Rollback: `DROP TABLE IF EXISTS tb_vacaciones_goces, tb_vacaciones_record;`

## Open Questions

- [ ] Reduction source: `calcular` accepts `diasReduccion` param now. When Licencia/Inasistencia tables exist, should auto-calculate. Manual acceptable for v1.
- [ ] Auto-expiry: evaluated in `calcular` call — no scheduler. Acceptable? Or needs @Scheduled daily job?

# Design: RRHH Base

## Technical Approach

Foundation for the RRHH module: expand Trabajador for REMYPE/labor compliance, migrate CMP from Medico via backfill, add Contrato lifecycle (state machine) and Derechohabiente (beneficiary) management. Single-V20 migration, explicit ADRs for every structural choice.

---

## Architecture Decisions

### ADR 1: CMP Migration Strategy

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Drop `med_cmp`, force all reads via Trabajador | Breaks rollback, requires coordinated deploy | **Keep `med_cmp` as deprecated** |
| Copy-on-write | Data drift between columns | **V20 backfill once** |

**Rationale**: `med_cmp` stays as dead column for zero-downtime rollback. `Medico.getCmp()` delegates to `trabajador.getNroColegiatura()`. V20 does one-time `UPDATE tb_trabajadores SET tra_colegiatura_numero = m.med_cmp FROM tb_medicos m WHERE m.med_trabajador_id = tra_id AND m.med_cmp IS NOT NULL`. Future cleanup migration removes `med_cmp`.

### ADR 2: Contrato State Machine

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Separate table per state | Over-normalized for MVP | **Single table, `estado` VARCHAR** |
| DB CHECK constraints | Breaks on future state additions | **Service-layer validation only** |

**Rationale**: String `estado` column — ACTIVO, SUSPENDIDO, VENCIDO, RESUELTO. Transitions enforced in `ContratoService`. `VENCIDO` is auto (date check); `RESUELTO` is terminal. Single-active-contrato rule enforced at service level (auto-vencido previous on new activation).

### ADR 3: Derechohabiente Auto-Inactivation

| Option | Tradeoff | Decision |
|--------|----------|----------|
| `@TransactionalEventListener` + event bus | Premature abstraction for MVP | **Direct service call, same tx** |
| DB trigger | Opaque, hard to debug | **Explicit `DerechohabienteService.inactivarPorTrabajador()`** |

**Rationale**: `ContratoService.resolver()` calls `derechohabienteService.inactivarPorTrabajador()` in the same `@Transactional` method. No events, no async. Upgrade path to events when cross-module consumers emerge.

### ADR 4: Column Prefix Convention

| Table | Prefix | Pattern Source |
|-------|--------|----------------|
| `tb_trabajadores` | `tra_` | Existing |
| `tb_contratos` | `con_` | Follows `hosp_`, `med_`, `cama_` |
| `tb_derechohabientes` | `der_` | Follows `hosp_`, `med_`, `cama_` |

**Rationale**: Two-letter (or 3-4) prefix per table, matching `clinica` module conventions exactly (cf. `hosp_`, `med_`, `cama_`, `hab_`).

### ADR 5: Enums vs Maestro Catalogs

| Domain | Storage | Reason |
|--------|---------|--------|
| TipoTrabajador, RegimenLaboral, TipoJornada, EstadoContrato, TipoRelacionDerechohabiente, SituacionEspecial | Java enum in `rrhh/type/` | Fixed values, no runtime changes |
| TipoContrato, TipoColegiatura | Maestro `tb_tipos_contrato`, `tb_tipos_colegiatura` | Values change over time (law updates) |

**Rationale**: Enums for values intrinsic to domain logic; maestro tables for administration-changeable catalogs.

---

## Data Model

### tb_trabajadores (ALTER — 12 new columns)

| Column | Type | Constraints |
|--------|------|-------------|
| `tra_tipo` | VARCHAR(20) | NOT NULL |
| `tra_regimen_laboral` | VARCHAR(20) | NOT NULL |
| `tra_fecha_ingreso` | DATE | NOT NULL (already exists, now NOT NULL) |
| `tra_fecha_cese` | DATE | NULLABLE |
| `tra_motivo_cese` | VARCHAR(255) | NULLABLE |
| `tra_banco` | VARCHAR(50) | NULLABLE |
| `tra_cuenta_sueldo` | VARCHAR(30) | NULLABLE |
| `tra_cci` | VARCHAR(30) | NULLABLE |
| `tra_contacto_emergencia_nombre` | VARCHAR(100) | NULLABLE |
| `tra_contacto_emergencia_telefono` | VARCHAR(20) | NULLABLE |
| `tra_cantidad_hijos` | INTEGER | DEFAULT 0 |
| `tra_nro_colegiatura` | VARCHAR(20) | NULLABLE |
| `tra_tipo_colegiatura_id` | BIGINT | NULLABLE → FK tb_tipos_colegiatura |
| `tra_discapacidad` | BOOLEAN | DEFAULT FALSE |
| `tra_sindicalizado` | BOOLEAN | DEFAULT FALSE |

### tb_contratos (CREATE)

| Column | Type | Constraints |
|--------|------|-------------|
| `con_id` | BIGINT | PK, IDENTITY |
| `con_trabajador_id` | BIGINT | FK tb_trabajadores, NOT NULL |
| `con_tipo_contrato_id` | BIGINT | FK tb_tipos_contrato, NOT NULL |
| `con_fecha_inicio` | DATE | NOT NULL |
| `con_fecha_fin` | DATE | NULLABLE |
| `con_periodo_prueba_meses` | INTEGER | NULLABLE |
| `con_remuneracion` | DECIMAL(10,2) | NOT NULL |
| `con_jornada` | VARCHAR(20) | NOT NULL (REGULAR/PARCIAL/NOCTURNA) |
| `con_estado` | VARCHAR(20) | NOT NULL DEFAULT 'ACTIVO' |
| `con_motivo_cese` | VARCHAR(255) | NULLABLE |
| `con_contrato_escaneado_id` | BIGINT | NULLABLE (future FK, no constraint yet) |
| `con_created_at` | TIMESTAMP | NOT NULL |
| `con_updated_at` | TIMESTAMP | NULLABLE |
| `con_activo` | BOOLEAN | DEFAULT TRUE |

### tb_derechohabientes (CREATE)

| Column | Type | Constraints |
|--------|------|-------------|
| `der_id` | BIGINT | PK, IDENTITY |
| `der_trabajador_id` | BIGINT | FK tb_trabajadores, NOT NULL |
| `der_persona_id` | BIGINT | FK tb_personas, NOT NULL |
| `der_relacion` | VARCHAR(20) | NOT NULL |
| `der_fecha_inicio` | DATE | NOT NULL |
| `der_fecha_fin` | DATE | NULLABLE |
| `der_estado` | VARCHAR(20) | NOT NULL DEFAULT 'ACTIVO' |
| `der_created_at` | TIMESTAMP | NOT NULL |
| `der_updated_at` | TIMESTAMP | NULLABLE |
| `der_activo` | BOOLEAN | DEFAULT TRUE |

### Maestro catalogs (CREATE)

**tb_tipos_contrato**: `tcon_id`, `tcon_codigo` (INDETERMINADO, DETERMINADO, CAS, LOCACION, TIEMPO_PARCIAL, INTERMITENTE), `tcon_nombre`, `tcon_created_at`, `tcon_updated_at`, `tcon_activo`.

**tb_tipos_colegiatura**: `tcol_id`, `tcol_codigo` (CMP, CEP, CPN, OTROS), `tcol_nombre`, `tcol_created_at`, `tcol_updated_at`, `tcol_activo`.

---

## Column Prefix Map

| Table | Prefix | Entity |
|-------|--------|--------|
| `tb_trabajadores` | `tra_` | Trabajador (modified) |
| `tb_contratos` | `con_` | Contrato (new) |
| `tb_derechohabientes` | `der_` | Derechohabiente (new) |
| `tb_tipos_contrato` | `tcon_` | TipoContrato (new, maestro) |
| `tb_tipos_colegiatura` | `tcol_` | TipoColegiatura (new, maestro) |

---

## Entity List

| Entity | Package | Action |
|--------|---------|--------|
| `Trabajador` | `rrhh.trabajador.entity` | Modify — 12 new fields |
| `Contrato` | `rrhh.contrato.entity` | Create |
| `Derechohabiente` | `rrhh.derechohabiente.entity` | Create |
| `TipoContrato` | `maestro.entity.rrhh` | Create |
| `TipoColegiatura` | `maestro.entity.rrhh` | Create |
| `TipoTrabajador` | `rrhh.type` | Create (enum) |
| `RegimenLaboral` | `rrhh.type` | Create (enum) |
| `TipoJornada` | `rrhh.type` | Create (enum) |
| `EstadoContrato` | `rrhh.type` | Create (enum) |
| `TipoRelacionDerechohabiente` | `rrhh.type` | Create (enum) |
| `SituacionEspecial` | `rrhh.type` | Create (bitmask flag class) |
| `Medico` | `clinica.medico.entity` | Modify — `getCmp()` delegates to Trabajador |

---

## File Changes

| File | Action |
|------|--------|
| `rrhh/trabajador/entity/Trabajador.java` | Modify — 12 fields |
| `rrhh/trabajador/dto/TrabajadorRequest.java` | Modify — 15 new fields |
| `rrhh/trabajador/dto/TrabajadorResponse.java` | Modify — 15 new fields, PII @JsonIgnore/@ToString.Exclude |
| `rrhh/trabajador/service/TrabajadorService.java` | Modify — colegiatura validation, new field mapping |
| `rrhh/trabajador/repository/TrabajadorRepository.java` | Modify — add query methods if needed |
| `rrhh/trabajador/controller/TrabajadorController.java` | Modify — no endpoint changes, validation carries |
| `rrhh/contrato/entity/Contrato.java` | Create |
| `rrhh/contrato/dto/ContratoRequest.java` | Create |
| `rrhh/contrato/dto/ContratoResponse.java` | Create |
| `rrhh/contrato/repository/ContratoRepository.java` | Create |
| `rrhh/contrato/service/ContratoService.java` | Create — state machine, single-active enforcement |
| `rrhh/contrato/controller/ContratoController.java` | Create — sub-resource + action endpoints |
| `rrhh/derechohabiente/entity/Derechohabiente.java` | Create |
| `rrhh/derechohabiente/dto/DerechohabienteRequest.java` | Create |
| `rrhh/derechohabiente/dto/DerechohabienteResponse.java` | Create |
| `rrhh/derechohabiente/repository/DerechohabienteRepository.java` | Create |
| `rrhh/derechohabiente/service/DerechohabienteService.java` | Create — cascading inactivation |
| `rrhh/derechohabiente/controller/DerechohabienteController.java` | Create |
| `rrhh/type/TipoTrabajador.java` | Create (enum) |
| `rrhh/type/RegimenLaboral.java` | Create (enum) |
| `rrhh/type/TipoJornada.java` | Create (enum) |
| `rrhh/type/EstadoContrato.java` | Create (enum) |
| `rrhh/type/TipoRelacionDerechohabiente.java` | Create (enum) |
| `rrhh/type/SituacionEspecial.java` | Create (flags) |
| `maestro/entity/rrhh/TipoContrato.java` | Create |
| `maestro/entity/rrhh/TipoColegiatura.java` | Create |
| `maestro/repository/rrhh/TipoContratoRepository.java` | Create |
| `maestro/repository/rrhh/TipoColegiaturaRepository.java` | Create |
| `maestro/service/rrhh/TipoContratoService.java` | Create |
| `maestro/service/rrhh/TipoColegiaturaService.java` | Create |
| `maestro/controller/rrhh/TipoContratoController.java` | Create |
| `maestro/controller/rrhh/TipoColegiaturaController.java` | Create |
| `clinica/medico/entity/Medico.java` | Modify — `getCmp()` delegates |
| `db/migration/V20__rrhh_base.sql` | Create |
| `seguridad/bootstrap/DataInitializer.java` | Modify — +6 rrhh permisos |
| `rrhh/trabajador/**/*Test.java` | Modify — expand tests |
| `rrhh/contrato/**/*Test.java` | Create — unit + integration |
| `rrhh/derechohabiente/**/*Test.java` | Create — unit + integration |

~22 new, 6 modified. Total ~28 files.

---

## Flyway V20 Structure

```
V20__rrhh_base.sql
├── 1. CREATE tb_tipos_contrato + seed (6 rows)
├── 2. CREATE tb_tipos_colegiatura + seed (4 rows)
├── 3. ALTER tb_trabajadores — ADD COLUMN (12 columns)
├── 4. UPDATE tb_trabajadores — CMP backfill from tb_medicos
├── 5. CREATE tb_contratos
├── 6. CREATE tb_derechohabientes
├── 7. CREATE INDEXES (trabajador_id FK indexes, estado indexes)
└── 8. ALTER tb_trabajadores ALTER COLUMN tra_fecha_ingreso SET NOT NULL
```

---

## Key Flows

### Create Trabajador (with colegiatura validation)
```
POST /trabajadores
  → validate unique persona + codigo
  → if tipo in (MEDICO, ENFERMERA, NUTRICIONISTA) → validate nroColegiatura + tipoColegiatura
  → set defaults (cantidadHijos=0, discapacidad=false, sindicalizado=false)
  → save → return 201
```

### Create Contrato (with single-active enforcement)
```
POST /trabajadores/{id}/contratos { estado: ACTIVO }
  → validate fechaFin required for DETERMINADO
  → if estado=ACTIVO → find existing ACTIVO contrato → set to VENCIDO
  → save new → return 201
```

### Resolver Contrato (terminal + cascade)
```
PUT /contratos/{id}/resolver
  → validate estado is ACTIVO or SUSPENDIDO
  → set estado=RESUELTO, motivoCese
  → derechohabienteService.inactivarPorTrabajador(trabajadorId)
  → save → return 200
```

### Add Derechohabiente (with age auto-calculation)
```
POST /trabajadores/{id}/derechohabientes { personaId, relacion: "HIJO" }
  → validate Persona exists
  → if HIJO → calculate fechaFin = fechaInicio + 18 years
  → save estado=ACTIVO → return 201
```

---

## State Machine

```
                    ┌─────────────┐
                    │   CREATED   │
                    └─────┬───────┘
                          │ activar
                          ▼
              ┌──────────────────────┐
         ┌───▶│       ACTIVO         │◀──────────┐
         │    └──┬───────────────┬───┘           │
         │       │              │                │
         │  suspender     fecha > fechaFin  reactivar
         │       │              │                │
         │       ▼              ▼                │
         │  ┌──────────┐  ┌──────────┐           │
         │  │SUSPENDIDO│  │ VENCIDO  │ (terminal)│
         │  └─────┬────┘  └──────────┘           │
         │        │                              │
         │   resolver (también desde ACTIVO)     │
         │        │                              │
         │        ▼                              │
         │  ┌──────────┐                         │
         └──│ RESUELTO │ (terminal — no exit)    │
            └──────────┘                         │
                                                 │
            Auto: new ACTIVO sets old → VENCIDO ─┘
```

- `CREADO → ACTIVO`: only on first activation
- `ACTIVO → SUSPENDIDO`: suspend
- `SUSPENDIDO → ACTIVO`: reactivar
- `ACTIVO → VENCIDO`: automatic by date check
- `ACTIVO / SUSPENDIDO → RESUELTO`: manual, terminal
- `RESUELTO` has no outgoing transitions
- Creating a new `ACTIVO` contrato auto-expires previous `ACTIVO` to `VENCIDO`

---

## Testing Strategy

| Layer | Scope | Approach |
|-------|-------|----------|
| Unit | TrabajadorService — colegiatura validation | Mockito, parametrized by tipo |
| Unit | ContratoService — state transitions | Mockito, each transition tested |
| Unit | DerechohabienteService — age calc, cascade | Mockito |
| Integration | ContratoRepository — single-active constraint | @DataJpaTest with H2 |
| Integration | V20 migration — full apply + rollback | @DataJpaTest, verify columns |

---

## Rollback (V20 down-migration)

1. `DROP TABLE tb_derechohabientes`
2. `DROP TABLE tb_contratos`
3. `ALTER TABLE tb_trabajadores DROP COLUMN` (12 new columns, in reverse order)
4. `ALTER TABLE tb_trabajadores ALTER COLUMN tra_fecha_ingreso DROP NOT NULL` (restore original nullable)
5. `DROP TABLE tb_tipos_colegiatura`
6. `DROP TABLE tb_tipos_contrato`
7. Revert Medico entity to use `med_cmp` directly

`med_cmp` column on `tb_medicos` is preserved throughout — no data loss.

---

## Open Questions

- None — all decisions documented above.

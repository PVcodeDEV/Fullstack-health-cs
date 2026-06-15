# Spec: Caja Tarifario

## Description

Tariff schedule for clinical services consumed during a patient's stay. Tarifario defines the unit prices for prestaciones (procedures, medications, accommodation days) that feed into the Cuenta charges. Prices are time-bounded via `fecha_desde`/`fecha_hasta` — new prices create new rows preserving historical data. Packages group multiple prestaciones into a bundled price for account assignment. The structure supports future per-insurer tariff schedules; MVP delivers a single tarifario for particular (self-pay) patients.

Base package: `com.clinica.caja.tarifario`.

## Cross-References

- **CTA-001** (clinica-cuenta): Cuenta created on admission references a `paqueteQuirurgicoId` — that ID comes from Caja's Paquete catalog.
- **CTA-005** (clinica-cuenta): Cuenta stays in clinica; tarifario lives in caja. Caja reads Cuenta via the clinica API.
- **MAESTRO-R-003** (maestro-catalogos-financieros): UnidadMedida seeded values used in tarifario items (NIU, Paquete, Kit, etc.).

## Requirements

### Requirement: TRF-001 — Tarifario item definition

- **Priority**: MUST
- **Description**: The system MUST support creating Tarifario items with at least: `codigo` (unique, alphanumeric), `nombre`, `descripcion` (optional), `precioBase` (costo farmacia, before markup), `unidadMedidaId` (FK to maestro UnidadMedida), `fechaDesde`, `fechaHasta` (nullable), `activo`. Items belong to a single Tarifario schedule.
- **Acceptance Criteria**:
  - [ ] POST `/api/v1/caja/tarifario-item` with valid data returns 201
  - [ ] `codigo` uniqueness is enforced across all items
  - [ ] `fechaHasta` in the past is allowed (historical price preservation)
  - [ ] Item with `fechaHasta` null is the current/active price

#### Scenario: TRF-001-1 — Create new tarifario item

- **GIVEN** no tarifario item exists with codigo "CON-001"
- **WHEN** POST `/api/v1/caja/tarifario-item` with `{ codigo: "CON-001", nombre: "Consulta General", precioBase: 80.00, unidadMedidaId: "NIU", fechaDesde: "2026-01-01" }`
- **THEN** the response is 201 with the created item
- **AND** `fechaHasta` is null (active price)

#### Scenario: TRF-001-2 — Duplicate codigo rejected

- **GIVEN** a tarifario item exists with codigo "CON-001"
- **WHEN** POST `/api/v1/caja/tarifario-item` with the same codigo
- **THEN** the response is 409 Conflict — codigo already exists

### Requirement: TRF-002 — Price validity dates (CAJ-011)

- **Priority**: MUST
- **Description**: Price changes MUST create a new row with updated `fechaDesde`/`fechaHasta`, never mutate an existing row. When a new price is set for an existing codigo, the current row gets `fechaHasta` set to the day before the new `fechaDesde`, and a new row is inserted. Historical prices are always queryable.
- **Acceptance Criteria**:
  - [ ] Updating a price creates a new row; the old row gets `fechaHasta` set
  - [ ] Querying items without date parameters returns only active (fechaHasta null) items
  - [ ] Querying with `?fecha=2025-06-01` returns prices effective on that date

#### Scenario: TRF-002-1 — Price revision creates new row

- **GIVEN** item "CON-001" exists with precioBase=80.00, fechaDesde=2026-01-01, fechaHasta=null
- **WHEN** POST `/api/v1/caja/tarifario-item/price-change` with `{ codigo: "CON-001", nuevoPrecio: 90.00, fechaDesde: "2026-07-01" }`
- **THEN** the old row's `fechaHasta` is set to "2026-06-30"
- **AND** a new row is created with precioBase=90.00, fechaDesde="2026-07-01", fechaHasta=null
- **AND** the system returns 201 with the new row

#### Scenario: TRF-002-2 — Query historical price

- **GIVEN** item "CON-001" has price history (80.00 valid until 2026-06-30, 90.00 from 2026-07-01)
- **WHEN** GET `/api/v1/caja/tarifario-item?codigo=CON-001&fecha=2026-06-15`
- **THEN** the response includes precioBase=80.00 (the price effective on that date)

#### Scenario: TRF-002-3 — Overlapping dates rejected

- **GIVEN** item "CON-001" has active price with fechaDesde=2026-01-01, fechaHasta=null
- **WHEN** POST price-change with `fechaDesde: "2026-05-01"`
- **THEN** the new row's `fechaDesde` is accepted and old row's `fechaHasta` capped to "2026-04-30"
- **AND** both rows exist with non-overlapping validity periods

### Requirement: TRF-003 — Pricing formula (CAJ-003)

- **Priority**: MUST
- **Description**: The final clinical price is computed as: `precioFinal = precioBase + IGV(18% of precioBase) + utilidadClinica(50% of precioBase)`. Equivalent to `precioFinal = precioBase × 1.68`. The formula is configurable via system parameters (`utilidadClinicaPorcentaje`, `igvPorcentaje`) in `tb_configuracion_api`. The `precioFinal` is stored as a computed column or cached on the tarifario item to avoid recalculating on every read.
- **Acceptance Criteria**:
  - [ ] precioFinal = precioBase × 1.68 when using default values (50% utilidad, 18% IGV)
  - [ ] Changing utilidadClinicaPorcentaje in config recalculates precioFinal
  - [ ] precioFinal is rounded to nearest 0.10 (half-up at 0.05)

#### Scenario: TRF-003-1 — Default pricing calculation

- **GIVEN** precioBase=100.00, utilidadClinica=50%, igv=18%
- **WHEN** the system calculates precioFinal
- **THEN** precioFinal = 168.00 (100.00 + 18.00 IGV + 50.00 utilidad)

#### Scenario: TRF-003-2 — Config-driven utilidad change

- **GIVEN** `tb_configuracion_api` has `utilidadClinicaPorcentaje=60`, `igvPorcentaje=18`
- **WHEN** a new tarifario item is created with precioBase=100.00
- **THEN** precioFinal = 178.00 (100.00 + 18.00 IGV + 60.00 utilidad)

### Requirement: TRF-004 — Package definition (CAJ-012)

- **Priority**: MUST
- **Description**: Paquetes are predefined bundles of prestaciones with a single price. Each Paquete has: `codigo` (unique), `nombre`, `descripcion`, `precioTotal` (the bundled price, may differ from sum of items), `activo`. A Paquete contains one or more PaqueteDetalle items referencing Tarifario items (`tarifarioItemId`, `cantidad`). Admission assigns a Paquete to the Cuenta, which then generates charges from the package contents.
- **Acceptance Criteria**:
  - [ ] POST `/api/v1/caja/paquete` with items returns 201
  - [ ] Paquete `precioTotal` can differ from sum of parts (bundle discount)
  - [ ] Deleting a Paquete referenced by a Cuenta returns 409

#### Scenario: TRF-004-1 — Create package

- **GIVEN** tarifario items exist for "Cirugía" (3000.00), "Honorarios Médicos" (1500.00), and "Días Cama x3" (900.00)
- **WHEN** POST `/api/v1/caja/paquete` with `{ codigo: "PAQ-PN-001", nombre: "Paquete Parto Natural", precioTotal: 5000.00, items: [{ tarifarioItemId: 1, cantidad: 1 }, { tarifarioItemId: 2, cantidad: 1 }, { tarifarioItemId: 3, cantidad: 3 }] }`
- **THEN** the response is 201 with paquete and its detalle items
- **AND** `precioTotal` (5000.00) is less than the sum of items (3000 + 1500 + 2700 = 7200)

#### Scenario: TRF-004-2 — Package referenced by active Cuenta cannot be deleted

- **GIVEN** Paquete "PAQ-PN-001" is assigned to an active Cuenta
- **WHEN** DELETE `/api/v1/caja/paquete/PAQ-PN-001`
- **THEN** the response is 409 Conflict — package in use

### Requirement: TRF-005 — Multiple tarifario support structure (CAJ-013)

- **Priority**: COULD (future)
- **Description**: The Tarifario entity SHALL support multiple schedules, one per `aseguradoraId` (nullable FK). MVP: only one tarifario exists where `aseguradoraId IS NULL` (particular/self-pay patients). When insurers are added in the future, each gets its own tarifario with insurer-specific prices. Schema and API structure MUST accommodate this without breaking changes.
- **Acceptance Criteria**:
  - [ ] Tarifario table has nullable `aseguradoraId` FK
  - [ ] MVP query filters `WHERE aseguradoraId IS NULL` by default
  - [ ] Adding an aseguradoraId in the future requires no schema migration

#### Scenario: TRF-005-1 — Default tarifario for particular patients

- **GIVEN** the system has only the default tarifario (aseguradoraId IS NULL)
- **WHEN** GET `/api/v1/caja/tarifario`
- **THEN** the response returns the single default tarifario schedule

#### Scenario: TRF-005-2 — Future insurer tarifario

- **GIVEN** a seed for "Seguros Rímac" (aseguradoraId=10) tarifario exists with discounted prices
- **WHEN** GET `/api/v1/caja/tarifario?aseguradoraId=10`
- **THEN** the response returns the insurer-specific schedule
- **AND** the default tarifario is unchanged

### Requirement: TRF-006 — Tarifario query for Cuenta pricing

- **Priority**: MUST
- **Description**: Caja provides a lookup endpoint for clinica to resolve tarifario prices when charges are created. Given a codigo and date, the system returns the effective price for that item. This serves clinica's Cuenta charge creation flow.
- **Acceptance Criteria**:
  - [ ] GET `/api/v1/caja/tarifario-item/{codigo}/precio?fecha=YYYY-MM-DD` returns active price
  - [ ] Returns 404 if no active price for that codigo and date

#### Scenario: TRF-006-1 — Resolve price for charge creation

- **GIVEN** tarifario item "CON-001" has active price 90.00 from 2026-07-01
- **WHEN** GET `/api/v1/caja/tarifario-item/CON-001/precio?fecha=2026-08-15`
- **THEN** the response contains `{ codigo: "CON-001", precioBase: 90.00, precioFinal: 151.20, fechaDesde: "2026-07-01" }`

#### Scenario: TRF-006-2 — No price for given date

- **GIVEN** tarifario item "CON-001" only has prices valid until 2026-06-30
- **WHEN** GET `/api/v1/caja/tarifario-item/CON-001/precio?fecha=2026-09-01`
- **THEN** the response is 404

### Requirement: TRF-007 — Permissions

- **Priority**: MUST
- **Description**: Tarifario operations use `caja:{accion}` pattern: `caja:crear` for POST, `caja:ver` for GET, `caja:editar` for price changes. Only users with CAJA or ADMIN roles can modify tarifario items.
- **Acceptance Criteria**:
  - [ ] Usuario with role CAJA can create and modify tarifario items
  - [ ] Usuario without CAJA or ADMIN role receives 403 on POST/PUT/DELETE
  - [ ] GET is accessible to CAJA, ADMIN, and clinical roles (MEDICO, ENFERMERIA)

#### Scenario: TRF-007-1 — Medicare role cannot modify tarifario

- **GIVEN** a Usuario with role MEDICO
- **WHEN** POST `/api/v1/caja/tarifario-item`
- **THEN** the response is 403 Forbidden

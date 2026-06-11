# Spec: Caja Liquidación

## Description

The billing and payment collection hub for the clinical cashier. Liquidación covers the full flow from a Cuenta flagged as ready-for-billing through pre-bill generation, payment capture, and final account closure. Supports multiple payment methods per transaction, discount application within regulatory limits, exchange rate handling for USD transactions, and the integration contract with clinica's Cuenta entity (which Caja reads but does not own). Once payment is confirmed, Caja triggers Cuenta closure via clinica's `confirmar-cobro` endpoint.

Base package: `com.clinica.caja.liquidacion`.

## Cross-References

- **CTA-003** (clinica-cuenta): Cuenta moves to `PENDIENTE_COBRO` on alta. Caja picks up pending accounts and drives them to `CERRADA`.
- **CTA-005** (clinica-cuenta): Cuenta stays in clinica. Caja never owns Cuenta directly — it reads via clinica API and calls `confirmar-cobro`.
- **CTA-004** (clinica-cuenta): Caja queries Cuenta with charges for pre-liquidación generation.
- **CAJ-003** (tarifario pricing): Precios used in liquidación come from tarifario, not calculated in liquidación.
- **CPR-001** (caja-comprobante): After payment, Liquidación triggers Comprobante issuance.

## Requirements

### Requirement: LIQ-001 — Payment methods catalog (CAJ-001)

- **Priority**: MUST
- **Description**: The system MUST support four payment methods, seeded as a catalog (in maestro or caja-internal enum): Efectivo, POS (debito/crédito), YAPE/PLIN, Transferencia bancaria. Multiple payment methods MAY be combined in a single transaction (e.g., partial Efectivo + partial POS). Each payment leg records: `metodoPagoId`, `monto`, `referencia` (optional: voucher number, transaction ID, phone number for YAPE).
- **Acceptance Criteria**:
  - [ ] Payment method catalog is seeded with Efectivo, POS, YAPE/PLIN, Transferencia
  - [ ] A single payment can split across multiple methods
  - [ ] Sum of all payment legs equals the total to collect
  - [ ] `referencia` is required for POS, YAPE/PLIN, Transferencia; optional for Efectivo

#### Scenario: LIQ-001-1 — Single method payment

- **GIVEN** a pre-liquidación with total 250.00
- **WHEN** POST `/api/v1/caja/liquidacion/{cuentaId}/pagar` with `{ pagos: [{ metodoPago: "EFECTIVO", monto: 250.00 }] }`
- **THEN** the response is 200 with payment confirmed

#### Scenario: LIQ-001-2 — Split payment across methods

- **GIVEN** a pre-liquidación with total 500.00
- **WHEN** POST the payment with `{ pagos: [{ metodoPago: "EFECTIVO", monto: 200.00 }, { metodoPago: "POS", monto: 300.00, referencia: "VOUCHER-12345" }] }`
- **THEN** the payment is accepted
- **AND** two payment legs are recorded

#### Scenario: LIQ-001-3 — Split payment sum mismatch

- **GIVEN** a pre-liquidación with total 500.00
- **WHEN** POST the payment with `{ pagos: [{ metodoPago: "EFECTIVO", monto: 200.00 }, { metodoPago: "POS", monto: 200.00 }] }` (total 400 ≠ 500)
- **THEN** the response is 422 — sum of payments does not match total

#### Scenario: LIQ-001-4 — Referencia required for POS

- **GIVEN** a payment attempt using POS
- **WHEN** POST without `referencia`
- **THEN** the response is 400 — referencia required for POS transactions

### Requirement: LIQ-002 — Pre-liquidación generation (CAJ-005)

- **Priority**: MUST
- **Description**: Before payment, the cashier generates a pre-liquidación (pre-bill) for patient review. The pre-liquidación reads the Cuenta with its charges from clinica, resolves prices from tarifario, calculates totals, applies discounts, and presents a detailed breakdown. The pre-liquidación is a read-only view — it does not persist as an entity until payment is confirmed.
- **Acceptance Criteria**:
  - [ ] GET `/api/v1/caja/liquidacion/pre/{cuentaId}` returns charge details with prices
  - [ ] The response includes: charge list with description, unit price, quantity, subtotal per charge, discount lines (if any), subtotal, IGV, total
  - [ ] The pre-liquidación does not create any database record until payment is processed

#### Scenario: LIQ-002-1 — Generate pre-bill for patient

- **GIVEN** Cuenta CTA-001 has 3 cargos (cirugía 3000.00, honorarios 1500.00, días cama 2700.00) and estado PENDIENTE_COBRO
- **WHEN** GET `/api/v1/caja/liquidacion/pre/CTA-001`
- **THEN** the response includes all 3 charges with resolved prices, IGV, and total = 7200.00 × 1.18 = 8496.00
- **AND** the response has `estado: "PREVIEW"` — no record persisted

#### Scenario: LIQ-002-2 — Pre-bill for non-pending cuenta

- **GIVEN** Cuenta CTA-002 has estado ABIERTO (not yet PENDIENTE_COBRO)
- **WHEN** GET `/api/v1/caja/liquidacion/pre/CTA-002`
- **THEN** the response is 422 — account not ready for billing

### Requirement: LIQ-003 — Payment processing and account closure

- **Priority**: MUST
- **Description**: The cashier confirms payment, which: (1) creates a Liquidacion record with payment details, (2) sets the Liquidacion status to PAGADO, (3) calls clinica's PUT `/api/v1/clinica/cuenta/{id}/confirmar-cobro` to mark Cuenta as CERRADA. The Cuenta closure triggers bed release and Hospitalizacion finalization (per CTA-003-2). The Liquidacion record is the caja-side audit trail of the transaction.
- **Acceptance Criteria**:
  - [ ] POST liquidacion/pagar creates a Liquidacion record with status PAGADO
  - [ ] The clinica endpoint is called on successful payment
  - [ ] If clinica endpoint fails, the transaction rolls back (Liquidacion not created)
  - [ ] The Liquidacion records: cuentaId, fecha, montoTotal, monedaId, tipoCambioId (nullable), descuentoTotal, metodoPagoSummary, liquidacionEstado

#### Scenario: LIQ-003-1 — Successful payment and closure

- **GIVEN** Cuenta CTA-001 is PENDIENTE_COBRO with total 8496.00
- **WHEN** POST `/api/v1/caja/liquidacion/{cuentaId}/pagar` with valid payment
- **THEN** a Liquidacion is created with estado PAGADO
- **AND** clinica's `confirmar-cobro` endpoint is called
- **AND** the Cuenta becomes CERRADA

#### Scenario: LIQ-003-2 — Clinica confirmar-cobro fails

- **GIVEN** the payment is valid
- **WHEN** clinica's `confirmar-cobro` returns an error (e.g., Cuenta not found)
- **THEN** the Liquidacion is NOT created (transaction rollback)
- **AND** a 500 error is returned with a message indicating closure failure

### Requirement: LIQ-004 — Discount limits (CAJ-004)

- **Priority**: MUST
- **Description**: Discounts on the total bill are capped at 20%. Any discount requires Gerencia or Admin approval (logged with `usuarioApruebaId` and `fechaAprobacion`). The final price after discount MUST NOT be less than `costo + IGV` (i.e., the clinical utility may be reduced but the clinic cannot operate at a loss). The discount is applied at the pre-liquidación level, not per line item.
- **Acceptance Criteria**:
  - [ ] Discount > 20% is rejected with 422
  - [ ] Discount resulting in price < costo + IGV is rejected with 422
  - [ ] Discount > 0% requires `usuarioApruebaId` with Gerencia or Admin role
  - [ ] Discount = 0% does not require approval

#### Scenario: LIQ-004-1 — Discount within limits

- **GIVEN** a pre-liquidación with total 1000.00, costo total 500.00
- **WHEN** POST payment with descuento 10% (100.00), approved by Gerencia
- **THEN** the payment is accepted (final = 900.00, costo+IGV = 590.00, 900 > 590)

#### Scenario: LIQ-004-2 — Discount exceeds 20%

- **GIVEN** same pre-liquidación
- **WHEN** POST payment with descuento 25% (250.00)
- **THEN** the response is 422 — discount exceeds maximum allowed (20%)

#### Scenario: LIQ-004-3 — Discount below cost floor

- **GIVEN** a pre-liquidación with total 700.00, costo total 600.00 (costo+IGV = 708.00)
- **WHEN** POST payment with descuento 5% (35.00)
- **THEN** the response is 422 — final price 665.00 is below costo+IGV 708.00

#### Scenario: LIQ-004-4 — Discount without authorization

- **GIVEN** any pre-liquidación
- **WHEN** POST payment with descuento 5% but no `usuarioApruebaId`
- **THEN** the response is 400 — authorization required for discounts

### Requirement: LIQ-005 — Currency and exchange rate (CAJ-006)

- **Priority**: MUST
- **Description**: The default currency is SOLES (PEN). Transactions in USD require recording a `TipoCambio` per transaction: `monedaOrigen` (USD), `monedaDestino` (PEN), `tipoCambio`, `fecha`, `usuarioId`. The TipoCambio is captured at the time of payment and stored with the Liquidacion. Exchange rate is entered manually by the cashier (no auto-fetch from external API in MVP).
- **Acceptance Criteria**:
  - [ ] Default liquidación currency is PEN
  - [ ] USD liquidación requires `tipoCambioId` referencing a recorded TipoCambio
  - [ ] TipoCambio records: monedaOrigen, monedaDestino, tipoCambio, fecha, usuarioId
  - [ ] The liquidación stores both the original amount (in USD) and the equivalent in PEN

#### Scenario: LIQ-005-1 — Default PEN transaction

- **GIVEN** a pre-liquidación with charges in PEN
- **WHEN** POST payment without monedaId
- **THEN** the liquidación uses PEN (default)

#### Scenario: LIQ-005-2 — USD transaction with TipoCambio

- **GIVEN** a pre-liquidación where the patient wishes to pay in USD
- **WHEN** POST liquidacion with monedaId=USD and tipoCambioId referencing a rate of 3.75
- **THEN** the liquidación records: montoUSD, montoPEN (converted), tasaCambio=3.75
- **AND** the TipoCambio record is persisted for audit

#### Scenario: LIQ-005-3 — USD transaction without exchange rate

- **GIVEN** monedaId=USD
- **WHEN** POST without `tipoCambioId`
- **THEN** the response is 400 — exchange rate required for USD transactions

### Requirement: LIQ-006 — Medication charges from farmacia

- **Priority**: MUST
- **Description**: Medication consumption from farmacia does NOT flow directly to Caja. Instead, farmacia sends charges to the patient's Cuenta in clinica (as a CARGO with tipo INSUMOS). Caja reads these charges from Cuenta as part of the pre-liquidación. No direct integration between `caja` and `farmacia` packages.
- **Acceptance Criteria**:
  - [ ] No import from `com.clinica.farmacia` exists in `com.clinica.caja.liquidacion`
  - [ ] Farm charges appear in pre-liquidación as CARGO items from Cuenta
  - [ ] Farm charges are priced at tarifario rates, not farmacia prices

#### Scenario: LIQ-006-1 — Farm charges included in pre-liquidación

- **GIVEN** Cuenta CTA-001 has cargos including 2 farmacia consumption items (insumos: 150.00, 200.00)
- **WHEN** GET pre-liquidación
- **THEN** the farmacia items appear as part of the charge list
- **AND** they are priced using tarifario rates

### Requirement: LIQ-007 — Permissions

- **Priority**: MUST
- **Description**: Liquidación operations use `caja:{accion}` pattern: `caja:ver` for pre-liquidación preview, `caja:crear` for payment processing, `caja:aprobar` for discount approval. Only CAJA and ADMIN roles can process payments.
- **Acceptance Criteria**:
  - [ ] Usuario with role CAJA can generate pre-liquidación and process payment
  - [ ] Usuario without CAJA or ADMIN receives 403 on payment endpoints
  - [ ] Pre-liquidación GET is readable by CAJA, ADMIN, and MEDICO roles

#### Scenario: LIQ-007-1 — MEDICO cannot process payment

- **GIVEN** a Usuario with role MEDICO
- **WHEN** POST `/api/v1/caja/liquidacion/{cuentaId}/pagar`
- **THEN** the response is 403 Forbidden

### Requirement: LIQ-008 — Data privacy

- **Priority**: MUST
- **Description**: Payment amounts and discount data MUST NOT appear in system logs. `monto`, `descuento`, and `tipoCambio` fields SHALL be annotated with `@ToString.Exclude`. Liquidacion list endpoints MUST NOT expose payment amounts.
- **Acceptance Criteria**:
  - [ ] Payment amounts excluded from application logs
  - [ ] List endpoints return Liquidacion header only (no monto or descuento)

#### Scenario: LIQ-008-1 — Payment amounts not in logs

- **GIVEN** a payment transaction with monto=10000.00
- **WHEN** the application logs the transaction
- **THEN** the log output does NOT contain the string "10000.00"

# Spec: Caja Comprobante

## Description

SUNAT-compliant electronic invoice generation for clinical billing. Caja Clínica uses Series 001 (Boleta and Factura), issues Notas de Crédito for cancellations/adjustments, and supports reprinting with "COPIA" watermark. Comprobante is generated after payment confirmation and follows SUNAT's XML schema. MVP generates and stores the XML without electronic submission to SUNAT — the XML is available for manual upload or future OSE integration.

Base package: `com.clinica.caja.comprobante`.

## Cross-References

- **LIQ-003** (caja-liquidacion): Comprobante is triggered after liquidación confirms payment.
- **MAESTRO-R-001** (maestro-catalogos-financieros): TipoComprobante seeded with Factura (01), Boleta (03), Nota Crédito (07).
- **CAJ-005** (billing flow): Payment → Invoice → Cuenta CERRADA — comprobante is the third step.

## Requirements

### Requirement: CPR-001 — Electronic invoice issuance (Boleta/Factura) (CAJ-007)

- **Priority**: MUST
- **Description**: After payment is confirmed, the system MUST issue an electronic comprobante. The cashier selects type: Boleta (for consumers, no RUC) or Factura (for businesses, requires RUC). The comprobante records: `tipoComprobanteId` (FK to maestro TipoComprobante), `serie` ("001"), `correlativo` (auto-increment, per series), `fechaEmision`, `clienteId` (FK Persona), `clienteRuc` (nullable, for Factura), `clienteRazonSocial` (nullable), `subtotal`, `igv`, `total`, `monedaId`, `liquidacionId` (FK), `xmlGenerado` (CLOB, the SUNAT XML), `estado` (EMITIDO/ANULADO). The comprobante serie for Caja Clínica is fixed to "001".
- **Acceptance Criteria**:
  - [ ] POST `/api/v1/caja/comprobante/{liquidacionId}/emitir` with tipo=Boleta creates comprobante with estado EMITIDO
  - [ ] POST with tipo=Factura requires `clienteRuc` and `clienteRazonSocial`
  - [ ] Correlativo auto-increments within Series 001
  - [ ] SUNAT XML is generated and stored in `xmlGenerado`
  - [ ] The comprobante is linked to the Liquidacion via `liquidacionId`

#### Scenario: CPR-001-1 — Issue Boleta for consumer

- **GIVEN** a Liquidacion PAGADO with total 500.00
- **WHEN** POST `/api/v1/caja/comprobante/{liquidacionId}/emitir` with `{ tipoComprobante: "03", clienteId: 123 }` (Boleta)
- **THEN** the response is 201 with comprobante data
- **AND** `serie` = "001", `correlativo` = "00000001" (first of series)
- **AND** `estado` = "EMITIDO"
- **AND** `xmlGenerado` contains valid SUNAT XML

#### Scenario: CPR-001-2 — Issue Factura for business

- **GIVEN** a Liquidacion PAGADO with total 5000.00
- **WHEN** POST emitir with `{ tipoComprobante: "01", clienteId: 123, clienteRuc: "20123456789", clienteRazonSocial: "Clínica Ejemplo SAC" }`
- **THEN** the response is 201 with `tipoComprobante: "01"` (Factura)
- **AND** the XML includes the RUC and razón social

#### Scenario: CPR-001-3 — Factura missing RUC

- **GIVEN** tipoComprobante = Factura (01)
- **WHEN** POST emitir without `clienteRuc`
- **THEN** the response is 400 — RUC required for Factura

#### Scenario: CPR-001-4 — Auto-increment correlativo

- **GIVEN** the last comprobante in Series 001 had correlativo "00000005"
- **WHEN** a new comprobante is issued
- **THEN** `correlativo` = "00000006"

### Requirement: CPR-002 — Series 001 for Caja Clínica (CAJ-002)

- **Priority**: MUST
- **Description**: Caja Clínica uses Series "001" exclusively. This is fixed — not configurable by users. Series 004 is reserved for Farmacia (existing module). The system MUST enforce that all comprobantes from `caja` use series "001". Correlativo tracking is independent per series.
- **Acceptance Criteria**:
  - [ ] All comprobantes issued by caja module have `serie = "001"`
  - [ ] Attempting to create a comprobante with a different serie returns 400
  - [ ] Farmacia comprobantes use Series "004" (unchanged)

#### Scenario: CPR-002-1 — Enforce series 001

- **GIVEN** the logged-in user is in the caja clinical context
- **WHEN** POST emitir with serie "002"
- **THEN** the response is 400 — only series 001 is allowed for Caja Clínica

### Requirement: CPR-003 — Nota de Crédito (CAJ-008)

- **Priority**: MUST
- **Description**: After a comprobante is issued, the system MAY generate a Nota de Crédito (TipoComprobante 07) to cancel or adjust it. The Nota Crédito references the original comprobante (`comprobanteOriginalId`), records the `motivo` (text), and the `monto` (which MUST NOT exceed the original total). The original comprobante's estado changes to "ANULADO" only for full cancellations (monto = original total). For partial adjustments, the original remains "EMITIDO".
- **Acceptance Criteria**:
  - [ ] POST `/api/v1/caja/comprobante/{comprobanteId}/nota-credito` creates Nota Crédito with tipoComprobante=07
  - [ ] Full cancellation (monto = total) sets original estado to ANULADO
  - [ ] Partial adjustment (monto < total) leaves original as EMITIDO
  - [ ] Nota Crédito monto cannot exceed original comprobante total
  - [ ] Nota Crédito has its own correlativo in Series 001

#### Scenario: CPR-003-1 — Full cancellation

- **GIVEN** comprobante CPR-001 exists with total 500.00 and estado EMITIDO
- **WHEN** POST `/api/v1/caja/comprobante/CPR-001/nota-credito` with `{ monto: 500.00, motivo: "Cancelación total por error en datos" }`
- **THEN** the response is 201 with Nota Crédito in Series 001
- **AND** the original comprobante estado becomes "ANULADO"

#### Scenario: CPR-003-2 — Partial adjustment

- **GIVEN** comprobante CPR-002 exists with total 1000.00 and estado EMITIDO
- **WHEN** POST nota-credito with `{ monto: 200.00, motivo: "Descuento no aplicado" }`
- **THEN** the Nota Crédito is created for 200.00
- **AND** the original comprobante remains "EMITIDO"

#### Scenario: CPR-003-3 — Nota Crédito exceeds original total

- **GIVEN** comprobante with total 500.00
- **WHEN** POST nota-credito with monto = 600.00
- **THEN** the response is 422 — amount exceeds original comprobante total

#### Scenario: CPR-003-4 — Nota Crédito on already cancelled comprobante

- **GIVEN** comprobante with estado ANULADO
- **WHEN** POST nota-credito
- **THEN** the response is 422 — comprobante already cancelled

### Requirement: CPR-004 — Reprint with watermark (CAJ-009)

- **Priority**: MUST
- **Description**: Any issued comprobante MAY be reprinted. The reprinted PDF or visual representation SHALL display "COPIA" / "REIMPRESIÓN" watermark prominently. The original data is NEVER modified — reprint reads the stored XML and generates a display copy. No re-issuance to SUNAT occurs on reprint. All reprint actions are logged with `usuarioId`, `fecha`, `comprobanteId`.
- **Acceptance Criteria**:
  - [ ] GET `/api/v1/caja/comprobante/{id}/reimprimir` returns the comprobante data with "COPIA" watermark
  - [ ] Original data is unmodified after reprint
  - [ ] Reprint is logged in a reprint audit table
  - [ ] No new XML is generated — stored XML is reused

#### Scenario: CPR-004-1 — Successful reprint

- **GIVEN** comprobante CPR-003 exists with estado EMITIDO
- **WHEN** GET `/api/v1/caja/comprobante/CPR-003/reimprimir`
- **THEN** the response includes all original comprobante data
- **AND** the output contains a visible "COPIA" / "REIMPRESIÓN" watermark
- **AND** the original comprobante data is unchanged
- **AND** a reprint log entry is created

#### Scenario: CPR-004-2 — Reprint of cancelled comprobante

- **GIVEN** comprobante CPR-004 has estado ANULADO
- **WHEN** GET reimprimir
- **THEN** the response shows the original data with watermark
- **AND** the output also indicates "COMPROBANTE ANULADO"

### Requirement: CPR-005 — SUNAT XML generation

- **Priority**: MUST
- **Description**: The system MUST generate a SUNAT-compliant XML for each issued comprobante. The XML includes: emisor (RUC, razón social, dirección), cliente (tipo doc, número doc, nombre), detalle items (descripción, cantidad, precio unitario, subtotal, IGV), totales (subtotal, igv, total), and SUNAT-specific hashing (hash value for digital signature). MVP generates and stores the XML in the `xmlGenerado` CLOB field. Electronic submission to SUNAT/OSE is deferred.
- **Acceptance Criteria**:
  - [ ] XML is generated in valid SUNAT format (UBL 2.1)
  - [ ] XML is stored in the database as a CLOB
  - [ ] XML validates against SUNAT XSD schema
  - [ ] A hash/resumen field is computed per SUNAT requirements

#### Scenario: CPR-005-1 — Generate SUNAT XML

- **GIVEN** a comprobante with tipo 03 (Boleta), cliente, and 3 line items
- **WHEN** the comprobante is issued
- **THEN** `xmlGenerado` is populated with valid UBL 2.1 XML
- **AND** the XML includes emisor, cliente, detalle, and totales sections

#### Scenario: CPR-005-2 — XML for Nota Crédito

- **GIVEN** a Nota Crédito referencing original comprobante
- **WHEN** the Nota Crédito is issued
- **THEN** the XML includes reference to the original comprobante ID
- **AND** `tipoComprobante` in XML is "07"

### Requirement: CPR-006 — Permissions

- **Priority**: MUST
- **Description**: Comprobante operations use `caja:{accion}` pattern: `caja:crear` for issuance, `caja:anular` for Nota Crédito, `caja:ver` for reprint views. Only CAJA and ADMIN roles can issue and cancel comprobantes. Reprint is accessible to CAJA and ADMIN.
- **Acceptance Criteria**:
  - [ ] Usuario with role CAJA can issue, view, and reprint comprobantes
  - [ ] Usuario without CAJA or ADMIN role receives 403 on all comprobante endpoints

#### Scenario: CPR-006-1 — CAJA role can issue comprobante

- **GIVEN** a Usuario with role CAJA
- **WHEN** POST emitir comprobante
- **THEN** the response is 201

#### Scenario: CPR-006-2 — MEDICO cannot issue comprobante

- **GIVEN** a Usuario with role MEDICO
- **WHEN** POST emitir comprobante
- **THEN** the response is 403 Forbidden

### Requirement: CPR-007 — Data privacy

- **Priority**: MUST
- **Description**: Comprobante XML contains PII (cliente name, RUC/DNI, address). The `xmlGenerado` field MUST be annotated with `@ToString.Exclude` and excluded from application logs. List endpoints MUST NOT expose the full XML.
- **Acceptance Criteria**:
  - [ ] `xmlGenerado` is excluded from entity toString()
  - [ ] List endpoint GET `/api/v1/caja/comprobante` returns header info only (no XML)
  - [ ] Detailed GET `/api/v1/caja/comprobante/{id}` returns XML only with explicit `?includeXml=true` parameter

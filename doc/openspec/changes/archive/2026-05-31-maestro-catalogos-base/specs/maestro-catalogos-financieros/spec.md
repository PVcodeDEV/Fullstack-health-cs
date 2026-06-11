# Spec: maestro-catalogos-financieros

## Overview

Financial catalogs aligned with SUNAT (Peruvian tax authority) standards. Used by the `caja` module for electronic invoicing, payment processing, and inventory valuation. Codes are normative — once created they MUST NOT change.

## Requirements

- R-001: `TipoComprobante` MUST store `codigo_sunat` (PK, 2 digits), `nombre`, and `activo`. Seeded: Factura (01), Boleta (03), Nota Crédito (07), Nota Débito (08), Liquidación Compra (52).
- R-002: `TipoMoneda` MUST store `codigo_sunat` (PK), `nombre`, `simbolo`. Seeded: PEN (Soles, "S/"), USD (Dólares, "$").
- R-003: `UnidadMedida` MUST store `codigo_sunat` (PK), `nombre`, `abreviatura`. Seeded entries: Und (NIU), Tableta, ml (LTR), g (KGM), mg, Caja, Blister, Frasco, Ampolla, Paquete, Kit.
- R-004: SUNAT `codigo_sunat` values MUST be immutable after creation. The API MUST reject updates to the code field.
- R-005: All three entities MUST be seeded with complete SUNAT standard values via Flyway. No manual inserts required at deployment.
- R-006: Financial catalog data is NOT PII and MAY be cached.

## Scenarios

### SC-001: Seed SUNAT invoice types
Given Flyway migration V1__seed_tipo_comprobante.sql executes
When I GET `/api/v1/maestro/tipo-comprobante`
Then the system returns exactly 5 records with codes 01, 03, 07, 08, 52

### SC-002: Reject immutable code update
Given TipoMoneda with `codigo_sunat: "PEN"` exists
When I PUT `/api/v1/maestro/tipo-moneda/PEN` with a different `codigo_sunat`
Then the system returns 400 Bad Request — codigo_sunat is immutable

### SC-003: Query measurement units
When I GET `/api/v1/maestro/unidad-medida`
Then each result contains `codigo_sunat`, `nombre`, and `abreviatura`

### SC-004: List currencies
When I GET `/api/v1/maestro/tipo-moneda`
Then the system returns PEN (Soles) and USD (Dólares) as active records

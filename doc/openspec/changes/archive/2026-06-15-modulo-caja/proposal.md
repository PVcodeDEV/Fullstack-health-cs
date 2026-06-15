# Proposal: Módulo Caja

## Intent

Clinical billing is currently scattered across modules with no centralized payment, invoice, or account closure flow. Caja centralizes cashier operations: collects clinical charges (admission, hospitalization, pharmacy), processes payments, issues SUNAT-compliant electronic invoices, and closes patient accounts.

## Scope

### In Scope
- Tarifario: price list for clinical services with validity dates (fecha_desde/fecha_hasta)
- Paquetes: predefined service packages (e.g., "Paquete Parto Natural") for account assignment
- Cash register session (open/close) for Caja Clínica (Series 001)
- Pre-liquidación (pre-bill) generation for patient review
- Payment processing: Efectivo, POS, YAPE/PLIN, Transferencia
- Cuenta closure from clinica: payment collection + account closure
- Electronic invoice issuance (Boleta/Factura, SUNAT) + reprinting with "COPIA" watermark
- Notas de crédito for cancellations/adjustments
- TipoCambio (exchange rate) per transaction
- Caja-specific permissions (CAJA role exists; seed `caja:*` permisos)
- Flyway V37+ for Caja schema

### Out of Scope
- Caja Farmacia (exists in `farmacia.caja`, Series 004 — keep separate)
- Cuenta entity extraction from clinica (marked `@ExtractionPoint` for future)
- Tarifario por seguros (future: each insurer has its own tariff schedule)

### Future (documented, not implemented)
- Cash drawer / petty cash / fondos fijos
- Payment installments/quotas
- Tarifario por aseguradora (seguros particulares con precios negociados)

## Capabilities

### New Capabilities
- `caja-tarifario`: Price list for clinical services, with validity dates. Supports future tariff-per-insurer model.
- `caja-paquete`: Predefined service packages (e.g., cirugía + honorarios + días de cama) that map to Cuenta charges.
- `caja-sesion`: Cash register session (open/close)
- `caja-liquidacion`: Pre-bill generation, payment collection, account closure
- `caja-comprobante`: SUNAT electronic invoice, Nota Crédito, reprinting with "COPIA" watermark
- `caja-tipo-cambio`: Exchange rate configuration per transaction

### Modified Capabilities
- `clinica-cuenta`: Add endpoint for Caja to confirm payment and close account (see CTA-003/CTA-006)

## Business Rules

| ID | Rule | Detail |
|----|------|--------|
| CAJ-001 | Payment methods | Efectivo, POS, YAPE/PLIN, Transferencia — catalog in `maestro` |
| CAJ-002 | Invoice series | Caja Clínica → Series 001; Farmacia (existing) → Series 004 |
| CAJ-003 | Pricing | `final = costo_farmacia + IGV(18%) + utilidad_clinica(50% sobre costo)` |
| CAJ-004 | Discounts | Max 20%, needs Gerencia/Admin auth, min = costo + IGV |
| CAJ-005 | Billing flow | Charges → Cuenta (PENDIENTE_COBRO) → Pre-liquidación → Payment → Invoice → Cuenta CERRADA |
| CAJ-006 | Moneda | Default SOLES, TipoCambio configurable per transaction for USD |
| CAJ-007 | Invoice type | Boleta o Factura según SUNAT; single payment, no installments (MVP) |
| CAJ-008 | Nota Crédito | Supports cancellations and adjustments after invoicing |
| CAJ-009 | Reimpresión | Any issued comprobante can be reprinted with "COPIA" / "REIMPRESIÓN" watermark; original data never modified |
| CAJ-010 | Permissions | Pattern `caja:{accion}` with accion: crear, editar, aprobar, anular, ver |
| CAJ-011 | Tarifario vigencia | Items have `fecha_desde` + `fecha_hasta` (NULL = vigente). New prices create new rows; historical prices preserved |
| CAJ-012 | Paquetes | Package = collection of prestaciones with bundled price. Admission assigns package to Cuenta |
| CAJ-013 | Tarifario futuro seguros | Structure supports multiple tarifarios (one per aseguradora). MVP: single tarifario for particular patients |

## Dependency: módulo-entidad (previo/paralelo a caja)

The `com.clinica.entidad` module is a prerequisite for Caja. It manages legal entities (empresas) with RUC — used both as **clients** (invoice issuance) and **suppliers** (purchase registration). Lives in its own module because both Caja and Farmacia consume it.

### Scope
- `Empresa` entity: RUC (PK), tipo (RUC_10 / RUC_20), razónSocial, direccionFiscal, ubigeo, teléfono, email
- Rol automático: CLIENTE / PROVEEDOR / AMBOS — detectado por contexto, no manual
- `SunatRucClient`: consulta a SUNAT API (https://ww1.sunat.gob.pe/ol-ti-itfisdenreg/itfisdenreg.htm) con parseo diferenciado para RUC 10 (solo nombre) y RUC 20 (razón social + dirección + ubigeo)
- CRUD Empresa + auto-role promotion (si aparece en compra y venta → AMBOS)
- Vinculación opcional con Persona (para RUC 10, si la persona natural ya existe)

### Diferencia SUNAT RUC API
| Tipo RUC | Prefijo | Datos devueltos |
|----------|---------|-----------------|
| Persona Natural | RUC 10 (1er dígito) | Solo `apenomdenunciado` = nombre completo |
| Persona Jurídica | RUC 20 (1er dígito) | Razón social + dirección fiscal + ubigeo completo |

## Integration Matrix

| Module | Integration | Direction |
|--------|-------------|-----------|
| `entidad` | Empresa (RUC) for invoice clients and payment suppliers | Reads (nuevo módulo dependencia) |
| `clinica.cuenta` | Reads Cuenta + Cargos; marks PENDIENTE_COBRO → CERRADA | Bidirectional |
| `farmacia.caja` | Pharmacy consumption charges flow to patient account → Caja | Reads from clinica |
| `maestro` | TipoComprobante, TipoMoneda, UnidadMedida catalogs | Reads |
| `seguridad` | Role CAJA exists; seed `caja:*` permisos | Seeds |
| `persona` | Patient/client data for invoice issuance | Reads |

## Approach

Package-per-module under `com.clinica.caja` with sub-packages: `tarifario/`, `paquete/`, `sesion/`, `liquidacion/`, `comprobante/`, `tipo-cambio/`. Cuenta stays in clinica (extraction boundary documented). Tarifario uses fecha_desde/fecha_hasta for price history; future insurers get their own tarifario. Electronic invoice generation as a service layer with SUNAT XML template (MVP: generate + store; electronic submission deferred). Reprints display stored data with "COPIA" watermark — no re-issuance to SUNAT. Flyway V37+ for schema. Thymeleaf + HTMX for cashier UI.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `com.clinica.caja/*` | New | 4 sub-packages, ~12 entities |
| `clinica.cuenta/*` | Modified | Add confirm-payment endpoint |
| `seguridad` DataInitializer | Modified | Seed `caja:*` permisos |
| Backend resources/db/migration | New | V37__ to V42__ |

## Risk Assessment

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Cuenta extraction scope creep | Medium | Keep extraction boundary doc; Cuenta stays in clinica |
| SUNAT XML schema changes | Low | Service abstraction; template per SUNAT version |
| Exchange rate data quality | Low | Manual entry per transaction; audit log |
| Farmacia consumption billing | Medium | Pharmacy sends charges to clinica Cuenta first |

## Estimated Size

~3,800–4,500 lines (14 entities, 6 services, 6 controllers, Thymeleaf views, 8 migrations, permission seeds).

## Suggested PR Split

Given 400-line review budget, recommended 5 chained PRs:

1. **PR #1: Tarifario + Paquetes** (~700 lines) — V37 migrations for tarifario/paquete schema, entities/repos/services/controllers. Base: `feature/modulo-caja`.
2. **PR #2: SesionCaja + TipoCambio** (~600 lines) — V38 migrations, SesionCaja entity/repo/service/controller, TipoCambio service. Base: previous PR branch.
3. **PR #3: Liquidación + Cobro** (~800 lines) — Pre-liquidación, payment collection, Cuenta closure endpoint, clinica integration. Base: previous PR branch.
4. **PR #4: Comprobante + SUNAT** (~800 lines) — Invoice entity, Nota Crédito, SUNAT XML generation, reprint with "COPIA". Base: previous PR branch.
5. **PR #5: Permisos + UI polish** (~500 lines) — `caja:*` permission seeds in DataInitializer, Thymeleaf views polish, error handling edge cases. Base: previous PR branch.

## Rollback Plan

1. Revert Flyway migrations V37 → V42
2. Remove `caja:*` permiso seeds from DataInitializer
3. Revert clinica.cuenta confirm-payment endpoint changes
4. Re-deploy; verify other modules unaffected

## Dependencies

- `clinica-cuenta` — Cuenta + Cargos (extraction boundary, not moved)
- `maestro-catalogos-financieros` — TipoComprobante, TipoMoneda, UnidadMedida
- `modulo-autorizacion` — CAJA role (already seeded); `@PreAuthorize` infrastructure
- `modulo-persona` — Patient/client data for invoices
- `farmacia-core-v1` — Existing pharmacy cash sessions (kept separate)

## Success Criteria

- [ ] Cashier opens/closes a clinical cash session
- [ ] Cuenta in PENDIENTE_COBRO → payment collected → Cuenta CERRADA + bed released
- [ ] Pre-liquidación generated with charge details for patient review
- [ ] Electronic invoice (Boleta/Factura) issued with Series 001
- [ ] Nota Crédito cancels or adjusts an invoice
- [ ] Payment in USD applies TipoCambio correctly
- [ ] Discount >20% rejected; discount < costo+IGV rejected
- [ ] All endpoints return 403 for unauthorized roles

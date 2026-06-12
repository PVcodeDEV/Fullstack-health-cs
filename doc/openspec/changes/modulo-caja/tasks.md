# Tasks: Módulo Caja

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~3,800–4,500 across 5 sub-domains |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | 5 chained PRs |
| Delivery strategy | ask-on-risk |
| Chain strategy | feature-branch-chain |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: feature-branch-chain
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Entidad + Tarifario foundation | PR #1 | V37–V38 migrations, entities/repos/services/controllers for Empresa, Tarifario, Paquete |
| 2 | SesionCaja + TipoCambio | PR #2 | V39 migration, SesionCaja entity/service/controller, TipoCambio service |
| 3 | Liquidación + Cuenta closure | PR #3 | V40 migration, Liquidacion entity/service, payment processing, clinica confirmar-cobro |
| 4 | Comprobante + SUNAT | PR #4 | V41 migration, Comprobante entity, SUNAT XML builder, Nota Crédito, reprint |
| 5 | Permisos + UI + edge cases | PR #5 | DataInitializer seeds, Thymeleaf views, error handling |

## Phase 1: Foundation — Módulo Entidad ✅

- [x] 1.1 Create V37 migration: `empresa` + `sunat_consulta_log` tables
- [x] 1.2 Create `Empresa` entity extending `BaseEntity` with RUC módulo-11 validation
- [x] 1.3 Create `EmpresaRepository` with search/filter methods
- [x] 1.4 Create `RucValidator` reusing Modulo11Validator with RUC weight sequence
- [x] 1.5 Create `SunatRucClient` with configurable HTTP timeout for SUNAT API
- [x] 1.6 Create `EmpresaService` with CRUD, auto-role promotion, Persona linking (ENT-002, ENT-005)
- [x] 1.7 Create Empresa DTOs as records with Jakarta validation
- [x] 1.8 Create `EmpresaController` with POST/GET/PUT/DELETE + SUNAT consult endpoint
- [x] 1.9 Add `app.entidad.sunat-ruc-url` and timeout to `application.yml`
- [x] 1.10 Test: `RucValidator` parameterized (ENT-001-2), `SunatRucClient` mock (ENT-003-1/2/3)
- [x] 1.11 Test: `@DataJpaTest` CRUD + duplicate rejection + role promotion (ENT-001-3, ENT-002-1/2, ENT-004-1)

## Phase 2: Tarifario + Paquetes ✅

- [x] 2.1 Create V38 migration: `tarifario`, `tarifario_item`, `paquete`, `paquete_detalle` tables
- [x] 2.2 Create entities: `Tarifario`, `TarifarioItem`, `Paquete`, `PaqueteDetalle`
- [x] 2.3 Create repositories with temporal query methods (effective-date lookup)
- [x] 2.4 Create `PrecioCalculator` priced formula component (IGV + utilidad from config)
- [x] 2.5 Create `TarifarioService`: CRUD, price-change revision (new row per TRF-002), Paquete CRUD
- [x] 2.6 Create Tarifario/Paquete DTOs and `TarifarioController`
- [x] 2.7 Test: `PrecioCalculator` parameterized tests (TRF-003-1/2)
- [x] 2.8 Test: `@DataJpaTest` temporal price revision + historical query (TRF-002-1/2/3)
- [x] 2.9 Test: Paquete creation + deletion guard for referenced packages (TRF-004-1/2)
- [x] 2.10 Test: `@WebMvcTest` permission enforcement (TRF-007-1)

## Phase 3: SesionCaja + TipoCambio

- [ ] 3.1 Create V39 migration: `sesion_caja`, `tipo_cambio` tables
- [ ] 3.2 Create `SesionCaja` entity with open/close/discrepancy tracking
- [ ] 3.3 Create `TipoCambio` entity per-transaction FX rate record
- [ ] 3.4 Create `SesionCajaService`: open (reject double), close (compute diferencia, tolerance check)
- [ ] 3.5 Create SesionCaja DTOs and `SesionCajaController`
- [ ] 3.6 Test: double-open rejection (SES-001-2), close scenarios (SES-002-1/2/3/4)
- [ ] 3.7 Test: current session query (SES-003-1/2), payment-session link (SES-004-1/2)
- [ ] 3.8 Test: farmacia role cannot open clinical session (SES-005-1)

## Phase 4: Liquidación + Cuenta Closure

- [ ] 4.1 Create V40 migration: `liquidacion`, `payment_leg` tables
- [ ] 4.2 Create `Liquidacion` entity (PAGADO, linked to SesionCaja, Cuenta, TipoCambio)
- [ ] 4.3 Create `PaymentLeg` entity + `DescuentoValidator` (20% cap, cost-floor guard)
- [ ] 4.4 Create `LiquidacionService`: pre-liquidación view + pagar (validate discount → create payment legs → call confirmar-cobro)
- [ ] 4.5 Verify clinica `CuentaController.confirmarCobro()` endpoint contract (already exists)
- [ ] 4.6 Create Liquidacion DTOs and `LiquidacionController`
- [ ] 4.7 Test: pre-liquidación generation (LIQ-002-1), non-pending rejection (LIQ-002-2)
- [ ] 4.8 Test: multi-method payment (LIQ-001-2), sum mismatch (LIQ-001-3), referencia required (LIQ-001-4)
- [ ] 4.9 Test: discount validation (LIQ-004-1/2/3/4), USD + TipoCambio (LIQ-005-2/3)
- [ ] 4.10 Test: `@DataJpaTest` full flow payment → confirmar-cobro (LIQ-003-1/2)

## Phase 5: Comprobante + SUNAT XML

- [ ] 5.1 Create V41 migration: `comprobante`, `reprint_log` tables
- [ ] 5.2 Create `Comprobante` entity (EMITIDO/ANULADO, CLOB xmlGenerado, denormalized client data)
- [ ] 5.3 Create `ReprintLog` entity for reprint audit trail
- [ ] 5.4 Create `SunatXmlGenerator` builder (UBL 2.1 XML for Boleta/Factura/NotaCrédito)
- [ ] 5.5 Create `ComprobanteService`: emitir (mutually-exclusive persona/empresa, correlativo), nota-credito (state transition), reimprimir (watermark)
- [ ] 5.6 Create Comprobante DTOs and `ComprobanteController`
- [ ] 5.7 Test: Boleta/Factura issuance (CPR-001-1/2), missing empresaId (CPR-001-3), mutual exclusion (CPR-001-4)
- [ ] 5.8 Test: correlativo auto-increment (CPR-001-5), series 001 enforcement (CPR-002-1)
- [ ] 5.9 Test: Nota Crédito full/partial/exceed (CPR-003-1/2/3/4)
- [ ] 5.10 Test: reprint watermark + log (CPR-004-1/2), XML generation validity (CPR-005-1/2)
- [ ] 5.11 Test: `@WebMvcTest` permission enforcement (CPR-006-1/2)

## Phase 6: Permissions, UI, Integration Polish

- [ ] 6.1 Modify `DataInitializer` to seed `entidad:*` and `caja:*` permissions
- [ ] 6.2 Create Thymeleaf views: Empresa search/form, Tarifario admin, Sesion open/close
- [ ] 6.3 Create Thymeleaf views: Liquidacion preview/payment, Comprobante issuance/reprint
- [ ] 6.4 Add HTMX interactions for inline SUNAT consultation and price preview
- [ ] 6.5 Add RFC 9457 ProblemDetail error handling for all new endpoints
- [ ] 6.6 Test: `@WebMvcTest` role-access matrix for all endpoint groups (ENT-007-1/2, SES-006-1/2, LIQ-007-1, CPR-006-1/2)

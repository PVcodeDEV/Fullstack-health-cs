# Design: Módulo Caja

## Technical Approach

Package-per-subdomain under `com.clinica.caja` (tarifario, sesion, liquidacion, comprobante, tipo-cambio) plus a new dependency module `com.clinica.entidad`. Caja consumes clinica's Cuenta via direct service calls (same JVM — no HTTP loopback). SUNAT XML is generated via template builder and stored as CLOB; electronic submission deferred to future. Tarifario uses temporal data pattern (fecha_desde/fecha_hasta). All flows share a single `@Transactional` boundary per operation.

## Architecture Decisions

| Decision | Options Considered | Chosen | Rationale |
|---|---|---|---|
| Tarifario temporal data | Merge-on-update / Snapshot table | fecha_desde/fecha_hasta rows | Preserves price history per TRF-002; future insurer schedules map to aseguradoraId nullable FK per TRF-005 |
| Pricing formula computation | Runtime calc only / Cached column | Cached `precioFinal` column on item | Avoids recalculating on every read; formula configurable via tb_configuracion_api per TRF-003 |
| Cuenta closure transaction | REST call / Direct service call | Direct `CuentaService.confirmarCobro()` call | Same JVM, single `@Transactional`; simpler rollback than compensating saga per LIQ-003 |
| SUNAT XML generation | Template engine / JAXB / Builder | Builder pattern (XMLStreamWriter) | No extra dependency; fully testable; MVP generates without OSE submission per CPR-005 |
| Comprobante client data | FK-only / Denormalized copy | Denormalized at issuance | SUNAT XML immutability per CPR-001; source FKs preserved for audit |
| Entidad auto-role promotion | Computed at query / Stored column | Stored `rol` column + promotion on context | Efficient querying; audit log records all promotions per ENT-002 |
| RUC validation | New validator / Reuse Modulo11Validator | Reuse with configurable weights | Same modulo 11 algorithm; RUC uses different weight sequence from DNI |
| SUNAT RUC client | Separate client / Extend existing | New `SunatRucClient` (different endpoint/parsing) | SUNAT RUC endpoint returns HTML/JSON with different structure than DNI endpoint per ENT-003 |

## Data Flow

```
Cuenta (PENDIENTE_COBRO)
  → SesionCaja (must be ABIERTA — SES-001)
    → LiquidacionService.preLiquidar()
      → reads Cuenta + Cargos from clinica
      → resolves tarifario prices
      → returns PreLiquidacionResponse (transient, not persisted)
    → LiquidacionService.pagar()
      → validates discount (max 20%, min costo+IGV)
      → creates Liquidacion + PaymentLegs
      → links SesionCaja + TipoCambio (if USD)
      → calls CuentaService.confirmarCobro() → Cuenta CERRADA
    → ComprobanteService.emitir()
      → denormalizes client data (Persona or Empresa)
      → generates SUNAT XML via builder
      → stores Comprobante with xmlGenerado CLOB + serie "001"
```

## API Contracts

| Domain | Method | Endpoint | Purpose |
|---|---|---|---|
| Tarifario | POST | `/api/v1/caja/tarifario-item` | Create item (TRF-001) |
| Tarifario | POST | `/api/v1/caja/tarifario-item/price-change` | Price revision (TRF-002) |
| Tarifario | GET | `/api/v1/caja/tarifario-item/{codigo}/precio` | Resolve price for clinica (TRF-006) |
| Tarifario | POST | `/api/v1/caja/paquete` | Create package (TRF-004) |
| Sesion | POST | `/api/v1/caja/sesion/abrir` | Open session (SES-001) |
| Sesion | PUT | `/api/v1/caja/sesion/{id}/cerrar` | Close session (SES-002) |
| Sesion | GET | `/api/v1/caja/sesion/actual` | Current open session (SES-003) |
| Liquidacion | GET | `/api/v1/caja/liquidacion/pre/{cuentaId}` | Pre-bill preview (LIQ-002) |
| Liquidacion | POST | `/api/v1/caja/liquidacion/{cuentaId}/pagar` | Process payment (LIQ-003) |
| Comprobante | POST | `/api/v1/caja/comprobante/{liquidacionId}/emitir` | Issue invoice (CPR-001) |
| Comprobante | POST | `/api/v1/caja/comprobante/{id}/nota-credito` | Nota Crédito (CPR-003) |
| Comprobante | GET | `/api/v1/caja/comprobante/{id}/reimprimir` | Reprint with watermark (CPR-004) |
| Entidad | POST | `/api/v1/entidad/empresa` | Create empresa (ENT-001) |
| Entidad | GET | `/api/v1/entidad/sunat/consultar/{ruc}` | Consult SUNAT RUC API (ENT-003) |

## File Changes

| File | Action |
|---|---|
| `caja/tarifario/entity/{Tarifario,TarifarioItem,Paquete,PaqueteDetalle}.java` | Create |
| `caja/tarifario/repository/{Tarifario,TarifarioItem,Paquete}Repository.java` | Create |
| `caja/tarifario/service/{TarifarioService,PrecioCalculator}.java` | Create |
| `caja/tarifario/dto/{TarifarioItemRequest,TarifarioItemResponse,PaqueteRequest,PaqueteResponse,PrecioResponse}.java` | Create |
| `caja/tarifario/controller/TarifarioController.java` | Create |
| `caja/sesion/entity/SesionCaja.java` | Create |
| `caja/sesion/repository/SesionCajaRepository.java` | Create |
| `caja/sesion/service/SesionCajaService.java` | Create |
| `caja/sesion/dto/{SesionCajaRequest,SesionCajaResponse}.java` | Create |
| `caja/sesion/controller/SesionCajaController.java` | Create |
| `caja/liquidacion/entity/{Liquidacion,PaymentLeg,TipoCambio}.java` | Create |
| `caja/liquidacion/repository/{Liquidacion,PaymentLeg,TipoCambio}Repository.java` | Create |
| `caja/liquidacion/service/{LiquidacionService,DescuentoValidator}.java` | Create |
| `caja/liquidacion/dto/{PreLiquidacionResponse,PagoRequest,LiquidacionResponse,TipoCambioRequest,TipoCambioResponse}.java` | Create |
| `caja/liquidacion/controller/LiquidacionController.java` | Create |
| `caja/comprobante/entity/{Comprobante,ReprintLog}.java` | Create |
| `caja/comprobante/repository/{Comprobante,ReprintLog}Repository.java` | Create |
| `caja/comprobante/service/{ComprobanteService,SunatXmlGenerator}.java` | Create |
| `caja/comprobante/dto/{ComprobanteRequest,ComprobanteResponse,NotaCreditoRequest}.java` | Create |
| `caja/comprobante/controller/ComprobanteController.java` | Create |
| `entidad/entity/{Empresa,SunatConsultaLog}.java` | Create |
| `entidad/repository/{Empresa,SunatConsultaLog}Repository.java` | Create |
| `entidad/service/{EmpresaService,RucValidator,SunatRucClient}.java` | Create |
| `entidad/dto/{EmpresaRequest,EmpresaResponse,SunatRucResponse}.java` | Create |
| `entidad/controller/EmpresaController.java` | Create |
| `config/EntidadProperties.java` | Create |
| `seguridad/*/DataInitializer.java` | Modify — seed `caja:*` + `entidad:*` permisos |
| `clinica/admision/service/CuentaService.java` | Modify — add `confirmarCobro(Long cuentaId)` |
| `application.yml` | Modify — add `app.entidad.sunat-ruc-url` + timeout |
| `db/migration/V37__entidad_empresa.sql` | Create |
| `db/migration/V38__caja_tarifario.sql` | Create |
| `db/migration/V39__caja_sesion_tipo_cambio.sql` | Create |
| `db/migration/V40__caja_liquidacion.sql` | Create |
| `db/migration/V41__caja_comprobante.sql` | Create |

## Entity Conventions (per existing patterns)

- All entities extend `BaseEntity` with `@AttributeOverride` for column prefixes: `tar_` (tarifario), `paq_` (paquete), `ses_` (sesion), `liq_` (liquidacion), `pag_` (payment), `tcam_` (tipoCambio), `com_` (comprobante), `rep_` (reprint), `emp_` (empresa), `sun_` (sunat log)
- Primary keys: `BigInt` with `GenerationType.IDENTITY`
- BigDecimal for monetary fields (precision 10, scale 2)
- All DTOs as Java records with Jakarta validation annotations
- `@ToString.Exclude` on PII fields (document numbers, names, payment amounts, XML)

## Pricing Formula Implementation (TRF-003)

```java
// PrecioCalculator.java — stateless component
public BigDecimal calcularPrecioFinal(BigDecimal precioBase) {
    BigDecimal igv = precioBase.multiply(config.igvPorcentaje().divide(BigDecimal.valueOf(100)));
    BigDecimal utilidad = precioBase.multiply(config.utilidadPorcentaje().divide(BigDecimal.valueOf(100)));
    BigDecimal final_ = precioBase.add(igv).add(utilidad);
    // Round to nearest 0.10 (half-up at 0.05)
    return final_.multiply(BigDecimal.TEN)
        .setScale(0, RoundingMode.HALF_UP)
        .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
}
```

## Migration Plan

| Version | Schema | Tables |
|---|---|---|
| V37 | `entidad` | `empresa`, `sunat_consulta_log` |
| V38 | `caja` | `tarifario`, `tarifario_item`, `paquete`, `paquete_detalle` |
| V39 | `caja` | `sesion_caja`, `tipo_cambio` |
| V40 | `caja` | `liquidacion`, `payment_leg` |
| V41 | `caja` | `comprobante`, `reprint_log` |

## Testing Strategy

| Layer | What | Approach |
|---|---|---|
| Unit | PrecioCalculator, DescuentoValidator, RucValidator, Modulo11 for RUC | Parameterized tests, edge cases (boundary values, nulls) |
| Unit | SunatXmlGenerator | XML output validation against expected fields; no real SUNAT |
| Unit | SunatRucClient | Mock HTTP responses for RUC 10, RUC 20, timeout |
| Integration | Tarifario CRUD + price-change temporal rows | @DataJpaTest; verify fecha_hasta is set correctly on revision |
| Integration | SesionCaja open/close + balance | Verify double-open rejection, discrepancy tolerance, close blocks payments |
| Integration | Liquidacion + Cuenta closure | Full flow: preliquidation → payment → confirmar-cobro → comprobante |
| Integration | Comprobante issuance | Verify correlativo auto-increment, denormalized client data, XML storage |
| Security | Permission enforcement | @WebMvcTest with mocked SecurityContext; verify 403 for unauthorized roles |

## PR Split (5 chained PRs for 400-line budget)

1. **PR #1**: V37–V38 migrations + tarifario + paquete entities/services/controllers (~700 lines)
2. **PR #2**: V39 migration + sesion + tipo-cambio entities/services/controllers (~600 lines)
3. **PR #3**: V40 migration + liquidacion + Cuenta confirmar-cobro in clinica (~800 lines)
4. **PR #4**: V41 migration + comprobante + SUNAT XML generation + Nota Crédito (~800 lines)
5. **PR #5**: Permissions seeding + UI polish + error handling edge cases (~500 lines)

All PRs target `feature/modulo-caja` branch chain per proposal.

## Open Questions

None. Specs and proposal are fully detailed; all architecture decisions documented above.

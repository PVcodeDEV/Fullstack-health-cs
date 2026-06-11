# Exploration: Módulo Farmacia

## Current State

The `farmacia` package under `com.clinica.farmacia` is empty (only `.gitkeep`). The project already has catalog tables in `maestro` that the pharmacy module will reuse:

- **`maestro/entity/clinico/`**: `FormaFarmaceutica` (tb_formas_farmaceuticas), `ViaAdministracion` (tb_vias_administracion)
- **`maestro/entity/organizacion/`**: `CategoriaInsumo` (tb_categorias_insumo — hierarchical, seeded: Medicamento, Material Médico, etc.)
- **`maestro/entity/financiero/`**: `UnidadMedida` (tb_unidades_medida — includes SUNAT codes), `TipoMoneda`, `TipoComprobante`
- **`persona/entity/`**: `Persona` (tb_personas — can be linked as customer)
- **`maestro/entity/organizacion/`**: `AreaFuncional` — already includes "Farmacia" as a functional area

The existing module follows a clear 5-layer pattern (`entity/` → `repository/` → `service/` → `dto/` → `controller/`) with sub-packages per entity group. Business modules use the same pattern, sometimes adding a `type/` subpackage for enums.

Last Flyway migration is V29. Pharmacy will start at **V30**.

## Domain Rules (Confirmed)

1. **Farmacia vende al público general** — no requiere ser paciente de la clínica
2. **Cobro dentro de farmacia** — NO pasa por Caja. Caja maneja: cuentas de pacientes, copias de HCE, servicio de inyección, otros servicios administrativos
3. **REMYPE** — Pequeña Empresa, constraints de recursos (16 GB RAM servidor, 8 GB RAM clientes)
4. **SUNAT** — facturación electrónica es deferred, pero la estructura debe considerar `codigo_sunat` en productos y unidades

## Affected Areas

| Path | Why |
|------|-----|
| `backend/src/main/java/com/clinica/farmacia/` | New module — currently empty |
| `backend/src/main/resources/db/migration/V30__*` | First farmacia migration |
| `backend/src/main/resources/db/migration/V31__*` + | Subsequent farmacia migrations |
| `doc/openspec/specs/` | New spec folder `farmacia-core/` needed |
| `maestro/entity/clinico/` | Already has FormaFarmaceutica, ViaAdministracion — may need `Laboratorio` and `PrincipioActivo` catalogs |
| `maestro/entity/organizacion/` | CategoriaInsumo already usable — may need `TipoOperacion` or `TipoMovimiento` catalog |

## Approaches

### Approach 1: Full Pharmacy Module (v1 + v2 deferred but designed)

Full module covering: products/inventory core + sales POS. V2 features (purchasing, prescriptions, SUNAT e-invoicing, alerts) are deferred but the entity model accounts for them.

- **Pros**: Complete domain model, extensible, no painful refactors later
- **Cons**: More upfront design effort
- **Effort**: High

### Approach 2: Lean v1 — Sales Core Only

Only the bare minimum: product catalog, simple stock tracking (no lots/warehouses initially), and over-the-counter sales with receipt printing.

- **Pros**: Fastest time-to-value
- **Cons**: Will need to retrofit lot tracking, warehouses, purchases later — likely schema migrations
- **Effort**: Medium

### Approach 3: Recommended — v1 with Core Entities, Deferred Advanced Features

Build the essential entities fully (Producto, Lote, Almacen, Stock, Venta) but defer purchasing, prescriptions, SUNAT integration, alerts, and advanced inventory adjustments to v2. Sales POS is a simple cash-register flow without SUNAT integration.

- **Pros**: Solid foundation, no major schema changes later, fast enough to ship
- **Cons**: Still substantial v1 scope
- **Effort**: Medium-High

## Recommendation

**Approach 3**. Recommended because:

1. **Lotes (batches) with expiry dates are core to pharmacy compliance** — without them you can't track expiry or handle recalls. Retrofitting lots after simple stock is painful.
2. **Almacenes (warehouses)** are needed for the physical layout (main pharmacy + maybe a stock room) — required by SUNAT inventory bookkeeping even if deferred.
3. **Sales POS within farmacia** is a hard requirement — it's the primary value of the module.
4. **Purchasing, prescriptions, SUNAT** are clean deferred boundaries — they add tables but don't require changing existing ones.

## Module Structure

### Suggested package layout under `com.clinica.farmacia`

```
farmacia/
├── producto/              # Medication/product master
│   ├── entity/Producto.java
│   ├── repository/ProductoRepository.java
│   ├── service/ProductoService.java
│   ├── dto/ProductoRequest.java, ProductoResponse.java
│   └── controller/ProductoController.java
├── almacen/               # Warehouses / storage locations
│   ├── entity/Almacen.java
│   ├── repository/AlmacenRepository.java
│   ├── service/AlmacenService.java
│   ├── dto/AlmacenRequest.java, AlmacenResponse.java
│   └── controller/AlmacenController.java
├── lote/                  # Lot/batch tracking with expiry
│   ├── entity/Lote.java
│   ├── repository/LoteRepository.java
│   ├── service/LoteService.java
│   ├── dto/LoteRequest.java, LoteResponse.java
│   └── controller/LoteController.java
├── venta/                 # Over-the-counter sales (POS)
│   ├── entity/Venta.java, DetalleVenta.java
│   ├── repository/VentaRepository.java
│   ├── service/VentaService.java
│   ├── dto/VentaRequest.java, VentaResponse.java
│   └── controller/VentaController.java
├── movimiento/            # Stock movements (future: purchases, adjustments)
│   ├── entity/MovimientoStock.java
│   ├── repository/MovimientoStockRepository.java
│   ├── service/MovimientoStockService.java
│   ├── dto/MovimientoStockRequest.java, MovimientoStockResponse.java
│   └── controller/MovimientoStockController.java
└── type/                  # Enums
    ├── TipoProducto.java
    ├── TipoMovimiento.java
    └── EstadoVenta.java
```

### Key Entities

#### v1 Core

| Entity | Table | Purpose |
|--------|-------|---------|
| **Producto** | tb_productos | Sellable item: code, name, active ingredient, concentration, lab, forma farmacéutica, via adm., CategoriaInsumo, UnidadMedida, precio_venta, fraccionable flag |
| **Almacen** | tb_almacenes | Physical storage: code, name, location, default flag |
| **Lote** | tb_lotes | Inventory lot: Producto, Almacen, código_lote, fecha_vencimiento, stock_inicial, stock_actual, costo_unitario |
| **Venta** | tb_ventas | POS header: correlativo, fecha, Cliente (nullable Persona), subtotal, igv, total, estado, usuario |
| **DetalleVenta** | tb_detalles_venta | Line item: Venta, Producto, Lote, cantidad, precio_unitario, subtotal |
| **MovimientoStock** | tb_movimientos_stock | Audit trail: Producto, Lote, tipo (ENTRADA/SALIDA/AJUSTE), cantidad, referencia (venta_id, etc.), saldo_resultante |

#### v2 (Deferred)

| Entity | When |
|--------|------|
| OrdenCompra / DetalleOrdenCompra | Purchasing |
| RecepcionCompra | Goods receipt |
| Receta | Prescription management |
| DespachoReceta | Prescription dispensing |
| AjusteInventario | Waste, losses, returns |
| ComprobanteElectronico | SUNAT e-invoice |
| NotificacionVencimiento | Expiry alerts |

### Dependencies on Existing Modules

| Dependency | Module | Why |
|------------|--------|-----|
| `Persona` | `com.clinica.persona` | Customer reference on Venta (may be nullable for walk-in customers) |
| `FormaFarmaceutica` | `maestro.entity.clinico` | Product dosage form |
| `ViaAdministracion` | `maestro.entity.clinico` | Product administration route |
| `CategoriaInsumo` | `maestro.entity.organizacion` | Product category hierarchy |
| `UnidadMedida` | `maestro.entity.financiero` | Product measurement unit + SUNAT code |
| `TipoMoneda` | `maestro.entity.financiero` | Sale currency (future) |
| `TipoComprobante` | `maestro.entity.financiero` | Receipt/invoice type (future) |

New catalogs needed in maestro (v1):
- **`Laboratorio`** — Pharmaceutical lab/manufacturer (code, name, ruc)
- **`PrincipioActivo`** — Active pharmaceutical ingredient (code, name) — optional, could be a string on Producto

### v1 Feature List

1. **Product CRUD** — register medications and supplies with all pharmaceutical attributes, pricing
2. **Warehouse CRUD** — define storage locations (default: "Farmacia Principal")
3. **Lot/Batch tracking** — receive stock with lot numbers and expiry dates
4. **Stock query** — view current stock by product, warehouse, expiry date
5. **Over-the-counter sales (POS)** — create sale, select items, calculate total, close sale, print simple receipt
6. **Stock auto-decrement** — on sale completion, decrease lot stock
7. **Stock movement audit** — every stock change logged with type and reference

### v2 (Deferred) Features

1. Purchase orders and goods receipt
2. Prescription registration and linked dispensing
3. Physical inventory / adjustments (waste, losses, returns)
4. Expiry date alerts (dashboard widget)
5. SUNAT electronic invoicing integration (boleta/factura electrónica)
6. Supplier management
7. Product barcode scanning

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Column name collisions** — `created_at`/`updated_at` from BaseEntity use generic column names | Entities with different prefixes (fprod_, farm_...) need `@AttributeOverride` | Follow existing pattern from other modules |
| **Concurrent stock** — two cashiers selling same last unit | Overselling, negative stock | Use `@Version` (optimistic locking) on Lote entity |
| **Cliente != Persona** — walk-in customers without DNI | Need a lightweight customer model | Allow nullable Persona + optional quick customer record |
| **Decimal precision** — Peruvian Sol has IGV 18%, small fractions | Rounding errors over time | Use `BigDecimal` with scale 2 minimum; validate IGV calc |
| **Windows Server deployment** — no Docker, no Kubernetes | Manual deployment of fat JAR | Keep it simple: `java -jar clinica-erp.jar` + Windows Service wrapper |

## Open Questions

1. **Cliente de farmacia** — ¿usamos Persona existente (creando cliente sobre la marcha) o una tabla liviana `farmacia_cliente` con solo nombre + teléfono para el público general que no es paciente?
2. **Fraccionamiento** — ¿se permite vender fracciones (ej: media tableta, 3 ampollas de una caja de 10)? Afecta modelo de venta.
3. **Impresión de comprobante** — ¿impresora térmica USB en el mostrador de farmacia o solo comprobante electrónico? v1 necesita respuesta.
4. **IGV** — ¿productos con IGV y sin IGV (exonerados)? Afecta precio y comprobante.
5. **Usuario/vendedor** — ¿quién hace la venta? ¿cualquier usuario del sistema o solo personal de farmacia (AreaFuncional)?
6. **Precios** — ¿precio único o múltiples listas de precio (ej: público general vs asegurado)?

## Ready for Proposal

**Yes.** The analysis is complete. Proceed to create the `proposal` phase for a new SDD change (e.g., `farmacia-core-v1`).

Key decisions to confirm with the user before spec:
- Cliente model (Persona vs lightweight)
- Fractional sales (yes/no)
- Receipt printing vs purely electronic
- IGV handling
- Single price vs price lists

# Spec: farmacia-core-v1

## Purpose

Pharmacy POS v1 for over-the-counter public sales. Product catalog (medications & supplies), lot-tracked inventory, two-list pricing, auto-discounts on near-expiry lots, cash sessions. Independent from Caja. Package: `com.clinica.farmacia`.

## Requirements

### R-FARM-PRO: Productos

PRO-01: Two types `MEDICAMENTO`/`INSUMO`. Common: `codigo` (unique), `precioCosto`, `utilidadMedico%`, `utilidadPublico%`, `precioVentaMedico`, `precioVentaPublico`, `stockMinimo`, `stockCritico`, `categoriaInsumoId`, `unidadMedidaId`, `activo`. — MUST

PRO-02: MEDICAMENTO: `generico` (DCI), `descripcion`, `origen` (boolean), `tipoMedicamentoId`, `formaFarmaceuticaId`, `formaPresentacionId`, `grupoFarmacologicoId`. — MUST

PRO-03: INSUMO: `marcaId` (FK, required), `descripcion`. — MUST

PRO-04: New maestro catalogs: `TipoMedicamento`, `FormaPresentacion`, `GrupoFarmacologico`, `Marca`. GrupoFarmacologico manageable by pharmacy user. — MUST

PRO-05: Soft-delete via `activo`. Referenced products return 409. — MUST

**Scenarios:** SC-01: POST MEDICAMENTO → 201 with prices. SC-02: POST INSUMO no marcaId → 400. SC-03: DELETE with active stock → 409.

### R-FARM-PRE: Pricing

PRE-01: `precioVenta = (costo + IGV) × (1 + utilidad%)`, round to nearest 0.10 (half-up at 0.05). IGV = 18% costo. — MUST

PRE-02: Two lists per product: MEDICO and PUBLICO. POS selects per sale. — MUST

PRE-03: Costo ≤ umbral → utilidad = `utilidad_base` (20%). Costo > umbral → utilidad ∈ [`utilidad_alta_min`..`utilidad_alta_max`] (10-20%, default 15%). — MUST

PRE-04: Params in `tb_configuracion_api`: `umbral` (90), `utilidad_base` (20%), `utilidad_alta_min` (10%), `utilidad_alta_max` (20%). — MUST

PRE-05: Químico MAY override `utilidadMedico`/`utilidadPublico`. Rejects if `precioVenta < costo + IGV`. — MUST

PRE-06: Internal calc uses full BigDecimal. Rounding for presentation only. — MUST

**Scenarios:** SC-04: costo=5.00, utilidad=20% → precio=7.10. SC-05: costo=120 (>90) → utilidad=15%. SC-06: utilidad=-5% on costo=5.00 → rejected.

### R-FARM-STK: Stock / Lotes

STK-01: Lote: `productoId`, `codigoLote`, `fechaVencimiento`, `stockInicial`, `stockActual`, `precioCosto`, `almacenId`. — MUST

STK-02: Stock received via direct entry ("Recepción de stock"). No purchase orders. — MUST

STK-03: `stockActual` guarded by `@Version` (optimistic lock). — MUST

STK-04: Available stock = sum `stockActual` non-expired lots, default warehouse. — MUST

**Scenarios:** SC-07: Receive 100 units → stockActual=100. SC-08: Two sales sell last unit → one succeeds, other gets OptimisticLockException. SC-09: stockActual=0 → lot skipped in POS.

### R-FARM-VEN: POS / Ventas

VEN-01: Venta: `correlativo` (sequential per session), `fecha`, `clienteId` (FK Persona, DNI or "00000000"), `tipoPrecio` (MEDICO/PUBLICO, default PUBLICO), `subtotal`, `igv`, `total`, `estado` (BORRADOR/COMPLETADA/ANULADA), `usuarioId`, `sesionCajaId`, `conImpresion`. — MUST

VEN-02: DetalleVenta: `ventaId`, `productoId`, `loteId`, `cantidad`, `precioUnitario` (snapshot), `subtotal`, `descuentoAplicado`, `subtotalFinal`. — MUST

VEN-03: On COMPLETADA, decrement `stockActual` per lot. — MUST

VEN-04: Customer type selected explicitly at POS. No auto-detection. — MUST

**Scenarios:** SC-10: tipoPrecio=PUBLICO → uses precioVentaPublico. SC-11: COMPLETAR → stock decrements. SC-12: DNI "00000000" → sale proceeds.

### R-FARM-CAJ: SesionCaja

CAJ-01: SesionCaja: `usuarioId`, `fechaApertura`, `fechaCierre`, `montoApertura`, `montoCierre`, `totalVentas`, `estado` (ABIERTA/CERRADA), `diferencia`. — MUST

CAJ-02: Only QUIMICO, TECNICO, ENCARGADO_SISTEMA MAY open a session. — MUST

CAJ-03: Sale MUST link to an open SesionCaja for current user. — MUST

**Scenarios:** SC-13: Open session → estado=ABIERTA. SC-14: Close con montoCierre=500, totalVentas=290 → diferencia=10. SC-15: Unauthorized role → 403.

### R-FARM-DES: Descuentos por Vencimiento

DES-01: Auto-discount if `lote.fechaVencimiento <= hoy + umbral_dias`. Params: `umbral_dias` (90), `descuento_max_porcentaje` (20%). — MUST

DES-02: Discount ≤ max% AND discounted price ≥ costo + IGV. Max constraint applicable. — MUST

**Scenarios:** SC-16: Lot expires 45d, precio=10, costo+IGV=6 → discount ≤20%, price ≥6. SC-17: costo+IGV=9.50, precio=10 → max discount 5%.

### R-FARM-REP: Lista de Reposición

REP-01: `GET /api/v1/farmacia/productos/reposicion` returns products where `stockActual <= stockMinimo`. `?critico=true` returns where `stockActual <= stockCritico`. — MUST

**Scenarios:** SC-18: stockMinimo=20, stockActual=15 → included. SC-19: ?critico=true, stockCritico=5, stockActual=3 → included; stock=15 → excluded.

### R-FARM-ALM: Almacenes

ALM-01: Multiple `Almacen` records supported. Each has `codigo` (unique), `nombre`, `ubicacion` (optional), `default` (boolean flag, exactly one true). — MUST

ALM-02: API MUST allow CRUD on `Almacen` (not just the seeded default). — MUST

**Scenarios:** SC-20: POST Almacen → 201. SC-21: Two Almacenes with `default=true` → 409.

### R-FARM-TRF: Transferencias entre Almacenes

TRF-01: `MovimientoStock` supports type `TRANSFERENCIA` with `almacenOrigenId` and `almacenDestinoId`. — MUST

TRF-02: `POST /api/v1/farmacia/transferencias` moves `cantidad` units of a product from one `Lote` in `almacenOrigen` to a new `Lote` in `almacenDestino`, preserving `codigoLote` and `fechaVencimiento`. Atomic transaction. — MUST

TRF-03: Reject if `cantidad > stockActual` of source Lote. — MUST

TRF-04: Generates one `MovimientoStock(TRANSFERENCIA)` for audit; source Lote stock decremented, destination Lote created with same lot code. — MUST

**Scenarios:** SC-22: Transfer 20 from A→B → source decrements 20, dest created with 20. SC-23: Transfer more than stock → 400.

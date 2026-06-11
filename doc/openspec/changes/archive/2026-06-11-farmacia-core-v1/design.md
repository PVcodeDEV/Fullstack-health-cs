# Design: farmacia-core-v1

## Technical Approach

New `com.clinica.farmacia` module with sub-domain packages (`producto`, `lote`, `venta`, `sesioncaja`, `almacen`, `reposicion`) following existing clinica pattern. Four new maestro catalogs. Pricing engine with threshold-based utilidad + 0.10 rounding. Auto-discount on near-expiry lots at POS line-item add. Atomic COMPLETAR with optimistic lock on `stockActual`.

## Architecture Decisions

| Decision | Options | Chosen |
|----------|---------|--------|
| Sub-domain vs flat layers | clinica-style sub-packages vs flat entity/repository/service | **Sub-domain** — `farmacia.{producto,lote,venta,...}/` each with own 5-layer structure |
| `BaseEntity` reuse | Extend `maestro.entity.BaseEntity` vs own | **Extend** — consistent `@AttributeOverride` pattern (prefix per entity) |
| New maestro catalogs | Under `clinico/` vs new `farmacia/` sub-package | **New `maestro.entity.farmacia/`** — pharmacy-specific catalogs, clean separation |
| Venta cliente FK | Persona entity vs string DNI | **`persona_id` FK** with `"00000000"` fallback — consistent with existing persona module |
| `@Version` placement | On Venta vs Lote.stockActual | **On `stockActual`** — protects the bottleneck: concurrent decrement |
| Correlativo scope | Per-sesión vs per-day | **Per sesión** — simpler, matches physical cashier session, resets on close |
| IGV application | Precio = (costo × IGV) × (1+utilidad) | **Precio = (costo + costo×igv) × (1+utilidad)** — spec states IGV = 18% costo |

## Data Model

### New Maestro Catalogs (V30, `maestro.entity.farmacia/`)

| Entity | Table | Prefix | Fields |
|--------|-------|--------|--------|
| `TipoMedicamento` | `tb_tipos_medicamento` | `tmed_` | codigo, nombre |
| `FormaPresentacion` | `tb_formas_presentacion` | `fpre_` | codigo, nombre |
| `GrupoFarmacologico` | `tb_grupos_farmacologicos` | `gfar_` | codigo, nombre |
| `Marca` | `tb_marcas` | `marc_` | codigo, nombre |

All extend `BaseEntity` with `@AttributeOverride`. Existing FK refs: `CategoriaInsumo` (categ), `UnidadMedida` (umed), `FormaFarmaceutica` (ffar).

### Core Farmacia Entities

| Entity | Table | Prefix | Mig | Key Fields |
|--------|-------|--------|-----|------------|
| `Almacen` | `tb_almacenes` | `alm_` | V31 | nombre, codigo, default flag |
| `Producto` | `tb_productos` | `prod_` | V31 | codigo(UQ), tipo(MEDICAMENTO/INSUMO), precioCosto(12,4), utilidadMedico%, utilidadPublico%, precioVentaMedico(10,2), precioVentaPublico(10,2), stockMinimo, stockCritico, FK→categoriaInsumo, FK→unidadMedida. MEDICAMENTO adds: generico, descripcion, origen(b), FK→tipoMedicamento, FK→formaFarmaceutica, FK→formaPresentacion, FK→grupoFarmacologico. INSUMO adds: FK→marca, descripcion |
| `Lote` | `tb_lotes` | `lote_` | V32 | FK→producto, codigoLote, fechaVencimiento(DATE), stockInicial, **stockActual`@Version`**, precioCosto(12,4), FK→almacen |
| `MovimientoStock` | `tb_movimientos_stock` | `movs_` | V32 | FK→lote, tipo(ENTRADA/SALIDA/AJUSTE/**TRANSFERENCIA**), cantidad, motivo, FK→venta(nullable), FK→usuario, almacenOrigenId(nullable), almacenDestinoId(nullable) |
| `Venta` | `tb_ventas` | `vent_` | V33 | correlativo, fecha, FK→persona(cliente), tipoPrecio(MEDICO/PUBLICO), subtotal(10,2), igv(10,2), total(10,2), estado(BORRADOR/COMPLETADA/ANULADA), FK→usuario, FK→sesionCaja, conImpresion(b) |
| `DetalleVenta` | `tb_detalles_venta` | `dven_` | V33 | FK→venta, FK→producto, FK→lote, cantidad, precioUnitario(10,2), subtotal(10,2), descuentoAplicado(10,2), subtotalFinal(10,2) |
| `SesionCaja` | `tb_sesiones_caja` | `scaj_` | V34 | FK→usuario, fechaApertura, fechaCierre(nullable), montoApertura(10,2), montoCierre(nullable), totalVentas(10,2), estado(ABIERTA/CERRADA), diferencia(10,2) |

**Decimal strategy**: `precioCosto` → scale 4 (internal precision). Prices, totals → scale 2 (display). Rounding utility truncates scale 4 to scale 2 presentation.

**Indexes**: `(lote_fecha_vencimiento, lote_producto_id)` for near-expiry queries. `(vent_sesion_caja_id, vent_correlativo)` unique. `(lote_producto_id, lote_stock_actual)` for POS filtering.

## Pricing Engine

```
PricingService.calcularPrecioVenta(costo, tipoPrecio)
  → igv = costo × ConfiguracionApi("farmacia.igv", 18%)
  → umbral = ConfiguracionApi("farmacia.umbral_costo", 90)
  → utilidad = (costo > umbral)
       ? clamp(default, umbral_alto_min, umbral_alto_max)
       : ConfiguracionApi("farmacia.utilidad_base", 20%)
  → precio = (costo + igv) × (1 + utilidad/100)
  → RoundingUtil.roundPrecio(precio)
```

**RoundingUtil.roundPrecio(precio)**: `BigDecimal.valueOf(Math.round(precio.doubleValue() * 10) / 10.0)` with HALF_UP. Internal calc uses full precision; rounding only at API boundary.

## Discount Engine

```
DescuentoService.calcularDescuento(lote, precioOriginal, costoMasIgv)
  → if lote.fechaVencimiento > today + config("umbral_dias", 90) return 0
  → descMax = precioOriginal × config("descuento_max_pct", 20%) / 100
  → descFisico = precioOriginal - costoMasIgv  // can't sell below cost+IGV
  → return min(descMax, descFisico)
```

Applied at `agregarDetalle` time, snapshotted to `detalle.descuentoAplicado`.

## Service Layer Algorithms

| Service | Method | Key Logic |
|---------|--------|-----------|
| `ProductoService` | `crear(request)` | Validate tipo, compute both prices via `PricingService`, reject if precio < costo+IGV |
| `ProductoService` | `actualizarUtilidad(id, ...)` | Químico only. Recompute prices, validate floor, persist new utilidad permanently |
| `LoteService` | `recibir(...)` | Atomic: create Lote + MovimientoStock(ENTRADA) in same `@Transactional` |
| `VentaService` | `crear(sesionCajaId)` | Validate open SesionCaja for user, create Venta(BORRADOR), generate next correlativo |
| `VentaService` | `agregarDetalle(...)` | Validate stock, apply auto-discount, snapshot price from Producto.precioVenta{tipoPrecio} |
| `VentaService` | `completar(ventaId)` | Atomic: validate stock for all items, decrement each Lote (optimistic lock), transition COMPLETADA, update SesionCaja.totalVentas |
| `SesionCajaService` | `abrir(usuarioId)` | Role check QUIMICO/TECNICO/ENCARGADO_SISTEMA, fail if any open session for user |
| `SesionCajaService` | `cerrar(sesionId)` | Calculate diferencia = montoCierre - montoApertura - totalVentas |
| `ReposicionService` | `listar(query)` | Native/JPQL aggregation: `SUM(lote.stockActual) AS totalStock` grouped by producto, filtered by stockMinimo/stockCritico |
| `TransferenciaService` | `transferir(productoId, loteOrigenId, almacenDestinoId, cantidad, motivo)` | Atomic: validate source stock, create dest Lote (same `codigoLote` + `fechaVencimiento`), decrement source, log `MovimientoStock(TRANSFERENCIA)` |

## API Contracts

| Controller | Endpoints | Auth | Notes |
|------------|-----------|------|-------|
| `ProductoController` | CRUD `/api/v1/farmacia/productos` | QUIMICO POST/PUT/DELETE; TECNICO GET | DELETE → 409 if stock exists |
| `LoteController` | `POST recibir`, `GET porProducto`, `GET vencimientos` | QUIMICO on POST, TECNICO on GET | vencimientos: próximos N días |
| `AlmacenController` | CRUD `/api/v1/farmacia/almacenes` | QUIMICO | Multi-almacén, exactly one `default=true` |
| `VentaController` | `POST crear`, `POST agregarDetalle`, `POST completar`, `GET/{id}` | TECNICO/QUIMICO | All within open SesionCaja |
| `SesionCajaController` | `POST abrir`, `POST cerrar/{id}`, `GET activa` | TECNICO/QUIMICO/ENCARGADO_SISTEMA | Active = for current user |
| `ReposicionController` | `GET /api/v1/farmacia/reposicion?critico=` | TECNICO/QUIMICO | Read-only aggregation |
| `TransferenciaController` | `POST /api/v1/farmacia/transferencias` | QUIMICO | Atomic move between warehouses |

## Configuration (`tb_configuracion_api`, modulo = "farmacia")

| Clave | Default | Read By | Type |
|-------|---------|---------|------|
| `umbral_costo` | 90 | PricingService | decimal |
| `utilidad_base` | 20 | PricingService | decimal |
| `utilidad_alta_min` | 10 | PricingService | decimal |
| `utilidad_alta_max` | 20 | PricingService | decimal |
| `descuento_vencimiento_dias` | 90 | DescuentoService | integer |
| `descuento_vencimiento_max_pct` | 20 | DescuentoService | decimal |
| `igv` | 18 | PricingService | decimal |

All read via `ConfiguracionApiService.findByModuloAndClave("farmacia", clave)` with fallback to default in code.

## Security

| Role | Scope | @PreAuthorize |
|------|-------|---------------|
| `QUIMICO` | Full CRUD, override utilidad, receive stock | `hasRole('QUIMICO')` |
| `TECNICO` | Ventas, list, read-only stock | `hasAnyRole('QUIMICO','TECNICO')` |
| `ENCARGADO_SISTEMA` | Open/close sessions | `hasAnyRole('QUIMICO','TECNICO','ENCARGADO_SISTEMA')` |

Role names are string enum values from `tb_roles.codigo`. v1 uses `@PreAuthorize("hasRole('QUIMICO')")` — future migration to permisos.

## Testing Strategy

| Layer | Focus | Tool |
|-------|-------|------|
| Repository | Producto/Categoria relationship, Lote stock queries, Venta cascade | `@DataJpaTest` + `@Sql` |
| Service | PricingService rounding edge cases, DescuentoService threshold logic, VentaService optimistic lock exception | `@ExtendWith(MockitoExtension.class)` |
| Controller | Validation errors, auth 403, POS flow status codes | `@WebMvcTest` + `@WithMockUser` |
| Integration | Full POS flow: abrir → crear → agregarDetalle → completar → cerrar | `@SpringBootTest` + `@AutoConfigureMockMvc` |

## Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| Concurrent stock decrement | `@Version` on `stockActual` → `OptimisticLockException` at completar, retry or fail |
| Rounding errors | Full BigDecimal internal; RoundingUtil at boundary only |
| precio < costo+IGV | Reject in `agregarDetalle` after discount calc |
| Unconfigured thresholds | Hardcoded defaults in each service, logged on use |
| Missing `tb_configuracion_api` rows | `ConfiguracionApiService` throws EntityNotFound — services must handle and fallback |

## Migration / Rollout

V30: new maestro catalogs (seed data). V31: almacen + producto. V32: lote + movimiento_stock. V33: venta + detalle_venta. V34: sesion_caja. Rollback per version via `DROP TABLE`. No data migration — greenfield module.

## Open Questions

- [ ] Correlativo por sesión o por día? Asumo **por sesión** (resetea al cerrar) — confirmar con usuario de negocio
- [ ] Re-impresión de comprobante? Spec no menciona, asumo `conImpresion=true` flag para v1
- [ ] Ticket/boleta template: predefinida o configurable? Asumo predefinida para v1
- [ ] Varias sesiones por día por usuario? Spec no lo prohíbe, asumo que una activa a la vez
- [ ] Default warehouse (Almacén por defecto): se crea en V31 seed o se requiere crear manual?

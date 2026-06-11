# Tasks: farmacia-core-v1 — Pharmacy POS Module

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated lines | ~5,400 (3,800 code + 1,600 test) |
| 400-line risk | High |
| Chained PRs | Yes |
| Split | 5 stacked PRs |
| Strategy | ask-on-risk |
| Chain | **stacked-to-main** (confirmed) |

Decision needed before apply: No (resolved: chained PRs, stacked-to-main)
Chained PRs recommended: Yes
Chain strategy: **stacked-to-main**
400-line budget risk: High

### Work Units

| # | Goal | PR |
|---|------|---|
| 1 | Catalogs + Almacén + Producto + Pricing | PR #1 |
| 2 | Lote + MovimientoStock + Descuento + Transferencias | PR #2 |
| 3 | Venta + DetalleVenta | PR #3 |
| 4 | SesionCaja + Reposicion + Security + Almacén CRUD | PR #4 |
| 5 | Integration tests | PR #5 |

## PR #1: Catalogs + Almacén + Producto + Pricing

- [x] 1.1 V30 Flyway: `tb_tipos_medicamento`, `tb_formas_presentacion`, `tb_grupos_farmacologicos`, `tb_marcas` (PRO-04)
- [x] 1.2 V31 Flyway: `tb_almacenes`, `tb_productos` with type columns (PRO-01..PRO-03)
- [x] 1.3 4 maestro entities + repos + services + controllers (PRO-04)
- [x] 1.4 `Almacen` entity/repo/service/controller + default flag (CAJ-01)
- [x] 1.5 `Producto` entity with MEDICAMENTO/INSUMO discriminator (PRO-01..PRO-03)
- [x] 1.6 `PricingService` + `RoundingUtil` (PRE-01..PRE-06, SC-04..SC-06)
- [x] 1.7 `ProductoService` (crear, soft-delete, price validation) (PRO-05, PRE-05)
- [x] 1.8 `ProductoRequest`/`ProductoResponse` DTOs with type validation (PRO-01..PRO-03)
- [x] 1.9 `ProductoController` CRUD endpoints (SC-01, SC-02, SC-03)
- [x] 1.10 Tests: Producto layers, PricingRounding edge cases

## PR #2: Lote + MovimientoStock + Descuento + Transferencias

- [x] 2.1 V32 Flyway: `tb_lotes`, `tb_movimientos_stock` (STK-01) + TRANSFERENCIA columns
- [x] 2.2 `Lote` entity with `@Version stockActual` + repo (STK-01, STK-03)
- [x] 2.3 `LoteService.recibir` (atomic Lote + MovStock ENTRADA) + controller (STK-02, SC-07)
- [x] 2.4 `MovimientoStock` entity + repo with TRANSFERENCIA type (STK-01, TRF-01)
- [x] 2.5 `DescuentoService` threshold/max logic (DES-01, DES-02, SC-16, SC-17)
- [x] 2.6 `TransferenciaService` + controller (TRF-01, TRF-02, TRF-03, TRF-04, SC-22, SC-23)
- [x] 2.7 Config seeding: farmacia defaults in `tb_configuracion_api` (PRE-04)
- [x] 2.8 Tests: LoteService, DescuentoService, MovStock, TransferenciaService

## PR #3: Venta + DetalleVenta

- [x] 3.1 V33 Flyway: `tb_ventas`, `tb_detalles_venta` + stub `tb_sesiones_caja` (VEN-01, VEN-02)
- [x] 3.2 `Venta` + `DetalleVenta` entities + enums + repos (VEN-01, VEN-02)
- [x] 3.3 `VentaService.completar` with correlativo per sesión + discounts + stock decrement (VEN-01)
- [x] 3.4 `VentaService` applies auto-discount via DescuentoService + manual discount (VEN-02, DES-01)
- [x] 3.5 `VentaService.completar` atomic decrement with opt-lock retry (VEN-03, SC-08, SC-11)
- [x] 3.6 `VentaController` REST + DTOs + `anular` endpoint (SC-10, SC-12)
- [x] 3.7 Tests: VentaServiceTest with 10 scenarios

## PR #4: SesionCaja + Reposicion + Security

- [x] 4.1 V34 Flyway: `tb_sesiones_caja` (CAJ-01)
- [x] 4.2 `SesionCaja` entity + repo (CAJ-01)
- [x] 4.3 `SesionCajaService` (abrir/cerrar + role check) + controller (CAJ-02, CAJ-03, SC-13..SC-15)
- [x] 4.4 `ReposicionService` aggregation query + controller (REP-01, SC-18, SC-19)
- [x] 4.5 `@PreAuthorize` on all farmacia controllers (QUIMICO/TECNICO/ENCARGADO_SISTEMA)
- [x] 4.6 Tests: SesionCaja, Reposicion, role enforcement
- [x] 4.7 `Almacen` CRUD endpoint (ALM-01, ALM-02, SC-20, SC-21)

## PR #5: Integration Tests

- [x] 5.1 `@SpringBootTest`: full POS flow abrir→crear→agregarDetalle→completar→cerrar
- [x] 5.2 `@SpringBootTest`: SesionCaja open/close with diferencia calc
- [x] 5.3 `@SpringBootTest`: Reposicion list with critico filter
- [x] 5.4 `@DataJpaTest`: Lote near-expiry queries, Venta cascade

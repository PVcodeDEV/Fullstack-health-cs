# 🏥 Módulo de Farmacia - Documento Consolidado

## 📌 Descripción General
El módulo de farmacia es uno de los componentes críticos del sistema integral clínico, con requerimientos específicos para gestión de inventario, control de stock, trazabilidad y operaciones de ventas.

## 🔍 Requerimientos Identificados

### Funcionalidades Básicas
- Compras de productos farmacéuticos (solo químico)
- Gestión de inventario (mínimo, máximo, crítico)
- Alertas automáticas de stock
- Control de vencimientos
- Trazabilidad por lote
- Trazabilidad inversa
- Ventas a pacientes
- Transferencias internas
- Notas de crédito
- Kardex
- Cargos a pacientes

### Normativa Aplicable
- Ley 29459
- RM 554-2022 MINSA
- DIGEMID / DIGESA

## 🔐 Roles en Farmacia

### Químico
- Full control
- Compras
- Edición de precios
- Gestión completa de inventario
- Aprobación de notas de crédito

### Técnico
- Operativo
- Ventas
- Control de stock
- Atención al paciente
- Gestión básica de inventario

## 💰 Pricing en Farmacia

### Modelo Definitivo
Precio venta = Costo + IGV + Utilidad

### Reglas de Pricing
- Utilidad base = 20% (configurable por administración)
- Precio mínimo: 0.20 (reunión #4)
- Validación: Precio nuevo < (Costo + IGV) → No aceptado (protección contra pérdida)

### Pricing Dinámico (reunión #7)
- Químico puede editar el precio final si considera que está por debajo del mercado
- Al editar, se recalcula la utilidad real para ese producto
- Nueva utilidad se guarda y se usará desde ese momento en adelante
- Ejemplo real:
  - Paracetamol: Costo + IGV = 0.3531
  - Precio inicial: 0.42 (con 20% utilidad)
  - Precio editado: 1.00 (con 140% utilidad)
  - Nueva utilidad (140%) se actualiza para futuras ventas

### Descuentos (reunión #7)
- Solo aplican cuando un lote está próximo a vencer (≤ 3 meses) y tiene stock activo
- Límite máximo de descuento: 20% (configurable por administración, no por químico/técnico)
- Descuento se aplica sobre precio de venta final
- En MVP: botón "Autorizar descuento" solo para rol Gerencia/Administrador
- Límite mínimo: no menor al costo + IGV

### Regla de Redondeo (NUEVO REQUISITO - reunión #7)
- Para evitar decimales complejos y facilitar cuadres:
  - 0.49 → 0.50
  - 0.44 → 0.40
  - 1.23 → 1.20
  - 1.25 → 1.30 (en caso de equidistancia, redondeo arriba)
- ⚠️ Importante: El cálculo interno para utilidad, IGV y costos debe trabajar con decimales completos (para evitar errores acumulativos). El redondeo es solo para presentación y facturación final.

## 📊 Problemas en el Sistema Actual (reunión #4)
- Inconsistencia en precios
- Inventario valorizado incorrecto
- Alta complejidad innecesaria
- Formas actuales de pricing:
  - Precio manual
  - Basado en compras:
    - Primera compra
    - Última compra
    - Promedio
    - Máximo
    - Mínimo
- Nota crédito cliente: < 24h

## 🚀 Solución Propuesta
- Rediseño desde cero
- Motor de precios y auditoría
- Integración con otros módulos (clínica, caja)
- Trazabilidad completa por lote

## ✅ Acciones Pendientes
- Definir regla de descuento para Caja (se propone también 20% como límite)
- Reunión específica para Rol Técnico de Farmacia (Reunión #8)
- Actualizar `rol_farmacia_quimico_v2.md` con:
  - Regla de edición de precio con utilidad dinámica
  - Regla de redondeo
  - Descuento solo por lote próximo a vencer
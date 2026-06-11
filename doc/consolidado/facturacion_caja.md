# 🧾 Módulo de Facturación y Caja - Documento Consolidado

## 📌 Descripción General
El módulo de facturación y caja es crítico para la operación financiera de la clínica, integrando servicios clínicos, farmacia y otros cargos al paciente.

## 🔍 Requerimientos Identificados

### Funcionalidades Básicas
- Apertura de caja
- Reporte de caja
- Generación manual de prestaciones
- Ajuste manual de precios
- Emisión de boletas y facturas
- Notas de crédito
- Series:
  - 001 clínica
  - 004 farmacia

### Integración
- Integrar alquileres a facturación
- Integración con farmacia (consumos en tiempo real)
- Integración con hospitalización

## 💰 Pricing en Caja

### Modelo Definitivo (reunión #6)
- Precio final = Precio farmacia + IGV + Utilidad clínica
- Utilidad clínica = 50% sobre costo farmacia (según reunión #3)

### Descuentos
- En MVP: botón "Autorizar descuento" solo para rol Gerencia/Administrador
- Límite máximo de descuento: 20% (configurable)
- Límite mínimo: no menor al costo + IGV

## 📊 Problemas en el Sistema Actual (reunión #4)
- No integración con farmacia ni hospitalización
- Cobros manuales para:
  - Insumos extra
  - Días extra
  - Cambio de habitación
- Falta de automatización en flujos

## 🔐 Roles en Caja
- Solo cobro (según reunión #5)
- Permisos específicos para:
  - Crear
  - Editar
  - Aprobar
  - Eliminar
  - Ver

## 🗄️ Migración de Datos (reunión #6)
- Numeración de comprobantes: comienza desde 001 (nuevo sistema)
- Historial de comprobantes: ❌ No se migra

## 🚀 Solución Propuesta
- ERP modular integrado
- Motor de reglas para automatización de cobros
- Rediseño desde cero (no evolución del sistema actual)
- Integración con RENIEC/SUNAT vía API + caché local

## 🔗 Estrategia de Integración (reunión #6)
- Primera consulta a API externa (RENIEC/DNI, SUNAT/RUC)
- Datos se almacenan en BD local
- Consultas siguientes → se lee desde BD
- Flujo ejemplo:
  - Usuario ingresa DNI →
  - ¿Existe en BD local?
    - Sí: muestra datos guardados
    - No: consulta API RENIEC → guarda → muestra
- ✅ Beneficio: velocidad, trazabilidad, auditoría

## ✅ Acciones Pendientes
- Definir regla de descuento específica para Caja (se propone 20% como límite)
- Reunión específica para Rol Caja (Reunión #9)
- Implementar emisión manual de comprobantes (en MVP solo emisión manual)
- Definir reglas para integración con alquileres
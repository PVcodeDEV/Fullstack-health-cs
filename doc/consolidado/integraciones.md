# 🔗 Sistema de Integraciones - Documento Consolidado

## 📌 Descripción General
El sistema requiere múltiples integraciones críticas para funcionar de manera eficiente, tanto internas (entre módulos) como externas (con sistemas gubernamentales).

## 🔗 Integraciones Internas

### Farmacia - Clínica (reunión #3)
- Consumos manuales actualmente
- Necesidad de integración en tiempo real
- Nutrición solicita insumos a farmacia

### Caja - Farmacia (reunión #4)
- Problema crítico: No integración con farmacia ni hospitalización

### Admisión - Hospitalización (reunión #4)
- Flujo desconectado con otras áreas

## 🔌 Integraciones Externas

### RENIEC/SUNAT (reunión #6)
- Estrategia de Integración (API + Caché Local):
  - Primera consulta a API externa (RENIEC/DNI, SUNAT/RUC)
  - Datos se almacenan en BD local
  - Consultas siguientes → se lee desde BD
  - Mismas reglas para:
    - DNI (pacientes, trabajadores)
    - RUC (proveedores, empresas)
    - Validación de comprobantes

### Flujo de Ejemplo (reunión #6)
- Usuario ingresa DNI →
- ¿Existe en BD local?
  - Sí: muestra datos guardados
  - No: consulta API RENIEC → guarda → muestra
- ✅ Beneficio: velocidad, trazabilidad, auditoría

## 📊 Problemas Actuales (reunión #4)
- Módulos aislados
- Procesos manuales
- Falta de trazabilidad
- Control débil
- Sistema actual presenta funcionalidad parcial pero mal integrada

## 🚀 Solución Propuesta (reunión #6)
- Rediseño desde cero
- Core: clínica + farmacia + caja
- Motor de precios y auditoría
- ERP modular:
  - Clínica
  - Farmacia
  - Caja
  - Contabilidad
  - RR.HH

## 🧾 Integración con Facturación
- Series:
  - 001 clínica
  - 004 farmacia
- Integrar alquileres a facturación

## 🔐 Auditoría y Trazabilidad (reunión #6)
- Toda modificación en precios se loguea (quién, cuándo, valor anterior/nuevo)
- Todo consumo de farmacia a paciente se loguea
- Todo cambio en HC se loguea
- Todo acceso remoto (si se habilita en futuro) se loguea

## 🗄️ Migración de Datos (reunión #6)
- Historias clínicas activas: ✅ Sí (digitalizadas al nuevo formato)
- Historial de comprobantes: ❌ No (se comienza nueva numeración desde 001)
- Productos / stock: ❌ No (Inventario inicial manual)
- Pacientes activos: ✅ Sí (solo datos filiación)
- Usuarios: ❌ No (Se crean desde cero con nuevo modelo de roles)

## ✅ Acciones Pendientes
- Implementar API para RENIEC/SUNAT
- Desarrollar caché local para consultas
- Definir esquema de datos para integración
- Crear mecanismos de auditoría para todas las transacciones
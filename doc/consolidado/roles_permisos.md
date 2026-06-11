# 🔐 Sistema de Roles y Permisos - Documento Consolidado

## 📌 Descripción General
El sistema de roles y permisos es fundamental para garantizar la seguridad, trazabilidad y funcionalidad adecuada del sistema integral clínico.

## 🏢 Áreas Físicas de la Clínica (reunión #5, #7)
1. Admisión
2. Sala de Operaciones (SOP)
3. Hospitalización (administra habitaciones)
4. Caja
5. Contabilidad
6. Nutrición

## 👥 Personal y Roles Funcionales (reunión #5)
- 40-41 trabajadores distribuidos en las áreas físicas

## 🧠 Modelo Propuesto

### Problema Identificado (reunión #5)
- Confusión entre rol (acceso) y función (responsabilidad real)
- Roles básicos que NO reflejan la realidad operativa

### Solución Propuesta (reunión #5)
- Modelo híbrido:
  - Roles por área funcional
  - Permisos por acción

### Áreas Funcionales y Sub-roles (reunión #5)
- **Admisión** (opera en área física de Admisión)
- **Farmacia** (área operativa, no listada como área física pero funcional)
  - Químico
  - Técnico
- **Caja** (opera en área física de Caja)
- **Enfermería** (personal que opera en Hospitalización y SOP)
  - Licenciada
  - Técnico
- **Nutrición** (opera en área física de Nutrición)
- **Administración**
- **Sistemas**

### Tipos de Permisos (reunión #5)
- Crear
- Editar
- Aprobar
- Eliminar
- Ver

## 🔑 Decisiones Específicas por Rol

### Admisión (reunión #7)
- **Área física:** Admisión
- Permiso para cambiar habitación: ✅ Sí (aunque las habitaciones son gestionadas por Hospitalización)
- Actualización de matriz de permisos requerida

### Hospitalización - Personal de Enfermería (reunión #7)
- **Área física:** Hospitalización
- Responsabilidad: Administración de habitaciones y atención al paciente
- Permiso para cambiar habitación: ✅ Sí
- Consumo de productos de farmacia (reunión #5)

### Farmacia (reunión #7)
- **Área funcional:** Farmacia (opera independientemente)
- Químico:
  - Puede editar precios con actualización de utilidad
  - Gestión completa de inventario
  - Autorización de descuentos por vencimiento
- Técnico:
  - Operativo en ventas
  - Control básico de stock

### Caja (reunión #5)
- **Área física:** Caja
- Solo cobro

### Administrador (reunión #5)
- Separar:
  - Administrador negocio
  - Administrador técnico

### Gerencia/Administrador (reunión #6)
- Único con permiso para "Autorizar descuento"

## 🚨 Riesgos Identificados
- Acceso indebido
- Falta de control
- Resistencia al cambio (reunión #2)

## 📊 Matriz de Permisos Consolidada

| Rol Funcional | Área Física donde Opera | Cambio de Habitación | Edición de Precios | Autorizar Descuentos | Crear HC | Registrar Cobros |
|---------------|-------------------------|----------------------|-------------------|----------------------|----------|-----------------|
| Admisión | Admisión | ✅ Sí | ❌ No | ❌ No | ✅ Sí | ❌ No |
| Enfermería | Hospitalización / SOP | ✅ Sí | ❌ No | ❌ No | ❌ No | ❌ No |
| Químico | Farmacia | ❌ No | ✅ Sí | ✅ (solo por vencimiento) | ❌ No | ❌ No |
| Técnico Farmacia | Farmacia | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No |
| Caja | Caja | ❌ No | ❌ No | ❌ No | ❌ No | ✅ Sí |
| Gerencia/Admin | Administración | ❌ No | ❌ No | ✅ Sí (todos) | ❌ No | ✅ Sí |

## 🚀 Plan para Definición Completa (reunión #7)
El cliente solicita reuniones separadas, una por cada rol principal, para definirlos con profundidad:

| Rol | Área Funcional | Estado | Próxima Acción |
|-----|----------------|--------|----------------|
| Técnico de farmacia | Farmacia | ❌ No definido | Reunión #8 |
| Caja (cobranza) | Caja | ❌ No definido | Reunión #9 |
| Enfermería (hospitalización) | Hospitalización | ❌ No definido | Reunión #10 |
| Médico (firma digital, HC) | SOP / Hospitalización | ❌ No definido | Reunión #11 |
| Administrador del sistema | Sistemas | ❌ No definido | Reunión #12 |

## ✅ Acciones Pendientes
- Actualizar `rol_admision.md` → agregar permiso "Cambiar habitación = ✅"
- Actualizar `rol_farmacia_quimico_v2.md` con nuevas reglas
- Generar invitación para Reunión #8: Rol Técnico de Farmacia
- Continuar con reuniones específicas para cada rol
- Aclarar relación entre personal de Enfermería y área de Hospitalización
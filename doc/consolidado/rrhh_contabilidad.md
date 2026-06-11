# 👥 RR.HH y Contabilidad - Documento Consolidado

## 📌 Descripción General
Los módulos de RR.HH y Contabilidad son componentes importantes del sistema integral clínico, aunque con alcance limitado en el MVP.

## 👥 RR.HH (Recursos Humanos)

### Versión Mínima para MVP (reunión #6)
- Solo incluye:
  - Registro de trabajador: nombres, DNI, área, rol, fecha ingreso
  - Asignación de usuario del sistema
  - Relación trabajador ↔ usuario
- ✅ Esto permite que "todo usuario del sistema sea un trabajador registrado"

### No incluye en MVP:
- Planillas
- PLAME
- T-Registro
- CTS
- Vacaciones

### Pendientes para Fase 2 (reunión #6)
- RR.HH completo (planillas, PLAME)

### Estructura Organizacional (reunión #5)
- 40-41 trabajadores
- Áreas: Admisión, Farmacia, Caja, Enfermería, Nutrición, Gerencia, Sistemas

## 💰 Contabilidad

### Funcionalidades (reunión #3)
- Libros electrónicos
- Balances mensuales y anuales

### Alcance en MVP (reunión #6)
- Contabilidad: ❌ No incluida en MVP
- Reportes avanzados / SUNAT: ❌ No (Fase 3)

### Pendientes para Fase 2 (reunión #6)
- Contabilidad (libros electrónicos, balances)
- Reportes gerenciales avanzados
- Facturación electrónica automática (en MVP solo emisión manual)

## 📊 Problemas Identificados (reunión #4)
- SIG no funcional
- Compras administrativas subutilizado

## 🧠 Solución Propuesta (reunión #3)
- ERP modular:
  - Clínica
  - Farmacia
  - Caja
  - Contabilidad
  - RR.HH

## 🚀 Estrategia de Implementación
- El sistema debe construirse por fases (reunión #3)
- MVP clínico-farmacia-caja sin RR.HH completo, contabilidad ni nutrición (reunión #6)

## 🗂️ Alcance del MVP (reunión #6)

| Módulo | Incluido en MVP | Observación |
|--------|-----------------|-------------|
| RR.HH básico | ✅ Parcial | Solo registro de trabajadores (sin planillas, PLAME, CTS) |
| Nutrición | ❌ No | Fase posterior |
| Contabilidad | ❌ No | Fase 2 |
| Reportes avanzados / SUNAT | ❌ No | Fase 3 |

## ✅ Acciones Pendientes
- Registrar trabajadores para el MVP
- Planificar implementación de RR.HH completo para Fase 2
- Definir requerimientos para contabilidad en Fase 2
- Crear estrategia para migración de datos de RR.HH
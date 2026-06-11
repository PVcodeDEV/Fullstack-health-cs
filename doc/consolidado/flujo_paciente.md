# 🏥 Flujo del Paciente - Documento Consolidado

## 📌 Descripción General
El flujo del paciente es el proceso central que integra todos los módulos del sistema, desde la programación hasta el alta y facturación.

## 🏢 Áreas Físicas Involucradas
- Admisión
- Sala de Operaciones (SOP)
- Hospitalización (administra habitaciones)
- Caja

## 🔄 Flujo Operativo (reunión #2)
1. **Programación** (Admisión)
   - Validación de salas (A, B, C)
   - Datos: paciente, cirugía, habitación, anestesiólogo

2. **Admisión** (Área de Admisión)
   - Registro completo del paciente
   - Creación de cuenta
   - Registro de hora de ingreso

3. **Hospitalización** (Área de Hospitalización)
   - Asignación de habitación
   - Atención por personal de enfermería
   - Administración de medicamentos (vía Farmacia)

4. **Sala de Operaciones - SOP** (Área de SOP)
   - Paquetes quirúrgicos que incluyen:
     - Cirugía + tipo de habitación (individual/compartida)
     - Insumos
     - Enfermería
     - URPA
     - 1 día de hospitalización

5. **URPA** (parte de SOP)

6. **Retorno a Hospitalización** (Área de Hospitalización)
   - Continuidad de atención
   - Posible cambio de habitación (gestionado por Hospitalización)

7. **Alta** (coordinado entre Hospitalización y Admisión)

8. **Caja** (Área de Caja)
   - Cierre de cuenta
   - Generación de comprobantes

## 💊 Integración con Farmacia (reunión #3)
- Consumos manuales actualmente
- Necesidad de integración en tiempo real
- Nutrición solicita insumos a farmacia

## 🧾 Cuentas del Paciente (reunión #1)
- Flujos:
  - Creación de cuenta
  - Asignación de paquete
  - Cargos adicionales
  - Pago final
- Problemas:
  - Cobros manuales para:
    - Insumos extra
    - Días extra
    - Cambio de habitación
  - Falta de automatización

## 📊 Problemas Identificados
- Procesos manuales críticos (reunión #1)
- Falta de integración (reunión #3)
- Uso de papel (reunión #2)
- Baja trazabilidad (reunión #2)
- Riesgos clínicos (reunión #2)

## 🚨 Casos Específicos

### Cambio de Habitación (reunión #7)
- **Responsable del área:** Hospitalización
- Permisos para ejecutar el cambio:
  - Admisión: ✅ Puede cambiar habitación
  - Enfermería (personal de Hospitalización): ✅ Puede cambiar habitación
- Impacto en pricing:
  - Clínica aplica +50% sobre farmacia (reunión #3)
  - Necesidad de recalcular cargos

### Cargos Adicionales (reunión #1)
- Insumos extra
- Días extra
- Cambio de habitación
- Problema: Cobros manuales, falta de automatización

## 🚀 Solución Propuesta
- Sistema modular integrado:
  - Pacientes
  - Hospitalización (incluye gestión de habitaciones)
  - SOP
  - Cuentas clínicas
  - Farmacia
  - Inventario
  - Facturación
- Motor de reglas para automatización de cobros

## ✅ Acciones Pendientes
- Mapear flujos detallados por área física
- Priorizar con RICE (reunión #1)
- Definir reglas para cargos adicionales
- Implementar integración en tiempo real entre módulos
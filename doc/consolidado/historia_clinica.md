# 📄 Historia Clínica Digital - Documento Consolidado

## 📌 Descripción General
La Historia Clínica Electrónica (HCE) es el núcleo del sistema integral clínico, diseñada para reemplazar los documentos físicos y mejorar la trazabilidad y eficiencia en la atención al paciente.

## 🏢 Áreas Físicas donde se Genera Documentación Clínica
- Admisión (documentos iniciales)
- Sala de Operaciones - SOP (reportes operatorios)
- Hospitalización (evoluciones, notas de enfermería, epicrisis)

## 🏥 Documentos Clínicos (reunión #2)
- Filiación
- Consentimientos
- Historia clínica
- Evolución
- Reporte operatorio
- Epicrisis
- Kardex
- Notas de enfermería

## 🔄 Flujo del Paciente y Generación de Documentos (reunión #2)
1. **Programación** (Admisión)
   - Validación de salas (A, B, C)
   - Datos: paciente, cirugía, habitación, anestesiólogo

2. **Admisión** (Área de Admisión)
   - Registro completo del paciente
   - Creación de cuenta
   - Registro de hora de ingreso
   - **Documentos generados:** Filiación, Consentimientos, HC inicial

3. **Hospitalización** (Área de Hospitalización)
   - **Documentos generados:** Evoluciones, Notas de enfermería, Kardex

4. **SOP** (Área de Sala de Operaciones)
   - **Documentos generados:** Reporte operatorio

5. **URPA** (parte de SOP)

6. **Retorno a Hospitalización** (Área de Hospitalización)
   - **Documentos generados:** Evoluciones post-operatorias, Notas de enfermería

7. **Alta** (coordinado entre Hospitalización y Admisión)
   - **Documentos generados:** Epicrisis

8. **Caja** (Área de Caja)

## ⚠️ Problemas Identificados (reunión #2)
- Uso de papel
- Baja trazabilidad
- Riesgos clínicos
- Flujo desconectado con otras áreas (reunión #4)

## 🧠 Solución Propuesta (reunión #2)
- Historia Clínica Electrónica (HCE):
  - Formularios digitales
  - Firmas digitales
  - Acceso por roles

## 🔐 Módulo de Admisión (reunión #4)
### Funcionalidades
- Crear historia clínica
- Búsqueda por HC / DNI / nombre
- Creación de cuenta
- Solicitud de hospitalización
- Asignación de habitación (gestionado por Hospitalización)
- Transferencia de habitación (ejecutado por Hospitalización)
- Registro de diagnóstico (CIE-10)
- Registro de médicos (CMP, RNE, especialidad)

### Problemas
- Dietas no usadas
- Flujo desconectado con otras áreas

## 🔐 Firma Digital Interna (reunión #6)

### Implementación
- No se usa firma legal externa
- Cada usuario (médico, enfermería, químico) tiene clave personal
- Al firmar documento electrónico:
  - Se solicita clave
  - Se registra: `usuario_id`, `timestamp`, `hash(documento)`, `IP`
  - La "firma" es un sello de integridad + no repudio interno

### Documentos que Aplican en MVP
- Historia clínica (Admisión)
- Reporte operatorio (SOP)
- Epicrisis (Hospitalización)
- Evoluciones (Hospitalización)
- Notas de enfermería (Hospitalización)

## 🗄️ Migración de Datos (reunión #6)
- Historias clínicas activas: ✅ Sí (digitalizadas al nuevo formato)
- Pacientes activos: ✅ Sí (solo datos filiación)

## 🚨 Riesgos Identificados (reunión #2)
- Resistencia al cambio
- Cumplimiento legal
- Firma digital

## ❓ Preguntas Abiertas (reunión #2)
- Firma digital legal
- Retención de datos
- Acceso remoto

## 🚀 Recomendación (reunión #4)
- Rediseño desde cero
- Core: clínica + farmacia + caja

## ✅ Acciones Pendientes
- Definir reglas para firma digital
- Planificar migración de historias clínicas activas
- Definir estructura de datos para HC digital
- Reunión específica para Rol Médico (Reunión #11)
- Mapear qué documentos se generan en cada área física
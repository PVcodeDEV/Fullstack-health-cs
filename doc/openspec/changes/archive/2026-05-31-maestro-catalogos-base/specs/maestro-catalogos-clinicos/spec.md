# Spec: maestro-catalogos-clinicos

## Overview

Clinical catalog tables used by admissions, hospitalization, pharmacy, and billing modules. Defines medical specialties, patient types, care types, pharmaceutical forms, administration routes, and room types.

## Requirements

- R-001: `EspecialidadMedica` MUST store `codigo`, `nombre`, `abreviatura` (e.g., "CG" for Cirugía General), and `activo` flag.
- R-002: `TipoPaciente` MUST store `nombre` and `codigo`. Seeded values: Asegurado Essalud, SIS, Particular, Convenio. This field SHALL affect pricing calculations in the `caja` module.
- R-003: `TipoAtencion` MUST store `nombre`, `codigo`, and `requiere_habitacion` boolean flag. Seeded: Consulta Externa (false), Emergencia (false), Hospitalización (true), SOP (false).
- R-004: `ViaAdministracion` MUST store `nombre` and `codigo`. Seeded: Oral, IV, IM, SC, Tópico, Inhalatorio, Rectal, Oftálmico, Ótico.
- R-005: `FormaFarmaceutica` MUST store `nombre`, `codigo`, and `requiere_preparacion` boolean flag. Seeded: Tableta, Cápsula, Jarabe, Inyectable, Crema, Ungüento, Supositorio, Gotas, Polvo, Solución.
- R-006: `TipoHabitacion` MUST store `nombre`, `codigo`, `tarifa_base` (BigDecimal), `capacidad` (Integer), and `activo`. Seeded: Individual (1), Compartida(2), Compartida(3), Suite (1).
- R-007: Catalog values are NOT PII. `tarifa_base` is financial data and MUST be audited on modification.
- R-008: All entities MUST support `activo` flag for soft delete. Referenced records MUST NOT be deactivated.

## Scenarios

### SC-001: Create medical specialty
When I POST EspecialidadMedica with `codigo: "CG"`, `nombre: "Cirugía General"`, `abreviatura: "CG"`
Then the system creates the record with `activo: true` and returns 201

### SC-002: List room types with pricing
When I GET `/api/v1/maestro/tipo-habitacion`
Then each result includes `tarifa_base`, `capacidad`, and `activo` fields

### SC-003: Protect referenced specialty deletion
Given EspecialidadMedica `codigo: "CG"` is assigned to at least one doctor
When I DELETE `/api/v1/maestro/especialidad-medica/CG`
Then the system returns 409 Conflict

### SC-004: Filter care types by room requirement
When I GET `/api/v1/maestro/tipo-atencion?requiere_habitacion=true`
Then the system returns only Hospitalización

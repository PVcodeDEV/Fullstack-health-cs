# Spec: maestro-catalogos-organizacion

## Overview

Organizational catalogs defining the clinic's functional structure, insurers, supply categories, and clinical document types. Used for user assignment, billing contracts, inventory classification, and medical record management.

## Requirements

- R-001: `AreaFuncional` MUST store `nombre`, `codigo`, and `es_area_fisica` boolean flag. Seeded: Admisión, SOP, Hospitalización, Farmacia, Caja, Enfermería, Nutrición, Administración, Sistemas.
- R-002: `Aseguradora` MUST store `codigo`, `nombre`, `tipo` (PUBLICO/PRIVADO), and `contrato_vigente` boolean. Seeded: Essalud (público), SIS (público), and major EPS (privado).
- R-003: `CategoriaInsumo` MUST support hierarchical categories via self-referencing foreign key (`categoria_padre_id`). Seeded top-level: Medicamento, Material Médico, Insumo Quirúrgico, Material de Escritorio, Reactivo, Otros.
- R-004: `TipoDocumentoClinico` MUST store `nombre`, `codigo`, and `requiere_firma` boolean. Seeded: Historia Clínica, Evolución, Reporte Operatoria, Epicrisis, Nota de Enfermería, Kardex, Consentimiento Informado, Receta.
- R-005: Insurer contract status (`contrato_vigente`) MAY be updated independently. Historical records MUST be preserved (no hard delete of insurers with billing history).
- R-006: Clinical document types requiring digital signature (`requiere_firma = true`) MUST trigger signature workflow in the `seguridad` module.

## Scenarios

### SC-001: Create functional area
When I POST AreaFuncional with `codigo: "ADM"`, `nombre: "Admisión"`, `es_area_fisica: true`
Then the system creates the record and returns 201

### SC-002: Query subcategories
Given `CategoriaInsumo` "Medicamento" exists with child "Analgésicos"
When I GET `/api/v1/maestro/categoria-insumo?padre=Medicamento`
Then the results include "Analgésicos" as a child of "Medicamento"

### SC-003: Filter clinical documents by signature requirement
When I GET `/api/v1/maestro/tipo-documento-clinico?requiere_firma=true`
Then the system returns documents that require digital signature (Receta, Consentimiento Informado, Reporte Operatoria, Epicrisis)

### SC-004: Protect insurer with history
Given Aseguradora "Essalud" has billing records in `caja`
When I DELETE `/api/v1/maestro/aseguradora/Essalud`
Then the system returns 409 Conflict — insurer is protected

### SC-005: List physical areas
When I GET `/api/v1/maestro/area-funcional?es_area_fisica=true`
Then the system returns only areas where physical presence is required (Admisión, SOP, Hospitalización, Farmacia, Caja, Enfermería)

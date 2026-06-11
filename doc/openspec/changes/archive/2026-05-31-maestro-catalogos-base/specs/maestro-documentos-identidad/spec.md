# Spec: maestro-documentos-identidad

## Overview

Identity document types and civil status catalogs aligned with SUNAT and RENIEC standards. These are foundational reference tables required by every person-related entity across all modules (patients, employees, providers).

## Requirements

- R-001: The system MUST provide CRUD operations for `TipoDocumentoIdentidad` with the following SUNAT-aligned entries: DNI (8 digits, Peruvian adults), CE (12 digits, foreigners), Pasaporte (variable length, max 20), RUC (11 digits, companies/professionals).
- R-002: The system MUST provide CRUD operations for `EstadoCivil` with the following RENIEC-aligned entries: Soltero, Casado, Divorciado, Viudo, Conviviente.
- R-003: Each `TipoDocumentoIdentidad` MUST store its `codigo_sunat`, `nombre`, `longitud_minima`, `longitud_maxima`, and `activo` flag.
- R-004: The system MUST validate document number length against the type-specific `longitud_minima`/`longitud_maxima` on write.
- R-005: The system MUST validate DNI check digit using módulo 11 algorithm before persisting.
- R-006: A `TipoDocumentoIdentidad` or `EstadoCivil` MUST NOT be physically deleted if referenced by another entity. The system MUST use soft delete via `activo` flag.
- R-007: The system MUST reject creation of duplicate `codigo_sunat` values for `TipoDocumentoIdentidad`.
- R-008: Document numbers MUST be treated as PII (Ley 29733) and MUST NOT appear in plain-text logs or API error messages.

## Scenarios

### SC-001: Register a new document type
Given the maestro catalog admin endpoint
When I POST a TipoDocumentoIdentidad with `codigo_sunat: "01"`, `nombre: "DNI"`, `longitud_minima: 8`, `longitud_maxima: 8`
Then the system creates the record with `activo: true` and returns a 201 response

### SC-002: Reject duplicate document type code
Given TipoDocumentoIdentidad with `codigo_sunat: "01"` already exists
When I POST another TipoDocumentoIdentidad with `codigo_sunat: "01"`
Then the system returns 409 Conflict and the duplicate is not created

### SC-003: Reject DNI with invalid check digit
Given the DNI document type with módulo 11 validation
When I POST a person with `numero_documento: "12345678"` (invalid check digit)
Then the system returns 422 Unprocessable Entity with validation error

### SC-004: Protected deletion of referenced document type
Given TipoDocumentoIdentidad with `id: 1` (DNI) is referenced by at least one patient
When I DELETE `/api/v1/maestro/tipo-documento-identidad/1`
Then the system returns 409 Conflict and the record remains in the database

### SC-005: Soft delete an unreferenced document type
Given TipoDocumentoIdentidad with `id: 5` (Pasaporte) has zero references
When I DELETE `/api/v1/maestro/tipo-documento-identidad/5`
Then the system sets `activo: false` and returns 200 OK

### SC-006: Register civil status
When I POST EstadoCivil with `nombre: "Conviviente"` and `codigo_reniec: "05"`
Then the system creates the record with `activo: true` and returns 201

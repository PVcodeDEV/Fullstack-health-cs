# Spec: maestro-ubigeo

## Overview

Three-tier normalized geographic division (Departamento → Provincia → Distrito) using RENIEC official codes. Powers all address fields across the system. Data is read-dominated after initial import.

## Requirements

- R-001: The system MUST store three geographic levels: `UbigeoDepartamento` (2-digit RENIEC code), `UbigeoProvincia` (4-digit code inheriting departamento prefix), `UbigeoDistrito` (6-digit code inheriting provincia prefix).
- R-002: Each entity MUST contain `codigo` (PK), `nombre`, and `activo` flag.
- R-003: The system MUST seed all official RENIEC divisions (~24 departments, ~196 provinces, ~1874 districts) via Flyway migration.
- R-004: The system MUST provide hierarchical query: list provinces filtered by department, districts filtered by province.
- R-005: A geographic level MUST NOT be soft-deleted if it has child records at the next level.
- R-006: The system SHOULD expose a flat search endpoint (`/api/v1/maestro/ubigeo/search?q=`) for autocomplete across all levels.
- R-007: Ubigeo data is public (not PII) and MAY be cached aggressively.

## Scenarios

### SC-001: Query departments
When I GET `/api/v1/maestro/ubigeo/departamentos`
Then the system returns all 24 departments sorted by nombre

### SC-002: Filter provinces by department
Given a known department `codigo: "15"` (Lima)
When I GET `/api/v1/maestro/ubigeo/provincias?departamento=15`
Then the system returns only provinces whose `codigo` starts with "15"

### SC-003: Filter districts by province
Given a known province `codigo: "1501"` (Lima Province)
When I GET `/api/v1/maestro/ubigeo/distritos?provincia=1501`
Then the system returns only districts whose `codigo` starts with "1501"

### SC-004: Protect parent deletion
Given Departamento `codigo: "15"` has at least one child provincia
When I DELETE `/api/v1/maestro/ubigeo/departamentos/15`
Then the system returns 409 Conflict and does not modify the record

### SC-005: Bulk import from seed
Given the Flyway migration V1__seed_ubigeo.sql executes
Then the database contains exactly 24 departamentos, ~196 provincias, and ~1874 distritos

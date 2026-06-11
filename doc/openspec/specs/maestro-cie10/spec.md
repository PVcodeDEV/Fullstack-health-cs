# Spec: maestro-cie11

## Overview

CIE-11 (Clasificación Internacional de Enfermedades, 11th edition) diagnostic codes catalog. Contains ~17k records imported via Flyway migration. Used by clinical modules for diagnosis coding.

## Requirements

- R-001: The `CIE11Diagnostico` entity MUST contain `cie_id` (BIGSERIAL PK), `cie_codigo` (VARCHAR(8) UNIQUE NOT NULL, e.g., "1A00.0"), `cie_descripcion` (VARCHAR(500) NOT NULL), `categoria` (letter A–Z), `sexo_aplicable` (M/F/AMBOS), `edad_minima` (nullable Integer), `edad_maxima` (nullable Integer), and `version` (VARCHAR(10), default "CIE-11").
- R-002: The system MUST seed all ~17k CIE-11 codes via Flyway migration (`V9__seed_cie11.sql`).
- R-003: The system MUST support search by code OR description using ILIKE (or equivalent) for autocomplete.
- R-004: Existing CIE-11 codes MUST NOT be modifiable after creation. No editing allowed — codes are normative OMS catalog.
- R-005: CIE-11 codes do NOT use soft delete. ALL codes are always visible. They MUST be ordered by usage frequency (most used in diagnoses and surgical packages first).
- R-006: Diagnostic codes are NOT PII and MAY be cached.

## Scenarios

### SC-001: Search by partial code
When I GET `/api/v1/maestro/cie11?q=1A00`
Then the system returns all codes starting with "1A00" (e.g., "1A00.0", "1A00.1") sorted by frequency desc, then by codigo

### SC-002: Search by description fragment
When I GET `/api/v1/maestro/cie11?q=diabetes`
Then the system returns all codes whose description contains "diabetes" (case-insensitive), ordered by usage frequency

### SC-003: No modification of existing codes
Given CIE11Diagnostico with `cie_codigo: "1A00.0"` exists
When I PUT/PATCH `/api/v1/maestro/cie11/1`
Then the system returns 405 Method Not Allowed — codes are normative and immutable

### SC-004: Filter by sexo_aplicable
When I GET `/api/v1/maestro/cie11?sexo=M`
Then the system returns only codes where `sexo_aplicable` is "M" or "AMBOS", ordered by frequency

### SC-005: All codes always visible
When I GET `/api/v1/maestro/cie11?page=0&size=50`
Then the system returns all codes including inactive/infrequent ones — no soft delete filter applies

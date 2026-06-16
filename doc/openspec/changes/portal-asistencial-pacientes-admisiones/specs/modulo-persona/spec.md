# Delta for modulo-persona

## ADDED Requirements

### Requirement: PER-009 — Portal search endpoint response shape

The search endpoint `GET /api/v1/personas` MUST return a response payload containing at minimum `id`, `numeroDocumento`, `nombres`, `apellidos` for each matching Persona. This enables the Portal Asistencial Pacientes view to render search results and link to detail.

#### Scenario: PER-009-1 — Search by DNI returns minimal fields

- GIVEN a Persona with `id: 1`, `numeroDocumento: "12345678"`, `nombres: "Juan"`, `apellidos: "Pérez"`
- WHEN GET `/api/v1/personas?numeroDocumento=12345678`
- THEN response body contains array with object `{ "id": 1, "numeroDocumento": "12345678", "nombres": "Juan", "apellidos": "Pérez" }`
- AND response MAY include additional fields but MUST include these four

#### Scenario: PER-009-2 — Search by partial name returns minimal fields

- GIVEN Personas with `nombres: "Juan"`, `apellidos: "Pérez"` and `nombres: "Juana"`, `apellidos: "Gómez"`
- WHEN GET `/api/v1/personas?nombres=Juan`
- THEN response body contains array of objects each with `id`, `numeroDocumento`, `nombres`, `apellidos`
- AND inactive Personas are excluded per PER-008

#### Scenario: PER-009-3 — Empty results returns empty array

- GIVEN no Personas match the search criteria
- WHEN GET with non-matching parameters
- THEN response body is empty array `[]` with status 200
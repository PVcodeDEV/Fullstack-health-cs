# ERP Clínico — Project Instructions

## Stack

- **Backend**: Java 25 + Spring Boot 4.0.0 + Maven (single-module, package-per-module)
- **Frontend**: Thymeleaf + HTMX + Tailwind CSS (via CLI build)
- **Database**: PostgreSQL 18 (prod), H2 (dev profile)
- **Base package**: `com.clinica`

## Architecture

Modular monolith with package-per-module under `com.clinica`:

| Module | Package | Purpose |
|--------|---------|---------|
| `maestro` | `com.clinica.maestro` | Master/catalog tables |
| `seguridad` | `com.clinica.seguridad` | Auth, users, roles, permissions |
| `clinica` | `com.clinica.clinica` | Core clinical (admission, hospitalization, SOP) |
| `farmacia` | `com.clinica.farmacia` | Pharmacy, inventory, warehouse |
| `caja` | `com.clinica.caja` | Cashier, billing, invoices |
| `rrhh` | `com.clinica.rrhh` | HR, payroll, basic HR management |

Each module follows the layering convention: `entity/` → `repository/` → `service/` → `dto/` → `controller/`.

## Workflow

- **Use SDD (Spec-Driven Development)** for all changes. Run `/sdd-new` to start a new change, `/sdd-continue` to advance phases, `/sdd-ff` to fast-forward.
- SDD phases: `proposal → spec → design → tasks → apply → verify → archive`
- Artifacts live in `doc/openspec/`

## Frontend Build

- **Never use npm.** Use `bun` for all frontend package management.
- Tailwind CLI builds CSS from `frontend/` into `backend/src/main/resources/static/css/output.css`.
- `bun run build` — build CSS
- `bun run watch` — watch mode

## Conventions

- Commits in conventional commits format (`feat:`, `fix:`, `chore:`, etc.)
- Always run `mvn compile` before committing to verify the build
- Tests: JUnit 5 + Mockito + Spring Boot Test via `mvn test`
- Error responses follow RFC 9457 (ProblemDetail)
- Security: permissive during bootstrap, enforce in `seguridad` module
- The project is greenfield — no legacy migration

## Deployment

- **Production**: HPE ProLiant MicroServer Gen11, Windows Server 2022, 16 GB RAM
- **Clients**: Windows 11, 8 GB RAM, browser-only
- Consider Windows Server constraints in design decisions

## Data Privacy

Comply with Peruvian Ley de Protección de Datos Personales (Law 29733).
Mark PII fields and document data retention requirements in specs.

## Skills (uso contextual)

El orquestador carga skills del registry (`.atl/skill-registry.md`) según el contexto. Las skills project-specific relevantes:

| Contexto | Skills a cargar |
|----------|----------------|
| Backend Spring (entidades, servicios, DTOs) | `backend-layering`, `spring-boot-backend` |
| Thymeleaf templates (fragments, formularios) | `thymeleaf` |
| SQL / migraciones / consultas | `postgres-best-practices`, `postgres-indexes` |
| TypeScript / config frontend | `typescript-patterns` |
| Angular (si se incorpora) | `angular-cli` |
| Spring CLI (scaffolding rápido) | `spring-cli` |
| PRs grandes (+400 líneas) | `chained-pr`, `work-unit-commits` |
| PR creation / revisión | `branch-pr`, `comment-writer` |
| Issues / bugs | `issue-creation` |
| Revisión adversarial | `judgment-day` |
| Documentación técnica | `cognitive-doc-design` |

## Reglas de Dominio

- **Persona y tipos de documento**: Cuando se crea un paciente, el DNI puede consultarse vía API externa (RENIEC). Para Carnet de Extranjería (CE) y Pasaporte, los datos se ingresan manualmente — no hay API disponible para esos tipos.
- **Persona como base**: Persona es la entidad base compartida. Paciente, Trabajador, Médico y Cliente (para comprobante de pago) son especializaciones de Persona. La validación de dígito verificador (módulo 11) aplica al crear una Persona con DNI.

## SDD Init Status

SDD initialized with OpenSpec at `doc/openspec/`. Bootstrap change (`proyecto-bootstrap`) archived. Ready for domain module implementation.

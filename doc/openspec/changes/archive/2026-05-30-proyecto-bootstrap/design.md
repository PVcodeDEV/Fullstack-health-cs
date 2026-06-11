# Design: Proyecto Bootstrap

## Technical Approach

Single-module Maven project with package-per-module layout. All business modules live under `com.clinica` with `entity/repository/service/dto/controller` subpackages. Frontend is a standalone `frontend/` directory with Tailwind CLI building CSS into backend static resources. Dev profile uses H2 for zero-config startup; PostgreSQL config ready for prod. Flyway, Actuator, and Security skeleton in place but permissive — no business logic enforced yet.

## Architecture Decisions

### Decision: Maven single-module vs multi-module

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Single POM with package-per-module | Simpler build, faster iteration, one `target/` | **Chosen** — bootstrap phase has no reason to split JARs |
| Multi-module with parent POM | Independent builds, clearer boundaries | Overkill until modules need separate deployment |
| Gradle | Faster builds, Kotlin DSL | Team convention Maven + Spring Boot 4 archetype |

### Decision: Base package `com.clinica`

Maps directly to the ERP Clinico domain. No company or client prefix — avoids rename churn if the project is reused.

### Decision: H2 for dev, PostgreSQL for prod

| Option | Tradeoff | Decision |
|--------|----------|----------|
| H2 in-memory dev + PG prod | Zero setup, fast CI, separate Flyway scripts | **Chosen** — proposal states no Docker dependency for dev |
| PG for both (Testcontainers) | Production-identical, requires Docker | Adds Docker as hard dependency — rejected for bootstrap |

### Decision: Standalone `frontend/` directory

Not inside `backend/src/main/resources/static/` because Tailwind/node_modules are development-only. Output CSS is built to `backend/src/main/resources/static/css/output.css` and committed.

### Decision: Permissive SecurityFilterChain

All endpoints permitAll(). This is intentional — no auth UI exists yet. BCryptPasswordEncoder bean is declared. Security will be enforced when `seguridad/` module is built.

## Module Structure

```
backend/
├── pom.xml
└── src/main/java/com/clinica/
    ├── ClinicaApplication.java          ← @SpringBootApplication
    ├── config/                          ← Shared config (security, error handling, CORS)
    │   ├── SecurityConfig.java
    │   ├── GlobalExceptionHandler.java
    │   └── WebConfig.java               ← CORS
    ├── maestro/                         ← Master/catalog tables (future)
    │   └── .gitkeep
    ├── seguridad/                       ← Auth & user mgmt (future)
    │   └── .gitkeep
    ├── clinica/                         ← Core clinical (future)
    │   └── .gitkeep
    ├── farmacia/                        ← Pharmacy / inventory (future)
    │   └── .gitkeep
    ├── caja/                            ← Cashier / billing (future)
    │   └── .gitkeep
    └── rrhh/                            ← HR (future)
        └── .gitkeep
```

## Data Flow (Bootstrap — no business data)

```
User → Browser →  localhost:8080
                     │
                     ├─ SecurityFilterChain (permitAll)
                     ├─ DispatcherServlet
                     │    └─ Thymeleaf controller → index.html
                     ├─ Actuator /health (liveness, readiness)
                     └─ H2 console (dev only)
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `backend/pom.xml` | Create | Spring Boot 4 parent, dependencies (web, jpa, security, thymeleaf, flyway, actuator, lombok, devtools, h2, postgresql, validation, thymeleaf-layout-dialect) |
| `backend/src/main/java/com/clinica/ClinicaApplication.java` | Create | `@SpringBootApplication` entry point |
| `backend/src/main/java/com/clinica/config/SecurityConfig.java` | Create | `SecurityFilterChain` (permitAll), `BCryptPasswordEncoder` bean |
| `backend/src/main/java/com/clinica/config/GlobalExceptionHandler.java` | Create | `@RestControllerAdvice` with `ProblemDetail` for validation + not-found |
| `backend/src/main/java/com/clinica/config/WebConfig.java` | Create | CORS configuration (permissive for dev) |
| `backend/src/main/resources/application.yml` | Create | Dev profile (H2, debug log, devtools), prod profile (PG, prod settings) |
| `backend/src/main/resources/templates/layouts/main.html` | Create | Layout Dialect base template with header, footer, content, scripts fragments |
| `backend/src/main/resources/templates/fragments/header.html` | Create | Navigation fragment with logo |
| `backend/src/main/resources/templates/fragments/footer.html` | Create | Footer fragment with dynamic copyright year |
| `backend/src/main/resources/templates/index.html` | Create | Landing page extending main layout |
| `backend/src/main/resources/static/.gitkeep` | Create | Placeholder for static assets |
| `backend/src/main/resources/db/migration/.gitkeep` | Create | Flyway migrations directory |
| `backend/src/main/java/com/clinica/maestro/.gitkeep` | Create | Module placeholder |
| `backend/src/main/java/com/clinica/seguridad/.gitkeep` | Create | Module placeholder |
| `backend/src/main/java/com/clinica/clinica/.gitkeep` | Create | Module placeholder |
| `backend/src/main/java/com/clinica/farmacia/.gitkeep` | Create | Module placeholder |
| `backend/src/main/java/com/clinica/caja/.gitkeep` | Create | Module placeholder |
| `backend/src/main/java/com/clinica/rrhh/.gitkeep` | Create | Module placeholder |
| `frontend/package.json` | Create | Tailwind CLI + PostCSS dev dependency |
| `frontend/tailwind.config.js` | Create | Content paths for Thymeleaf templates |
| `frontend/postcss.config.js` | Create | PostCSS with tailwindcss + autoprefixer |
| `frontend/src/input.css` | Create | `@tailwind base/components/utilities` + custom styles |
| `database/scripts/.gitkeep` | Create | SQL scripts directory |
| `database/seed/.gitkeep` | Create | Seed data directory |

## Interfaces / Contracts

No API contracts yet — this is infrastructure only. The `GlobalExceptionHandler` establishes the error contract format:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/v1/...",
  "errors": [
    { "field": "nombre", "message": "must not be blank" }
  ]
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Build | `mvn compile` succeeds | Manual verification per success criteria |
| Startup | `mvn spring-boot:run` starts | Verify via Actuator `/health` |
| CSS | `npm run build` produces valid CSS | Manual — verify output file exists |

No unit tests this phase — testing infrastructure is established but no business logic exists to test. Testing capabilities will be configured in a later change.

## Migration / Rollout

No migration required — this is initial project setup. Rollback: delete `backend/`, `frontend/`, `database/` directories.

## Open Questions

- [ ] Confirm Spring Boot 4.0.x GA artifact coordinates (spring-boot-starter-parent version)
- [ ] Verify Jakarta namespace vs javax — Spring Boot 4 uses Jakarta EE 11
- [ ] Confirm `thymeleaf-layout-dialect` coordinates for Spring Boot 4

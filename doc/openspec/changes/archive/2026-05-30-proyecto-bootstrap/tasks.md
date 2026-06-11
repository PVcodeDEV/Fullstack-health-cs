# Tasks: Proyecto Bootstrap

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 400–480 |
| 400-line budget risk | Medium |
| Chained PRs recommended | No |
| Suggested split | Single PR (bootstrap is atomic) |
| Delivery strategy | ask-on-risk |
| Chain strategy | size-exception |

Decision needed before apply: Yes
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: Medium

Bootstrap is inherently atomic — 22 files, all foundational. Splitting would leave project non-building. Single PR with `size:exception`.

## Phase 1: Project Scaffolding

- [x] 1.1 Create `pom.xml` — Spring Boot 4 parent, all deps (web, jpa, security, thymeleaf, actuator, flyway, lombok, devtools, h2, postgresql, validation, jackson, thymeleaf-layout-dialect)
- [x] 1.2 Create `ClinicaApplication.java` — `@SpringBootApplication`

## Phase 2: Backend Configuration

- [x] 2.1 Create `application.yml` — dev (H2) + prod (PG) profiles, Actuator health probes, Flyway enabled, port 8080
- [x] 2.2 Create `db/migration/.gitkeep` — Flyway migrations directory

## Phase 3: Security & Error Handling

- [x] 3.1 Create `config/SecurityConfig.java` — permitAll SecurityFilterChain + BCryptPasswordEncoder bean
- [x] 3.2 Create `config/GlobalExceptionHandler.java` — `@RestControllerAdvice` with ProblemDetail (RFC 9457)
- [x] 3.3 Create `config/WebConfig.java` — CORS configuration for localhost origins

## Phase 4: Frontend Build & Templates

- [x] 4.1 Create `frontend/package.json` — Tailwind CLI + PostCSS + autoprefixer
- [x] 4.2 Create `frontend/tailwind.config.js` — content paths pointing to backend templates
- [x] 4.3 Create `frontend/postcss.config.js` — tailwindcss + autoprefixer plugins
- [x] 4.4 Create `frontend/src/input.css` — Tailwind directives (base, components, utilities) + custom styles
- [x] 4.5 Create `templates/layouts/main.html` — Layout Dialect base with header/footer fragments
- [x] 4.6 Create `templates/fragments/header.html` — nav fragment with logo and placeholder links
- [x] 4.7 Create `templates/fragments/footer.html` — footer fragment with dynamic year parameter
- [x] 4.8 Create `templates/index.html` — landing page extending main layout with `th:text` welcome message
- [x] 4.9 _Also created: `static/.gitkeep` placeholder_

## Phase 5: Module Stubs

- [x] 5.1 Create `.gitkeep` in 6 modules: maestro, seguridad, clinica, farmacia, caja, rrhh
- [x] 5.2 Create `.gitkeep` in `database/scripts/`, `database/seed/`, and `backend/src/test/java/com/clinica/`

## Phase 6: Verification

- [x] 6.1 `mvn compile` succeeds — ✓ BUILD SUCCESS (Spring Boot 4.0.0 / Java 25 / 4 source files)
- [ ] 6.2 `npm run build` produces CSS in `static/css/` — requires `npm install` in frontend/
- [ ] 6.3 `mvn spring-boot:run` — Actuator `/health` returns 200 — requires running server

# Proposal: Proyecto Bootstrap

## Intent

Greenfield clinic in Peru (~40 employees, Windows Server 2022). Zero build files exist. This change lays the foundation — project structure, build chains, and basic configuration — so future modules can be developed incrementally.

## Scope

### In Scope
- **Backend**: Spring Boot 4 + Maven skeleton with modular packages (maestro, seguridad, clinica, farmacia, caja, rrhh)
- **Frontend**: Tailwind CSS build chain (package.json, tailwind.config.js, input.css → output to `backend/src/main/resources/static/`)
- **Database**: `database/` directory with seed scripts placeholder
- **Config**: `application.yml` (dev profile), `SecurityFilterChain` skeleton, Actuator health probes
- **Layering convention**: entity/repository/service/dto/controller per module package

### Out of Scope
- Business logic in any module
- Database schema or Flyway migrations
- Docker/containerization
- CI/CD pipeline
- Integration with RENIEC/SUNAT or any external API

## Capabilities

### New Capabilities
None — bootstrap creates no spec-level behavior (pure infrastructure).

### Modified Capabilities
None — no existing specs to modify.

## Approach

1. Generate Spring Boot 4 project skeleton via Maven archetype, strip sample code
2. Organize `com.clinica` packages: `config/` (shared config), then one package per module with layered subpackages
3. Add Flyway + Spring Security + Actuator dependencies in `pom.xml`
4. Write `application.yml` with dev profile (H2 for dev, PostgreSQL config ready for prod)
5. Create `SecurityFilterChain` bean permitting all for now (enforced later)
6. Frontend: standalone `package.json` with Tailwind CLI, output CSS built to backend's `static/` directory
7. Database: empty `seed/` and `scripts/` directories with `.gitkeep`

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `backend/` | New | Maven project with modular package structure |
| `frontend/` | New | Tailwind CSS build chain |
| `database/` | New | Seed scripts directory |
| `doc/openspec/` | Modified | Change folder added |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Java 25 + Spring Boot 4 incompatibility | Medium | Verify Spring Boot 4 GA supports Java 25 before committing |
| Windows Server tooling gaps (Maven, Node) | Medium | Test build on target OS during bootstrap |
| Module package naming conflicts later | Low | Package-per-module with clear `com.clinica.<module>` prefix |

## Rollback Plan

Delete `backend/`, `frontend/`, and `database/` directories. No data loss — this is all skeleton code.

## Dependencies

- Spring Boot 4.0.x must be GA and compatible with Java 25
- Node.js 22+ for Tailwind CLI (developer workstation)

## Success Criteria

- [ ] `mvn compile` succeeds from `backend/`
- [ ] `npm run build` produces valid CSS in backend resources
- [ ] `mvn spring-boot:run` starts with dev profile, Actuator `/health` responds 200

# Proposal: Módulo Seguridad — Auth, Authorization & API Security

## Intent

ERP Clínico has zero auth — all endpoints `permitAll()`. Patient PII (Ley 29733) makes this a legal risk. Build `seguridad` module with dual auth (formLogin for browser, JWT for API), RBAC, and runtime API config storage.

## Scope

### In Scope
- Usuario entity (FK→Persona, FK→Trabajador) + service + AuthController
- Role & Permission entities with many-to-many mappings
- Dual auth: formLogin (session) for browser; `oauth2ResourceServer.jwt()` for `/api/**`
- 6 DB tables in V12 migration (tb_usuarios, tb_roles, tb_permisos, tb_roles_permisos, tb_usuarios_roles, tb_configuracion_api)
- API config via `tb_configuracion_api` — overrides `application.yml`, no restart, exposed via `@ConfigurationProperties`
- Add `spring-boot-starter-oauth2-resource-server` to POM
- Rewrite SecurityConfig from permissive to dual auth
- Bootstrap admin via DataInitializer

### Out of Scope
- Thymeleaf login views, OAuth2, LDAP/AD, session clustering
- Password reset / email verification
- Fine-grained `@PreAuthorize` on endpoints

## Capabilities

### New
- `seguridad-autenticacion`: User identity, BCrypt validation, formLogin + JWT dual auth
- `seguridad-autorizacion`: Role/Permission CRUD, role-permission & user-role mappings
- `seguridad-configuracion-api`: Runtime API provider config in DB, no-restart updates

### Modified
- `modulo-personas-api` (R-005): API config source moves from application properties to `tb_configuracion_api`

## Approach

1. Add oauth2-resource-server dependency (Nimbus JWT)
2. Usuario entity with FK→Persona + FK→Trabajador, unique username, BCrypt hash
3. Dual SecurityFilterChain — formLogin for UI, `oauth2ResourceServer.jwt()` for `/api/**`
4. AuthController issues JWT, validated statelessly via Nimbus
5. V12 migration: 6 tables + composite PKs + FKs
6. DbApiConfigService reads config table, exposes via `@ConfigurationProperties`

## Affected Areas

| Area | Impact |
|------|--------|
| `pom.xml`, `SecurityConfig.java` | Modified |
| `seguridad/entity/`, `seguridad/repository/` | New (3 entities, 6 repos) |
| `seguridad/service/`, `seguridad/config/` | New (AuthService, DbApiConfigService, JwtConfig) |
| `seguridad/controller/`, `seguridad/bootstrap/` | New (AuthController, DataInitializer) |
| `V12__seguridad_create_tables.sql` | New |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| JWT secret in source | Med | Env var `JWT_SECRET`, dev fallback |
| Bootstrap admin creds | Med | Random password, force change on first login |
| Token in logs | Low | Logback filter strips Authorization header |

## Rollback Plan

1. Revert SecurityConfig to `anyRequest().permitAll()` + `csrf.disable()`
2. Remove oauth2-resource-server from POM
3. `DROP TABLE tb_usuarios_roles, tb_roles_permisos, tb_configuracion_api, tb_permisos, tb_roles, tb_usuarios CASCADE`
4. Delete change folder

## Dependencies

- `spring-boot-starter-oauth2-resource-server`
- `modulo-personas-api` spec (R-005 update)
- Persona + Trabajador entities exist

## Success Criteria

- [ ] `GET /api/v1/personas` without token → 401
- [ ] `POST /api/v1/auth/login` with valid creds → JWT
- [ ] `GET /api/v1/personas` with valid JWT → 200
- [ ] Browser `/login` renders (Spring default)
- [ ] `tb_configuracion_api` changes reflect without restart
- [ ] `mvn compile` passes, all 6 tables created

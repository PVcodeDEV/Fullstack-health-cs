# Tasks: Módulo Seguridad — Auth, Authorization & API Security

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~2500–3500 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 → PR 2 → PR 3 → PR 4 → PR 5 |
| Delivery strategy | auto-chain |
| Chain strategy | stacked-to-main |

Decision needed before apply: No
Chained PRs recommended: Yes
Chain strategy: stacked-to-main
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | POM, package structure, V12 migration, entities, repositories | PR 1 | Base branch: main; data layer only |
| 2 | DTOs, services (Usuario/Rol/ConfiguracionApi), JwtTokenProvider, UsuarioDetailsService, DataInitializer | PR 2 | Base branch: main; depends on PR 1 entities |
| 3 | SecurityConfig rewrite (dual chain), JwtConfig, CorsConfig, DataInitializer | PR 3 | Base branch: main; replaces old SecurityConfig |
| 4 | AuthController, SeguridadAdminController, GlobalExceptionHandler update | PR 4 | Base branch: main; depends on PR 3 security |
| 5 | Tests + build verification + ReniecClient integration | PR 5 | Base branch: main; proves everything works |

## Phase 1: Foundation

- [x] 1.1 Add `spring-boot-starter-oauth2-resource-server` to `pom.xml`
- [x] 1.2 Create `com.clinica.seguridad.{entity,repository,service,dto,controller,config,bootstrap}` package structure
- [x] 1.3 Create `V12__seguridad_create_tables.sql` (tb_usuarios, tb_roles, tb_permisos, tb_roles_permisos, tb_usuarios_roles, tb_configuracion_api)

## Phase 2: Entities + Repositories

- [x] 2.1 Create `Usuario.java` (@AttributeOverride `usu_` prefix, FK→Persona, FK→Trabajador nullable, username unique)
- [x] 2.2 Create `Rol.java` (`rol_` prefix, codigo unique)
- [x] 2.3 Create `Permiso.java` (`per_` prefix, codigo unique, `{recurso}:{accion}` format)
- [x] 2.4 Create `ConfiguracionApi.java` (`conf_` prefix, modulo+clave unique)
- [x] 2.5 Create `UsuarioRol.java` (composite key `usro_usuario_id` + `usro_rol_id`)
- [x] 2.6 Create `RolPermiso.java` (composite key `rop_rol_id` + `rop_permiso_id`)
- [x] 2.7 Create 6 `@Repository` interfaces extending `JpaRepository`

## Phase 3: DTOs + Services + JWT

- [x] 3.1 Create `LoginRequest`/`LoginResponse`/`UsuarioRequest`/`UsuarioResponse`/`RolResponse`/`PermisoResponse`/`ConfiguracionApiRequest`/`ConfiguracionApiResponse` DTOs
- [x] 3.2 Create `UsuarioDetailsService` implementing `UserDetailsService` (includes `UsuarioPrincipal`)
- [x] 3.3 Create `JwtTokenProvider` (generate + validate HMAC-SHA256 JWT via NimbusJwtEncoder/NimbusJwtDecoder)
- [x] 3.4 Create `RolService` (CRUD + permission assignment)
- [x] 3.5 Create `ConfiguracionApiService` (`@Cacheable` reads, `@CacheEvict` on write)
- [x] 3.6 Create `PermisoService` (CRUD for permisos catalog)
- [x] 3.7 Create `DataInitializer` (`ApplicationRunner`: seeds 6 roles + 8 permisos + admin from env vars)
- [x] 3.8 Create `PasswordEncoderConfig` (`BCryptPasswordEncoder` bean for DataInitializer)
- [x] 3.9 Add `findByUsuarioId` to `UsuarioRolRepository`
- [x] 3.10 Add `app.jwt.secret` + `app.jwt.expiration-ms` to `application.yml`

## Phase 4: Security Config

- [x] 4.1 Create `seguridad/config/SecurityConfig.java` (dual `@Ordered` chains: formLogin for browser, oauth2ResourceServer.jwt for `/api/**`)
- [x] 4.2 Create `seguridad/config/JwtConfig.java` (`@ConfigurationProperties` + `JwtDecoder`/`JwtEncoder` beans)
- [x] 4.3 Create `CorsConfig.java` refinement
- [x] 4.4 Delete `config/SecurityConfig.java` (old permissive config)
- [x] 4.5 Add `@EnableMethodSecurity` to SecurityConfig

## Phase 5: Controllers

- [x] 5.1 Create `AuthController` (`POST /api/v1/auth/login` public, `GET /api/v1/auth/me` authenticated)
- [x] 5.2 Create `SeguridadAdminController` (CRUD usuarios/roles/permisos/config, `@PreAuthorize` ADMIN)
- [x] 5.3 Update `GlobalExceptionHandler` with `AccessDeniedException` → 403 ProblemDetail

## Phase 6: Testing + Integration

- [x] 6.1 Unit tests for `JwtTokenProvider` (sign + verify + claims extraction + malformed token)
- [x] 6.2 Unit tests for `UsuarioDetailsService` (load by username, authorities, not found)
- [x] 6.3 Unit tests for `RolService` (CRUD, assignPermisos idempotent, not found)
- [x] 6.4 Unit tests for `ConfiguracionApiService` (CRUD, cache evict, duplicate key)
- [x] 6.5 `@WebMvcTest(AuthController.class)` — login 200/invalid/validation
- [x] 6.6 `@DataJpaTest` for `UsuarioRepository` (save, findByUsername, unique constraint, FK)
- [ ] 6.7 `@SpringBootTest` — missing JWT returns 401, DataInitializer seeds 6 roles
- [ ] 6.8 `ReniecClient` swap: read config from `ConfiguracionApiService` instead of properties
- [x] 6.9 `mvn compile && mvn test` pass

## Implementation Order

PR 1 (Foundation) must go first — everything depends on the migration and entities. PR 2 (Services) next, then PR 3 (Security) to lock down endpoints before controllers in PR 4. PR 5 (Tests + Reniec integration) last to validate the full chain.

## Next Step

All work units are pre-split for auto-chain (stacked-to-main). PR 1 is ready for `sdd-apply` with `--unit 1`.

# Design: Módulo Seguridad — Auth, Authorization & API Security

## Technical Approach

Dual auth via two `@Ordered` SecurityFilterChain beans: formLogin + session for browser, JWT bearer for `/api/**`. RBAC through `tb_roles`, `tb_permisos`, and `@PreAuthorize`. Runtime API config in `tb_configuracion_api` with `@Cacheable` service. Bootstrap admin from env vars.

## Architecture Decisions

| Decision | Options | Tradeoff | Chosen |
|----------|---------|----------|--------|
| Auth chains | Single chain with path matchers vs dual `@Order` chains | Dual is clearer: each chain owns its auth strategy entirely | Dual `SecurityFilterChain` beans |
| JWT library | Nimbus (auto via oauth2-resource-server) vs jjwt vs auth0-jwt | Nimbus is zero-dep (comes with starter); others need manual config | Nimbus JOSE + JWT |
| API config refresh | `@Cacheable`/`@CacheEvict` vs polling vs event bus | Cache evict on write is simplest, no restart, no infra | `@Cacheable` reads + `@CacheEvict` on update |
| Bootstrap init | `@PostConstruct` vs `ApplicationRunner` | ApplicationRunner runs after full context — safer for JPA | `ApplicationRunner` in `DataInitializer` |
| JWT signing | HMAC-SHA256 env var vs RSA keypair | HMAC simpler for single-service; RSA enables key rotation later | HMAC-SHA256 (`JWT_SECRET`), RSA opt-in |

## Data Flow

```
Browser ──POST /login──→ SecurityFilterChain[formLogin]
                            ↓ DaoAuthenticationProvider
                            ↓ UsuarioDetailsService → tb_usuarios
                            ↓ BCryptPasswordEncoder
                            ↓ HTTP Session (SecurityContext)

API ──POST /api/v1/auth/login──→ AuthController
       ↓ AuthenticationManager.authenticate()
       ↓ JwtTokenProvider.generateToken(claims)
       ↓ { token, type, expiresIn }

API ──GET /api/v1/*──→ SecurityFilterChain[oauth2ResourceServer]
       ↓ BearerTokenAuthenticationFilter
       ↓ NimbusJwtDecoder (HMAC)
       ↓ JwtAuthenticationConverter → @PreAuthorize
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `pom.xml` | Modify | Add `spring-boot-starter-oauth2-resource-server` |
| `config/SecurityConfig.java` | Delete | Replaced by seguridad module config |
| `seguridad/config/SecurityConfig.java` | Create | Dual chain + `@EnableMethodSecurity` |
| `seguridad/config/JwtConfig.java` | Create | `@ConfigurationProperties` + JwtDecoder/JwtEncoder beans |
| `seguridad/config/UsuarioDetailsService.java` | Create | `UserDetailsService` impl loading from UsuarioRepository |
| `seguridad/entity/Usuario.java` | Create | `tb_usuarios` entity |
| `seguridad/entity/Rol.java` | Create | `tb_roles` entity |
| `seguridad/entity/Permiso.java` | Create | `tb_permisos` entity |
| `seguridad/entity/UsuarioRol.java` | Create | Join entity (composite key) |
| `seguridad/entity/RolPermiso.java` | Create | Join entity (composite key) |
| `seguridad/entity/ConfiguracionApi.java` | Create | `tb_configuracion_api` entity |
| `seguridad/repository/*.java` | Create | 6 `@Repository` interfaces extending JpaRepository |
| `seguridad/service/UsuarioService.java` | Create | Usuario CRUD, password hashing |
| `seguridad/service/RolService.java` | Create | Rol + permission assignment |
| `seguridad/service/ConfiguracionApiService.java` | Create | Cached reads, cache-evict on writes |
| `seguridad/controller/AuthController.java` | Create | POST `/api/v1/auth/login`, GET `/api/v1/auth/me` |
| `seguridad/controller/SeguridadAdminController.java` | Create | Admin CRUD for usuarios, roles, config |
| `seguridad/bootstrap/DataInitializer.java` | Create | `ApplicationRunner`: seed 6 roles + admin user |
| `V12__seguridad_create_tables.sql` | Create | 6 tables + composite PKs + FKs + indexes |
| `persona/service/ReniecClient.java` | Modify | Swap `ReniecProperties` for `ConfiguracionApiService` |

## Interfaces / Contracts

```java
record AuthRequest(String username, String password) {}
record AuthResponse(String token, String type, long expiresIn) {}
// JWT claims: sub=username, roles=[], permisos=[], exp=timestamp
```

**JwtConfig**: `@ConfigurationProperties(prefix = "jwt")` — `secret` (required), `expiration` (default 3600s). Exposes `JwtDecoder` and `JwtEncoder` beans (HMAC-SHA256).

**UsuarioDetailsService**: `loadUserByUsername(username)` → `UsuarioPrincipal` wrapping `Usuario` + `Set<GrantedAuthority>` from roles + permisos.

**ConfiguracionApiService**: `getConfig(modulo, clave)` with `@Cacheable("apiConfig")`; `setConfig(modulo, clave, valor)` with `@CacheEvict("apiConfig")`.

## Entity Layout (all extend BaseEntity with `@AttributeOverride`)

| Entity | Table | PK | Prefix | Notes |
|--------|-------|----|--------|-------|
| Usuario | `tb_usuarios` | `usu_id` (identity) | `usu_` | FK→persona (NN), FK→trabajador (nullable), username unique |
| Rol | `tb_roles` | `rol_id` | `rol_` | codigo unique |
| Permiso | `tb_permisos` | `per_id` | `per_` | codigo unique, `{recurso}:{accion}` naming |
| UsuarioRol | `tb_usuarios_roles` | `usro_usuario_id` + `usro_rol_id` | `usro_` | Composite PK, ManyToMany join |
| RolPermiso | `tb_roles_permisos` | `rop_rol_id` + `rop_permiso_id` | `rop_` | Composite PK, ManyToMany join |
| ConfiguracionApi | `tb_configuracion_api` | `conf_id` | `conf_` | modulo+clave unique pair |

## REST Endpoints

| Method | Path | Security | Authority |
|--------|------|----------|-----------|
| POST | `/api/v1/auth/login` | permitAll | — |
| GET | `/api/v1/auth/me` | authenticated | — |
| GET | `/api/v1/seguridad/usuarios` | authenticated | ADMIN |
| POST | `/api/v1/seguridad/usuarios` | authenticated | ADMIN |
| PUT | `/api/v1/seguridad/roles/{id}/permisos` | authenticated | ADMIN |
| GET | `/api/v1/seguridad/configuracion` | authenticated | ADMIN |
| PUT | `/api/v1/seguridad/configuracion` | authenticated | ADMIN |

## Testing Strategy

| Layer | What | How |
|-------|------|-----|
| Unit | JwtTokenProvider sign/verify | JUnit 5 + Mockito |
| Unit | UsuarioDetailsService loadByUsername | Mock repository |
| Integration | AuthController login/me | `@WebMvcTest(AuthController.class)` |
| Integration | SecurityConfig 401 on missing token | `@SpringBootTest` + TestRestTemplate |
| Integration | ConfiguracionApiService cache evict | `@DataJpaTest` + service bean |
| Integration | DataInitializer role seeding | `@SpringBootTest` verify 6 roles exist |

## Migration / Rollout

No data migration — greenfield. V12 creates 6 new tables. Seed roles and admin via `DataInitializer`. Rollback: replace SecurityConfig with permissive, drop V12 tables, revert POM.

## Open Questions

- [ ] `trabajador_id` fetch type on Usuario — LAZY (default) sufficient or need EAGER for auth flow?
- [ ] Prod JWT signing: is HMAC-SHA256 sufficient or should we plan RSA keypair rotation from day one?

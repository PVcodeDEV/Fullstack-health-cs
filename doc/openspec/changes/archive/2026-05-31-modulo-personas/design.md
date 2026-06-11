# Design: Módulo Personas

## Technical Approach

New `com.clinica.persona` module with standard layered structure (entity → repository → service/dto → controller). `Persona` entity is a JPA entity extending `BaseEntity` — no JPA inheritance. Role tables (Paciente, Trabajador, Medico, Cliente) live in their own modules and reference `tb_personas` via FK composition. API integration uses a strategy pattern with two implementations of `ReniecClient` interface, selected via `ReniecProperties`. DNI check digit validated by `Modulo11Validator` before persist. Flyway V10 for core table + indexes, V11 for all four role tables.

## Module Dependency Diagram

```
persona ────→ maestro (TipoDocumentoIdentidad, EstadoCivil, UbigeoDistrito)
     │
     ├──→ config (ReniecProperties via @ConfigurationProperties)
     │
     └── external APIs (SUNAT, Secure provider via ReniecClient)
```

Role modules (clinica, rrhh, caja) reference `persona` only via FK — no Java dependency.

## Architecture Decisions

| Option | Tradeoff | Decision |
|--------|----------|----------|
| JPA inheritance (JOINED) vs composition FKs | Inheritance couples all role tables to Persona schema; composition keeps modules independent | **Composition FK** — each role table has its own entity in its module |
| API client = single class vs strategy interface | Single class = simpler but hard to test/mock; interface = pluggable | **ReniecClient interface** + SunatApiClient + SecureApiClient implementations |
| API config in persona vs shared config | In persona = self-contained but duplicates config across modules; shared = cleaner | **ReniecProperties** in `com.clinica.config` (follows existing GlobalExceptionHandler location) |
| Modulo11 in maestro vs persona | Maestro owns catalogs only, not business validation logic | **Modulo11Validator** in `persona.service` |

## Data Flow

```
Client POST /api/v1/personas
  │
  ├→ PersonaController.create(PersonaRequest)
  │   │
  │   ├→ Modulo11Validator.validar(dni) ──→ 422 if invalid
  │   │
  │   ├→ IF tipoDocumento == DNI:
  │   │     ├→ SecureApiClient (if enabled) ──→ PersonaDatos
  │   │     └→ SunatApiClient (fallback) ──→ partial PersonaDatos
  │   │
  │   ├→ PersonaService.create(PersonaRequest + PersonaDatos)
  │   │   ├→ Sets nombres/apellidos from API (DNI) or manual (CE/Pasaporte)
  │   │   ├→ Saves Persona to tb_personas
  │   │   └→ Updates pers_fecha_ultima_consulta
  │   │
  │   └→ Returns PersonaResponse (201 Created)
  │
  GET /api/v1/personas?numeroDocumento=X → @Query exact match
  GET /api/v1/personas?nombres=X → @Query ILIKE, excludes inactive
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `backend/src/main/resources/db/migration/V10__persona_create_tb_personas.sql` | Create | `CREATE TABLE tb_personas` + indexes |
| `backend/src/main/resources/db/migration/V11__persona_create_role_tables.sql` | Create | `CREATE TABLE` for tb_pacientes, tb_trabajadores, tb_medicos, tb_clientes |
| `backend/src/main/java/com/clinica/config/ReniecProperties.java` | Create | `@ConfigurationProperties(prefix = "app.reniec")` record with sunatUrl, secureUrl, secureToken, secureEnabled |
| `backend/src/main/java/com/clinica/persona/entity/Persona.java` | Create | JPA entity extending `BaseEntity`, `@AttributeOverride` with `pers_` prefix, `@Table(name = "tb_personas")` |
| `backend/src/main/java/com/clinica/persona/repository/PersonaRepository.java` | Create | `JpaRepository<Persona, Long>` + search by documento, ILIKE, soft-delete filter |
| `backend/src/main/java/com/clinica/persona/service/Modulo11Validator.java` | Create | `@Component` with `validar(String dni)` — algorithm: multiply by (3,2,7,6,5,4,3,2), mod 11, digit mapping |
| `backend/src/main/java/com/clinica/persona/service/ReniecClient.java` | Create | Interface: `Optional<PersonaDatos> consultarPorDni(String dni)` |
| `backend/src/main/java/com/clinica/persona/service/api/PersonaDatos.java` | Create | Record: nombres, apellidoPaterno, apellidoMaterno, direccion, ubigeoDistrito, fechaNacimiento, sexo |
| `backend/src/main/java/com/clinica/persona/service/api/SunatApiClient.java` | Create | `ReniecClient` impl — HTTP GET to SUNAT URL, parse HTML response, extract names only |
| `backend/src/main/java/com/clinica/persona/service/api/SecureApiClient.java` | Create | `ReniecClient` impl — configurable URL + Bearer token, returns full PersonaDatos |
| `backend/src/main/java/com/clinica/persona/service/api/ApiConsultaException.java` | Create | Runtime exception for API failures (logged without PII) |
| `backend/src/main/java/com/clinica/persona/service/PersonaService.java` | Create | `@Service @Transactional` — CRUD + API auto-fill logic + fallback chain |
| `backend/src/main/java/com/clinica/persona/dto/PersonaRequest.java` | Create | Record: tipoDocumentoId, numeroDocumento, nombres, apellidoPaterno, apellidoMaterno, fechaNacimiento, sexo, estadoCivilId, direccion, ubigeoDistrito, telefono, email |
| `backend/src/main/java/com/clinica/persona/dto/PersonaResponse.java` | Create | Record with all Persona fields (non-PII exposed), `fromEntity()` factory |
| `backend/src/main/java/com/clinica/persona/dto/PersonaSearchResponse.java` | Create | Lightweight record for search results (id, tipoDocumento, numeroDocumento, nombres, apellidos) |
| `backend/src/main/java/com/clinica/persona/controller/PersonaController.java` | Create | `@RestController @RequestMapping("/api/v1/personas")` — CRUD + search endpoints |

## Interfaces / Contracts

```java
// ReniecClient — pluggable API strategy
public interface ReniecClient {
    Optional<PersonaDatos> consultarPorDni(String dni);
}

// PersonaDatos — API response contract
public record PersonaDatos(
    String nombres,
    String apellidoPaterno,
    String apellidoMadre,  // SUNAT returns "apellido materno"
    String direccion,
    String ubigeoDistrito,
    LocalDate fechaNacimiento,
    String sexo
) {}

// Modulo11Validator — DNI check digit
@Component
public class Modulo11Validator {
    public boolean validar(String dni) { ... }
    // Algorithm: digits[7..0] × [3,2,7,6,5,4,3,2], sum, mod 11,
    //   if remainder=0 → check=6,
    //   if remainder=1 → invalid,
    //   else check = 11 - remainder
}

// ReniecProperties — API config (in com.clinica.config)
@ConfigurationProperties(prefix = "app.reniec")
public record ReniecProperties(
    @DefaultValue("https://ww1.sunat.gob.pe/ol-ti-itfisdenreg/itfisdenreg.htm")
    String sunatUrl,
    String secureUrl,
    String secureToken,
    @DefaultValue("false")
    boolean secureEnabled
) {}

// PersonaRequest — validation
public record PersonaRequest(
    @NotNull Short tipoDocumentoId,
    @NotBlank @Size(max = 20) String numeroDocumento,
    @Size(max = 200) String nombres,         // null for DNI (auto-filled)
    @Size(max = 100) String apellidoPaterno,  // null for DNI
    @Size(max = 100) String apellidoMaterno,
    LocalDate fechaNacimiento,
    @Pattern(regexp = "[MF]") String sexo,
    Short estadoCivilId,
    @Size(max = 255) String direccion,
    @Size(max = 6) String ubigeoDistrito,
    @Size(max = 20) String telefono,
    @Email @Size(max = 100) String email
) {}
```

### REST Endpoints

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| `GET` | `/api/v1/personas` | 200 | Search: `?numeroDocumento=X` exact, `?nombres=X` ILIKE, `?apellidoPaterno=X` |
| `GET` | `/api/v1/personas/{id}` | 200 / 404 | Find by ID (triggers stale-data refresh check) |
| `POST` | `/api/v1/personas` | 201 / 409 / 422 | Create with validation + modulo 11 + API auto-fill |
| `PUT` | `/api/v1/personas/{id}` | 200 / 404 | Full update |
| `DELETE` | `/api/v1/personas/{id}` | 200 / 404 | Soft delete (activo = false) |

### Persona Entity — PII Markers

Fields marked as PII per Ley 29733: `numeroDocumento`, `nombres`, `apellidoPaterno`, `apellidoMaterno`, `direccion`, `telefono`, `email`. Use `@ToString.Exclude` on the entity and ensure services never log these fields directly.

### Flyway V10 — Index Strategy

```sql
CREATE UNIQUE INDEX IF NOT EXISTS idx_pers_numero_documento
    ON tb_personas(pers_numero_documento);
CREATE INDEX IF NOT EXISTS idx_pers_tipo_documento_id
    ON tb_personas(pers_tipo_documento_id);
CREATE INDEX IF NOT EXISTS idx_pers_nombres_apellidos
    ON tb_personas(pers_nombres, pers_apellido_paterno, pers_apellido_materno);
CREATE INDEX IF NOT EXISTS idx_pers_ubigeo_distrito
    ON tb_personas(pers_ubigeo_distrito);
CREATE INDEX IF NOT EXISTS idx_pers_fecha_ultima_consulta
    ON tb_personas(pers_fecha_ultima_consulta);
CREATE INDEX IF NOT EXISTS idx_pers_activo
    ON tb_personas(pers_activo) WHERE pers_activo = true;
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit — Modulo11Validator | Valid/invalid check digits, edge cases (remainder 0, remainder 1) | `@ExtendWith(MockitoExtension.class)`, pure algorithm tests |
| Unit — PersonaService | Create with DNI auto-fill, duplicate rejection, fallback chain, PII exclusion from logs, stale-data refresh | Mock PersonaRepository + ReniecClient + Modulo11Validator |
| Unit — SunatApiClient | HTML parsing (valid, malformed, empty), timeout, error states | Mock WebServer (okhttp MockWebServer) |
| Unit — SecureApiClient | Valid JSON response, 4xx/5xx errors, timeout, token header presence | Mock WebServer |
| Repository — PersonaRepository | `@DataJpaTest` — CRUD, unique constraint, ILIKE search, soft-delete filter | H2 in-memory, seed test data |
| Controller — PersonaController | `@WebMvcTest(PersonaController.class)` — HTTP 201/400/404/409/422, validation error shape, no PII in error responses | MockMvc + mocked service |
| Integration — Full flow | `@SpringBootTest` + `@AutoConfigureMockMvc` — persona creation → API auto-fill → role FK referencing | Full context (optional, deferred) |

## Migration / Rollout

Flyway V10 + V11 are additive — existing tables are unaffected. Apply in order. Rollback by running V10/V11 `DROP TABLE` statements (no production data yet). No feature flags needed.

## Open Questions

- [ ] SUNAT URL HTML response structure — needs verification; the HTML parser implementation depends on actual response format
- [ ] SecureApiClient JSON contract — unknown until a provider is selected; `PersonaDatos` field mapping may need adjustment
- [ ] The `pers_fecha_ultima_consulta` refresh on GET — should it be synchronous (blocking the response) or asynchronous (return stale data + trigger refresh)? Proposal/spec says "asynchronously" — needs async pattern decision (@Async vs task executor)

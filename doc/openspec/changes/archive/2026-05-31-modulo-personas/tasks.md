# Tasks: Módulo Personas

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: stacked-to-main
400-line budget risk: High

| Field | Value |
|-------|-------|
| Estimated changed lines | 1200–1500 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Delivery strategy | ask-on-risk |
| Suggested split | PR 1 (Foundation + Persona CRUD) → PR 2 (Validators & API) → PR 3 (Role tables + verify) |

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Foundation + Persona CRUD | PR 1 | ReniecProperties + Persona entity/repo/service/DTOs/controller + Flyway V10 |
| 2 | Validators & API clients | PR 2 | Modulo11Validator + ReniecClient + SunatApiClient + SecureApiClient + PersonaDatos |
| 3 | Role tables + build verify | PR 3 | Paciente/Trabajador/Medico full CRUD + Flyway V11 + compile + test |

## Phase 1: Foundation

- [x] 1.1 Create package dirs: `persona/{entity,repository,service,dto,controller,service/api}` under `com.clinica`
- [x] 1.2 Create `config/ReniecProperties.java` — `@ConfigurationProperties(prefix = "app.reniec")` record with sunatUrl, secureUrl, secureToken, secureEnabled

## Phase 2: Persona Entity + CRUD

- [x] 2.1 Create `persona/entity/Persona.java` — `@Entity @Table("tb_personas")`, extends `BaseEntity` with `@AttributeOverride` (pers_ prefix), all fields with `@ToString.Exclude` on PII fields (Ley 29733)
- [x] 2.2 Create `persona/repository/PersonaRepository.java` — `JpaRepository<Persona, Long>` with exact `findByNumeroDocumento`, `findByNombresContainingIgnoreCase`, `findByApellidoPaternoContainingIgnoreCase`, plus soft-delete filter (`findAllByActivoTrue`)
- [x] 2.3 Create `persona/dto/PersonaRequest.java` — record with `@NotNull/@NotBlank/@Size/@Email/@Pattern` validation
- [x] 2.4 Create `persona/dto/PersonaResponse.java` — record with `fromEntity()` factory, exclude PII fields from serialization
- [x] 2.5 Create `persona/dto/PersonaSearchResponse.java` — lightweight record (id, tipoDocumento, numeroDocumento, nombres, apellidos)
- [x] 2.6 Create `persona/service/PersonaService.java` — `@Service @Transactional` with CRUD + search + DNI API auto-fill + fallback chain + stale-data refresh logic
- [x] 2.7 Create `persona/controller/PersonaController.java` — `@RestController @RequestMapping("/api/v1/personas")` with GET (search + by-id), POST (201), PUT, DELETE (soft)
- [x] 2.8 Create `db/migration/V10__persona_create_tb_personas.sql` — `CREATE TABLE tb_personas` + 6 indexes (unique documento, tipo doc, nombres/apellidos, ubigeo, fecha_consulta, activo partial)

## Phase 3: Validators & API Clients

- [x] 3.1 Create `persona/service/Modulo11Validator.java` — `@Component` with `validar(String dni)` algorithm: multiply × (3,2,7,6,5,4,3,2), mod 11, digit mapping
- [x] 3.2 Create `persona/service/ReniecClient.java` — interface: `Optional<PersonaDatos> consultarPorDni(String dni)`
- [x] 3.3 Create `persona/service/api/PersonaDatos.java` — record with nombres, apellidoPaterno, apellidoMadre, direccion, ubigeoDistrito, fechaNacimiento, sexo
- [x] 3.4 Create `persona/service/api/ApiConsultaException.java` — `RuntimeException` for API failures
- [x] 3.5 Create `persona/service/api/SunatApiClient.java` — `ReniecClient` impl, HTTP GET to SUNAT URL, HTML parse, extract names only
- [x] 3.6 Create `persona/service/api/SecureApiClient.java` — `ReniecClient` impl, configurable URL + Bearer token, returns full PersonaDatos

## Phase 4: Role Tables

- [x] 4.1 Create package dirs: `clinica/paciente/`, `clinica/medico/`, `rrhh/trabajador/` entity/repository/service/dto/controller
- [x] 4.2 Create `clinica/paciente/entity/Paciente.java` — FK → Persona, tipoPaciente default PARTICULAR, extends `BaseEntity @AttributeOverride` (pac_ prefix)
- [x] 4.3 Create `clinica/paciente/` — repository, service, request DTO, response DTO, controller
- [x] 4.4 Create `rrhh/trabajador/entity/Trabajador.java` — FK → Persona, extends `BaseEntity @AttributeOverride` (tra_ prefix)
- [x] 4.5 Create `rrhh/trabajador/` — repository, service, request DTO, response DTO, controller
- [x] 4.6 Create `clinica/medico/entity/Medico.java` — FK → Trabajador, cmp UNIQUE, extends `BaseEntity @AttributeOverride` (med_ prefix)
- [x] 4.7 Create `clinica/medico/` — repository, service, request DTO, response DTO, controller
- [x] 4.8 Create `db/migration/V11__persona_create_role_tables.sql` — `CREATE TABLE tb_pacientes`, `tb_trabajadores`, `tb_medicos` with FKs + indexes (no tb_clientes)

## Phase 5: Testing

- [x] 5.1 Write `@ExtendWith(MockitoExtension.class)` unit test for Modulo11Validator — valid/invalid check digits, edge cases (remainder 0, remainder 1)
- [x] 5.2 Write `@ExtendWith(MockitoExtension.class)` unit test for PersonaService — create with DNI auto-fill, duplicate rejection, fallback chain, PII exclusion, stale-data refresh
- [x] 5.3 Write `@ExtendWith(MockitoExtension.class)` unit test for SunatApiClient — valid HTML parse, malformed HTML, timeout, error
- [x] 5.4 Write `@ExtendWith(MockitoExtension.class)` unit test for SecureApiClient — valid JSON, 4xx/5xx, timeout, token header
- [x] 5.5 Write `@DataJpaTest` PersonaRepositoryTest — CRUD, unique constraint, ILIKE search, soft-delete filter
- [x] 5.6 Write `@WebMvcTest(PersonaController.class)` PersonaControllerTest — HTTP 201/400/404/409, validation error shape, ProblemDetail format
- [x] 5.7 Write unit tests for PacienteService, TrabajadorService, MedicoService — CRUD, FK integrity, soft delete
- [x] 5.8 Write `@DataJpaTest` for each role table repository — CRUD, FK constraint, soft-delete filter

## Phase 6: Build Verification

- [x] 6.1 Run `mvn compile` — verify all imports resolve (Persona → BaseEntity, FK → TipoDocumentoIdentidad/EstadoCivil/UbigeoDistrito)
- [x] 6.2 Run `mvn test` — all Phase 5 tests pass

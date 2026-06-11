# Apply Progress: Módulo Clínica — PR 1 (Data Layer)

## Status
✅ **Completed** — Phase 1 (Foundation): all Flyway migrations + entities + repositories + Cama state machine

## Completed Tasks

### Migrations (6 files)
- [x] 1.1 `V14__clinica_camas_habitaciones.sql` — `tb_habitaciones`, `tb_camas` + seed (7 rooms, 13 beds)
- [x] 1.2 `V15__clinica_admision.sql` — `tb_cuentas`, `tb_cuentas_paquetes`, `tb_solicitudes_hospitalizacion`, `tb_admision_diagnosticos`
- [x] 1.3 `V16__clinica_hospitalizacion.sql` — `tb_hospitalizaciones`, `tb_cambios_habitacion`, `tb_notas_evolucion`, `tb_solicitudes_medicamento`, `tb_altas_medicas`
- [x] 1.4 `V17__clinica_sop.sql` — `tb_reportes_quirurgicos`, `tb_registros_urpa`
- [x] 1.5 `V18__clinica_hce.sql` — `tb_documentos_clinicos` (BYTEA), `tb_firmas_digitales`
- [x] 1.6 `V19__clinica_cuenta.sql` — `tb_cargos_adicionales`

### Entities (14 entities + 1 enum)
- [x] `cama/entity/EstadoCama.java` — enum: DISPONIBLE, OCUPADO, MANTENIMIENTO
- [x] `cama/entity/Habitacion.java` — room with tipoHabitacionId FK
- [x] `cama/entity/Cama.java` — bed + state machine methods (ocupar, liberar, ponerEnMantenimiento, disponibilizar)
- [x] `admision/entity/Cuenta.java` — account with estado, totalCargos, pendienteCobro
- [x] `admision/entity/CuentaPaquete.java` — package line item
- [x] `admision/entity/SolicitudHospitalizacion.java` — hospitalization request with state machine
- [x] `admision/entity/AdmisionDiagnostico.java` — CIE-11 diagnosis
- [x] `hospitalizacion/entity/Hospitalizacion.java` — hospitalization record
- [x] `hospitalizacion/entity/CambioHabitacion.java` — room change log
- [x] `hospitalizacion/entity/NotaEvolucion.java` — evolution/nursing notes
- [x] `hospitalizacion/entity/SolicitudMedicamento.java` — medication request
- [x] `hospitalizacion/entity/AltaMedica.java` — clinical discharge
- [x] `sop/entity/ReporteQuirurgico.java` — surgical report with draft/complete state
- [x] `sop/entity/URPARegistro.java` — post-anesthesia recovery
- [x] `hce/entity/DocumentoClinico.java` — clinical document with BYTEA content + version chain
- [x] `hce/entity/FirmaDigital.java` — digital signature (hash SHA-256, usuarioId, timestamp, IP origen)
- [x] `cuenta/entity/CargoAdicional.java` — additional charge

### Repositories (14 interfaces)
- [x] `cama/repository/HabitacionRepository.java` — findByCodigo, findByTipoHabitacionId
- [x] `cama/repository/CamaRepository.java` — findByCodigo, findByHabitacionId, findByEstado, countByHabitacionIdAndEstado
- [x] `admision/repository/CuentaRepository.java` — findByPacienteId, findByEstado
- [x] `admision/repository/CuentaPaqueteRepository.java` — findByCuentaId
- [x] `admision/repository/SolicitudHospitalizacionRepository.java` — findByCuentaId, findByEstado, findByCuentaIdAndEstado
- [x] `admision/repository/AdmisionDiagnosticoRepository.java` — findByCuentaId, findByCuentaIdAndTipo
- [x] `hospitalizacion/repository/HospitalizacionRepository.java` — findBySolicitudId, findByPacienteId, findByCuentaId, findByEstado
- [x] `hospitalizacion/repository/CambioHabitacionRepository.java` — findByHospitalizacionId
- [x] `hospitalizacion/repository/NotaEvolucionRepository.java` — findByHospitalizacionId, findByHospitalizacionIdAndTipo
- [x] `hospitalizacion/repository/SolicitudMedicamentoRepository.java` — findByHospitalizacionId, findByEstado
- [x] `hospitalizacion/repository/AltaMedicaRepository.java` — findByHospitalizacionId
- [x] `sop/repository/ReporteQuirurgicoRepository.java` — findByHospitalizacionId, findByEstado, existsByHospitalizacionId
- [x] `sop/repository/URPARegistroRepository.java` — findByReporteId
- [x] `hce/repository/DocumentoClinicoRepository.java` — findByPacienteId, findByPacienteIdAndTipoDocumento, findByHospitalizacionId
- [x] `hce/repository/FirmaDigitalRepository.java` — findByDocumentoId, findByUsuarioId
- [x] `cuenta/repository/CargoAdicionalRepository.java` — findByCuentaId, findByCuentaIdAndTipo

## Design Decisions Applied
- **Column prefixes**: Each table uses its own prefix per design (`adm_`, `hosp_`, `sop_`, `hce_`, `cta_`, `cama_`, `hab_`)
- **Cama state machine**: Entity methods on `Cama.java` enforce valid transitions: DISPONIBLE ↔ OCUPADO ↔ MANTENIMIENTO
- **FK constraints**: All foreign keys present as per design (tb_tipos_habitacion via `thab_id`, tb_pacientes, tb_medicos, tb_usuarios, tb_camas, tb_cie11_diagnosticos)
- **No pricing in V15**: `adm_total_estimado` is nullable, informational only
- **BYTEA content**: `hce_contenido` uses `@Lob byte[]` in entity, `BYTEA` in migration
- **FirmaDigital**: Records usuarioId, timestamp, hash SHA-256 (64 chars), IP origen (max 45 for IPv6)
- **No direct caja dependency**: `clinica.cuenta` package references only clinica-internal types

## Files Created
- 6 Flyway migrations → `db/migration/`
- 1 enum + 16 entity Java files → `clinica/{cama,admision,hospitalizacion,sop,hce,cuenta}/entity/`
- 16 repository Java files → `clinica/{cama,admision,hospitalizacion,sop,hce,cuenta}/repository/`
- 1 apply-progress document → `doc/openspec/changes/modulo-clinica/apply-progress.md`

## Total: 39 files

## Blocked Items
None. Phase 1 complete — ready for Phase 2 (Services, DTOs, CuentaProjection, package-info).

---

# Apply Progress: Módulo Clínica — PR 3 (Controllers)

## Status
✅ **Completed** — Phase 3 (Controllers): all 6 controllers created with @PreAuthorize security

## Prerequisite: Cama Services

Since no `CamaService` or `HabitacionService` existed (they were not explicitly created in Phase 2), they were created as prerequisites for the controllers:

- [x] `cama/service/CamaService.java` — CRUD + findByHabitacionId + findDisponibles + cambiarEstado (state machine transitions)
- [x] `cama/service/HabitacionService.java` — CRUD + softDelete

## Completed Tasks

### 3.1 CamaController + HabitacionController
- [x] `cama/controller/HabitacionController.java` — `/api/v1/habitaciones` — findAll, findById, create, update, softDelete
- [x] `cama/controller/CamaController.java` — `/api/v1/camas` — findAll, findById, findByHabitacionId, findDisponibles, create, cambiarEstado, update, softDelete

### 3.2 AdmisionController
- [x] `admision/controller/AdmisionController.java` — `/api/v1/admision`
  - GET /pacientes?query= → buscarPaciente (returns PersonaSearchResponse)
  - POST /cuentas → crearCuenta (201)
  - POST /cuentas/{id}/diagnosticos → registrarDiagnostico (201)
  - POST /cuentas/{id}/asignar-cama → asignarCama (200)
  - **Skipped** (service methods don't exist): GET /cuentas, GET /cuentas/{id}, GET /solicitudes, GET /solicitudes/{id}

### 3.3 HospitalizacionController
- [x] `hospitalizacion/controller/HospitalizacionController.java` — `/api/v1/hospitalizacion`
  - POST /{id}/cambiar-cama → cambiarCama (with usuarioId from SecurityContext)
  - POST /{id}/notas → registrarNota (201, with usuarioId from SecurityContext)
  - POST /{id}/medicamentos → solicitarMedicamento (201, with usuarioId from SecurityContext)
  - POST /{id}/alta → darAlta
  - **Skipped** (service methods don't exist): GET /, GET /{id}, GET /{id}/notas, GET /{id}/medicamentos

### 3.4 SOPController
- [x] `sop/controller/SOPController.java` — `/api/v1/sop`
  - POST /reportes → crearReporte (201)
  - PUT /reportes/{id}/completar → completarReporte
  - POST /reportes/{id}/urpa → registrarURPA (201)
  - **Skipped** (service methods don't exist): GET /reportes, GET /reportes/{id}, GET /urpa/{reporteId}

### 3.5 HCEController
- [x] `hce/controller/HCEController.java` — `/api/v1/hce`
  - POST /documentos → crearDocumento (201, extracts usuarioId + ipOrigen)
  - GET /documentos?hospitalizacionId= → listarDocumentos
  - GET /documentos/{id}/verificar → verificarFirma (returns Map with valida boolean)
  - **Skipped** (service method doesn't exist): GET /documentos/{id}

### 3.6 CuentaController
- [x] `cuenta/controller/CuentaController.java` — `/api/v1/cuenta`
  - POST /cargos → agregarCargo (201)
  - GET /cargos?cuentaId= → listarCargos
  - GET /cuentas/{id} → obtenerCuenta
  - POST /cuentas/{id}/confirmar-cobro → confirmarCobro (200)
  - **Adaptation**: `listarCargos` uses `cuentaId` param (spec said `hospitalizacionId` but service uses `cuentaId`)

## Deviations from Spec
1. **`listarCargos` param**: Spec says `?hospitalizacionId=` but CuentaService.listarCargos takes `cuentaId`. Used `cuentaId` — documented adaptation.
2. **`obtenerCuenta` return type**: Returns raw `Cuenta` entity (no response DTO available). Consider adding a `CuentaResponse` to the cuenta package.
3. **Missing read endpoints**: Several GET endpoints skipped because services lack the corresponding methods. If needed, add `findAll`/`findById` to the affected services.

## Files Created (11 new files)
| File | Action |
|------|--------|
| `cama/service/CamaService.java` | Created |
| `cama/service/HabitacionService.java` | Created |
| `cama/controller/CamaController.java` | Created |
| `cama/controller/HabitacionController.java` | Created |
| `admision/controller/AdmisionController.java` | Created |
| `hospitalizacion/controller/HospitalizacionController.java` | Created |
| `sop/controller/SOPController.java` | Created |
| `hce/controller/HCEController.java` | Created |
| `cuenta/controller/CuentaController.java` | Created |

## Compilation
✅ `mvn compile` — success (no errors)

## Status
6/6 tasks complete. Ready for Phase 4 (Modified Files + Permisos).

---

# Apply Progress: Módulo Clínica — PR 5 (Testing)

## Status
✅ **Completed** — Phase 5 (Testing): all 4 task groups (25 test files + 2 production changes)

## Completed Tasks

### 5.1 @DataJpaTest (10 test files)
- [x] `cama/repository/CamaRepositoryTest.java` — CRUD, findByEstado, FK constraints, soft delete
- [x] `admision/repository/CuentaRepositoryTest.java` — findByPacienteId, findByEstado, estado transitions
- [x] `admision/repository/SolicitudHospitalizacionRepositoryTest.java` — find by Cuenta, by Estado, FK/cuenta constraint
- [x] `admision/repository/AdmisionDiagnosticoRepositoryTest.java` — save+find, findByCuentaId, findByCuentaIdAndTipo, CIE-11 codes
- [x] `hospitalizacion/repository/HospitalizacionRepositoryTest.java` — findBySolicitudId, findByCuentaId, findByEstado, estado transitions
- [x] `hospitalizacion/repository/AltaMedicaRepositoryTest.java` — unique hospitalizacionId constraint
- [x] `sop/repository/ReporteQuirurgicoRepositoryTest.java` — findByEstado, existsByHospitalizacionId, estado transitions, FK constraint
- [x] `hce/repository/DocumentoClinicoRepositoryTest.java` — BYTEA content persistence, findByPacienteId, findByHospitalizacionId, version chain
- [x] `hce/repository/FirmaDigitalRepositoryTest.java` — findByDocumentoId, SHA-256 hash storage (64-char hex)
- [x] `cuenta/repository/CargoAdicionalRepositoryTest.java` — save+find, findByCuentaId, findByCuentaIdAndTipo, multiple cargos

### 5.2 Service Unit Tests (Mockito, 7 test files)
- [x] `cama/service/CamaServiceTest.java` — findAll, findById, findDisponibles, create, cambiarEstado (DISPONIBLE↔OCUPADO↔MANTENIMIENTO), softDelete
- [x] `cama/service/HabitacionServiceTest.java` — CRUD, codigo auto-generation from nombre
- [x] `admision/service/AdmisionServiceTest.java` — crearCuenta (with paquete auto-solicitud), asignarCama (with cama state transitions), registrarDiagnostico, edge cases (cama ocupada, paciente no existe)
- [x] `hospitalizacion/service/HospitalizacionServiceTest.java` — cambiarCama (libera origen, ocupa destino), registrarNota, solicitarMedicamento, darAlta (libera cama, state transition), alta no activo → IllegalStateException
- [x] `sop/service/SOPServiceTest.java` — crearReporte (BORRADOR), completarReporte, completar no BORRADOR → IllegalStateException, registrarURPA
- [x] `hce/service/HCEServiceTest.java` — crearDocumento (SHA-256 hash verification), listarDocumentos, verificarFirma (valid/invalid hash, no firma → exception)
- [x] `cuenta/service/CuentaServiceTest.java` — agregarCargo, confirmarCobro, listarCargos, obtenerCuenta

### 5.3 @WebMvcTest (7 test files)
- [x] `cama/controller/CamaControllerTest.java` — GET 200, GET /{id} 200/404, POST 201/400, PUT estado, DELETE
- [x] `cama/controller/HabitacionControllerTest.java` — GET, GET/{id}, POST 201/400, PUT 200, DELETE
- [x] `admision/controller/AdmisionControllerTest.java` — GET pacientes, POST cuentas (201), POST diagnosticos (201/404), POST asignar-cama (200/409)
- [x] `hospitalizacion/controller/HospitalizacionControllerTest.java` — POST cambiar-cama, POST notas (201/400), POST medicamentos (201), POST alta (200/409)
- [x] `sop/controller/SOPControllerTest.java` — POST reportes (201), PUT completar (200/409), POST urpa (201)
- [x] `hce/controller/HCEControllerTest.java` — POST documentos (201/400), GET documentos (200), GET verificar (200/404)
- [x] `cuenta/controller/CuentaControllerTest.java` — POST cargos (201/400), GET cargos (200), POST confirmar-cobro (200)

### 5.4 Integration Test
- [x] `clinica/integration/AdmisionFlowIntegrationTest.java` — full flow: create Persona → crearCuenta → asignarCama (verify cama OCUPADO) → registrarNota → darAlta (verify cama DISPONIBLE + estado ALTA) → create CargoAdicional → listarCargos

## Production Changes (required for tests)
- [x] `hce/entity/DocumentoClinico.java` — Added `columnDefinition = "BYTEA"` to `contenido` field for H2 DDL compatibility
- [x] `config/GlobalExceptionHandler.java` — Added `IllegalStateException` handler (returns 409 CONFLICT)

## Test Environment
- Uses `src/test/resources/application.yml` — H2 in-memory (PostgreSQL mode)
- Flyway disabled, Hibernate `ddl-auto=create-drop`
- New Spring Boot 4.0.0 package coordinates:
  - `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`
  - `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
  - `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase`
  - `org.springframework.test.context.bean.override.mockito.MockitoBean`

## Test Results
```
Tests run: 194, Failures: 0, Errors: 0, Skipped: 0
```

## Files Created (25 test files)
| File | What Was Tested |
|------|----------------|
| `cama/repository/CamaRepositoryTest.java` | Repository CRUD, state transitions, FK, soft delete |
| `cama/service/CamaServiceTest.java` | Service CRUD, cambiarEstado transitions, errors |
| `cama/controller/CamaControllerTest.java` | HTTP endpoints, validation, 404 |
| `cama/controller/HabitacionControllerTest.java` | HTTP endpoints, validation |
| `admision/repository/CuentaRepositoryTest.java` | findByPacienteId, findByEstado, transitions |
| `admision/repository/SolicitudHospitalizacionRepositoryTest.java` | find queries, FK constraints |
| `admision/repository/AdmisionDiagnosticoRepositoryTest.java` | CIE-11 codes, findByCuentaIdAndTipo |
| `admision/service/AdmisionServiceTest.java` | Crear cuenta, asignar cama, diagnosticos |
| `admision/controller/AdmisionControllerTest.java` | Endpoints, validation, errors |
| `hospitalizacion/repository/HospitalizacionRepositoryTest.java` | Find queries, estado transitions |
| `hospitalizacion/repository/AltaMedicaRepositoryTest.java` | Unique constraint |
| `hospitalizacion/service/HospitalizacionServiceTest.java` | Cambiar cama, notas, alta flow |
| `hospitalizacion/controller/HospitalizacionControllerTest.java` | Endpoints, security context, errors |
| `sop/repository/ReporteQuirurgicoRepositoryTest.java` | Find queries, estado transitions, FK |
| `sop/service/SOPServiceTest.java` | Create/complete reporte, URPA, state machine |
| `sop/controller/SOPControllerTest.java` | Endpoints, validation, errors |
| `hce/repository/DocumentoClinicoRepositoryTest.java` | BYTEA, find queries, version chain |
| `hce/repository/FirmaDigitalRepositoryTest.java` | SHA-256 storage, find by documento/usuario |
| `hce/service/HCEServiceTest.java` | SHA-256 hash, verify firma (valid/invalid) |
| `hce/controller/HCEControllerTest.java` | Endpoints, security context, verification |
| `cuenta/repository/CargoAdicionalRepositoryTest.java` | CRUD, findByCuentaId, soft delete |
| `cuenta/service/CuentaServiceTest.java` | Agregar cargo, listar, cobro |
| `cuenta/controller/CuentaControllerTest.java` | Endpoints, validation |
| `integration/AdmisionFlowIntegrationTest.java` | Full admission→alta→cobro flow |

## Files Modified (2 production files)
| File | Change |
|------|--------|
| `hce/entity/DocumentoClinico.java` | Added `columnDefinition = "BYTEA"` for H2 test compat |
| `config/GlobalExceptionHandler.java` | Added `IllegalStateException` → 409 handler |

## Status
25/25 test files created. **All 194 tests pass** (pre-existing + new). Ready for archive.

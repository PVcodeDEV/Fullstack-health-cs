# Design: Módulo Clínica

## Technical Approach

Five sub-packages (`admision`, `hospitalizacion`, `sop`, `hce`, `cuenta`) per backend-layering convention + cross-cutting `cama/` for bed/room state machine. Each follows `entity/` → `repository/` → `service/` → `dto/` → `controller/` with `@PreAuthorize`. Entity column prefix per table (`cue_`, `cup_`, `sol_`, `diag_`, `hosp_`, `cam_`, `nota_`, `smed_`, `alt_`, `sop_`, `urpa_`, `hce_`, `fir_`, `cta_`, `cama_`, `hab_`). HCE uses BYTEA for MVP content storage. Cuenta lives in clinica with extraction interface for Caja module.

## Architecture Decisions

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Cama in `hospitalizacion/` vs own package | Own package avoids circular dependency | `clinica/cama/` — admisión assigns, hospitalización manages state |
| HCE content in BYTEA vs S3 | BYTEA: simple, no infra; perf limit at scale | BYTEA for MVP, S3 migration path documented |
| Cuenta in `admision/` vs own sub-package | Cuenta lifecycle spans beyond admission | `clinica/cuenta/` — separate sub-package with `@ExtractionPlan` marker for Caja |
| Single migration vs per-subpackage | Per-subpackage: traceable but more files | One migration per subpackage (V14–V19) |
| Solicitud auto-generada via service vs event | Event: loose coupling, async complexity | Synchronous service call in single `@Transactional` — simpler, correct for MVP |

## Data Flow

```
Admisión (UI busca paciente)
  │
  ▼
Crear Cuenta (select paquete quirúrgico → tipo hab known)
  │
  ▼  [@Transactional]
Auto-genera SolicitudHospitalización (estado: PENDIENTE)
  │
  ▼
Asigna cama específica (disponible → ocupado)
  │
  ▼
Crea HC (DocumentoClinico tipo HISTORIA_CLINICA)
  │
  ▼
Hospitalización confirma cama desde solicitud
  │
  ├── CambioHabitacion (opcional, log)
  ├── NotaEvolucion / SolicitudMedicamento
  │
  ▼
AltaMédica (estado: ALTA)
  │
  ▼
Caja cobra cuenta ──→ Cama liberada (ocupado → disponible)
```

**Bed state machine**: `disponible ↔ ocupado ↔ mantenimiento`

## File Changes

### New — Backend entities (~75 files)

| Sub-package | Key Entities | Prefix |
|-------------|-------------|--------|
| `clinica/cama/` | Cama, Habitacion | `cama_`, `hab_` |
| `clinica/admision/` | Cuenta, CuentaPaquete, SolicitudHospitalizacion, AdmisionDiagnostico | `cue_`, `cup_`, `sol_`, `diag_` |
| `clinica/hospitalizacion/` | Hospitalizacion, CambioHabitacion, NotaEvolucion, SolicitudMedicamento, AltaMedica | `hosp_`, `cam_`, `nota_`, `smed_`, `alt_` |
| `clinica/sop/` | ReporteQuirurgico, URPARegistro | `sop_`, `urpa_` |
| `clinica/hce/` | DocumentoClinico (contenido BYTEA + firma: usuario_id, timestamp, hash SHA-256, IP origen) | `hce_`, `fir_` |
| `clinica/cuenta/` | CargoAdicional, view interface for Caja extraction | `cta_` |

Each gets: entity/ → repository/ → service/ → dto/{Request,Response} → controller/ = ~5 files per entity

### Modified

| File | Change |
|------|--------|
| `clinica/paciente/controller/PacienteController.java` | Add `@PreAuthorize("hasAuthority('paciente:*')")` |
| `clinica/medico/controller/MedicoController.java` | Add `@PreAuthorize("hasAuthority('medico:*')")` |
| `seguridad/bootstrap/DataInitializer.java` | Seed ~25 new permisos (crear/editar/eliminar/ver/aprobar × 5 subdominios) |

### Flyway Migrations

| File | Content |
|------|---------|
| `V14__clinica_camas_habitaciones.sql` | `tb_camas`, `tb_habitaciones` + seed rooms |
| `V15__clinica_admision.sql` | `tb_cuentas`, `tb_cuentas_paquetes`, `tb_solicitudes_hospitalizacion`, `tb_admision_diagnosticos` |
| `V16__clinica_hospitalizacion.sql` | `tb_hospitalizaciones`, `tb_cambios_habitacion`, `tb_notas_evolucion`, `tb_solicitudes_medicamento`, `tb_altas_medicas` |
| `V17__clinica_sop.sql` | `tb_reportes_quirurgicos`, `tb_registros_urpa` |
| `V18__clinica_hce.sql` | `tb_documentos_clinicos` (BYTEA content) |
| `V19__clinica_cuenta.sql` | `tb_cargos_adicionales` |

## Interfaces / Contracts

```java
// Cuenta extraction boundary for Caja module (future)
public sealed interface CuentaProjection permits Cuenta, CuentaExtract {
    Long id();
    Long pacienteId();
    String estado();
    List<CargoProjection> cargos();
}

// HCE digital signature
public record FirmaDigital(
    Long usuarioId,
    LocalDateTime timestamp,
    String hashSha256,
    String ipOrigen
) {}
```

## Testing Strategy

| Layer | What | How |
|-------|------|-----|
| Repository | Custom queries, state transitions, cascade | `@DataJpaTest` with H2 |
| Service | Admission flow, bed state transitions, discharge logic | Mockito unit tests |
| Controller | HTTP status, validation, @PreAuthorize 403 | `@WebMvcTest` + MockMvc |
| Integration | Full admission→discharge flow (multi-step) | `@SpringBootTest` + `@AutoConfigureMockMvc` |

## Migration / Rollout

1. Deploy off-hours (16 GB RAM Windows Server: expect ~5 min compile + Flyway apply).
2. Run `mvn compile` to verify new entities + modified controllers.
3. Seed ~25 permisos + ADMIN gets all via existing `assignAllPermisosToAdmin()`. Service accounts (RECEPCION, MEDICO, ENFERMERIA) get granular permisos manually post-deploy.
4. `git revert` rollback: revert V14–V19 + DataInitializer changes + new entities. `PacienteController` / `MedicoController` unchanged by revert.

## Resolved Questions

- ✅ **Paquete quirúrgico**: seed table `tb_paquetes_quirurgicos` in clinica (MVP) with extraction plan when Caja module exists. No pricing fields — Admisión sees only non-financial data.
- ✅ **HCE size limit**: 10 MB max before S3 migration triggers. Below 10 MB → BYTEA in DB. Above → external storage migration path.

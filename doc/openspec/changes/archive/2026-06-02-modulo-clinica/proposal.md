# Proposal: Módulo Clínica

## Intent

Core clinical module covering patient admission, hospitalization, surgical procedures, electronic health records (HCE), and patient account tracking. This is the primary operational module of the ERP Clínico, used daily by recepción, médicos, enfermería, and farmacia.

## Scope

### In Scope
- 5 sub-packages: `admision/`, `hospitalizacion/`, `sop/`, `hce/`, `cuenta/`
- Room/bed: created by ADMIN (Sistemas), assigned by Admisión, changed by Hospitalización, released on alta+cobro
- Surgical package (incluye tipo habitación) selected at account creation, solicitud de hospitalización auto-generada
- Admisión ve paquete sin precio, selecciona cama específica dentro del tipo de habitación del paquete
- HCE: digital docs with internal signature (usuario + timestamp + hash + IP), MVP via BYTEA
- `@PreAuthorize` on all new + existing controllers (Paciente, Medico)
- ~25 new permisos seeded in `seguridad` DataInitializer
- Cuenta in clinica with documented extraction boundary

### Out of Scope
- Paquete quirúrgico pricing/catalog (Caja — future)
- Inventory, drug dispensing (Farmacia)
- Alta + payment confirmation (Caja)
- External HCE storage beyond BYTEA

## Capabilities

### New Capabilities
- `clinica-admision`: Admission, surgery scheduling, account creation, HC creation, CIE-11 diagnosis, bed assignment
- `clinica-hospitalizacion`: Room changes, nursing care, evolution notes, medication requests, discharge
- `clinica-sop`: Surgical reports, URPA recovery
- `clinica-hce`: Digital clinical documents, internal signature chain
- `clinica-cuenta`: Account tracking, package + extra charges, extraction-ready

### Modified Capabilities
None — no existing spec changes. `modulo-autorizacion` permission model reused as-is.

## Approach

5 sub-packages per backend-layering convention. Each gets CRUD + `@PreAuthorize`. Cuenta with extraction markers. HCE uses `BYTEA` for MVP. Paquete quirúrgico incluye tipo habitación → al seleccionarlo genera solicitud de hospitalización automática → Admisión asigna cama específica. Room/bed as state machine: disponible → ocupado → mantenimiento.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `clinica/{admision,hosp,sop,hce,cuenta}/` | New | 5 sub-packages, ~20 entities |
| `clinica/{paciente,medico}/controller/*` | Modified | Add @PreAuthorize |
| `seguridad/config/DataInitializer.java` | Modified | Seed ~25 new permisos |
| `clinica/entity/Cama.java` | New | Room/bed entity + state |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Cuenta/Caja boundary rework | Medium | Mark extraction plan in design |
| BYTEA performance at scale | Low | Document S3 migration path |
| Rollout disrupts operations | Medium | Deploy off-hours |

## Rollback Plan

1. `git revert` new clinica migrations + DataInitializer permiso changes
2. Re-deploy; verify existing Paciente/Medico controllers work

## Dependencies

- `modulo-autorizacion` — @PreAuthorize support
- `modulo-persona` — Persona FK in Paciente/Medico
- `maestro-catalogos-clinicos` — TipoHabitacion, TipoAtencion
- `maestro-cie11` — diagnosis codes
- `maestro-catalogos-organizacion` — TipoDocumentoClinico, AreaFuncional

## Success Criteria

- [ ] 5 sub-packages with CRUD + @PreAuthorize
- [ ] Admisión crea cuenta → selecciona paquete (incluye tipo hab.) → genera solicitud hospitalización automática → asigna cama específica → crea HC
- [ ] Hospitalización confirma la cama asignada desde la solicitud automática
- [ ] Cambio de habitación durante estancia (Hospitalización)
- [ ] Alta clínica → Caja cobra → cama liberada
- [ ] HCE document with valid digital signature
- [ ] 403 for unauthorized roles on all new endpoints
- [ ] Existing Paciente/Medico endpoints secured

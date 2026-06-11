# Design: Maestro — Catálogos Base

## Technical Approach

Build layer-zero master data: 19 JPA entities, Flyway migrations (schema + seed), REST CRUD controllers. Flat-per-layer structure under `com.clinica.maestro`. All tables use soft delete, audit timestamps, and the approved naming convention (`tb_<plural>` / `<abrev>_<campo>`).

## Architecture Decisions

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Per-entity subpackages vs flat layers | Per-entity isolates concerns; flat reduces boilerplate for identical CRUD patterns | **Flat**: `maestro/entity/`, `maestro/repository/`, etc. |
| String PK vs auto-increment | String PK avoids codigo JOIN; auto-increment simplifies Hibernate identity | **Mixed**: Ubigeo/CIE-10/SUNAT use codigo-as-PK (external standard). Others: BIGSERIAL + unique codigo |
| BaseEntity with @AttributeOverride | Audit column names differ per entity (tdi_, esc_, etc.) | **BaseEntity** with `@AttributeOverrides` mapping common createdAt/updatedAt/activo to per-entity column names |
| Schema source | Hibernate ddl-auto vs Flyway | **Flyway creates schema**; Hibernate `ddl-auto: validate` (existing config). Flyway is single source of truth |
| Ubigeo hierarchy query | Three separate controllers vs one aggregated | **Separate controllers** per level; flat search endpoint for autocomplete |

## Database Model

All tables: `tb_<plural_snake>`. All columns: `<abrev>_<campo>`. Every table has `<abrev>_created_at` (TIMESTAMP NOT NULL), `<abrev>_updated_at` (TIMESTAMP), `<abrev>_activo` (BOOLEAN NOT NULL DEFAULT TRUE).

### Table Definitions

**tb_tipos_documento_identidad** (tdi) — PK: tdi_id BIGSERIAL. Columns: tdi_codigo_sunat VARCHAR(5) UNIQUE NOT NULL, tdi_nombre VARCHAR(100) NOT NULL, tdi_longitud_minima INT NOT NULL, tdi_longitud_maxima INT NOT NULL.

**tb_estados_civil** (esc) — PK: esc_id BIGSERIAL. Columns: esc_codigo_reniec VARCHAR(3) UNIQUE NOT NULL, esc_nombre VARCHAR(50) NOT NULL.

**tb_ubigeo_departamentos** (ubdep) — PK: ubdep_codigo VARCHAR(2). Columns: ubdep_nombre VARCHAR(100) NOT NULL.

**tb_ubigeo_provincias** (ubprov) — PK: ubprov_codigo VARCHAR(4). Columns: ubprov_nombre VARCHAR(100) NOT NULL, ubprov_departamento VARCHAR(2) NOT NULL FK → tb_ubigeo_departamentos(ubdep_codigo) ON DELETE RESTRICT.

**tb_ubigeo_distritos** (ubdist) — PK: ubdist_codigo VARCHAR(6). Columns: ubdist_nombre VARCHAR(100) NOT NULL, ubdist_provincia VARCHAR(4) NOT NULL FK → tb_ubigeo_provincias(ubprov_codigo) ON DELETE RESTRICT.

**tb_cie11_diagnosticos** (cie) — PK: cie_id BIGSERIAL. Columns: cie_codigo VARCHAR(8) UNIQUE NOT NULL, cie_descripcion VARCHAR(500) NOT NULL, cie_categoria VARCHAR(1) NOT NULL, cie_sexo_aplicable VARCHAR(5) NOT NULL DEFAULT 'AMBOS' CHECK (IN ('M','F','AMBOS')), cie_edad_minina INT, cie_edad_maxima INT, cie_version VARCHAR(10) NOT NULL DEFAULT 'CIE-11'. Without soft delete — all codes always visible, ordered by usage frequency.

**tb_especialidades_medicas** (espm) — PK: espm_id BIGSERIAL. Columns: espm_codigo VARCHAR(10) UNIQUE NOT NULL, espm_nombre VARCHAR(100) NOT NULL, espm_abreviatura VARCHAR(10).

**tb_tipos_paciente** (tpac) — PK: tpac_id BIGSERIAL. Columns: tpac_codigo VARCHAR(10) UNIQUE NOT NULL, tpac_nombre VARCHAR(100) NOT NULL.

**tb_tipos_atencion** (tate) — PK: tate_id BIGSERIAL. Columns: tate_codigo VARCHAR(10) UNIQUE NOT NULL, tate_nombre VARCHAR(100) NOT NULL, tate_requiere_habitacion BOOLEAN NOT NULL DEFAULT FALSE.

**tb_vias_administracion** (via) — PK: via_id BIGSERIAL. Columns: via_codigo VARCHAR(10) UNIQUE NOT NULL, via_nombre VARCHAR(100) NOT NULL.

**tb_formas_farmaceuticas** (ffar) — PK: ffar_id BIGSERIAL. Columns: ffar_codigo VARCHAR(10) UNIQUE NOT NULL, ffar_nombre VARCHAR(100) NOT NULL, ffar_requiere_preparacion BOOLEAN NOT NULL DEFAULT FALSE.

**tb_tipos_habitacion** (thab) — PK: thab_id BIGSERIAL. Columns: thab_codigo VARCHAR(10) UNIQUE NOT NULL, thab_nombre VARCHAR(100) NOT NULL, thab_tarifa_base DECIMAL(10,2) NOT NULL, thab_capacidad INT NOT NULL.

**tb_tipos_comprobante** (tcomp) — PK: tcomp_id SMALLINT. Columns: tcomp_codigo_sunat VARCHAR(2) UNIQUE NOT NULL, tcomp_nombre VARCHAR(100) NOT NULL.

**tb_tipos_moneda** (tmon) — PK: tmon_id SMALLINT. Columns: tmon_codigo_sunat VARCHAR(3) UNIQUE NOT NULL, tmon_nombre VARCHAR(100) NOT NULL, tmon_simbolo VARCHAR(5) NOT NULL.

**tb_unidades_medida** (umed) — PK: umed_id SMALLINT. Columns: umed_codigo_sunat VARCHAR(5) UNIQUE NOT NULL, umed_nombre VARCHAR(100) NOT NULL, umed_abreviatura VARCHAR(10).

**tb_areas_funcionales** (areaf) — PK: areaf_id BIGSERIAL. Columns: areaf_codigo VARCHAR(10) UNIQUE NOT NULL, areaf_nombre VARCHAR(100) NOT NULL, areaf_es_area_fisica BOOLEAN NOT NULL DEFAULT FALSE.

**tb_aseguradoras** (aseg) — PK: aseg_id BIGSERIAL. Columns: aseg_codigo VARCHAR(10) UNIQUE NOT NULL, aseg_nombre VARCHAR(150) NOT NULL, aseg_tipo VARCHAR(10) NOT NULL CHECK (IN ('PUBLICO','PRIVADO')), aseg_contrato_vigente BOOLEAN NOT NULL DEFAULT TRUE.

**tb_categorias_insumo** (categ) — PK: categ_id BIGSERIAL. Columns: categ_codigo VARCHAR(10) UNIQUE NOT NULL, categ_nombre VARCHAR(100) NOT NULL, categ_categoria_padre_id BIGINT FK → tb_categorias_insumo(categ_id) ON DELETE RESTRICT (nullable self-ref).

**tb_tipos_documento_clinico** (tdc) — PK: tdc_id BIGSERIAL. Columns: tdc_codigo VARCHAR(10) UNIQUE NOT NULL, tdc_nombre VARCHAR(100) NOT NULL, tdc_requiere_firma BOOLEAN NOT NULL DEFAULT FALSE.

### Index Strategy

| Table | Index | Type |
|-------|-------|------|
| tb_cie11_diagnosticos | ON cie_descripcion | GIN trigram (`CREATE INDEX IF NOT EXISTS idx_cie_desc_trgm ON tb_cie11_diagnosticos USING gin (cie_descripcion gin_trgm_ops)`) |
| tb_cie11_diagnosticos | ON cie_categoria | BTREE |
| tb_cie11_diagnosticos | ON cie_codigo | BTREE (unique, for autocomplete search) |
| All FK columns | ON fk_col | BTREE |
| All codigo columns | UNIQUE | BTREE (implicit from UNIQUE constraint) |
| High-volume catalogs (>500 rows) | Partial WHERE activo=true | BTREE partial |

### FK ON DELETE

All FKs use ON DELETE RESTRICT — master data must not cascade delete. Ubigeo chain (dep → prov → dist) uses RESTRICT at each level.

## Data Flow

```
Client → /api/v1/maestro/{recurso} → Controller (validate DTO)
  → Service (@Transactional, business rules) → Repository → DB
```

## File Changes

| Path | Action | Count |
|------|--------|-------|
| `maestro/entity/BaseEntity.java` + 19 entity classes | Create | 20 files |
| `maestro/repository/` 19 interfaces | Create | 19 files |
| `maestro/service/` 19 CRUD + 3 import services | Create | 22 files |
| `maestro/dto/` Request + Response records | Create | ~38 files |
| `maestro/controller/` 19 REST controllers | Create | 19 files |
| `db/migration/V1__` through `V9__` | Create | 9 files |
| `maestro/` tests per batch | Create | ~20 files |
| `ClinicaApplication.java` | Modify | Add `@EnableJpaAuditing` |

Total: ~147 new files, 1 modified.

## Java Entity Design

### BaseEntity

```java
@Getter @MappedSuperclass @EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate @Column
    private LocalDateTime updatedAt;
    @Column(nullable = false)
    private Boolean activo = true;
}
```

### Example: TipoDocumentoIdentidad

```java
@Entity @Table(name = "tb_tipos_documento_identidad")
@AttributeOverride(name = "createdAt", column = @Column(name = "tdi_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tdi_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tdi_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TipoDocumentoIdentidad extends BaseEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(name = "tdi_codigo_sunat", nullable = false, unique = true)
    private String codigoSunat;
    @Column(name = "tdi_nombre", nullable = false)
    private String nombre;
    @Column(name = "tdi_longitud_minima", nullable = false)
    private Integer longitudMinima;
    @Column(name = "tdi_longitud_maxima", nullable = false)
    private Integer longitudMaxima;
}
```

### Example: UbigeoProvincia (FK)

```java
@ManyToOne(fetch = LAZY)
@JoinColumn(name = "ubprov_departamento", referencedColumnName = "ubdep_codigo")
private UbigeoDepartamento departamento;
```

### Enable Auditing

Add `@EnableJpaAuditing` to `ClinicaApplication`.

## Flyway Migrations

| Version | Name | Schema | Seed |
|---------|------|--------|------|
| V1 | maestro_tipos_documento_identidad | CREATE TABLE | DNI, CE, RUC, Pasaporte, Carnet Ext. |
| V2 | maestro_estados_civil | CREATE TABLE | Soltero, Casado, Divorciado, Viudo, Conviviente |
| V3 | maestro_ubigeo | 3x CREATE TABLE | No (data in V8) |
| V4 | maestro_cie11 | CREATE TABLE | No (data in V9) |
| V5 | maestro_catalogos_clinicos | 7x CREATE TABLE | All clinical seeds |
| V6 | maestro_catalogos_financieros | 3x CREATE TABLE | SUNAT catalogs |
| V7 | maestro_catalogos_organizacion | 4x CREATE TABLE | Organization seeds |
| V8 | seed_ubigeo | — | ~1874 districts |
| V9 | seed_cie10 | — | ~17k codes |

Use `CREATE INDEX IF NOT EXISTS ... CONCURRENTLY` for production safety. For H2 dev: omit `CONCURRENTLY` (not supported).

## Testing Strategy

| Layer | Tool | Focus |
|-------|------|-------|
| Repository | @DataJpaTest + @Sql | CRUD operations, unique constraint violations, FK integrity |
| Service | Mockito (mocked repo) | Business rules: codigo uniqueness, soft delete validation, inactive filter |
| Controller | @WebMvcTest | HTTP status (200/201/400/404/409), DTO validation errors, response shape |
| Integration | @SpringBootTest + MockMvc | Full flow: migrate → seed → read → write per batch |

## Migration / Rollout

Layer zero — no dependent data. Rollback: `DROP TABLE IF EXISTS tb_* CASCADE`. Seed data idempotent via Flyway checksums.

## Open Questions

- [ ] H2 compatibility for GIN trigram indexes: may need conditional migration or skip for dev profile
- [ ] CIE-10 exact CSV source format from MINSA for seed import script
- [ ] Confirm DNI check digit (módulo 11) validation — implement as service method or Bean Validation constraint

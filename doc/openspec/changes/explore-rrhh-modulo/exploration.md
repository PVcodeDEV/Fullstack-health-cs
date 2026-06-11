## Exploration: Módulo RRHH Full REMYPE — Re-exploración Completa

### Current State

#### Paquete `com.clinica.rrhh` — Estado Actual

El paquete `rrhh/` existe con una **sola** subcarpeta `trabajador/` con CRUD completo pero limitado:

| Capa | Archivo | Estado |
|------|---------|--------|
| Entity | `Trabajador.java` | ✅ Básico (5 campos: persona, codigo, fechaIngreso, cargo, areaFuncional) |
| Repository | `TrabajadorRepository.java` | ✅ Búsquedas básicas |
| Service | `TrabajadorService.java` | ✅ CRUD simple |
| Controller | `TrabajadorController.java` | ✅ REST endpoints |
| DTOs | Request/Response | ✅ Básicos |
| Tests | Service + Repository | ✅ Cobertura CRUD |

No existe ningún otro sub-paquete: ni `contrato/`, `planilla/`, `asistencia/`, `cts/`, `vacacion/`, `documento/`.

#### Entity `Trabajador` Actual

```java
public class Trabajador extends BaseEntity {
    Long id;
    Persona persona;                       // FK → tb_personas (UNIQUE)
    String codigoTrabajador;               // UNIQUE
    LocalDate fechaIngreso;
    String cargo;                          // Free text
    Long areaFuncionalId;                  // FK → tb_areas_funcionales
}
```

**Insuficiente** para REMYPE. Faltan: tipoTrabajador, regimenLaboral, datos bancarios, colegiatura, carga familiar, contacto emergencia, situación especial, fecha cese, motivo cese.

#### Entity `Medico` (en `clinica.medico`)

```java
public class Medico extends BaseEntity {
    Long id;
    Persona persona;
    Trabajador trabajador;        // nullable — médicos externos
    String cmp;                   // UNIQUE
    Long especialidadId;
    Boolean esEspecialista;
}
```

El `cmp` (colegiatura médica) vive en Medico. Para otros profesionales (enfermeras con CEP, técnicos) no hay entidad de colegiatura. **Hay que migrar esto a RRHH o crear un mecanismo general de colegiatura profesional.**

#### Migraciones Existentes

- **V11**: Creó `tb_trabajadores` y `tb_medicos`
- **V13**: Agregó `med_persona_id` a médicos, hizo trabajador_id nullable
- **V7**: Creó catálogos de organización (tb_areas_funcionales, etc.)
- **V19**: Última migración — próxima sería V20

#### Catálogos en `maestro` Relevantes

- `TipoDocumentoIdentidad` (tb_tipos_documento_identidad) — DNI, CE, Pasaporte
- `EstadoCivil` (tb_estados_civil)
- `AreaFuncional` (tb_areas_funcionales) — usado por Trabajador
- `EspecialidadMedica` (tb_especialidades_medicas) — usado por Medico

### Datos Normativos Peruanos Vigentes (2026)

| Concepto | Valor | Base Legal |
|----------|-------|-----------|
| **RMV** (Remuneración Mínima Vital) | S/ 1,130/mes | DS 006-2024-TR (desde ene-2025) |
| **UIT** 2026 | S/ 5,500 | DS 301-2025-EF |
| **REMYPE Micro** | Ventas ≤ 150 UIT (S/ 825,000) | Ley 30056 |
| **REMYPE Pequeña** | Ventas ≤ 1,700 UIT (S/ 9,350,000) | Ley 30056 |
| **ONP** (aporte trabajador) | 13% | DL 19990 |
| **AFP** (aporte obligatorio) | 10% | DL 25897 |
| **AFP comisión flujo** | 1.47% (Habitat) ~ 1.69% (Profuturo) | SBS 2026 |
| **AFP prima seguro** | 1.37% (igual todas) | SBS |
| **ESSALUD** (empleador) | 9% | Ley 26790 |
| **Asignación Familiar** | 10% RMV = S/ 113 | Ley 25129 |
| **Bonif. Extra Gratif.** (Ley 30334) | 9% sobre gratificación | Ley 30334 |

#### Beneficios REMYPE por Tamaño (2026)

| Beneficio | Microempresa | Pequeña Empresa | Régimen General |
|-----------|-------------|-----------------|-----------------|
| CTS | No paga | 15 días/año (½ sueldo) | 1 sueldo/año |
| Gratificaciones | No paga | ½ sueldo × 2 (jul+ dic) | 1 sueldo × 2 |
| Vacaciones | 15 días | 15 días | 30 días |
| Indemnización despido | 10 días/año (tope 90) | 20 días/año (tope 120) | 1.5 sueldos/año |
| EsSalud | 9% (o SIS-MYPE opcional) | 9% obligatorio | 9% |
| Asignación Familiar | No aplica | No aplica | 10% RMV |

### Affected Areas

| Archivo/Path | Por qué está afectado |
|-------------|----------------------|
| `rrhh/trabajador/entity/Trabajador.java` | Expandir: tipoTrabajador, regimenLaboral, fechaini/ findel, motivos, datos bancarios, filiación, colegiatura, carga familiar |
| `rrhh/contrato/` | Crear sub-paquete completo |
| `rrhh/planilla/` | Crear sub-paquete completo |
| `rrhh/asistencia/` | Crear sub-paquete completo |
| `rrhh/cts/` | Crear sub-paquete completo |
| `rrhh/vacacion/` | Crear sub-paquete completo |
| `rrhh/gratificacion/` | Crear sub-paquete completo |
| `rrhh/documento/` | Crear sub-paquete completo |
| `rrhh/type/` | Crear enums y catálogos internos |
| `clinica/medico/entity/Medico.java` | Colegiatura: migrar CMP a entidad general de colegiatura en rrhh |
| `maestro/entity/` | Potenciales nuevos catálogos: TipoContrato, CategoriaOcupacional, MotivoCese, Banco, TipoSuspension |
| `db/migration/V20+` | Múltiples nuevas migraciones |
| `seguridad/bootstrap/DataInitializer.java` | Nuevos permisos granulares para rrhh |
| `application.yml` | Parámetros de configuración: tasas AFP/ONP/ESSALUD, valores por defecto |
| `pom.xml` | Posibles dependencias para exportación (T-Registro, PDF generation) |

### Sub-packages Propuestos

```
com.clinica.rrhh/
├── trabajador/           ← Expandir significativamente
│   ├── entity/Trabajador.java
│   ├── entity/ColegiaturaProfesional.java   ← NUEVO (reemplaza cmp en Medico)
│   ├── entity/CargaFamiliar.java           ← NUEVO
│   ├── repository/
│   ├── service/
│   ├── dto/
│   └── controller/
├── contrato/             ← NUEVO
│   ├── entity/Contrato.java
│   ├── entity/ContratoRenovacion.java
│   ├── repository/
│   ├── service/
│   ├── dto/
│   └── controller/
├── planilla/             ← NUEVO (el más complejo)
│   ├── entity/PeriodoPlanilla.java
│   ├── entity/Liquidacion.java
│   ├── entity/Descuento.java
│   ├── entity/AporteEmpleador.java
│   ├── repository/
│   ├── service/
│   │   ├── PlanillaService.java
│   │   ├── CalculadorONPService.java
│   │   ├── CalculadorAFPStrategy.java    ← Strategy pattern por AFP
│   │   ├── CalculadorQuintaCategoria.java
│   │   └── ExportadorPlameService.java
│   └── dto/
├── tregistro/            ← NUEVO (exportación SUNAT T-Registro)
│   ├── service/ExportadorTRegistroService.java
│   └── dto/TRegistroRow.java
├── cts/                  ← NUEVO
│   ├── entity/DepositoCTS.java
│   ├── entity/RetiroCTS.java
│   ├── repository/
│   └── service/
├── gratificacion/        ← NUEVO
│   ├── entity/Gratificacion.java
│   └── service/
├── vacacion/             ← NUEVO
│   ├── entity/Vacacion.java
│   ├── entity/ProgramacionVacaciones.java
│   └── service/
├── asistencia/           ← NUEVO
│   ├── entity/Marcacion.java
│   ├── entity/Horario.java
│   ├── entity/Turno.java
│   ├── entity/Permiso.java
│   ├── repository/
│   └── service/
├── documento/            ← NUEVO
│   ├── entity/DocumentoTrabajador.java
│   ├── repository/
│   └── service/
├── utilidad/             ← NUEVO (profit sharing)
│   ├── entity/Utilidad.java
│   └── service/
└── type/                 ← NUEVO (enums)
    ├── TipoTrabajador.java
    ├── RegimenLaboral.java
    ├── TipoContrato.java
    ├── SituacionEspecial.java
    ├── MotivoCese.java
    ├── TipoSuspension.java
    ├── TipoDocumento laboral.java
    ├── TipoColegiatura.java
    ├── EstadoVacacion.java
    └── TipoJornada.java
```

### Sub-paquetes por Módulo — Entities y Campos Clave

#### 1. Trabajador Expandido

```
Trabajador (tb_trabajadores) — expandir desde V11 actual
├── tipoTrabajador: TipoTrabajador enum (MEDICO, ENFERMERA, TECNICO, ADMINISTRATIVO, etc.)
├── regimenLaboral: RegimenLaboral enum (PRIVADO, CAS, LOCACION, TERCERO)
├── fechaCese: LocalDate (nullable)
├── motivoCese: MotivoCese enum (nullable)
├── estaSindicado: boolean
├── tieneDiscapacidad: boolean
├── esComisionConfianza: boolean
├── numeroHijos: int (para asignación familiar)
│
├── Datos Bancarios (embedded)
│   ├── banco: String
│   ├── cuentaSueldo: String
│   └── cci: String
│
├── Contacto Emergencia (embedded)
│   ├── emergenciaNombre: String
│   ├── emergenciaTelefono: String
│   └── emergenciaParentesco: String
│
└── Remuneración
    ├── remuneracionMensual: BigDecimal
    └── moneda: String (PEN/USD)
```

**Nueva entidad: `ColegiaturaProfesional`** (reemplaza `cmp` en Medico)

```java
ColegiaturaProfesional (tb_colegiaturas_profesionales)
├── trabajadorId: FK → tb_trabajadores
├── tipoColegiatura: TipoColegiatura enum (CMP, CEP, COP, CIP, etc.)
├── numeroColegiatura: String (UNIQUE)
├── fechaRegistro: LocalDate
└── activo: boolean
```

Esto migra el `cmp` desde `Medico`: los médicos tendrán una ColegiaturaProfesional con tipo=CMP. Médicos externos (sin trabajador) pueden mantener su referencia directa a Persona.

**Nueva entidad: `CargaFamiliar`**

```java
CargaFamiliar (tb_cargas_familiares)
├── trabajadorId: FK → tb_trabajadores
├── tipo: enum (CONYUGE, HIJO, OTRO)
├── nombres: String
├── numeroDocumento: String
├── fechaNacimiento: LocalDate
└── activo: boolean
```

#### 2. Contratos

```java
Contrato (tb_contratos)
├── trabajadorId: FK → tb_trabajadores
├── tipoContrato: TipoContrato enum
│   (INDETERMINADO, DETERMINADO, CAS, LOCACION, TIEMPO_PARCIAL, INTERMITENTE)
├── fechaInicio: LocalDate
├── fechaFin: LocalDate (nullable)
├── periodoPruebaDias: int
├── remuneracion: BigDecimal
├── tipoJornada: TipoJornada enum (REGULAR, PARCIAL, NOCTURNA)
├── condicionesEspeciales: TEXT
├── archivoPdfId: FK → tb_documentos_trabajador (nullable)
├── activo: boolean
└── esUltimo: boolean

ContratoRenovacion (tb_contratos_renovaciones)
├── contratoId: FK → tb_contratos
├── fechaRenovacion: LocalDate
├── nuevoInicio: LocalDate
├── nuevoFin: LocalDate (nullable)
├── nuevaRemuneracion: BigDecimal
── motivo: String
```

#### 3. Planilla / Nómina (PLAME)

```java
PeriodoPlanilla (tb_periodos_planilla)
├── anio: int
├── mes: int
├── tipo: enum (MENSUAL, QUINCENAL, SEMANAL)
├── cerrado: boolean
├── fechaCierre: LocalDate
└── fechaPago: LocalDate

Liquidacion (tb_liquidaciones) — por trabajador por período
├── periodoPlanillaId: FK
├── trabajadorId: FK
├── contratoId: FK (versión vigente)
├── diasLaborados: int
├── diasNoLaborados: int
├── horasExtras25: BigDecimal (125%)
├── horasExtras100: BigDecimal (100%)
├── remuneracionOrdinaria: BigDecimal  ← cálculo
├── asignacionFamiliar: BigDecimal      ← S/113 si aplica
├── totalIngresos: BigDecimal
│
├── descuentoSistemaPension: String     ← ONP o AFP
├── descuentoAportePension: BigDecimal  ← 10% (AFP) o 13% (ONP)
├── descuentoComisionAFP: BigDecimal    ← 1.47-1.69% según AFP
├── descuentoPrimaSeguroAFP: BigDecimal ← 1.37%
├── descuentoQuintaCategoria: BigDecimal
├── totalDescuentos: BigDecimal
│
├── aporteEssalud: BigDecimal           ← 9% empleador
├── aporteSenati: BigDecimal            ← si aplica
├── aporteSctr: BigDecimal              ← si aplica
│
├── netoPagar: BigDecimal
└── estado: enum (CALCULADO, PAGADO, ANULADO)
```

**Servicios de Cálculo:** Strategy Pattern

```java
// Interfaz strategy
interface CalculadorDescuentoPension {
    Resultado calcular(BigDecimal remuneracion);
}

// Implementaciones concretas
class CalculadorAFPStrategy implements CalculadorDescuentoPension {
    private final String afp;        // HABITAT, INTEGRA, PRIMA, PROFUTURO
    private final BigDecimal tasaAporte = new BigDecimal("10.00");
    private final BigDecimal tasaComision;     // 1.47, 1.55, 1.60, 1.69
    private final BigDecimal tasaPrima = new BigDecimal("1.37");
    private final BigDecimal topeRAM;          // S/12,598.91 (Q2 2026)
}

class CalculadorONPService implements CalculadorDescuentoPension {
    private final BigDecimal tasa = new BigDecimal("13.00");
}

class CalculadorQuintaCategoriaService {
    // UIT actual: S/5,500
    // 7 UIT exoneradas = S/38,500
    // Escala: 8%, 14%, 17%, 20%, 30%
    // Deducción: 3 UIT (gastos)
}
```

#### 4. T-Registro (SUNAT)

No es entidad persistente separada — se **exporta** desde los datos de Trabajador + Contrato + CargaFamiliar.

Campos requeridos por SUNAT T-Registro:
- Tipo Documento, Número Documento (trabajador)
- Apellido Paterno, Materno, Nombres
- Fecha Nacimiento, Sexo
- Nacionalidad
- Tipo Trabajador (código SUNAT)
- Régimen Laboral
- Tipo Contrato (código SUNAT)
- Fecha Ingreso
- Ocupación (código SUNAT)
- Régimen Pensionario (ONP/AFP + cuál)
- Tipo Seguro (ESSALUD/SIS)
- Categoría Ocupacional
- RUC Empleador
- Discapacidad (Sí/No)
- Sindicalizado (Sí/No)
- Número de Hijos
- Datos de derechohabientes (cónyuge, hijos)

Archivo plano de exportación en formato SUNAT (delimitado o de ancho fijo).

#### 5. CTS (Compensación por Tiempo de Servicios)

```java
DepositoCTS (tb_depositos_cts)
├── trabajadorId: FK
├── periodo: enum (MAYO_OCTUBRE, NOVIEMBRE_ABRIL)
├── anio: int
├── remuneracionComputable: BigDecimal
├── gratificacionProporcional: BigDecimal (1/6 gratif semestral)
├── montoDeposito: BigDecimal
├── banco: String
├── cuentaCTS: String
├── fechaDeposito: LocalDate
├── tasaInteres: BigDecimal
└── estado: enum (DEPOSITADO, RETIRADO, PARCIAL)

RetiroCTS (tb_retiros_cts)
├── depositoId: FK
├── fechaRetiro: LocalDate
├── montoRetiro: BigDecimal
├── motivo: String  (necesidad acreditada según ley)
└── documentoSustento: byte[]
```

**Cálculo REMYPE Pequeña Empresa:** 15 días por año completo (½ sueldo).
Por semestre: (Remuneración Computable + 1/6 Gratificación) / 2 * 50%.

**Cálculo REMYPE Microempresa:** No paga CTS (0).

#### 6. Gratificaciones

```java
Gratificacion (tb_gratificaciones)
├── trabajadorId: FK
├── periodo: enum (JULIO, DICIEMBRE)
├── anio: int
├── mesesTrabajados: int (para proporcional)
├── remuneracionComputable: BigDecimal
├── montoGratificacion: BigDecimal
├── bonificacionExtraordinaria: BigDecimal (9% Ley 30334)
├── esProporcional: boolean
├── fechaPago: LocalDate
└── estado: enum (CALCULADO, PAGADO)
```

**REMYPE Pequeña:** ½ sueldo × 2. **Micro:** No paga.

#### 7. Vacaciones

```java
Vacacion (tb_vacaciones)
├── trabajadorId: FK
├── periodoGenerado: String (ej. "2025-2026")
├── diasGenerados: int (REMYPE: 15, General: 30)
├── diasGozados: int
├── diasCompensados: int
├── saldoPendiente: int
└── activo: boolean

ProgramacionVacaciones (tb_programacion_vacaciones)
├── vacacionId: FK
├── fechaInicio: LocalDate
├── fechaFin: LocalDate
├── fraccionada: boolean
├── diasFraccion: int (si fraccionada)
├── estado: enum (PROGRAMADA, EN_CURSO, GOZADA, CANCELADA)
└── compensacionEconomica: boolean
```

#### 8. Utilidades (Profit Sharing)

```java
Utilidad (tb_utilidades)
├── anio: int
├── rentaNetaAnual: BigDecimal
├── porcentajeLegal: BigDecimal (= 8% para >20 trab, 0 para <20)
├── montoTotalDistribuir: BigDecimal
├── fechaPago: LocalDate
└── estado: enum (CALCULADO, DISTRIBUIDO, PAGADO)

DistribucionUtilidad (tb_distribucion_utilidades)
├── utilidadId: FK
├── trabajadorId: FK
├── diasLaborados: int
├── remuneracionAnual: BigDecimal
├── factorDias: BigDecimal (50% × días laborados / total días)
├── factorRemuneracion: BigDecimal (50% × remuneración / total remuneraciones)
├── montoAsignado: BigDecimal
└── pagado: boolean
```

**Para REMYPE:** Aplicar según Ley MYPE. Límite: 18 sueldos máximo.

#### 9. Asistencia / Horarios

```java
Horario (tb_horarios)
├── codigo: String (único)
├── nombre: String
├── horaEntrada: LocalTime
├── horaSalida: LocalTime
├── horaRefrigerioInicio: LocalTime (nullable)
├── horaRefrigerioFin: LocalTime (nullable)
├── tipoTurno: enum (MANIANA, TARDE, NOCHE, 24H, ROTATIVO)
├── diasSemana: String (ej. "L,M,X,J,V")
├── toleranciaMinutos: int
└── activo: boolean

Marcacion (tb_marcaciones)
├── trabajadorId: FK
├── horarioId: FK
├── fecha: LocalDate
├── horaEntrada: LocalTime
├── horaSalida: LocalTime
├── horaRefrigerioInicio: LocalTime (nullable)
├── horaRefrigerioFin: LocalTime (nullable)
├── tipo: enum (ORDINARIO, EXTRA, GUARDIA)
├── horasExtras25: BigDecimal
├── horasExtras100: BigDecimal
├── tardanzaMinutos: int
└── justificada: boolean

Permiso (tb_permisos)
├── trabajadorId: FK
├── fechaInicio: LocalDate
├── fechaFin: LocalDate
├── tipoPermiso: enum (VACACION, ENFERMEDAD, PERSONAL, LUTO, MATERNIDAD, PATERNIDAD, OTRO)
├── justificado: boolean
├── documentoSustento: byte[]
└── aprobadoPor: Long (usuarioid)
```

#### 10. Documentos del Trabajador

```java
DocumentoTrabajador (tb_documentos_trabajador)
├── trabajadorId: FK
├── tipoDocumento: enum
│   (CURRICULUM, CERTIFICADO_TRABAJO, CONTRATO, DNI_COPIA, TITULO,
│    CERTIFICADO_ESTUDIOS, BOLETA_PAGO, COMPROBANTE_CTS, CERTIFICADO_SALUD, OTRO)
├── nombreArchivo: String
├── contenido: byte[] (BYTEA)     ← mismo patrón que DocumentoClinico
├── tipoMime: String (application/pdf, image/jpeg, etc.)
├── tamanoBytes: Long
├── fechaSubida: LocalDate
├── vigenciaDesde: LocalDate (nullable)
├── vigenciaHasta: LocalDate (nullable)
└── activo: boolean
```

### Catálogos: Enums vs Tablas Maestro

| Elemento | ¿Enum o Tabla? | Razón |
|----------|---------------|-------|
| `TipoTrabajador` | **Tabla** en maestro | Puede crecer (nuevos roles clínicos), necesita código SUNAT |
| `RegimenLaboral` | **Enum** en rrhh | Fijo: PRIVADO, CAS, LOCACION, TERCERO |
| `TipoContrato` | **Tabla** en maestro | Cambia con legislación, necesita código SUNAT |
| `MotivoCese` | **Tabla** en maestro | Varios según SUNAT, puede cambiar |
| `TipoColegiatura` | **Tabla** en maestro | CMP, CEP, COP, CIP, etc. — relativamente fijo pero extensible |
| `SituacionEspecial` | **Enum** | SINDICALIZADO, DISCAPACIDAD, COMISION_CONFIANZA |
| `CategoriaOcupacional` | **Tabla** en maestro | Códigos SUNAT fijos pero muchos |
| `TipoDocumentoRRHH` | **Enum** | Fijo: CURRICULUM, CONTRATO, etc. |
| `EstadoVacacion` | **Enum** | Fijo: PROGRAMADA, GOZADA, etc. |
| `TipoJornada` | **Enum** | Fijo: REGULAR, PARCIAL, NOCTURNA |
| `TipoPermiso` | **Enum** | Fijo pero extensible |
| `AFP` | **Tabla** en maestro | Las 4 AFP fijas pero sus tasas cambian periódicamente |
| `Banco` | **Tabla** en maestro o existente | Ya puede existir en catalogo financiero |
| `TipoSuspension` | **Tabla** en maestro | Varios según ley laboral |

### Work Estimate — Líneas de Código Estimadas

| Sub-paquete | CL | Entidades | Servicios | Controladores | Tests | Migraciones SQL |
|------------|-----|-----------|-----------|---------------|-------|-----------------|
| `trabajador` (expandido) | ~800 | +3 (Colegiatura, CargaFamiliar) | Expandir existente | Expandir | ~150 | V20 |
| `contrato` | ~600 | 2 | 1 | 1 | ~200 | V21 |
| `planilla` | ~1,800 | 3 | 5 (estrategia + exportación) | 2 | ~500 | V22, V23 |
| `tregistro` | ~400 | 1 (DTO export) | 1 | 0 (solo export) | ~150 | — (usa V22) |
| `cts` | ~500 | 2 | 1 | 1 | ~150 | V24 |
| `gratificacion` | ~400 | 1 | 1 | 1 | ~100 | V25 |
| `vacacion` | ~500 | 2 | 1 | 1 | ~150 | V26 |
| `asistencia` | ~800 | 3 | 1 | 1 | ~200 | V27 |
| `documento` | ~300 | 1 | 1 | 1 | ~100 | V28 |
| `utilidad` | ~400 | 2 | 1 | 1 | ~100 | V29 |
| `type` (enums) | ~200 | — | — | — | — | — |
| Catálogos maestro | ~300 | ~6 entidades | Crear CRUDs | Crear | ~100 | V30 (seed data) |
| **Total** | **~7,000** | **~26** | **~14** | **~10** | **~1,900** | **~10 migraciones** |

### PR Slicing Strategy

Basado en un **presupuesto de 400 líneas por PR**, se necesitarían ~18 PRs. Agrupando por cohesión de dominio:

#### PR Slice 1: Worker Expansion + Catalogs (~350 líneas)
- Expandir `Trabajador` entity con nuevos campos
- Crear enums (`TipoTrabajador`, `RegimenLaboral`, `SituacionEspecial`, `TipoJornada`)
- Crear catálogos en maestro (`TipoContrato`, `CategoriaOcupacional`, `MotivoCese`, `TipoColegiatura`)
- Migración V20: ALTER TABLE tb_trabajadores + nuevas tablas catálogo
- Seed data para catálogos

#### PR Slice 2: Colegiatura + Carga Familiar (~250 líneas)
- `ColegiaturaProfesional` entity, repository, service, controller
- `CargaFamiliar` entity, repository, service, controller
- Migración V21
- Refactor: eliminar `cmp` de Medico, crear `ColegiaturaProfesional` para médicos existentes

#### PR Slice 3: Contratos (~350 líneas)
- `Contrato` entity, repository, service, controller, DTOs
- `ContratoRenovacion` entity, repository, service
- Migración V22
- Tests

#### PR Slice 4: Documentos del Trabajador (~250 líneas)
- `DocumentoTrabajador` entity (BYTEA pattern), repository, service, controller
- Migración V23
- Soporte de subida/descarga de archivos

#### PR Slice 5: Asistencia Base (~350 líneas)
- `Horario` entity, repository, service
- `Marcacion` entity, repository, service
- `Permiso` entity
- Migración V24

#### PR Slice 6: Planilla — Períodos + Liquidación Base (~400 líneas)
- `PeriodoPlanilla` entity
- `Liquidacion` entity + service (cálculo básico)
- Migración V25

#### PR Slice 7: Planilla — Cálculo de Descuentos (~400 líneas)
- `CalculadorONPService`
- `CalculadorAFPStrategy` (con AFP enum/table)
- `CalculadorQuintaCategoriaService`
- Tasas configurables en application.yml o tb_configuracion_api

#### PR Slice 8: Planilla — PLAME + T-Registro (~300 líneas)
- `ExportadorPlameService`
- `ExportadorTRegistroService`
- Formato de archivo plano SUNAT

#### PR Slice 9: CTS (~350 líneas)
- `DepositoCTS` entity, repository, service
- `RetiroCTS` entity
- Cálculo según REMYPE
- Migración V26

#### PR Slice 10: Gratificaciones (~250 líneas)
- `Gratificacion` entity, service
- Cálculo con bonificación extraordinaria
- Proporcional
- Migración V27

#### PR Slice 11: Vacaciones (~300 líneas)
- `Vacacion` entity, service
- `ProgramacionVacaciones` entity
- Cálculo proporcional, REMYPE (15 días)
- Migración V28

#### PR Slice 12: Utilidades (~300 líneas)
- `Utilidad` + `DistribucionUtilidad` entities
- Cálculo de distribución (50% días, 50% remuneración)
- Límite 18 sueldos
- Migración V29

#### PR Slice 13: Permisos + Seguridad (~200 líneas)
- Nuevos permisos granular en `DataInitializer`
- Roles: RRHH_ADMIN, RRHH_VIEWER

### Complexity Assessment

| Componente | Complejidad | Justificación |
|-----------|-------------|---------------|
| **CRUD Trabajador expandido** | Baja | Misma estructura existente, más campos |
| **Catálogos maestro** | Baja | Patrón repetido (TipoDocumentoIdentidad como referencia) |
| **Documentos trabajador** | Baja | BYTEA pattern ya existe en DocumentoClinico |
| **Contratos** | Media | Lógica de fechas, renovaciones, archivos |
| **Asistencia** | Media | Turnos rotativos de clínica, 24h, guardias |
| **Vacaciones** | Media | Cálculo proporcional, programación, REMYPE vs General |
| **CTS** | Media-Alta | Fórmula: remuneración computable + 1/6 gratificación, semestres, intereses |
| **Gratificaciones** | Media | Proporcional, bonif. extraordinaria 9% |
| **Utilidades** | Media | Fórmula de distribución, topes, REMYPE |
| **Cálculo AFP (Strategy Pattern)** | Media | 4 AFP con tasas diferentes, tope RAM, comisión mixta vs flujo |
| **Cálculo ONP** | Baja | Fijo 13% |
| **Cálculo Renta 5ta Categoría** | Alta | Escala progresiva, gratificaciones, deducciones, proyección anual |
| **PLAME Export** | Alta | Formato exacto SUNAT, validación de campos, cambios de versión |
| **T-Registro Export** | Media | Formato SUNAT, muchos campos, debe coincidir con T-Registro SUNAT |
| **Migración de CMP desde Medico** | Media-Alta | Data migration, referencias existentes, consistencia |

### Migration Strategy

| Migración | Contenido | Tipo |
|-----------|-----------|------|
| `V20__rrhh_expand_trabajador.sql` | ALTER tb_trabajadores (nuevos campos), CREATE tb_colegiaturas_profesionales, CREATE tb_cargas_familiares | DDL + seed |
| `V21__rrhh_contratos.sql` | CREATE tb_contratos, tb_contratos_renovaciones | DDL |
| `V22__rrhh_documentos.sql` | CREATE tb_documentos_trabajador | DDL |
| `V23__rrhh_horarios_asistencia.sql` | CREATE tb_horarios, tb_marcaciones, tb_permisos | DDL |
| `V24__rrhh_periodos_liquidaciones.sql` | CREATE tb_periodos_planilla, tb_liquidaciones | DDL |
| `V25__rrhh_cts.sql` | CREATE tb_depositos_cts, tb_retiros_cts | DDL |
| `V26__rrhh_gratificaciones.sql` | CREATE tb_gratificaciones | DDL |
| `V27__rrhh_vacaciones.sql` | CREATE tb_vacaciones, tb_programacion_vacaciones | DDL |
| `V28__rrhh_utilidades.sql` | CREATE tb_utilidades, tb_distribucion_utilidades | DDL |
| `V29__rrhh_catalogos_maestro.sql` | Nuevas tablas catálogo en maestro con seed data | DDL + seed |
| `V30__rrhh_seed_afp_bancos.sql` | Seed AFP con tasas, seed bancos | Seed |

Nota: V20-V28 son del módulo rrhh, V29-V30 son del módulo maestro.

### Risk Areas

1. **Precisión de Cálculos de Planilla** (ALTO RIESGO)
   - Errores en cálculo de AFP (comisión mixta vs flujo, tope RAM que cambia trimestralmente)
   - Renta de 5ta categoría: escala progresiva compleja, deducciones, proyección anual
   - EsSalud: 9% sobre remuneración, mínimo sobre RMV
   - **Mitigación**: Test parametrizados con valores actuales + strategy pattern + tabla de tasas configurable

2. **Cambios en Tasas Legales** (RIESGO MEDIO)
   - RMV cambia, AFP comisiones cambian, prima seguro cambia cada 2 años
   - UIT cambia anualmente
   - **Mitigación**: Tasas en `tb_configuracion_api` o `application.yml` con profile por año. No hardcodear.

3. **Formato SUNAT PLAME/T-Registro** (RIESGO MEDIO)
   - SUNAT cambia formatos sin previo aviso
   - Versiones de formulario (actual: PDT 0601 v4.3.0, PLAME Web desde ene-2024)
   - **Mitigación**: Abstraer exportación detrás de interfaz, versionar formatos. Mantener actualización separada.

4. **Migración de CMP desde Medico** (RIESGO ALTO)
   - Médicos existentes tienen `cmp` en tb_medicos
   - Médicos externos no tienen Trabajador
   - **Mitigación**: Migración V20 debe crear ColegiaturaProfesional para cada Medico con CMP. Médicos externos mantienen referencia directa a Persona. No eliminar columna `med_cmp` inmediatamente — marcar deprecated primero.

5. **Volumen de Datos de Asistencia** (RIESGO BAJO)
   - ~40 empleados × 1 marcación/día ≈ 1,200 registros/mes. Manejable en PostgreSQL.
   - **Mitigación**: Índices por trabajador + fecha. Particionamiento innecesario para este volumen.

6. **REMYPE: Clasificación correcta** (RIESGO MEDIO)
   - Micro vs Pequeña empresa cambia beneficios drásticamente (CTS, gratif, vacaciones)
   - Empresa puede cambiar de categoría anualmente según ventas
   - **Mitigación**: Parámetro `empresa.tipoRemype` en configuración con fecha de vigencia. Validaciones por periodo.

7. **Tipos de Contrato y Códigos SUNAT** (RIESGO BAJO)
   - Códigos de tipo trabajador, ocupación, contrato en SUNAT son fijos pero numerosos
   - **Mitigación**: Seed data completa. Validación de campos obligatorios para exportación T-Registro.

8. **Permisos y Seguridad** (RIESGO BAJO)
   - RRHH maneja datos sensibles (sueldos, datos bancarios, DNI)
   - **Mitigación**: Permisos granulares (`rrhh:sueldo:ver`, `rrhh:contrato:editar`). Logging de acceso a datos sensibles.

### Recommendation

**Enfoque**: Implementación completa del módulo RRHH con capacidad REMYPE total, en **13 PRs encadenados** ordenados por dependencia:

1. **PR #1**: Worker Expansion + Catalogs (base para todo lo demás)
2. **PR #2**: Colegiatura + Carga Familiar (depende de PR#1)
3. **PR #3**: Contratos (depende de PR#1)
4. **PR #4**: Documentos (independiente de PR#2, PR#3)
5. **PR #5**: Asistencia (independiente)
6. **PR #6-8**: Planilla (el más complejo, depende de PR#1)
7. **PR #9**: CTS (depende de planilla para datos de remuneración)
8. **PR #10**: Gratificaciones (depende de planilla)
9. **PR #11**: Vacaciones (independiente pero usa datos de trabajador)
10. **PR #12**: Utilidades (depende de planilla para datos anuales)
11. **PR #13**: Permisos + Seguridad (final, después de todos los módulos)

**Orden óptimo de implementación**: PR#1 → PR#2 → PR#3 → PR#4 → PR#5 → PR#6 → PR#7 → PR#8 → PR#9 → PR#10 → PR#11 → PR#12 → PR#13

Se puede comenzar con un **MVP de 6 PRs** (PR#1-6) que cubra: trabajador completo, contratos, documentos, asistencia básica, y cálculo de planilla (sin exportación PLAME aún).

### Decisiones Arquitectónicas Clave

| Decisión | Opción | Recomendación |
|----------|--------|---------------|
| Cálculo de pensiones | Strategy Pattern | `CalculadorDescuentoPension` interface con implementaciones por AFP y ONP |
| Tasas variables | Configuración dinámica | Tabla `tb_configuracion_api` + recarga periódica. No hardcodear. |
| Almacenamiento de documentos | BYTEA en DB | Mismo patrón que `DocumentoClinico`. Para ~40 empleados es suficiente. Alternativa futura: S3/MinIO. |
| Exportación SUNAT | Servicio dedicado | `ExportadorTRegistroService` y `ExportadorPlameService` con versión de formato. |
| Fechas de corte laboral | Periodos mensuales | Tabla `PeriodoPlanilla` con estado abierto/cerrado. Nunca modificar periodo cerrado. |
| Moneda | PEN siempre | El ERP es para Perú. Si se necesita USD, se agrega después. |
| Redondeo | BigDecimal con HALF_EVEN | Precisión de 2 decimales para todos los cálculos monetarios. |

### Ready for Proposal

**Yes** — la exploración está completa con alcance REMYPE full, incluyendo planilla. El orquestador debería proceder con `sdd-propose` para iniciar el primer cambio (`rrhh-worker-expansion`).

**Preguntas que el orquestador debe hacer al usuario antes de proponer:**

1. **Orden de implementación**: ¿Quieres comenzar con un MVP de 6 PRs (trabajador expandido + contratos + documentos + asistencia + planilla básica) o ir por los 13 PRs completos desde el inicio?

2. **Migración de CMP**: ¿Aceptas migrar el campo `cmp` de `Medico` a una entidad general `ColegiaturaProfesional` en RRHH? Esto implica modificar el módulo clínico existente.

3. **Tasas en DB vs Config**: ¿Prefieres las tasas (AFP, ONP, ESSALUD) en `tb_configuracion_api` (configurable desde UI) o en `application.yml` (requiere deploy)?

4. **SIS-MYPE**: Para microempresa, ¿quieres soporte para SIS-MYPE (S/30/mes) como alternativa a EsSalud 9%?

5. **Asistencia**: ¿La clínica tiene ya un sistema de marcación (biométrico, tarjeta) o necesitas captura manual desde el sistema?

## Exploration: Módulo RRHH Planilla — Payroll, AFP/ONP, ESSALUD, CTS, Gratificaciones, Vacaciones, PLAME/T-Registro

### Current State

#### Base existente (`rrhh-base` archivada)

El módulo RRHH base está completo y archivado. Lo que ya tenemos:

| Sub-paquete | Entidades | CRUD | Migraciones |
|---|---|---|---|
| `trabajador/` | `Trabajador` (expandido) | ✅ Service + Controller + DTOs | V20 (base) |
| `contrato/` | `Contrato` | ✅ Service + Controller + DTOs | V20 (base) |
| `periodo/` | `PeriodoLaboral` | ✅ Service + Controller + DTOs | V20 (base) |
| `derechohabiente/` | `Derechohabiente` | ✅ Service + Controller + DTOs | V21 |
| `type/` | 6 enums | ✅ | — |

**Campos relevantes de `Trabajador` para planilla:**
- `tipo` (TipoTrabajador), `regimenLaboral` (RegimenLaboral)
- `fechaIngreso`, `cargo`, `areaFuncionalId`
- `banco`, `cuentaSueldo`, `cci`
- `cantidadHijos` (para Asignación Familiar = 10% RMV)
- `discapacidad`, `sindicalizado`
- `nroColegiatura`, `tipoColegiatura`

**Campos relevantes de `Contrato`:**
- `tipoContrato` (TipoContrato — INDETERMINADO, DETERMINADO, CAS, etc.)
- `fechaInicio`, `fechaFin`
- `remuneracion` (BigDecimal)
- `jornada` (REGULAR, PARCIAL, NOCTURNA)
- `estado` (ACTIVO, SUSPENDIDO, VENCIDO, RESUELTO)
- `periodoPruebaMeses`

**Lo que FALTA para planilla:**
- ❌ Sistema pensionario del trabajador (AFP/ONP, CUSPP, datos de AFP específica)
- ❌ Periodos de planilla mensual
- ❌ Liquidaciones / boletas de pago
- ❌ Cálculo de descuentos (AFP/ONP, Renta 5ta)
- ❌ Cálculo de aportes empleador (ESSALUD 9%)
- ❌ CTS tracking y cálculo
- ❌ Gratificaciones tracking y cálculo
- ❌ Vacaciones tracking y programación
- ❌ Exportación PLAME / T-Registro

#### Patrones existentes a replicar

| Aspecto | Patrón usado | Archivo de referencia |
|---|---|---|
| Entity | `extends BaseEntity` + `@AttributeOverride` | `Trabajador.java` |
| Service | `@Service @Transactional` con DI por constructor | `TrabajadorService.java` |
| Controller | `@RestController @RequestMapping("/api/v1/...")` | `TrabajadorController.java` |
| DTOs | Java records (Request/Response) con `fromEntity()` | `TrabajadorRequest.java`, `TrabajadorResponse.java` |
| Repository | `extends JpaRepository<T, Long>` | `TrabajadorRepository.java` |
| Seguridad | `@PreAuthorize("hasAuthority('rrhh:...')")` | `TrabajadorController.java` |
| Catálogos maestro | Entity extends `BaseEntity` con codigo+nombre+descripcion | `TipoContrato.java` en `maestro/entity/rrhh/` |

### Datos Normativos Peruanos Vigentes (2026)

| Concepto | Valor | Base Legal |
|---|---|---|
| **RMV** | S/ 1,130/mes | DS 006-2024-TR |
| **UIT** 2026 | S/ 5,500 | DS 301-2025-EF |
| **Asignación Familiar** | 10% RMV = S/ 113 | Ley 25129 (si 2+ hijos) |
| **ESSALUD** (empleador) | 9% sobre remuneración | Ley 26790 |
| **ONP** (aporte trabajador) | 13% sobre remuneración | DL 19990 |
| **AFP aporte obligatorio** | 10% | DL 25897 |
| **AFP comisión flujo** | 1.47% (Habitat) ~ 1.69% (Profuturo) | SBS 2026 |
| **AFP comisión mixta** | 0% flujo + 0.68% ~ 1.25% sobre saldo | SBS 2026 |
| **AFP prima seguro** | 1.37% (todas iguales) | SBS |
| **Tope AFP (RAM)** | S/ 12,598.91 (variable trimestral) | SBS |
| **Bonif. Extra Gratif.** | 9% sobre gratificación (Ley 30334) | Ley 30334 |
| **Renta 5ta Categoría** | Escala: 8% / 14% / 17% / 20% / 30% | Ley IR |
| **Deducción 5ta** | 7 UIT (S/ 38,500) + hasta 3 UIT gastos | Ley IR |
| **CTS depósito** | 15-18 mayo y 15-18 noviembre | DL 650 |
| **Gratificaciones** | 15 julio y 15 diciembre | Ley 27735 |

### Entities Candidates

#### 1. Sistema Pensionario del Trabajador

El trabajador necesita elegir entre SNP (ONP) o SPP (AFP). Esto debe estar asociado al trabajador, no al contrato (puede cambiar pero es infrecuente).

```java
// com.clinica.rrhh.pension.entity.InformacionPensionaria
@Table(name = "tb_info_pensionaria")
public class InformacionPensionaria extends BaseEntity {
    Long id;
    Trabajador trabajador;          // FK → tb_trabajadores (unique)
    
    // Régimen: SNP o SPP
    String regimenPensionario;      // "SNP" o "SPP"
    
    // Para SNP (ONP)
    String codigoONP;               // Código de afiliación ONP
    
    // Para SPP (AFP)
    Long afpId;                     // FK → tb_afps (maestro)
    String cuspp;                   // Código Único del SPP
    String tipoFondo;               // 0, 1, 2, 3 (Fondo 0=Protección, 1=Mixto, 2=Balanceado, 3=Creativo)
    String tipoComision;            // "FLUJO" o "MIXTA"
    
    // SCTR (Seguro Complementario Trabajo Riesgo)
    Boolean tieneSctr;
    String sctrEntidad;             // Nombre aseguradora SCTR
    
    // Seguro Vida Ley
    Boolean tieneVidaLey;
    
    LocalDate fechaAfiliacion;
}
```

#### 2. Catálogo AFP (en maestro)

```java
// com.clinica.maestro.entity.rrhh.Afp
@Table(name = "tb_afps")
public class Afp extends BaseEntity {
    Long id;
    String codigo;                  // HABITAT, INTEGRA, PRIMA, PROFUTURO
    String nombre;
    BigDecimal comisionFlujo;       // 1.47, 1.55, 1.60, 1.69
    BigDecimal comisionMixtaFlujo;  // 0.00
    BigDecimal comisionMixtaSaldo;  // 1.25, 0.78, 1.25, 0.68
    BigDecimal primaSeguro;         // 1.37
    LocalDate vigenciaDesde;
    LocalDate vigenciaHasta;        // nullable = vigente
}
```

La tabla `tb_afps` permite mantener histórico de tasas. Cada vez que SBS actualiza, se inserta un nuevo registro con `vigenciaDesde` y se marca el anterior con `vigenciaHasta`.

#### 3. PeriodoPlanilla

```java
// com.clinica.rrhh.planilla.entity.PeriodoPlanilla
@Table(name = "tb_periodos_planilla")
public class PeriodoPlanilla extends BaseEntity {
    Long id;
    Integer anio;                   // 2026
    Integer mes;                    // 1-12
    LocalDate fechaInicio;          // 2026-06-01
    LocalDate fechaFin;             // 2026-06-30
    String estado;                  // ABIERTO, CERRADO, ANULADO
    LocalDate fechaCierre;
    LocalDate fechaPago;
}
```

#### 4. Liquidacion (boleta de pago mensual por trabajador)

```java
// com.clinica.rrhh.planilla.entity.Liquidacion
@Table(name = "tb_liquidaciones")
public class Liquidacion extends BaseEntity {
    Long id;
    PeriodoPlanilla periodo;        // FK
    Trabajador trabajador;          // FK
    Contrato contrato;              // FK — versión del contrato vigente en el periodo
    
    // --- INGRESOS ---
    Integer diasLaborados;          // días efectivamente trabajados
    Integer diasNoLaborados;        // faltas, permisos sin goce
    BigDecimal remuneracionOrdinaria;     // sueldo base / 30 * diasLaborados
    BigDecimal asignacionFamiliar;        // S/113 si aplica (cantidadHijos > 0)
    BigDecimal horasExtras25;             // 25% adicional
    BigDecimal horasExtras100;            // 100% adicional
    BigDecimal bonificaciones;            // bonos, comisiones, etc.
    BigDecimal totalIngresos;
    
    // --- DESCUENTOS (trabajador) ---
    String regimenPensionario;      // SNP o SPP
    BigDecimal descuentoPension;    // 13% ONP o 10% AFP
    BigDecimal descuentoComisionAFP; // comisión variable según AFP
    BigDecimal descuentoPrimaSeguro; // 1.37%
    BigDecimal descuentoQuintaCategoria; // retención IR mensual
    BigDecimal otrosDescuentos;     // adelantos, judiciales, etc.
    BigDecimal totalDescuentos;
    
    // --- APORTES (empleador, solo registro informativo) ---
    BigDecimal aporteEssalud;       // 9%
    BigDecimal aporteSctr;          // si aplica
    BigDecimal aporteVidaLey;       // si aplica
    BigDecimal aporteSenati;        // si aplica (0.75% empresas industriales)
    
    // --- NETO ---
    BigDecimal netoPagar;
    
    String estado;                  // CALCULADA, PAGADA, ANULADA, REEMPLAZADA
}
```

#### 5. CTS

```java
// com.clinica.rrhh.cts.entity.DepositoCTS
@Table(name = "tb_depositos_cts")
public class DepositoCTS extends BaseEntity {
    Long id;
    Trabajador trabajador;          // FK
    Integer anio;                   // 2026
    String periodo;                 // MAYO (nov-abr) o NOVIEMBRE (may-oct)
    BigDecimal remuneracionComputable;
    BigDecimal gratificacionProporcional;  // 1/6 de la última gratificación
    BigDecimal montoDeposito;
    String banco;
    String cuentaCTS;
    LocalDate fechaDeposito;
    String estado;                  // PENDIENTE, DEPOSITADO, ANULADO
}
```

#### 6. Gratificacion

```java
// com.clinica.rrhh.gratificacion.entity.Gratificacion
@Table(name = "tb_gratificaciones")
public class Gratificacion extends BaseEntity {
    Long id;
    Trabajador trabajador;          // FK
    Integer anio;                   // 2026
    String periodo;                 // JULIO o DICIEMBRE
    Integer mesesTrabajados;        // 0-6 (para proporcional)
    BigDecimal remuneracionComputable;
    BigDecimal montoGratificacion;  // sueldo completo o proporcional
    BigDecimal bonificacionExtraordinaria; // 9% Ley 30334
    BigDecimal totalPagar;
    String tipoSeguro;              // ESSALUD (9%) o EPS (6.75%) — determina % bonif
    LocalDate fechaPago;
    String estado;                  // CALCULADA, PAGADA, ANULADA
}
```

#### 7. Vacacion

```java
// com.clinica.rrhh.vacacion.entity.Vacacion
@Table(name = "tb_vacaciones")
public class Vacacion extends BaseEntity {
    Long id;
    Trabajador trabajador;          // FK
    String periodoGenerado;         // ej: "2025-2026"
    Integer diasGenerados;          // 30 (o 15 REMYPE pequeña)
    Integer diasGozados;
    Integer diasVendidos;           // venta de hasta 15 días
    Integer diasPendientes;
    LocalDate fechaCorte;           // fecha en que cumplió el récord vacacional
}
```

```java
// com.clinica.rrhh.vacacion.entity.ProgramacionVacacion
@Table(name = "tb_programacion_vacaciones")
public class ProgramacionVacacion extends BaseEntity {
    Long id;
    Vacacion vacacion;              // FK
    LocalDate fechaInicio;
    LocalDate fechaFin;
    Integer dias;
    String tipo;                    // COMPLETO, FRACCION_7, FRACCION_8, FRACCION_1
    String estado;                  // PROGRAMADA, EN_CURSO, GOZADA, CANCELADA
    String autorizadoPor;           // quien autorizó
}
```

#### 8. Adicional: Conceptos de Planilla (catálogo flexible)

Para no hardcodear los conceptos de ingresos/descuentos, se recomienda una tabla catálogo:

```java
// com.clinica.maestro.entity.rrhh.ConceptoPlanilla
@Table(name = "tb_conceptos_planilla")
public class ConceptoPlanilla extends BaseEntity {
    Long id;
    String codigo;                  // SUELDO_BASE, ASIGNACION_FAMILIAR, HE_25, etc.
    String nombre;
    String tipo;                    // INGRESO, DESCUENTO, APORTE_EMPLEADOR
    String afectoEssalud;           // SI, NO
    String afectoPension;           // SI, NO
    String afectoQuinta;            // SI, NO
    String afectoCTS;               // SI, NO
}
```

Esto permite que la liquidación referencie conceptos en lugar de tener columnas fijas, pero para un MVP de ~40 empleados, columnas fijas en `Liquidacion` son más simples y directas.

### Service Boundaries

```
com.clinica.rrhh.planilla/
├── entity/
│   ├── PeriodoPlanilla.java
│   └── Liquidacion.java
├── repository/
│   ├── PeriodoPlanillaRepository.java
│   └── LiquidacionRepository.java
├── service/
│   ├── PlanillaService.java          ← Orquestador principal
│   ├── CalculadorRemuneracionService.java  ← Ingresos del trabajador
│   ├── CalculadorPensionService.java       ← Strategy: AFP / ONP
│   ├── CalculadorQuintaCategoriaService.java   ← Renta 5ta categoría
│   └── ExportadorPlameService.java    ← Generación PLAME/T-Registro
├── dto/
│   ├── PeriodoPlanillaRequest.java
│   ├── PeriodoPlanillaResponse.java
│   ├── LiquidacionRequest.java
│   ├── LiquidacionResponse.java
│   └── BoletaPagoResponse.java       ← Vista completa para boleta
└── controller/
    ├── PeriodoPlanillaController.java
    └── LiquidacionController.java

com.clinica.rrhh.pension/
├── entity/
│   └── InformacionPensionaria.java
├── repository/
│   └── InformacionPensionariaRepository.java
├── service/
│   └── InformacionPensionariaService.java
├── dto/
│   ├── InformacionPensionariaRequest.java
│   └── InformacionPensionariaResponse.java
└── controller/
    └── InformacionPensionariaController.java

com.clinica.rrhh.cts/
├── entity/
│   └── DepositoCTS.java
├── repository/
│   └── DepositoCTSRepository.java
├── service/
│   ├── CtsService.java              ← Cálculo y gestión
│   └── CalculadorCtsService.java    ← Fórmula matemática
├── dto/
│   ├── DepositoCTSRequest.java
│   └── DepositoCTSResponse.java
└── controller/
    └── CtsController.java

com.clinica.rrhh.gratificacion/
├── entity/
│   └── Gratificacion.java
├── repository/
│   └── GratificacionRepository.java
├── service/
│   └── GratificacionService.java
├── dto/
│   └── GratificacionResponse.java
└── controller/
    └── GratificacionController.java

com.clinica.rrhh.vacacion/
├── entity/
│   ├── Vacacion.java
│   └── ProgramacionVacacion.java
├── repository/
│   ├── VacacionRepository.java
│   └── ProgramacionVacacionRepository.java
├── service/
│   └── VacacionService.java
├── dto/
│   └── ... 
└── controller/
    └── VacacionController.java
```

### Peruvian Labor Law Rules and Formulas

#### 1. AFP (SPP) — Aporte del trabajador
```
DescuentoAFP = min(Remuneración, TopeRAM) × (10% + Comisión% + 1.37%)
```
- **Tope RAM**: Remuneración Asegurada Máxima (S/ 12,598.91 aprox, cambia trimestralmente)
- **Comisión**: depende de la AFP y tipo (flujo vs mixta)
- **Prima seguro**: 1.37% (igual para todas las AFP)
- Total descuento AFP ≈ 12.84% ~ 13.06% del sueldo (sobre la remuneración con tope)

Ejemplo para Habitat (comisión flujo 1.47%):
- Sueldo S/ 3,000: Descuento = 3,000 × (10% + 1.47% + 1.37%) = 3,000 × 12.84% = S/ 385.20
- Sueldo S/ 15,000 (aplica tope): TopeRAM ≈ S/ 12,598.91 → Descuento = 12,598.91 × 12.84% = S/ 1,617.70

#### 2. ONP (SNP) — Aporte del trabajador
```
DescuentoONP = Remuneración × 13%
```
- No tiene tope (toda la remuneración)
- No tiene comisión ni prima adicional
- Ejemplo: Sueldo S/ 3,000 → Descuento = S/ 390.00

#### 3. ESSALUD — Aporte del empleador
```
AporteESSALUD = Remuneración × 9%
```
- Lo paga COMPLETAMENTE el empleador
- No se descuenta del trabajador
- Ejemplo: Sueldo S/ 3,000 → Aporte empleador = S/ 270.00

#### 4. Asignación Familiar
```
Asignación = cantidadHijos > 0 ? RMV × 10% : 0
```
- RMV 2026 = S/ 1,130
- Asignación = S/ 113.00 (si tiene al menos un hijo menor de edad o estudiando hasta 24 años)
- Se suma a la remuneración bruta

#### 5. Renta 5ta Categoría — Retención mensual
```
Proyección Anual = (Remuneración Mensual × 12) + (Gratificación Julio + Diciembre)
Remuneración Neta Anual = Proyección Anual - 7 UIT - Deducciones (hasta 3 UIT)
Impuesto Anual = Aplicar tabla progresiva
Retención Mensual = Impuesto Anual / 12
```

Tramos UIT 2026 (UIT = S/ 5,500):
| Tramo | Desde | Hasta | Tasa |
|---|---|---|---|
| 1 | 0 UIT | 5 UIT (S/ 27,500) | 8% |
| 2 | 5 UIT | 20 UIT (S/ 110,000) | 14% |
| 3 | 20 UIT | 35 UIT (S/ 192,500) | 17% |
| 4 | 35 UIT | 45 UIT (S/ 247,500) | 20% |
| 5 | +45 UIT | — | 30% |

Ejemplo: Sueldo S/ 4,000 (14 sueldos = S/ 56,000 anual):
- Renta Bruta Anual: S/ 56,000
- Deducción 7 UIT: −S/ 38,500
- Renta Neta: S/ 17,500 (dentro del 1er tramo de 5 UIT = S/ 27,500)
- Impuesto Anual: S/ 17,500 × 8% = S/ 1,400
- Retención Mensual: S/ 116.67

#### 6. CTS — Cálculo semestral
```
Remuneración Computable = Sueldo + AsignaciónFamiliar + (1/6 × Gratificación)
CTS = (Remuneración Computable / 12) × MesesTrabajados
```

Ejemplo: Sueldo S/ 3,000 + asignación S/ 113 + grati S/ 500 (1/6):
- RC = 3,000 + 113 + 500 = S/ 3,613
- CTS semestral (6 meses) = 3,613 / 12 × 6 = S/ 1,806.50

Depósitos: 15 de mayo (periodo nov-abr) y 15 de noviembre (periodo may-oct).

#### 7. Gratificaciones — Julio y Diciembre
```
Gratificación = Remuneración Computable (sueldo + asignación familiar)
Proporcional: (RC / 6) × MesesTrabajados
Bonificación Extraordinaria = Gratificación × 9% (ESSALUD) o 6.75% (EPS)
```

Ejemplo: Sueldo S/ 3,000, 6 meses trabajados:
- Gratificación = S/ 3,000
- Bonif. Extra (9%) = S/ 270.00
- Total a pagar = S/ 3,270.00

#### 8. Vacaciones — 30 días por año completo
```
Remuneración Vacacional = Remuneración Computable (promedio últimos 6 meses)
Vacaciones Truncas (al cese) = (RC / 360) × Días trabajados
```

- 30 días calendario por cada año completo
- Récord mínimo: 260 días (jornada 6 días/sem) o 210 días (jornada 5 días/sem)
- Fraccionamiento: bloque rígido (7+8) + bloque flexible (días sueltos mínimo 1 día)
- Venta de vacaciones: hasta 15 días
- Triple vacacional: si no se goza dentro del año siguiente

### Order of Implementation Priority

| Prio | Sub-módulo | Depende de | Esfuerzo | Riesgo |
|---|---|---|---|---|
| **1** | `pension/` — Información pensionaria del trabajador | rrhh-base | Bajo (~200 LOC) | Bajo |
| **2** | `planilla/` — Periodos + Liquidación base | (1) | Alto (~800 LOC) | Medio |
| **3** | `planilla/` — Cálculo descuentos (AFP/ONP/5ta) | (1)+(2) | Medio (~400 LOC) | Alto |
| **4** | `gratificacion/` — Cálculo gratificaciones | (1) | Bajo (~300 LOC) | Medio |
| **5** | `cts/` — Cálculo CTS | (1) | Medio (~400 LOC) | Medio |
| **6** | `vacacion/` — Programación vacaciones | (1) | Medio (~500 LOC) | Medio |
| **7** | `planilla/` — Exportación PLAME/T-Registro | (1)+(2)+(3) | Medio (~400 LOC) | Alto |
| **8** | `maestro/` — Catálogo AFP con histórico tasas | — | Bajo (~100 LOC + seed) | Bajo |

### Estimated Module Size

| Sub-módulo | Entities | Servicios | Controllers | Migraciones | LOC estimado |
|---|---|---|---|---|---|
| `pension/` | 1 | 1 | 1 | 1 (V23) | ~250 |
| `planilla/` (base) | 2 | 1 | 2 | 1 (V24) | ~600 |
| `planilla/` (cálculos) | — | 3 (strategy) | — | — | ~500 |
| `planilla/` (export) | — | 1 | — | — | ~400 |
| `gratificacion/` | 1 | 1 | 1 | 1 (V25) | ~300 |
| `cts/` | 1 | 1 | 1 | 1 (V26) | ~400 |
| `vacacion/` | 2 | 1 | 1 | 1 (V27) | ~500 |
| `maestro/` (AFP) | 1 | 1 | 1 | 1 (V28) | ~150 |
| **Total** | **8** | **11** | **7** | **6** | **~3,100** |

### Architecture Decisions

| Decisión | Opción A | Opción B | Recomendación |
|---|---|---|---|
| **Cálculo pensiones** | Strategy Pattern con interface | Switch en un solo service | ✅ Strategy — cada AFP tiene tasas distintas que cambian |
| **Tasas variables** | Tabla `tb_afps` con vigencia | `application.yml` + @Value | ✅ Tabla — cambiar sin deploy, histórico de tasas |
| **Período planilla** | Una fila por mes calendario | Una fila por rango de fechas | ✅ Mes calendario — simple, alinea con PLAME mensual |
| **Liquidación** | Columnas fijas por concepto | Tabla detalle con FK a concepto | ✅ Columnas fijas para MVP (~40 empleados). Tabla detalle si el sistema escala |
| **Exportación PLAME** | Generar archivo plano SUNAT | API REST hacia SUNAT | ✅ Archivo plano — SUNAT no expone API moderna para PLAME |
| **Redondeo** | HALF_EVEN con 2 decimales | HALF_UP con 2 decimales | ✅ HALF_EVEN (estándar financiero). Todos los cálculos con BigDecimal |
| **REMYPE** | Parámetro global en config | Por trabajador | ✅ Parámetro global `app.remype: micro|pequena|general` en application.yml |

### Key Risks

1. **Precisión de cálculos financieros** (ALTO) — Errores en AFP/RAM/5ta categoría generan multas SUNAT y SUNAFIL.
   - Mitigación: suite de tests parametrizados con casos reales, tasas en DB con histórico.

2. **Cambios normativos** (MEDIO) — UIT, RMV, tasas AFP cambian anualmente.
   - Mitigación: tabla `tb_afps` con vigencia, `application.yml` para RMV/UIT, migraciones de seed data.

3. **Formato PLAME/T-Registro** (ALTO) — SUNAT cambia versiones sin previo aviso.
   - Mitigación: servicio de exportación abstraído, versionable. Formato plano no acoplado a la lógica de negocio.

4. **Renta 5ta Categoría** (ALTO) — Cálculo progresivo complejo con proyección anual, ajustes trimestrales.
   - Mitigación: implementar exactamente como SUNAT lo calcula (Art. 40 Reglamento LIR). Tests contra calculadora oficial SUNAT.

5. **Datos sensibles** (MEDIO) — Sueldos, cuentas bancarias, DNI en RRHH.
   - Mitigación: permisos granulares (`rrhh:sueldo:ver`), logging de acceso, datos bancarios en columna separada.

### Ready for Proposal

**Yes** — la exploración está completa. El orden recomendado es:

1. PR#1: `pension/` + catálogo AFP en maestro (base para todo)
2. PR#2: `planilla/` periodos + liquidación base con cálculos
3. PR#3: `gratificacion/` 
4. PR#4: `cts/`
5. PR#5: `vacacion/`
6. PR#6: Exportación PLAME/T-Registro

**Preguntas para el usuario:**
1. ¿La clínica es REMYPE micro, pequeña o régimen general? Esto cambia drásticamente beneficios (CTS 0 vs medio vs completo, vacaciones 15 vs 30 días).
2. ¿Tienen trabajadores con EPS (Entidad Prestadora de Salud) además de EsSalud?
3. ¿Hay trabajadores con SCTR (riesgo) o Vida Ley?
4. ¿Quieren empezar con un MVP que calcule planilla básica (solo sueldo+descuentos) y agregar gratif/cts/vac después?

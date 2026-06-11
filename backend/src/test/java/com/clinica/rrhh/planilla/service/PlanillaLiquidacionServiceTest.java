package com.clinica.rrhh.planilla.service;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.maestro.entity.rrhh.AfpTasaHistorica;
import com.clinica.maestro.entity.rrhh.ConceptoPlanilla;
import com.clinica.maestro.repository.rrhh.AfpRepository;
import com.clinica.maestro.repository.rrhh.ConceptoPlanillaRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.planilla.config.PlanillaProperties;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.entity.Planilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planilla.repository.PlanillaDetalleRepository;
import com.clinica.rrhh.planilla.repository.PlanillaRepository;
import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import com.clinica.rrhh.pension.repository.InformacionPensionariaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.type.EstadoContrato;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanillaLiquidacionServiceTest {

    @Mock
    private PeriodoPlanillaRepository periodoPlanillaRepository;
    @Mock
    private PlanillaRepository planillaRepository;
    @Mock
    private PlanillaDetalleRepository planillaDetalleRepository;
    @Mock
    private ContratoRepository contratoRepository;
    @Mock
    private InformacionPensionariaRepository pensionRepository;
    @Mock
    private AfpRepository afpRepository;
    @Mock
    private ConceptoPlanillaRepository conceptoRepository;

    private Renta5taCalculator renta5taCalculator;
    private PlanillaProperties properties;

    @InjectMocks
    private PlanillaLiquidacionService service;

    private PeriodoPlanilla periodo;
    private Trabajador trabajador;
    private Contrato contrato;
    private ConceptoPlanilla conceptoBasico;

    @BeforeEach
    void setUp() {
        properties = new PlanillaProperties(1130, 5350, "20123456789");
        renta5taCalculator = new Renta5taCalculator(properties);
        service = new PlanillaLiquidacionService(
            periodoPlanillaRepository, planillaRepository, planillaDetalleRepository,
            contratoRepository, pensionRepository, afpRepository, conceptoRepository,
            renta5taCalculator, properties);

        periodo = new PeriodoPlanilla();
        periodo.setId(1L);
        periodo.setAnio(2026);
        periodo.setMes(1);
        periodo.setFechaInicio(LocalDate.of(2026, 1, 1));
        periodo.setFechaFin(LocalDate.of(2026, 1, 31));
        periodo.setEstado("ABIERTO");

        var tdi = new TipoDocumentoIdentidad();
        tdi.setNombre("DNI");

        var persona = new Persona();
        persona.setId(1L);
        persona.setTipoDocumentoIdentidad(tdi);
        persona.setNumeroDocumento("12345678");
        persona.setNombres("TRABAJADOR");
        persona.setApellidoPaterno("TEST");

        trabajador = new Trabajador();
        trabajador.setId(1L);
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-001");
        trabajador.setCantidadHijos(0);

        contrato = new Contrato();
        contrato.setId(1L);
        contrato.setTrabajador(trabajador);
        contrato.setRemuneracion(new BigDecimal("2500"));
        contrato.setEstado(EstadoContrato.ACTIVO);

        conceptoBasico = new ConceptoPlanilla();
        conceptoBasico.setCodigo("BASICO");
        conceptoBasico.setNombre("Sueldo Básico");
        conceptoBasico.setTipo("INGRESO");
    }

    @Test
    void generar_WithAfpWorker_ReturnsPlanilla() {
        var afp = new Afp();
        afp.setId(1L);
        afp.setCodigo("PRIMA");
        afp.setNombre("Prima AFP");

        var tasa = new AfpTasaHistorica();
        tasa.setId(1L);
        tasa.setTasa(new BigDecimal("0.0185"));
        tasa.setPrimaSeguro(new BigDecimal("0.0087"));
        tasa.setVigenciaDesde(LocalDate.of(2025, 1, 1));
        tasa.setVigenciaHasta(null);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(planillaRepository.existsByPeriodoPlanillaId(1L)).thenReturn(false);
        when(conceptoRepository.findAllByActivoTrueOrderByOrden())
            .thenReturn(List.of(conceptoBasico));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(pensionRepository.findByTrabajadorId(1L)).thenReturn(Optional.of(
            crearInfoPensionaria(afp)));
        when(afpRepository.findCurrentRateByAfpId(1L)).thenReturn(Optional.of(tasa));
        when(planillaDetalleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(planillaRepository.save(any())).thenAnswer(i -> {
            var p = (com.clinica.rrhh.planilla.entity.Planilla) i.getArgument(0);
            p.setId(1L);
            return p;
        });

        var result = service.generar(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.cantidadTrabajadores()).isEqualTo(1);
        // sueldo 2500, AFP 1.85%+0.87%=2.72% → 68.00, Renta 5ta = (2500*12-37450=0) → 0
        // descuentos = 68.00, aportes = 225.00 (9%), neto = 2500-68 = 2432.00
        assertThat(result.totalNeto()).isEqualByComparingTo(new BigDecimal("2432.00"));
    }

    @Test
    void generar_WithOnpWorker_ReturnsPlanilla() {
        var onp = new Afp();
        onp.setId(2L);
        onp.setCodigo("ONP");
        onp.setNombre("ONP");

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(planillaRepository.existsByPeriodoPlanillaId(1L)).thenReturn(false);
        when(conceptoRepository.findAllByActivoTrueOrderByOrden())
            .thenReturn(List.of(conceptoBasico));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(pensionRepository.findByTrabajadorId(1L)).thenReturn(Optional.of(
            crearInfoPensionaria(onp)));
        when(planillaDetalleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(planillaRepository.save(any())).thenAnswer(i -> {
            var p = (com.clinica.rrhh.planilla.entity.Planilla) i.getArgument(0);
            p.setId(1L);
            return p;
        });

        var result = service.generar(1L);

        assertThat(result).isNotNull();
        assertThat(result.cantidadTrabajadores()).isEqualTo(1);
        // ONP 13% = 325.00, Renta 5ta = 0, neto = 2500-325 = 2175.00
        assertThat(result.totalNeto()).isEqualByComparingTo(new BigDecimal("2175.00"));
        assertThat(result.totalAportes()).isEqualByComparingTo(new BigDecimal("225.00"));
    }

    @Test
    void generar_WithAsignacionFamiliar_IncludesExtra() {
        trabajador.setCantidadHijos(2);
        var afp = new Afp();
        afp.setId(1L);
        afp.setCodigo("PRIMA");
        afp.setNombre("Prima AFP");

        var tasa = new AfpTasaHistorica();
        tasa.setId(1L);
        tasa.setTasa(new BigDecimal("0.0185"));
        tasa.setPrimaSeguro(new BigDecimal("0.0087"));

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(planillaRepository.existsByPeriodoPlanillaId(1L)).thenReturn(false);
        when(conceptoRepository.findAllByActivoTrueOrderByOrden())
            .thenReturn(List.of(conceptoBasico));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(pensionRepository.findByTrabajadorId(1L)).thenReturn(Optional.of(
            crearInfoPensionaria(afp)));
        when(afpRepository.findCurrentRateByAfpId(1L)).thenReturn(Optional.of(tasa));
        when(planillaDetalleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(planillaRepository.save(any())).thenAnswer(i -> {
            var p = (com.clinica.rrhh.planilla.entity.Planilla) i.getArgument(0);
            p.setId(1L);
            return p;
        });

        var result = service.generar(1L);

        assertThat(result).isNotNull();
        // ingresos = 2500 + 113 (10% RMV) = 2613
        // descuentos = AFP 68.00, Renta = 0 → 68.00
        // neto = 2613 - 68 = 2545
        assertThat(result.totalNeto()).isEqualByComparingTo(new BigDecimal("2545.00"));
    }

    @Test
    void generar_PeriodoNotFound_Throws() {
        when(periodoPlanillaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generar(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void generar_PeriodoNotAbierto_Throws() {
        periodo.setEstado("CERRADO");
        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));

        assertThatThrownBy(() -> service.generar(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no está ABIERTO");
    }

    @Test
    void generar_DuplicatePeriod_Throws() {
        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(planillaRepository.existsByPeriodoPlanillaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.generar(1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Ya existe una planilla");
    }

    @Test
    void generar_NoActiveContracts_ReturnsEmptyPlanilla() {
        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(planillaRepository.existsByPeriodoPlanillaId(1L)).thenReturn(false);
        when(conceptoRepository.findAllByActivoTrueOrderByOrden()).thenReturn(List.of());
        when(contratoRepository.findAll()).thenReturn(List.of());
        when(planillaRepository.save(any(Planilla.class))).thenAnswer(a -> a.getArgument(0));

        var response = service.generar(1L);

        assertThat(response.cantidadTrabajadores()).isZero();
        assertThat(response.totalNeto()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.estado()).isEqualTo("BORRADOR");
    }

    private InformacionPensionaria crearInfoPensionaria(Afp afp) {
        var info = new InformacionPensionaria();
        info.setId(1L);
        info.setTrabajador(trabajador);
        info.setAfp(afp);
        info.setCuspp("123456789012");
        info.setComisionTipo("FLUJO");
        info.setFechaAfiliacion(LocalDate.of(2025, 1, 1));
        info.setEstado("ACTIVO");
        return info;
    }
}

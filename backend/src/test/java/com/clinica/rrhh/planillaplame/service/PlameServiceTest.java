package com.clinica.rrhh.planillaplame.service;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.maestro.entity.rrhh.AfpTasaHistorica;
import com.clinica.maestro.repository.rrhh.AfpRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.cts.entity.DepositoCts;
import com.clinica.rrhh.cts.repository.DepositoCtsRepository;
import com.clinica.rrhh.gratificacion.entity.Gratificacion;
import com.clinica.rrhh.gratificacion.repository.GratificacionRepository;
import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import com.clinica.rrhh.pension.repository.InformacionPensionariaRepository;
import com.clinica.rrhh.planilla.config.PlanillaProperties;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.entity.Planilla;
import com.clinica.rrhh.planilla.entity.PlanillaDetalle;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planilla.repository.PlanillaDetalleRepository;
import com.clinica.rrhh.planilla.repository.PlanillaRepository;
import com.clinica.rrhh.planilla.service.Renta5taCalculator;
import com.clinica.rrhh.planillaplame.dto.ArchivoPlanillaResponse;
import com.clinica.rrhh.planillaplame.entity.ArchivoPlanilla;
import com.clinica.rrhh.planillaplame.repository.ArchivoPlanillaRepository;
import com.clinica.rrhh.vacacion.repository.VacacionGoceRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlameServiceTest {

    @Mock private PeriodoPlanillaRepository periodoPlanillaRepository;
    @Mock private PlanillaRepository planillaRepository;
    @Mock private PlanillaDetalleRepository planillaDetalleRepository;
    @Mock private GratificacionRepository gratificacionRepository;
    @Mock private DepositoCtsRepository depositoCtsRepository;
    @Mock private VacacionGoceRepository vacacionGoceRepository;
    @Mock private InformacionPensionariaRepository pensionRepository;
    @Mock private AfpRepository afpRepository;
    @Mock private ArchivoPlanillaRepository archivoRepository;
    @Mock private Renta5taCalculator renta5taCalculator;
    @Mock private PlanillaProperties properties;

    @InjectMocks
    private PlameService service;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getRucEmpleador()).thenReturn("20123456789");
    }

    private PeriodoPlanilla createPeriodoCerrado() {
        var p = new PeriodoPlanilla();
        p.setId(1L);
        p.setAnio(2026);
        p.setMes(1);
        p.setFechaInicio(LocalDate.of(2026, 1, 1));
        p.setFechaFin(LocalDate.of(2026, 1, 31));
        p.setEstado("CERRADO");
        return p;
    }

    private Trabajador createTrabajador(Long id, String numDoc) {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");

        var persona = new Persona();
        persona.setId(id);
        persona.setTipoDocumentoIdentidad(tdi);
        persona.setNumeroDocumento(numDoc);

        var t = new Trabajador();
        t.setId(id);
        t.setPersona(persona);
        t.setCodigoTrabajador("TR-" + id);
        return t;
    }

    private PlanillaDetalle createDetalle(Long id, Trabajador t, BigDecimal sueldoBase,
                                          BigDecimal asigFamiliar) {
        var d = new PlanillaDetalle();
        d.setId(id);
        d.setTrabajador(t);
        d.setSueldoBase(sueldoBase);
        d.setAsignacionFamiliar(asigFamiliar);
        d.setTotalIngresos(sueldoBase.add(asigFamiliar));
        return d;
    }

    private void mockUpsertArchivos() {
        when(archivoRepository.findByPeriodoPlanillaIdAndTipo(any(), any()))
                .thenReturn(Optional.empty());
        when(archivoRepository.save(any())).thenAnswer(i -> {
            var a = (ArchivoPlanilla) i.getArgument(0);
            if (a.getId() == null) a.setId((long) (Math.random() * 1000));
            return a;
        });
    }

    @Test
    void generar_WithAfpWorker_ShouldGenerate5Files() {
        var periodo = createPeriodoCerrado();
        var trabajador = createTrabajador(1L, "12345678");
        var detalle = createDetalle(1L, trabajador, new BigDecimal("1500.00"), BigDecimal.ZERO);

        var planilla = new Planilla();
        planilla.setId(1L);
        planilla.setPeriodoPlanilla(periodo);

        var afp = new Afp();
        afp.setId(1L);
        afp.setCodigo("PRIMA");
        afp.setNombre("Prima AFP");

        var pension = new InformacionPensionaria();
        pension.setTrabajador(trabajador);
        pension.setAfp(afp);

        var tasa = new AfpTasaHistorica();
        tasa.setTasa(new BigDecimal("0.1000"));
        tasa.setPrimaSeguro(new BigDecimal("0.0170"));

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(planillaRepository.findByPeriodoPlanillaId(1L)).thenReturn(Optional.of(planilla));
        when(planillaDetalleRepository.findByPlanillaId(1L)).thenReturn(List.of(detalle));
        when(gratificacionRepository.findByPeriodoPlanillaId(1L)).thenReturn(List.of());
        when(depositoCtsRepository.findByPeriodoPlanillaId(1L)).thenReturn(List.of());
        when(vacacionGoceRepository.findByEstadoAndFechaFinBetween(any(), any(), any())).thenReturn(List.of());
        when(pensionRepository.findByTrabajadorId(1L)).thenReturn(Optional.of(pension));
        when(afpRepository.findCurrentRateByAfpId(1L)).thenReturn(Optional.of(tasa));
        when(renta5taCalculator.calcular(any(), any(), anyInt())).thenReturn(BigDecimal.ZERO);

        mockUpsertArchivos();

        List<ArchivoPlanillaResponse> results = service.generar(1L);

        assertThat(results).hasSize(5);
        var tipos = results.stream().map(ArchivoPlanillaResponse::tipo).toList();
        assertThat(tipos).containsExactly("REM", "JOR", "SNL", "OR5", "TOC");

        // Verify .rem has 3 lines: 0121 sueldo + 0608 AFP + 0804 EsSalud
        ArgumentCaptor<ArchivoPlanilla> captor = ArgumentCaptor.forClass(ArchivoPlanilla.class);
        verify(archivoRepository, times(5)).save(captor.capture());

        var savedArchivos = captor.getAllValues();
        var remArchivo = savedArchivos.stream().filter(a -> "REM".equals(a.getTipo())).findFirst().orElseThrow();
        var remLines = remArchivo.getContenido().lines().toList();
        assertThat(remLines).hasSize(3);
        assertThat(remLines.get(0)).contains("0121").contains("1500.00");
        assertThat(remLines.get(1)).contains("0608");
        assertThat(remLines.get(2)).contains("0804").contains("135.00"); // 9% of 1500

        // Verify .jor has 1 line with 240 horas
        var jorArchivo = savedArchivos.stream().filter(a -> "JOR".equals(a.getTipo())).findFirst().orElseThrow();
        assertThat(jorArchivo.getContenido()).contains("1|12345678|240|0|0|0");

        // Verify .toc has AFP indicator (1)
        var tocArchivo = savedArchivos.stream().filter(a -> "TOC".equals(a.getTipo())).findFirst().orElseThrow();
        assertThat(tocArchivo.getContenido()).contains("1|12345678|1|0|0|1");
    }

    @Test
    void generar_WithOnpWorker_ShouldUseOnpIndicatorAndRate() {
        var periodo = createPeriodoCerrado();
        var trabajador = createTrabajador(2L, "87654321");
        var detalle = createDetalle(2L, trabajador, new BigDecimal("2000.00"), BigDecimal.ZERO);

        var planilla = new Planilla();
        planilla.setId(1L);
        planilla.setPeriodoPlanilla(periodo);

        var afp = new Afp();
        afp.setId(2L);
        afp.setCodigo("ONP");
        afp.setNombre("SNP - D.L.19990");

        var pension = new InformacionPensionaria();
        pension.setTrabajador(trabajador);
        pension.setAfp(afp);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(planillaRepository.findByPeriodoPlanillaId(1L)).thenReturn(Optional.of(planilla));
        when(planillaDetalleRepository.findByPlanillaId(1L)).thenReturn(List.of(detalle));
        when(gratificacionRepository.findByPeriodoPlanillaId(1L)).thenReturn(List.of());
        when(depositoCtsRepository.findByPeriodoPlanillaId(1L)).thenReturn(List.of());
        when(vacacionGoceRepository.findByEstadoAndFechaFinBetween(any(), any(), any())).thenReturn(List.of());
        when(pensionRepository.findByTrabajadorId(2L)).thenReturn(Optional.of(pension));
        when(renta5taCalculator.calcular(any(), any(), anyInt())).thenReturn(BigDecimal.ZERO);

        mockUpsertArchivos();

        List<ArchivoPlanillaResponse> results = service.generar(1L);

        assertThat(results).hasSize(5);

        ArgumentCaptor<ArchivoPlanilla> captor = ArgumentCaptor.forClass(ArchivoPlanilla.class);
        verify(archivoRepository, times(5)).save(captor.capture());

        var savedArchivos = captor.getAllValues();
        var remArchivo = savedArchivos.stream().filter(a -> "REM".equals(a.getTipo())).findFirst().orElseThrow();
        var remLines = remArchivo.getContenido().lines().toList();

        // Lines: 0121 sueldo + 0607 ONP + 0804 EsSalud
        assertThat(remLines).hasSize(3);
        assertThat(remLines.get(0)).contains("0121").contains("2000.00");
        assertThat(remLines.get(1)).contains("0607").contains("260.00"); // 13% of 2000
        assertThat(remLines.get(2)).contains("0804").contains("180.00"); // 9% of 2000

        // Verify .toc has ONP indicator (2)
        var tocArchivo = savedArchivos.stream().filter(a -> "TOC".equals(a.getTipo())).findFirst().orElseThrow();
        assertThat(tocArchivo.getContenido()).contains("1|87654321|2|0|0|1");
    }

    @Test
    void generar_WithGratifAndCts_ShouldIncludeThem() {
        var periodo = createPeriodoCerrado();
        var trabajador = createTrabajador(3L, "11111111");
        var detalle = createDetalle(3L, trabajador, new BigDecimal("1500.00"),
                new BigDecimal("113.00"));

        var planilla = new Planilla();
        planilla.setId(1L);
        planilla.setPeriodoPlanilla(periodo);

        var gratif = new Gratificacion();
        gratif.setTrabajador(trabajador);
        gratif.setTotal(new BigDecimal("750.00"));

        var cts = new DepositoCts();
        cts.setTrabajador(trabajador);
        cts.setMontoCts(new BigDecimal("500.00"));

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(planillaRepository.findByPeriodoPlanillaId(1L)).thenReturn(Optional.of(planilla));
        when(planillaDetalleRepository.findByPlanillaId(1L)).thenReturn(List.of(detalle));
        when(gratificacionRepository.findByPeriodoPlanillaId(1L)).thenReturn(List.of(gratif));
        when(depositoCtsRepository.findByPeriodoPlanillaId(1L)).thenReturn(List.of(cts));
        when(vacacionGoceRepository.findByEstadoAndFechaFinBetween(any(), any(), any())).thenReturn(List.of());
        when(pensionRepository.findByTrabajadorId(3L)).thenReturn(Optional.empty());
        when(renta5taCalculator.calcular(any(), any(), anyInt())).thenReturn(BigDecimal.ZERO);

        mockUpsertArchivos();

        List<ArchivoPlanillaResponse> results = service.generar(1L);

        assertThat(results).hasSize(5);

        ArgumentCaptor<ArchivoPlanilla> captor = ArgumentCaptor.forClass(ArchivoPlanilla.class);
        verify(archivoRepository, times(5)).save(captor.capture());

        var remArchivo = savedArchivosFrom(captor, "REM");
        var remLines = remArchivo.getContenido().lines().toList();

        // Lines: 0121 sueldo + 0201 asig fam + 0401 gratif + 0904 CTS
        assertThat(remLines).hasSize(4);
        assertThat(remLines.get(0)).contains("0121");
        assertThat(remLines.get(1)).contains("0201").contains("113.00");
        assertThat(remLines.get(2)).contains("0401").contains("750.00");
        assertThat(remLines.get(3)).contains("0904").contains("500.00");

        // TOC: no pension → indicator 0
        var tocArchivo = savedArchivosFrom(captor, "TOC");
        assertThat(tocArchivo.getContenido()).contains("1|11111111|0|0|0|1");
    }

    @Test
    void generar_AbiertoPeriod_ShouldThrow() {
        var periodo = new PeriodoPlanilla();
        periodo.setId(1L);
        periodo.setEstado("ABIERTO");

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));

        assertThatThrownBy(() -> service.generar(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CERRADO");
    }

    @Test
    void generar_PeriodoNotFound_ShouldThrow() {
        when(periodoPlanillaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generar(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void generar_EmptyData_ShouldGenerateEmptyFiles() {
        var periodo = createPeriodoCerrado();

        var planilla = new Planilla();
        planilla.setId(1L);
        planilla.setPeriodoPlanilla(periodo);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(planillaRepository.findByPeriodoPlanillaId(1L)).thenReturn(Optional.of(planilla));
        when(planillaDetalleRepository.findByPlanillaId(1L)).thenReturn(List.of());

        mockUpsertArchivos();

        List<ArchivoPlanillaResponse> results = service.generar(1L);

        assertThat(results).hasSize(5);
        assertThat(results.stream().map(ArchivoPlanillaResponse::tipo))
                .containsExactly("REM", "JOR", "SNL", "OR5", "TOC");
    }

    @Test
    void generar_NoPlanillaLiquidada_ShouldThrow() {
        var periodo = createPeriodoCerrado();

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(planillaRepository.findByPeriodoPlanillaId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generar(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("planilla liquidada");
    }

    @Test
    void getArchivos_ShouldDelegateToRepo() {
        when(archivoRepository.findByPeriodoPlanillaId(1L)).thenReturn(List.of());

        var result = service.getArchivos(1L);
        assertThat(result).isEmpty();
    }

    @Test
    void getArchivoParaDescarga_NotFound_ShouldThrow() {
        when(archivoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getArchivoParaDescarga(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getArchivoPorPeriodoTipo_ShouldReturnFile() {
        var periodo = createPeriodoCerrado();
        var archivo = new ArchivoPlanilla();
        archivo.setId(10L);
        archivo.setPeriodoPlanilla(periodo);
        archivo.setTipo("REM");
        archivo.setContenido("test");

        when(archivoRepository.findByPeriodoPlanillaIdAndTipo(1L, "REM"))
                .thenReturn(Optional.of(archivo));

        var result = service.getArchivoPorPeriodoTipo(1L, "REM");
        assertThat(result.getTipo()).isEqualTo("REM");
    }

    private ArchivoPlanilla savedArchivosFrom(ArgumentCaptor<ArchivoPlanilla> captor, String tipo) {
        return captor.getAllValues().stream()
                .filter(a -> tipo.equals(a.getTipo()))
                .findFirst().orElseThrow();
    }
}

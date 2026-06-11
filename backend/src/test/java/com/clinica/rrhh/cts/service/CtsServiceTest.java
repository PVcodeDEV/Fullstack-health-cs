package com.clinica.rrhh.cts.service;

import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.cts.dto.DepositoCtsResponse;
import com.clinica.rrhh.cts.entity.DepositoCts;
import com.clinica.rrhh.cts.repository.DepositoCtsRepository;
import com.clinica.rrhh.gratificacion.entity.Gratificacion;
import com.clinica.rrhh.gratificacion.repository.GratificacionRepository;
import com.clinica.rrhh.planilla.config.PlanillaProperties;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.type.EstadoContrato;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CtsServiceTest {

    @Mock
    private DepositoCtsRepository depositoCtsRepository;

    @Mock
    private PeriodoPlanillaRepository periodoPlanillaRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private GratificacionRepository gratificacionRepository;

    @Mock
    private PlanillaProperties properties;

    @InjectMocks
    private CtsService service;

    @Captor
    private ArgumentCaptor<DepositoCts> depositoCaptor;

    private PeriodoPlanilla mayoPeriodo;
    private PeriodoPlanilla noviembrePeriodo;
    private PeriodoPlanilla invalidPeriodo;
    private Trabajador trabajador;
    private Trabajador trabajadorConHijos;
    private Contrato contrato;
    private Contrato contratoLateStart;

    @BeforeEach
    void setUp() {
        // Mes=5 → MAYO periodo (semestre MAYO-OCTUBRE)
        mayoPeriodo = new PeriodoPlanilla();
        mayoPeriodo.setId(1L);
        mayoPeriodo.setAnio(2026);
        mayoPeriodo.setMes(5);
        mayoPeriodo.setFechaInicio(LocalDate.of(2026, 5, 1));
        mayoPeriodo.setFechaFin(LocalDate.of(2026, 5, 31));

        // Mes=11 → NOVIEMBRE periodo (semestre NOVIEMBRE-ABRIL)
        noviembrePeriodo = new PeriodoPlanilla();
        noviembrePeriodo.setId(2L);
        noviembrePeriodo.setAnio(2026);
        noviembrePeriodo.setMes(11);
        noviembrePeriodo.setFechaInicio(LocalDate.of(2026, 11, 1));
        noviembrePeriodo.setFechaFin(LocalDate.of(2026, 11, 30));

        // Invalid (mes=3)
        invalidPeriodo = new PeriodoPlanilla();
        invalidPeriodo.setId(3L);
        invalidPeriodo.setAnio(2026);
        invalidPeriodo.setMes(3);

        var persona = new Persona();
        persona.setNombres("JUAN");
        persona.setApellidoPaterno("PEREZ");

        trabajador = new Trabajador();
        trabajador.setId(1L);
        trabajador.setPersona(persona);
        trabajador.setCantidadHijos(0);

        var personaConHijos = new Persona();
        personaConHijos.setNombres("PEDRO");
        personaConHijos.setApellidoPaterno("SUAREZ");

        trabajadorConHijos = new Trabajador();
        trabajadorConHijos.setId(2L);
        trabajadorConHijos.setPersona(personaConHijos);
        trabajadorConHijos.setCantidadHijos(2);

        contrato = new Contrato();
        contrato.setId(1L);
        contrato.setTrabajador(trabajador);
        contrato.setRemuneracion(new BigDecimal("2000.00"));
        contrato.setFechaInicio(LocalDate.of(2025, 10, 1));
        contrato.setEstado(EstadoContrato.ACTIVO);

        contratoLateStart = new Contrato();
        contratoLateStart.setId(2L);
        contratoLateStart.setTrabajador(trabajadorConHijos);
        contratoLateStart.setRemuneracion(new BigDecimal("2500.00"));
        contratoLateStart.setFechaInicio(LocalDate.of(2026, 1, 20));
        contratoLateStart.setEstado(EstadoContrato.ACTIVO);
    }

    // --- Test: calcularDiasComputables ---

    @Test
    void calcularDiasComputables_FullSemestre_180Days() {
        // Contrato started before semester (Oct 2025) for semester Mayo-Octubre 2026
        int dias = service.calcularDiasComputables(
            LocalDate.of(2025, 10, 1),
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 10, 31));
        assertThat(dias).isEqualTo(180); // 6 months × 30
    }

    @Test
    void calcularDiasComputables_MidMonthStart_150Days() {
        // Started Jan 20, 2026 → day 15+, so start from Feb
        int dias = service.calcularDiasComputables(
            LocalDate.of(2026, 1, 20),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 6, 30));
        assertThat(dias).isEqualTo(150); // 5 months × 30 (Feb-Jun)
    }

    @Test
    void calcularDiasComputables_Day14Counts_180Days() {
        // Day 14 → counts as full month (Jan)
        int dias = service.calcularDiasComputables(
            LocalDate.of(2026, 1, 14),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 6, 30));
        assertThat(dias).isEqualTo(180); // 6 months × 30
    }

    @Test
    void calcularDiasComputables_ZeroMonths() {
        // Started after semester ends
        int dias = service.calcularDiasComputables(
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 6, 30));
        assertThat(dias).isEqualTo(0);
    }

    @Test
    void calcularDiasComputables_Day1StartsOnSemesterBoundary() {
        // Started exactly on semester start (May 1)
        int dias = service.calcularDiasComputables(
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 10, 31));
        assertThat(dias).isEqualTo(180); // 6 months × 30
    }

    // --- Test: calcular (full flow) ---

    @Test
    void calcular_FullSemestre_180Days_RCHalf() {
        // Full 6 months, no hijos, no gratificaciones
        // RC = 2000, monto = (2000/360) × 180 = 1000
        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(mayoPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(depositoCtsRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 1L))
            .thenReturn(Optional.empty());
        when(depositoCtsRepository.save(any())).thenAnswer(i -> {
            var d = (DepositoCts) i.getArgument(0);
            if (d.getId() == null) d.setId(1L);
            return d;
        });

        List<DepositoCtsResponse> result = service.calcular(1L);

        assertThat(result).hasSize(1);
        var r = result.get(0);
        assertThat(r.semestre()).isEqualTo("MAYO-OCTUBRE");
        assertThat(r.diasComputables()).isEqualTo(180);
        assertThat(r.remuneracionComputable()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(r.promedioGratificacion()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(r.promedioBonificacion()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(r.montoCts()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(r.estado()).isEqualTo("CALCULADO");

        verify(depositoCtsRepository).save(depositoCaptor.capture());
        var saved = depositoCaptor.getValue();
        assertThat(saved.getSemestre()).isEqualTo("MAYO-OCTUBRE");
        assertThat(saved.getDiasComputables()).isEqualTo(180);
    }

    @Test
    void calcular_Proportional_90Days() {
        // 3 months × 30 = 90 days, RC = 2000, monto = (2000/360) × 90 = 500
        // Start Aug 1, semester May-Oct → effective Aug 1, count Aug/Sep/Oct = 3 months
        var contratoAgosto = new Contrato();
        contratoAgosto.setId(3L);
        contratoAgosto.setTrabajador(trabajador);
        contratoAgosto.setRemuneracion(new BigDecimal("2000.00"));
        contratoAgosto.setFechaInicio(LocalDate.of(2026, 8, 1));
        contratoAgosto.setEstado(EstadoContrato.ACTIVO);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(mayoPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contratoAgosto));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(depositoCtsRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 1L))
            .thenReturn(Optional.empty());
        when(depositoCtsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<DepositoCtsResponse> result = service.calcular(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).diasComputables()).isEqualTo(90); // 3 months × 30

        BigDecimal montoEsperado = new BigDecimal("2000.00")
            .divide(BigDecimal.valueOf(360), 10, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(90))
            .setScale(2, RoundingMode.HALF_UP);
        assertThat(result.get(0).montoCts()).isEqualByComparingTo(montoEsperado);
    }

    @Test
    void calcular_WithAsignacionFamiliar() {
        // Worker has children, RC = 2500 + 113 = 2613
        // 5 months (start Jan 20 → Feb 1 effective) → 150 days
        // monto = (2613/360) × 150
        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(mayoPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contratoLateStart));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(2L)).thenReturn(List.of());
        when(depositoCtsRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 2L))
            .thenReturn(Optional.empty());
        when(depositoCtsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<DepositoCtsResponse> result = service.calcular(1L);

        assertThat(result).hasSize(1);
        var r = result.get(0);
        // contratoLateStart starts Jan 20, semester is MAYO-OCTUBRE (May 1 - Oct 31)
        // effectiveStart = max(Jan 20, May 1) = May 1
        // May 1: day 1-14 → start May 1
        // months from May to Oct = 6, days = 6 * 30 = 180
        assertThat(r.diasComputables()).isEqualTo(180);

        assertThat(r.remuneracionComputable()).isEqualByComparingTo(new BigDecimal("2613.00")); // 2500 + 113
        BigDecimal montoEsperado = new BigDecimal("2613.00")
            .divide(BigDecimal.valueOf(360), 10, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(180))
            .setScale(2, RoundingMode.HALF_UP);
        assertThat(r.montoCts()).isEqualByComparingTo(montoEsperado);
    }

    @Test
    void calcular_WithAverageGratificacion_2Records() {
        // 2 gratif records: 1056 and 1200, bonif: 95 and 108
        // promGratif = ((1056+1200)/2)/6 = 1128/6 = 188.00
        // promBonif = ((95+108)/2)/6 = 101.50/6 = 16.92
        // RC = 2000 + 188.00 + 16.92 = 2204.92
        // monto = (2204.92/360) × 180 = 1102.46
        var g1 = new Gratificacion();
        g1.setGratificacion(new BigDecimal("1056.00"));
        g1.setBonificacionExtraordinaria(new BigDecimal("95.00"));

        var g2 = new Gratificacion();
        g2.setGratificacion(new BigDecimal("1200.00"));
        g2.setBonificacionExtraordinaria(new BigDecimal("108.00"));

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(mayoPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(1L))
            .thenReturn(List.of(g1, g2));
        when(depositoCtsRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 1L))
            .thenReturn(Optional.empty());
        when(depositoCtsRepository.save(any())).thenAnswer(i -> {
            var d = (DepositoCts) i.getArgument(0);
            if (d.getId() == null) d.setId(1L);
            return d;
        });

        List<DepositoCtsResponse> result = service.calcular(1L);

        assertThat(result).hasSize(1);
        var r = result.get(0);
        assertThat(r.remuneracionComputable()).isEqualByComparingTo(new BigDecimal("2204.92"));
        assertThat(r.montoCts()).isEqualByComparingTo(new BigDecimal("1102.46"));
    }

    @Test
    void calcular_WithSingleGratificacionRecord() {
        // Only 1 gratif record: 1200, bonif: 108
        // promGratif = (1200/1)/6 = 200.00
        // promBonif = (108/1)/6 = 18.00
        // RC = 2000 + 200 + 18 = 2218.00
        // monto = (2218/360) × 180 = 1109.00
        var g1 = new Gratificacion();
        g1.setGratificacion(new BigDecimal("1200.00"));
        g1.setBonificacionExtraordinaria(new BigDecimal("108.00"));

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(mayoPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(1L))
            .thenReturn(List.of(g1));
        when(depositoCtsRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 1L))
            .thenReturn(Optional.empty());
        when(depositoCtsRepository.save(any())).thenAnswer(i -> {
            var d = (DepositoCts) i.getArgument(0);
            if (d.getId() == null) d.setId(1L);
            return d;
        });

        List<DepositoCtsResponse> result = service.calcular(1L);

        assertThat(result).hasSize(1);
        var r = result.get(0);
        assertThat(r.remuneracionComputable()).isEqualByComparingTo(new BigDecimal("2218.00"));
        assertThat(r.montoCts()).isEqualByComparingTo(new BigDecimal("1109.00"));
    }

    @Test
    void calcular_WithZeroGratificacionRecords() {
        // No gratif records → skip 1/6 addition
        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(mayoPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(depositoCtsRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 1L))
            .thenReturn(Optional.empty());
        when(depositoCtsRepository.save(any())).thenAnswer(i -> {
            var d = (DepositoCts) i.getArgument(0);
            if (d.getId() == null) d.setId(1L);
            return d;
        });

        List<DepositoCtsResponse> result = service.calcular(1L);

        assertThat(result).hasSize(1);
        var r = result.get(0);
        assertThat(r.promedioGratificacion()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(r.promedioBonificacion()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(r.remuneracionComputable()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(r.montoCts()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void calcular_MidMonthEntry_Day15Plus_FirstMonthExcluded() {
        // Start Jan 20, semester Jan-Jun → effective Feb 1 → 5 months × 30 = 150 days
        var contratoMidMonth = new Contrato();
        contratoMidMonth.setId(3L);
        contratoMidMonth.setTrabajador(trabajador);
        contratoMidMonth.setRemuneracion(new BigDecimal("2000.00"));
        contratoMidMonth.setFechaInicio(LocalDate.of(2026, 1, 20));
        contratoMidMonth.setEstado(EstadoContrato.ACTIVO);

        var periodoEneroJunio = new PeriodoPlanilla();
        periodoEneroJunio.setId(4L);
        periodoEneroJunio.setAnio(2026);
        periodoEneroJunio.setMes(5);
        // Actually for testing may-octubre, we can also define a shorter semester
        // Let me use a different contrato for this test
        // Actually, the semester is fixed by the periodo. For mes=5, the semestre is mayo-octubre.
        // So a contrato starting in jan would count from may (semester start)
        // Let's test a contrato starting May 20 with mes=5
        var contratoMidMay = new Contrato();
        contratoMidMay.setId(4L);
        contratoMidMay.setTrabajador(trabajador);
        contratoMidMay.setRemuneracion(new BigDecimal("2000.00"));
        contratoMidMay.setFechaInicio(LocalDate.of(2026, 5, 20)); // Day 15+ → starts June
        contratoMidMay.setEstado(EstadoContrato.ACTIVO);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(mayoPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contratoMidMay));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(depositoCtsRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 1L))
            .thenReturn(Optional.empty());
        when(depositoCtsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<DepositoCtsResponse> result = service.calcular(1L);

        assertThat(result).hasSize(1);
        // June to October = 5 months × 30 = 150 days
        assertThat(result.get(0).diasComputables()).isEqualTo(150);
    }

    @Test
    void calcular_Day1to14Entry_MonthCounts() {
        // Day 1-14 → month counts. May 10 start
        var contratoEarlyMay = new Contrato();
        contratoEarlyMay.setId(5L);
        contratoEarlyMay.setTrabajador(trabajador);
        contratoEarlyMay.setRemuneracion(new BigDecimal("2000.00"));
        contratoEarlyMay.setFechaInicio(LocalDate.of(2026, 5, 10)); // Day 1-14 → counts May
        contratoEarlyMay.setEstado(EstadoContrato.ACTIVO);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(mayoPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contratoEarlyMay));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(depositoCtsRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 1L))
            .thenReturn(Optional.empty());
        when(depositoCtsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<DepositoCtsResponse> result = service.calcular(1L);

        assertThat(result).hasSize(1);
        // May to October = 6 months × 30 = 180 days
        assertThat(result.get(0).diasComputables()).isEqualTo(180);
    }

    @Test
    void calcular_ZeroMonths_SkipsWorker() {
        // Worker starts after semester ends → skipped
        var contratoFuera = new Contrato();
        contratoFuera.setId(6L);
        contratoFuera.setTrabajador(trabajador);
        contratoFuera.setRemuneracion(new BigDecimal("2000.00"));
        contratoFuera.setFechaInicio(LocalDate.of(2027, 1, 1)); // After Oct 31, 2026
        contratoFuera.setEstado(EstadoContrato.ACTIVO);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(mayoPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contratoFuera));

        List<DepositoCtsResponse> result = service.calcular(1L);
        assertThat(result).isEmpty();
    }

    @Test
    void calcular_InvalidSemester_ThrowsIllegalArgument() {
        when(periodoPlanillaRepository.findById(3L)).thenReturn(Optional.of(invalidPeriodo));

        assertThatThrownBy(() -> service.calcular(3L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Periodo inválido")
            .hasMessageContaining("3");
    }

    @Test
    void calcular_Idempotent_UpdatesExisting() {
        var existing = new DepositoCts();
        existing.setId(1L);
        existing.setPeriodoPlanilla(mayoPeriodo);
        existing.setTrabajador(trabajador);
        existing.setSemestre("MAYO-OCTUBRE");
        existing.setDiasComputables(180);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(mayoPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(depositoCtsRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 1L))
            .thenReturn(Optional.of(existing));
        when(depositoCtsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.calcular(1L);

        verify(depositoCtsRepository, times(1)).save(any());
        assertThat(existing.getMontoCts()).isNotNull();
        assertThat(existing.getRemuneracionComputable()).isNotNull();
    }

    @Test
    void calcular_PeriodoNotFound_ThrowsEntityNotFound() {
        when(periodoPlanillaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calcular(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void calcular_DiciembrePeriodo_ReturnsNoviembreAbril() {
        when(periodoPlanillaRepository.findById(2L)).thenReturn(Optional.of(noviembrePeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(depositoCtsRepository.findByPeriodoPlanillaIdAndTrabajadorId(2L, 1L))
            .thenReturn(Optional.empty());
        when(depositoCtsRepository.save(any())).thenAnswer(i -> {
            var d = (DepositoCts) i.getArgument(0);
            if (d.getId() == null) d.setId(2L);
            return d;
        });

        List<DepositoCtsResponse> result = service.calcular(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).semestre()).isEqualTo("NOVIEMBRE-ABRIL");
    }

    // --- Test: findAll / findById ---

    @Test
    void findAll_ShouldReturnAll() {
        var d = new DepositoCts();
        d.setId(1L);
        d.setPeriodoPlanilla(mayoPeriodo);
        d.setTrabajador(trabajador);
        d.setSemestre("MAYO-OCTUBRE");
        d.setDiasComputables(180);

        when(depositoCtsRepository.findAll()).thenReturn(List.of(d));

        var result = service.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).semestre()).isEqualTo("MAYO-OCTUBRE");
    }

    @Test
    void findById_ShouldReturnResponse() {
        var d = new DepositoCts();
        d.setId(1L);
        d.setPeriodoPlanilla(mayoPeriodo);
        d.setTrabajador(trabajador);
        d.setSemestre("MAYO-OCTUBRE");
        d.setDiasComputables(180);

        when(depositoCtsRepository.findById(1L)).thenReturn(Optional.of(d));

        var result = service.findById(1L);
        assertThat(result.semestre()).isEqualTo("MAYO-OCTUBRE");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(depositoCtsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }
}

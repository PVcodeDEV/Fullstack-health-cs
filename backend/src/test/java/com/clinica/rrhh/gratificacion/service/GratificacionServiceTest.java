package com.clinica.rrhh.gratificacion.service;

import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.gratificacion.dto.GratificacionResponse;
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
class GratificacionServiceTest {

    @Mock
    private GratificacionRepository gratificacionRepository;

    @Mock
    private PeriodoPlanillaRepository periodoPlanillaRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private PlanillaProperties properties;

    @InjectMocks
    private GratificacionService service;

    @Captor
    private ArgumentCaptor<Gratificacion> gratificacionCaptor;

    private PeriodoPlanilla junioPeriodo;
    private PeriodoPlanilla diciembrePeriodo;
    private PeriodoPlanilla invalidPeriodo;
    private Trabajador trabajador;
    private Trabajador trabajadorConHijos;
    private Contrato contrato;
    private Contrato contratoLateStart;

    @BeforeEach
    void setUp() {
        junioPeriodo = new PeriodoPlanilla();
        junioPeriodo.setId(1L);
        junioPeriodo.setAnio(2026);
        junioPeriodo.setMes(6);
        junioPeriodo.setFechaInicio(LocalDate.of(2026, 6, 1));
        junioPeriodo.setFechaFin(LocalDate.of(2026, 6, 30));

        diciembrePeriodo = new PeriodoPlanilla();
        diciembrePeriodo.setId(2L);
        diciembrePeriodo.setAnio(2026);
        diciembrePeriodo.setMes(12);

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

    // --- Test: calcularMesesComputables ---

    @Test
    void calcularMesesComputables_FullSemester() {
        // Contrato started before semester (Oct 2025) for semester Enero-Junio 2026
        int meses = service.calcularMesesComputables(
            LocalDate.of(2025, 10, 1),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 6, 30));
        assertThat(meses).isEqualTo(6);
    }

    @Test
    void calcularMesesComputables_MidMonthStart() {
        // Contrato started Jan 20, 2026 → day 15+, so start from Feb
        int meses = service.calcularMesesComputables(
            LocalDate.of(2026, 1, 20),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 6, 30));
        assertThat(meses).isEqualTo(5);
    }

    @Test
    void calcularMesesComputables_ExactFirstDay() {
        // Contrato started Jan 1 → day 1-14, so count Jan
        int meses = service.calcularMesesComputables(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 6, 30));
        assertThat(meses).isEqualTo(6);
    }

    @Test
    void calcularMesesComputables_Day14Counts() {
        // Day 14 counts as full month
        int meses = service.calcularMesesComputables(
            LocalDate.of(2026, 1, 14),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 6, 30));
        assertThat(meses).isEqualTo(6);
    }

    @Test
    void calcularMesesComputables_ZeroMonths() {
        // Started in July, outside semester
        int meses = service.calcularMesesComputables(
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 6, 30));
        assertThat(meses).isEqualTo(0);
    }

    // --- Test: calcular (full flow) ---

    @Test
    void calcular_FullSemester_ReturnsHalfSueldoPlusBonus() {
        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(junioPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 1L))
            .thenReturn(Optional.empty());
        when(gratificacionRepository.save(any())).thenAnswer(i -> {
            var g = (Gratificacion) i.getArgument(0);
            if (g.getId() == null) g.setId(1L);
            return g;
        });

        // 6 months → ½ sueldo = 2000 * 0.5 = 1000
        // No hijos → no asignación familiar
        // Bonif Extra = 1000 * 0.09 = 90
        // Total = 1090
        List<GratificacionResponse> result = service.calcular(1L);

        assertThat(result).hasSize(1);
        var r = result.get(0);
        assertThat(r.mesesComputables()).isEqualTo(6);
        assertThat(r.remuneracionComputable()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(r.gratificacion()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(r.bonificacionExtraordinaria()).isEqualByComparingTo(new BigDecimal("90.00"));
        assertThat(r.total()).isEqualByComparingTo(new BigDecimal("1090.00"));
        assertThat(r.semestre()).isEqualTo("ENERO-JUNIO");
        assertThat(r.estado()).isEqualTo("CALCULADO");

        verify(gratificacionRepository).save(gratificacionCaptor.capture());
        var saved = gratificacionCaptor.getValue();
        assertThat(saved.getSemestre()).isEqualTo("ENERO-JUNIO");
    }

    @Test
    void calcular_Proportional_ReturnsProportionalGratificacion() {
        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(junioPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contratoLateStart));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 2L))
            .thenReturn(Optional.empty());
        when(gratificacionRepository.save(any())).thenAnswer(i -> {
            var g = (Gratificacion) i.getArgument(0);
            if (g.getId() == null) g.setId(2L);
            return g;
        });

        // Start Jan 20 → day 15+ → effective Feb 1
        // meses = Feb, Mar, Apr, May, Jun = 5
        // Asignación Familiar = 10% of RMV = 113 (once, regardless of children count)
        // Rem = 2500 + 113 = 2613
        // Gratif = (2613 / 12) * 5 = 1088.75
        // Bonif = 1088.75 * 0.09 = 97.99
        // Total = 1088.75 + 97.99 = 1186.74
        List<GratificacionResponse> result = service.calcular(1L);

        assertThat(result).hasSize(1);
        var r = result.get(0);
        assertThat(r.mesesComputables()).isEqualTo(5);
        assertThat(r.remuneracionComputable())
            .isEqualByComparingTo(new BigDecimal("2613.00")); // 2500 + 113

        BigDecimal gratifEsperada = new BigDecimal("2613.00")
            .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(5));
        assertThat(r.gratificacion()).isEqualByComparingTo(gratifEsperada);

        BigDecimal bonifEsperada = gratifEsperada.multiply(new BigDecimal("0.09"))
            .setScale(2, RoundingMode.HALF_UP);
        assertThat(r.bonificacionExtraordinaria()).isEqualByComparingTo(bonifEsperada);

        assertThat(r.total()).isEqualByComparingTo(gratifEsperada.add(bonifEsperada));
    }

    @Test
    void calcular_MidMonthEntry_Day15PlusExcludesMonth() {
        // Worker starts mid-month, should get proportional months
        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(junioPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contratoLateStart));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 2L))
            .thenReturn(Optional.empty());
        when(gratificacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<GratificacionResponse> result = service.calcular(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).mesesComputables()).isEqualTo(5); // Feb-Jun
    }

    @Test
    void calcular_ZeroMonths_SkipsWorker() {
        // Worker started after semester ends → skipped
        var contratoFuera = new Contrato();
        contratoFuera.setId(3L);
        contratoFuera.setTrabajador(trabajador);
        contratoFuera.setRemuneracion(new BigDecimal("2000.00"));
        contratoFuera.setFechaInicio(LocalDate.of(2026, 7, 1)); // After Jun 30
        contratoFuera.setEstado(EstadoContrato.ACTIVO);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(junioPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contratoFuera));

        List<GratificacionResponse> result = service.calcular(1L);
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
        var existing = new Gratificacion();
        existing.setId(1L);
        existing.setPeriodoPlanilla(junioPeriodo);
        existing.setTrabajador(trabajador);
        existing.setSemestre("ENERO-JUNIO");
        existing.setMesesComputables(6);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(junioPeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByPeriodoPlanillaIdAndTrabajadorId(1L, 1L))
            .thenReturn(Optional.of(existing));
        when(gratificacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.calcular(1L);

        verify(gratificacionRepository, times(1)).save(any());
        assertThat(existing.getGratificacion()).isNotNull();
        assertThat(existing.getTotal()).isNotNull();
    }

    @Test
    void calcular_PeriodoNotFound_ThrowsEntityNotFound() {
        when(periodoPlanillaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calcular(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void calcular_Diciembre_ReturnsJulioDiciembre() {
        when(periodoPlanillaRepository.findById(2L)).thenReturn(Optional.of(diciembrePeriodo));
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(properties.getRmv()).thenReturn(1130);
        when(gratificacionRepository.findByPeriodoPlanillaIdAndTrabajadorId(2L, 1L))
            .thenReturn(Optional.empty());
        when(gratificacionRepository.save(any())).thenAnswer(i -> {
            var g = (Gratificacion) i.getArgument(0);
            if (g.getId() == null) g.setId(3L);
            return g;
        });

        List<GratificacionResponse> result = service.calcular(2L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).semestre()).isEqualTo("JULIO-DICIEMBRE");
    }

    // --- Test: findAll / findById ---

    @Test
    void findAll_ShouldReturnAll() {
        var g = new Gratificacion();
        g.setId(1L);
        g.setPeriodoPlanilla(junioPeriodo);
        g.setTrabajador(trabajador);
        g.setSemestre("ENERO-JUNIO");
        g.setMesesComputables(6);

        when(gratificacionRepository.findAll()).thenReturn(List.of(g));

        var result = service.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).semestre()).isEqualTo("ENERO-JUNIO");
    }

    @Test
    void findById_ShouldReturnResponse() {
        var g = new Gratificacion();
        g.setId(1L);
        g.setPeriodoPlanilla(junioPeriodo);
        g.setTrabajador(trabajador);
        g.setSemestre("ENERO-JUNIO");
        g.setMesesComputables(6);

        when(gratificacionRepository.findById(1L)).thenReturn(Optional.of(g));

        var result = service.findById(1L);
        assertThat(result.semestre()).isEqualTo("ENERO-JUNIO");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(gratificacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }
}

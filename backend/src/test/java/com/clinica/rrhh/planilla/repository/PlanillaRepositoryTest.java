package com.clinica.rrhh.planilla.repository;

import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.entity.Planilla;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlanillaRepositoryTest {

    @Autowired
    private PeriodoPlanillaRepository periodoPlanillaRepository;

    @Autowired
    private PlanillaRepository planillaRepository;

    private PeriodoPlanilla periodo;

    @BeforeEach
    void setUp() {
        periodo = new PeriodoPlanilla();
        periodo.setAnio(2026);
        periodo.setMes(1);
        periodo.setFechaInicio(LocalDate.of(2026, 1, 1));
        periodo.setFechaFin(LocalDate.of(2026, 1, 31));
        periodo.setEstado("ABIERTO");
        periodo = periodoPlanillaRepository.saveAndFlush(periodo);
    }

    @Test
    void saveAndFindById() {
        var planilla = new Planilla();
        planilla.setPeriodoPlanilla(periodo);
        planilla.setFechaLiquidacion(LocalDate.now());
        planilla.setTotalIngresos(new BigDecimal("5000.00"));
        planilla.setTotalDescuentos(new BigDecimal("500.00"));
        planilla.setTotalAportes(new BigDecimal("450.00"));
        planilla.setTotalNeto(new BigDecimal("4500.00"));
        planilla.setCantidadTrabajadores(2);
        planilla.setEstado("LIQUIDADO");
        planilla = planillaRepository.saveAndFlush(planilla);

        var found = planillaRepository.findById(planilla.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTotalNeto()).isEqualByComparingTo(new BigDecimal("4500.00"));
        assertThat(found.get().getEstado()).isEqualTo("LIQUIDADO");
    }

    @Test
    void findByPeriodoPlanillaId() {
        var planilla = new Planilla();
        planilla.setPeriodoPlanilla(periodo);
        planilla.setFechaLiquidacion(LocalDate.now());
        planilla.setTotalIngresos(BigDecimal.ZERO);
        planilla.setTotalDescuentos(BigDecimal.ZERO);
        planilla.setTotalAportes(BigDecimal.ZERO);
        planilla.setTotalNeto(BigDecimal.ZERO);
        planilla.setCantidadTrabajadores(0);
        planilla.setEstado("BORRADOR");
        planillaRepository.saveAndFlush(planilla);

        var found = planillaRepository.findByPeriodoPlanillaId(periodo.getId());
        assertThat(found).isPresent();
    }

    @Test
    void existsByPeriodoPlanillaId() {
        assertThat(planillaRepository.existsByPeriodoPlanillaId(periodo.getId())).isFalse();

        var planilla = new Planilla();
        planilla.setPeriodoPlanilla(periodo);
        planilla.setFechaLiquidacion(LocalDate.now());
        planilla.setTotalIngresos(BigDecimal.ZERO);
        planilla.setTotalDescuentos(BigDecimal.ZERO);
        planilla.setTotalAportes(BigDecimal.ZERO);
        planilla.setTotalNeto(BigDecimal.ZERO);
        planilla.setCantidadTrabajadores(0);
        planilla.setEstado("BORRADOR");
        planillaRepository.saveAndFlush(planilla);

        assertThat(planillaRepository.existsByPeriodoPlanillaId(periodo.getId())).isTrue();
    }

    @Test
    void findAllByOrderByPeriodoPlanillaAnioDescMesDesc() {
        // Create two periods: 2026-02 and 2026-01
        var periodo2 = new PeriodoPlanilla();
        periodo2.setAnio(2026);
        periodo2.setMes(2);
        periodo2.setFechaInicio(LocalDate.of(2026, 2, 1));
        periodo2.setFechaFin(LocalDate.of(2026, 2, 28));
        periodo2.setEstado("ABIERTO");
        periodo2 = periodoPlanillaRepository.saveAndFlush(periodo2);

        var p1 = createPlanilla(periodo);
        var p2 = createPlanilla(periodo2);

        List<Planilla> all = planillaRepository.findAllByOrderByPeriodoPlanillaAnioDescPeriodoPlanillaMesDesc();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getPeriodoPlanilla().getMes()).isEqualTo(2); // 2026-02 first
        assertThat(all.get(1).getPeriodoPlanilla().getMes()).isEqualTo(1); // 2026-01 second
    }

    private Planilla createPlanilla(PeriodoPlanilla pp) {
        var p = new Planilla();
        p.setPeriodoPlanilla(pp);
        p.setFechaLiquidacion(LocalDate.now());
        p.setTotalIngresos(BigDecimal.ZERO);
        p.setTotalDescuentos(BigDecimal.ZERO);
        p.setTotalAportes(BigDecimal.ZERO);
        p.setTotalNeto(BigDecimal.ZERO);
        p.setCantidadTrabajadores(0);
        p.setEstado("BORRADOR");
        return planillaRepository.saveAndFlush(p);
    }
}

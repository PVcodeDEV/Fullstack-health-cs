package com.clinica.caja.tarifario.repository;

import com.clinica.caja.tarifario.entity.Tarifario;
import com.clinica.caja.tarifario.entity.TarifarioItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class TarifarioItemRepositoryTest {

    @Autowired
    private TarifarioRepository tarifarioRepository;

    @Autowired
    private TarifarioItemRepository tarifarioItemRepository;

    private Tarifario tarifario;

    @BeforeEach
    void setUp() {
        tarifario = new Tarifario();
        tarifario.setNombre("Tarifario Particular");
        tarifario.setDescripcion("Default tariff schedule");
        tarifario = tarifarioRepository.save(tarifario);

        // Item 1: active price from 2026-01-01
        TarifarioItem item1 = new TarifarioItem();
        item1.setTarifario(tarifario);
        item1.setCodigo("CON-001");
        item1.setNombre("Consulta General");
        item1.setPrecioBase(new BigDecimal("80.00"));
        item1.setPrecioFinal(new BigDecimal("134.40"));
        item1.setFechaDesde(LocalDate.of(2026, 1, 1));
        item1.setFechaHasta(null);
        item1.setActivo(true);
        tarifarioItemRepository.save(item1);

        // Item 2: historical price (ended)
        TarifarioItem item2 = new TarifarioItem();
        item2.setTarifario(tarifario);
        item2.setCodigo("CON-001");
        item2.setNombre("Consulta General");
        item2.setPrecioBase(new BigDecimal("70.00"));
        item2.setPrecioFinal(new BigDecimal("117.60"));
        item2.setFechaDesde(LocalDate.of(2025, 6, 1));
        item2.setFechaHasta(LocalDate.of(2025, 12, 31));
        item2.setActivo(true);
        tarifarioItemRepository.save(item2);
    }

    @Test
    void shouldFindCurrentActiveByCodigo() {
        Optional<TarifarioItem> result = tarifarioItemRepository.findCurrentActiveByCodigo("CON-001");
        assertThat(result).isPresent();
        assertThat(result.get().getPrecioBase()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(result.get().getFechaHasta()).isNull();
    }

    @Test
    void shouldFindEffectiveByCodigoAndDate() {
        // Date before first active period → should find nothing
        Optional<TarifarioItem> result = tarifarioItemRepository.findEffectiveByCodigoAndFecha(
            "CON-001", LocalDate.of(2025, 1, 1));
        assertThat(result).isEmpty();

        // Date during historical period
        result = tarifarioItemRepository.findEffectiveByCodigoAndFecha(
            "CON-001", LocalDate.of(2025, 6, 15));
        assertThat(result).isPresent();
        assertThat(result.get().getPrecioBase()).isEqualByComparingTo(new BigDecimal("70.00"));

        // Date in current active period
        result = tarifarioItemRepository.findEffectiveByCodigoAndFecha(
            "CON-001", LocalDate.of(2026, 6, 15));
        assertThat(result).isPresent();
        assertThat(result.get().getPrecioBase()).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    @Test
    void shouldFindHistoryByCodigo() {
        List<TarifarioItem> history = tarifarioItemRepository.findHistoryByCodigo("CON-001");
        assertThat(history).hasSize(2);
        // Ordered by fechaDesde DESC → most recent first
        assertThat(history.get(0).getPrecioBase()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(history.get(1).getPrecioBase()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    void shouldRejectDuplicateCodigoSameDate() {
        // Same codigo + same fecha_desde = unique constraint violation
        TarifarioItem dup = new TarifarioItem();
        dup.setTarifario(tarifario);
        dup.setCodigo("CON-001"); // duplicate
        dup.setNombre("Duplicate");
        dup.setPrecioBase(new BigDecimal("90.00"));
        dup.setPrecioFinal(new BigDecimal("151.20"));
        dup.setFechaDesde(LocalDate.of(2026, 1, 1)); // same fecha_desde as item1
        dup.setActivo(true);

        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> tarifarioItemRepository.saveAndFlush(dup)
        );
    }
}

package com.clinica.maestro.repository;

import com.clinica.maestro.entity.clinico.TipoHabitacion;
import com.clinica.maestro.repository.clinico.TipoHabitacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TipoHabitacionRepositoryTest {

    @Autowired
    private TipoHabitacionRepository repository;

    @BeforeEach
    void setUp() {
        var ind = new TipoHabitacion();
        ind.setCodigo("IND");
        ind.setNombre("Individual");
        ind.setTarifaBase(new BigDecimal("250.00"));
        ind.setCapacidad(1);
        repository.saveAndFlush(ind);
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("IND");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Individual");
        assertThat(result.get().getTarifaBase()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var entity = new TipoHabitacion();
        entity.setCodigo("IND");
        entity.setNombre("Duplicate");
        entity.setTarifaBase(BigDecimal.valueOf(100));
        entity.setCapacidad(1);
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

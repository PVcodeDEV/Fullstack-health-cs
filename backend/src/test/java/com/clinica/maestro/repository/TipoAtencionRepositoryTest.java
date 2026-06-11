package com.clinica.maestro.repository;

import com.clinica.maestro.entity.clinico.TipoAtencion;
import com.clinica.maestro.repository.clinico.TipoAtencionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TipoAtencionRepositoryTest {

    @Autowired
    private TipoAtencionRepository repository;

    @BeforeEach
    void setUp() {
        var cext = new TipoAtencion();
        cext.setCodigo("CEXT");
        cext.setNombre("Consulta Externa");
        cext.setRequiereHabitacion(false);
        repository.save(cext);
        var hosp = new TipoAtencion();
        hosp.setCodigo("HOSP");
        hosp.setNombre("Hospitalización");
        hosp.setRequiereHabitacion(true);
        repository.saveAndFlush(hosp);
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("CEXT");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Consulta Externa");
    }

    @Test
    void shouldCheckRequiereHabitacion() {
        var hosp = repository.findByCodigo("HOSP");
        assertThat(hosp).isPresent();
        assertThat(hosp.get().getRequiereHabitacion()).isTrue();

        var cext = repository.findByCodigo("CEXT");
        assertThat(cext).isPresent();
        assertThat(cext.get().getRequiereHabitacion()).isFalse();
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var entity = new TipoAtencion();
        entity.setCodigo("CEXT");
        entity.setNombre("Duplicate");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

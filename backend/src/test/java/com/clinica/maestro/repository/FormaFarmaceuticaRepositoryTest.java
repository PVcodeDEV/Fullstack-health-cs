package com.clinica.maestro.repository;

import com.clinica.maestro.entity.clinico.FormaFarmaceutica;
import com.clinica.maestro.repository.clinico.FormaFarmaceuticaRepository;
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
class FormaFarmaceuticaRepositoryTest {

    @Autowired
    private FormaFarmaceuticaRepository repository;

    @BeforeEach
    void setUp() {
        var tab = new FormaFarmaceutica();
        tab.setCodigo("TAB");
        tab.setNombre("Tableta");
        tab.setRequierePreparacion(false);
        repository.save(tab);
        var iny = new FormaFarmaceutica();
        iny.setCodigo("INY");
        iny.setNombre("Inyectable");
        iny.setRequierePreparacion(true);
        repository.saveAndFlush(iny);
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("TAB");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Tableta");
    }

    @Test
    void shouldCheckRequierePreparacion() {
        var iny = repository.findByCodigo("INY");
        assertThat(iny).isPresent();
        assertThat(iny.get().getRequierePreparacion()).isTrue();

        var tab = repository.findByCodigo("TAB");
        assertThat(tab).isPresent();
        assertThat(tab.get().getRequierePreparacion()).isFalse();
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var entity = new FormaFarmaceutica();
        entity.setCodigo("TAB");
        entity.setNombre("Duplicate");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

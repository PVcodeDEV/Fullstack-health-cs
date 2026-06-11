package com.clinica.maestro.repository;

import com.clinica.maestro.entity.organizacion.AreaFuncional;
import com.clinica.maestro.repository.organizacion.AreaFuncionalRepository;
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
class AreaFuncionalRepositoryTest {

    @Autowired
    private AreaFuncionalRepository repository;

    @BeforeEach
    void setUp() {
        var adm = new AreaFuncional();
        adm.setCodigo("ADM");
        adm.setNombre("Admisión");
        adm.setEsAreaFisica(true);
        repository.save(adm);
        var lab = new AreaFuncional();
        lab.setCodigo("LAB");
        lab.setNombre("Laboratorio");
        lab.setEsAreaFisica(false);
        repository.saveAndFlush(lab);
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("ADM");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Admisión");
    }

    @Test
    void shouldFindByEsAreaFisica() {
        var fisicas = repository.findByEsAreaFisica(true);
        assertThat(fisicas).isNotEmpty();
        var noFisicas = repository.findByEsAreaFisica(false);
        assertThat(noFisicas).isNotEmpty();
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var entity = new AreaFuncional();
        entity.setCodigo("ADM");
        entity.setNombre("Duplicate");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

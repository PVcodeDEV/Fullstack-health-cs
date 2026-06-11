package com.clinica.maestro.repository;

import com.clinica.maestro.entity.clinico.EspecialidadMedica;
import com.clinica.maestro.repository.clinico.EspecialidadMedicaRepository;
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
class EspecialidadMedicaRepositoryTest {

    @Autowired
    private EspecialidadMedicaRepository repository;

    @BeforeEach
    void setUp() {
        var cg = new EspecialidadMedica();
        cg.setCodigo("CG");
        cg.setNombre("Cirugía General");
        repository.saveAndFlush(cg);
    }

    @Test
    void shouldSaveAndFindById() {
        var entity = new EspecialidadMedica();
        entity.setCodigo("TEST");
        entity.setNombre("Test Specialty");
        var saved = repository.save(entity);
        assertThat(saved.getId()).isNotNull();
        var found = repository.findById(saved.getId());
        assertThat(found).isPresent();
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("CG");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Cirugía General");
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var entity = new EspecialidadMedica();
        entity.setCodigo("CG");
        entity.setNombre("Duplicate");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

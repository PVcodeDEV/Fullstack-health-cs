package com.clinica.maestro.repository;

import com.clinica.maestro.entity.identidad.EstadoCivil;
import com.clinica.maestro.repository.identidad.EstadoCivilRepository;
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
class EstadoCivilRepositoryTest {

    @Autowired
    private EstadoCivilRepository repository;

    @BeforeEach
    void setUp() {
        var soltero = new EstadoCivil();
        soltero.setCodigoReniec("01");
        soltero.setNombre("Soltero");
        repository.saveAndFlush(soltero);
    }

    @Test
    void shouldSaveAndFindById() {
        var entity = new EstadoCivil();
        entity.setCodigoReniec("99");
        entity.setNombre("Test Estado");
        var saved = repository.save(entity);
        assertThat(saved.getId()).isNotNull();
        var found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCodigoReniec()).isEqualTo("99");
    }

    @Test
    void shouldFindByCodigoReniec() {
        var result = repository.findByCodigoReniec("01");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Soltero");
    }

    @Test
    void shouldCheckExistsByCodigoReniec() {
        assertThat(repository.existsByCodigoReniec("01")).isTrue();
        assertThat(repository.existsByCodigoReniec("99")).isFalse();
    }

    @Test
    void shouldReturnAllOrderedByNombre() {
        var result = repository.findAllByOrderByNombreAsc();
        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldRejectDuplicateCodigoReniec() {
        var entity = new EstadoCivil();
        entity.setCodigoReniec("01");
        entity.setNombre("Duplicate");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

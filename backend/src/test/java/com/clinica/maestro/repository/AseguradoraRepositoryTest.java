package com.clinica.maestro.repository;

import com.clinica.maestro.entity.organizacion.Aseguradora;
import com.clinica.maestro.repository.organizacion.AseguradoraRepository;
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
class AseguradoraRepositoryTest {

    @Autowired
    private AseguradoraRepository repository;

    @BeforeEach
    void setUp() {
        var ess = new Aseguradora();
        ess.setCodigo("ESS");
        ess.setNombre("Essalud");
        ess.setTipo("PUBLICO");
        repository.save(ess);
        var rim = new Aseguradora();
        rim.setCodigo("RIM");
        rim.setNombre("Rímac");
        rim.setTipo("PRIVADO");
        repository.saveAndFlush(rim);
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("ESS");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Essalud");
    }

    @Test
    void shouldCheckTipo() {
        var ess = repository.findByCodigo("ESS");
        assertThat(ess).isPresent();
        assertThat(ess.get().getTipo()).isEqualTo("PUBLICO");

        var rim = repository.findByCodigo("RIM");
        assertThat(rim).isPresent();
        assertThat(rim.get().getTipo()).isEqualTo("PRIVADO");
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var entity = new Aseguradora();
        entity.setCodigo("ESS");
        entity.setNombre("Duplicate");
        entity.setTipo("PUBLICO");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

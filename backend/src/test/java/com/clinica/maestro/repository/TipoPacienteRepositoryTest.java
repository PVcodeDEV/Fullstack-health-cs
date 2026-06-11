package com.clinica.maestro.repository;

import com.clinica.maestro.entity.clinico.TipoPaciente;
import com.clinica.maestro.repository.clinico.TipoPacienteRepository;
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
class TipoPacienteRepositoryTest {

    @Autowired
    private TipoPacienteRepository repository;

    @BeforeEach
    void setUp() {
        var aseg = new TipoPaciente();
        aseg.setCodigo("ASEG");
        aseg.setNombre("Asegurado Essalud");
        repository.saveAndFlush(aseg);
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("ASEG");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Asegurado Essalud");
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var entity = new TipoPaciente();
        entity.setCodigo("ASEG");
        entity.setNombre("Duplicate");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

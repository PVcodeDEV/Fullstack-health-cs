package com.clinica.maestro.repository;

import com.clinica.maestro.entity.clinico.ViaAdministracion;
import com.clinica.maestro.repository.clinico.ViaAdministracionRepository;
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
class ViaAdministracionRepositoryTest {

    @Autowired
    private ViaAdministracionRepository repository;

    @BeforeEach
    void setUp() {
        var oral = new ViaAdministracion();
        oral.setCodigo("ORAL");
        oral.setNombre("Oral");
        repository.saveAndFlush(oral);
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("ORAL");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Oral");
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var entity = new ViaAdministracion();
        entity.setCodigo("ORAL");
        entity.setNombre("Duplicate");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

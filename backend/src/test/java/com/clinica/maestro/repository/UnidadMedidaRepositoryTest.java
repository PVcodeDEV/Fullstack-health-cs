package com.clinica.maestro.repository;

import com.clinica.maestro.entity.financiero.UnidadMedida;
import com.clinica.maestro.repository.financiero.UnidadMedidaRepository;
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
class UnidadMedidaRepositoryTest {

    @Autowired
    private UnidadMedidaRepository repository;

    @BeforeEach
    void setUp() {
        var niu = new UnidadMedida();
        niu.setCodigoSunat("NIU");
        niu.setNombre("Unidad (bienes)");
        repository.saveAndFlush(niu);
    }

    @Test
    void shouldFindByCodigoSunat() {
        var result = repository.findByCodigoSunat("NIU");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Unidad (bienes)");
    }

    @Test
    void shouldRejectDuplicateCodigoSunat() {
        var entity = new UnidadMedida();
        entity.setCodigoSunat("NIU");
        entity.setNombre("Duplicate");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

package com.clinica.maestro.repository;

import com.clinica.maestro.entity.financiero.TipoMoneda;
import com.clinica.maestro.repository.financiero.TipoMonedaRepository;
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
class TipoMonedaRepositoryTest {

    @Autowired
    private TipoMonedaRepository repository;

    @BeforeEach
    void setUp() {
        var pen = new TipoMoneda();
        pen.setCodigoSunat("PEN");
        pen.setNombre("Soles");
        pen.setSimbolo("S/");
        repository.saveAndFlush(pen);
    }

    @Test
    void shouldFindByCodigoSunat() {
        var result = repository.findByCodigoSunat("PEN");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Soles");
        assertThat(result.get().getSimbolo()).isEqualTo("S/");
    }

    @Test
    void shouldRejectDuplicateCodigoSunat() {
        var entity = new TipoMoneda();
        entity.setCodigoSunat("PEN");
        entity.setNombre("Duplicate");
        entity.setSimbolo("X");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

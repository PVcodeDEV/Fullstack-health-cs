package com.clinica.maestro.repository;

import com.clinica.maestro.entity.ubigeo.UbigeoDepartamento;
import com.clinica.maestro.repository.ubigeo.UbigeoDepartamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UbigeoDepartamentoRepositoryTest {

    @Autowired
    private UbigeoDepartamentoRepository repository;

    @BeforeEach
    void setUp() {
        var amazonas = new UbigeoDepartamento();
        amazonas.setCodigo("01");
        amazonas.setNombre("Amazonas");
        repository.saveAndFlush(amazonas);
    }

    @Test
    void shouldFindById() {
        var found = repository.findById("01");
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("Amazonas");
    }

    @Test
    void shouldReturnAllOrderedByNombre() {
        var result = repository.findAllByOrderByNombreAsc();
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldSaveNewDepartment() {
        var entity = new UbigeoDepartamento();
        entity.setCodigo("99");
        entity.setNombre("Test Department");
        var saved = repository.save(entity);
        assertThat(saved.getCodigo()).isEqualTo("99");
        var found = repository.findById("99");
        assertThat(found).isPresent();
    }
}

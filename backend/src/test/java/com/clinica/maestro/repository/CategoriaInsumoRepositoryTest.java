package com.clinica.maestro.repository;

import com.clinica.maestro.entity.organizacion.CategoriaInsumo;
import com.clinica.maestro.repository.organizacion.CategoriaInsumoRepository;
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
class CategoriaInsumoRepositoryTest {

    @Autowired
    private CategoriaInsumoRepository repository;

    @BeforeEach
    void setUp() {
        var med = new CategoriaInsumo();
        med.setCodigo("MED");
        med.setNombre("Medicamento");
        repository.saveAndFlush(med);
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("MED");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Medicamento");
    }

    @Test
    void shouldReturnRootCategories() {
        var roots = repository.findByCategoriaPadreIsNull();
        assertThat(roots).isNotEmpty();
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var entity = new CategoriaInsumo();
        entity.setCodigo("MED");
        entity.setNombre("Duplicate");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

package com.clinica.farmacia.almacen.repository;

import com.clinica.farmacia.almacen.entity.Almacen;
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
class AlmacenRepositoryTest {

    @Autowired
    private AlmacenRepository repository;

    @BeforeEach
    void setUp() {
        Almacen a = new Almacen();
        a.setCodigo("DEF");
        a.setNombre("Almacén Principal");
        a.setUbicacion("Sótano");
        a.setDefaultWarehouse(true);
        repository.saveAndFlush(a);
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("DEF");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Almacén Principal");
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        Almacen a = new Almacen();
        a.setCodigo("DEF");
        a.setNombre("Duplicado");
        assertThatThrownBy(() -> repository.saveAndFlush(a))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindDefaultWarehouse() {
        var def = repository.findByDefaultWarehouseTrue();
        assertThat(def).isPresent();
        assertThat(def.get().getCodigo()).isEqualTo("DEF");
    }

    @Test
    void shouldHaveExactlyOneDefault() {
        assertThat(repository.countByDefaultWarehouseTrue()).isEqualTo(1);
    }
}

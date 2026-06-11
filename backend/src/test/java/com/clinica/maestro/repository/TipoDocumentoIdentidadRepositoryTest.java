package com.clinica.maestro.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
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
class TipoDocumentoIdentidadRepositoryTest {

    @Autowired
    private TipoDocumentoIdentidadRepository repository;

    @BeforeEach
    void setUp() {
        var dni = new TipoDocumentoIdentidad();
        dni.setCodigoSunat("01");
        dni.setNombre("DNI");
        dni.setLongitudMinima(8);
        dni.setLongitudMaxima(8);
        repository.saveAndFlush(dni);
    }

    @Test
    void shouldSaveAndFindById() {
        var entity = new TipoDocumentoIdentidad();
        entity.setCodigoSunat("99");
        entity.setNombre("Test Doc");
        entity.setLongitudMinima(1);
        entity.setLongitudMaxima(20);
        var saved = repository.save(entity);
        assertThat(saved.getId()).isNotNull();
        var found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCodigoSunat()).isEqualTo("99");
    }

    @Test
    void shouldFindByCodigoSunat() {
        var result = repository.findByCodigoSunat("01");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("DNI");
    }

    @Test
    void shouldCheckExistsByCodigoSunat() {
        assertThat(repository.existsByCodigoSunat("01")).isTrue();
        assertThat(repository.existsByCodigoSunat("ZZ")).isFalse();
    }

    @Test
    void shouldReturnAllOrderedByNombre() {
        var result = repository.findAllByOrderByNombreAsc();
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getNombre()).isNotNull();
    }

    @Test
    void shouldRejectDuplicateCodigoSunat() {
        var entity = new TipoDocumentoIdentidad();
        entity.setCodigoSunat("01");
        entity.setNombre("Duplicate");
        entity.setLongitudMinima(1);
        entity.setLongitudMaxima(20);
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldPerformSoftDelete() {
        var entity = new TipoDocumentoIdentidad();
        entity.setCodigoSunat("98");
        entity.setNombre("To Delete");
        entity.setLongitudMinima(1);
        entity.setLongitudMaxima(10);
        var saved = repository.save(entity);
        saved.markAsInactive();
        repository.save(saved);
        var found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getActivo()).isFalse();
    }
}

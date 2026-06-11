package com.clinica.maestro.repository;

import com.clinica.maestro.entity.clinico.CIE11Diagnostico;
import com.clinica.maestro.repository.clinico.CIE11DiagnosticoRepository;
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
class CIE11DiagnosticoRepositoryTest {

    @Autowired
    private CIE11DiagnosticoRepository repository;

    @BeforeEach
    void setUp() {
        var cholerae = new CIE11Diagnostico();
        cholerae.setCodigo("1A00.0");
        cholerae.setDescripcion("Cólera por Vibrio cholerae");
        cholerae.setCategoria("A");
        cholerae.setSexoAplicable("AMBOS");
        cholerae.setVersion("CIE-11");
        repository.save(cholerae);
        var dengue = new CIE11Diagnostico();
        dengue.setCodigo("1A13.0");
        dengue.setDescripcion("Dengue");
        dengue.setCategoria("A");
        dengue.setSexoAplicable("AMBOS");
        dengue.setVersion("CIE-11");
        repository.saveAndFlush(dengue);
    }

    @Test
    void shouldSaveAndFindById() {
        var entity = new CIE11Diagnostico();
        entity.setCodigo("ZZ00.0");
        entity.setDescripcion("Test diagnosis");
        entity.setCategoria("Z");
        entity.setSexoAplicable("AMBOS");
        entity.setVersion("CIE-11");
        var saved = repository.save(entity);
        assertThat(saved.getId()).isNotNull();
        var found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCodigo()).isEqualTo("ZZ00.0");
    }

    @Test
    void shouldFindByCodigoStartingWith() {
        var result = repository.findByCodigoStartingWithIgnoreCaseOrderByFrecuenciaUsoDesc("1A");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getCodigo()).startsWith("1A");
    }

    @Test
    void shouldFindByDescripcionContaining() {
        var result = repository.findByDescripcionContainingIgnoreCaseOrderByFrecuenciaUsoDesc("dengue");
        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldCheckExistsByCodigo() {
        assertThat(repository.existsByCodigo("1A00.0")).isTrue();
        assertThat(repository.existsByCodigo("ZZ99")).isFalse();
    }

    @Test
    void shouldReturnAllOrderedByFrecuenciaUsoDesc() {
        var result = repository.findAllByOrderByFrecuenciaUsoDescCodigoAsc();
        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var entity = new CIE11Diagnostico();
        entity.setCodigo("1A00.0");
        entity.setDescripcion("Duplicate");
        entity.setCategoria("Z");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

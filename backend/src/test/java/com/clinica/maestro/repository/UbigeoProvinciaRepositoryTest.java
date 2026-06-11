package com.clinica.maestro.repository;

import com.clinica.maestro.entity.ubigeo.UbigeoDepartamento;
import com.clinica.maestro.entity.ubigeo.UbigeoProvincia;
import com.clinica.maestro.repository.ubigeo.UbigeoDepartamentoRepository;
import com.clinica.maestro.repository.ubigeo.UbigeoProvinciaRepository;
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
class UbigeoProvinciaRepositoryTest {

    @Autowired
    private UbigeoProvinciaRepository repository;

    @Autowired
    private UbigeoDepartamentoRepository departamentoRepository;

    @BeforeEach
    void setUp() {
        var amazonas = new UbigeoDepartamento();
        amazonas.setCodigo("01");
        amazonas.setNombre("Amazonas");
        departamentoRepository.saveAndFlush(amazonas);
        var chachapoyas = new UbigeoProvincia();
        chachapoyas.setCodigo("0101");
        chachapoyas.setNombre("Chachapoyas");
        chachapoyas.setDepartamento(amazonas);
        repository.saveAndFlush(chachapoyas);
    }

    @Test
    void shouldFindByDepartamentoCodigo() {
        var result = repository.findByDepartamentoCodigoOrderByNombreAsc("01");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getDepartamento().getCodigo()).isEqualTo("01");
    }

    @Test
    void shouldReturnAllOrderedByNombre() {
        var result = repository.findAllByOrderByNombreAsc();
        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldRejectInvalidDepartamentoFK() {
        var entity = new UbigeoProvincia();
        entity.setCodigo("9999");
        entity.setNombre("Test Province");
        var dep = new UbigeoDepartamento();
        dep.setCodigo("99");
        dep.setNombre("Non-existent");
        entity.setDepartamento(dep);
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

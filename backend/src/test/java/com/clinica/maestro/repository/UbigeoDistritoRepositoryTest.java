package com.clinica.maestro.repository;

import com.clinica.maestro.entity.ubigeo.UbigeoDepartamento;
import com.clinica.maestro.entity.ubigeo.UbigeoDistrito;
import com.clinica.maestro.entity.ubigeo.UbigeoProvincia;
import com.clinica.maestro.repository.ubigeo.UbigeoDepartamentoRepository;
import com.clinica.maestro.repository.ubigeo.UbigeoDistritoRepository;
import com.clinica.maestro.repository.ubigeo.UbigeoProvinciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UbigeoDistritoRepositoryTest {

    @Autowired
    private UbigeoDistritoRepository repository;

    @Autowired
    private UbigeoProvinciaRepository provinciaRepository;

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
        provinciaRepository.saveAndFlush(chachapoyas);
        var distrito = new UbigeoDistrito();
        distrito.setCodigo("010101");
        distrito.setNombre("Chachapoyas");
        distrito.setProvincia(chachapoyas);
        repository.saveAndFlush(distrito);
    }

    @Test
    void shouldFindByProvinciaCodigo() {
        var result = repository.findByProvinciaCodigoOrderByNombreAsc("0101");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getProvincia().getCodigo()).isEqualTo("0101");
    }

    @Test
    void shouldReturnAllOrderedByNombre() {
        var result = repository.findAllByOrderByNombreAsc();
        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldFindById() {
        var found = repository.findById("010101");
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("Chachapoyas");
    }
}

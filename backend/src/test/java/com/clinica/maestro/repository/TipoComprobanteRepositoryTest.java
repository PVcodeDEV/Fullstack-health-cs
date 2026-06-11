package com.clinica.maestro.repository;

import com.clinica.maestro.entity.financiero.TipoComprobante;
import com.clinica.maestro.repository.financiero.TipoComprobanteRepository;
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
class TipoComprobanteRepositoryTest {

    @Autowired
    private TipoComprobanteRepository repository;

    @BeforeEach
    void setUp() {
        var factura = new TipoComprobante();
        factura.setCodigoSunat("01");
        factura.setNombre("Factura");
        repository.saveAndFlush(factura);
    }

    @Test
    void shouldFindByCodigoSunat() {
        var result = repository.findByCodigoSunat("01");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Factura");
    }

    @Test
    void shouldRejectDuplicateCodigoSunat() {
        var entity = new TipoComprobante();
        entity.setCodigoSunat("01");
        entity.setNombre("Duplicate");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

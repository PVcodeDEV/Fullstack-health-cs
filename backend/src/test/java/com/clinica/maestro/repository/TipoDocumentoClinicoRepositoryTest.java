package com.clinica.maestro.repository;

import com.clinica.maestro.entity.organizacion.TipoDocumentoClinico;
import com.clinica.maestro.repository.organizacion.TipoDocumentoClinicoRepository;
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
class TipoDocumentoClinicoRepositoryTest {

    @Autowired
    private TipoDocumentoClinicoRepository repository;

    @BeforeEach
    void setUp() {
        var hc = new TipoDocumentoClinico();
        hc.setCodigo("HC");
        hc.setNombre("Historia Clínica");
        hc.setRequiereFirma(true);
        repository.save(hc);
        var rec = new TipoDocumentoClinico();
        rec.setCodigo("REC");
        rec.setNombre("Receta");
        rec.setRequiereFirma(false);
        repository.saveAndFlush(rec);
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("HC");
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Historia Clínica");
    }

    @Test
    void shouldFilterByRequiereFirma() {
        var conFirma = repository.findByRequiereFirma(true);
        assertThat(conFirma).isNotEmpty();
        var sinFirma = repository.findByRequiereFirma(false);
        assertThat(sinFirma).isNotEmpty();
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var entity = new TipoDocumentoClinico();
        entity.setCodigo("HC");
        entity.setNombre("Duplicate");
        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

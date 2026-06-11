package com.clinica.clinica.cuenta.repository;

import com.clinica.clinica.cuenta.entity.CargoAdicional;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CargoAdicionalRepositoryTest {

    @Autowired
    private CargoAdicionalRepository cargoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private CargoAdicional cargo;

    @BeforeEach
    void setUp() {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocumentoIdentidadRepository.saveAndFlush(tdi);

        var paciente = new Persona();
        paciente.setTipoDocumentoIdentidad(tdi);
        paciente.setNumeroDocumento("12345678");
        paciente.setNombres("JUAN");
        paciente.setApellidoPaterno("PEREZ");
        paciente.setActivo(true);
        personaRepository.saveAndFlush(paciente);

        cargo = new CargoAdicional();
        cargo.setCuentaId(1L);
        cargo.setTipo("GENERAL");
        cargo.setMonto(new BigDecimal("150.00"));
        cargo.setDescripcion("Honorarios médicos");
        cargo.setFechaRegistro(LocalDateTime.now());
        cargo.setUsuarioId(1L);
        cargo = cargoRepository.saveAndFlush(cargo);
    }

    @Test
    void shouldSaveAndFindById() {
        var c = new CargoAdicional();
        c.setCuentaId(2L);
        c.setTipo("FARMACIA");
        c.setMonto(new BigDecimal("75.50"));
        c.setDescripcion("Medicamentos");
        c.setFechaRegistro(LocalDateTime.now());
        c.setUsuarioId(1L);

        var saved = cargoRepository.save(c);
        assertThat(saved.getId()).isNotNull();

        var found = cargoRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getMonto()).isEqualByComparingTo(new BigDecimal("75.50"));
    }

    @Test
    void shouldFindByCuentaId() {
        var result = cargoRepository.findByCuentaId(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescripcion()).isEqualTo("Honorarios médicos");
    }

    @Test
    void shouldFindByCuentaIdAndTipo() {
        var result = cargoRepository.findByCuentaIdAndTipo(1L, "GENERAL");
        assertThat(result).hasSize(1);

        var empty = cargoRepository.findByCuentaIdAndTipo(1L, "FARMACIA");
        assertThat(empty).isEmpty();
    }

    @Test
    void shouldSupportSoftDelete() {
        cargo.markAsInactive();
        cargoRepository.save(cargo);

        var found = cargoRepository.findById(cargo.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getActivo()).isFalse();
    }

    @Test
    void shouldStoreMultipleCargos() {
        var c2 = new CargoAdicional();
        c2.setCuentaId(1L);
        c2.setTipo("FARMACIA");
        c2.setMonto(new BigDecimal("200.00"));
        c2.setDescripcion("Antibióticos");
        c2.setFechaRegistro(LocalDateTime.now());
        c2.setUsuarioId(1L);
        cargoRepository.saveAndFlush(c2);

        var result = cargoRepository.findByCuentaId(1L);
        assertThat(result).hasSize(2);
    }
}

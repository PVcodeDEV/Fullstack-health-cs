package com.clinica.clinica.admision.repository;

import com.clinica.clinica.admision.entity.Cuenta;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CuentaRepositoryTest {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private Persona paciente;

    @BeforeEach
    void setUp() {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocumentoIdentidadRepository.saveAndFlush(tdi);

        paciente = new Persona();
        paciente.setTipoDocumentoIdentidad(tdi);
        paciente.setNumeroDocumento("12345678");
        paciente.setNombres("JUAN");
        paciente.setApellidoPaterno("PEREZ");
        paciente.setActivo(true);
        paciente = personaRepository.saveAndFlush(paciente);

        var cuenta = new Cuenta();
        cuenta.setPacienteId(paciente.getId());
        cuenta.setFechaApertura(LocalDateTime.now());
        cuenta.setEstado("ABIERTA");
        cuentaRepository.saveAndFlush(cuenta);
    }

    @Test
    void shouldSaveAndFindById() {
        var cuenta = new Cuenta();
        cuenta.setPacienteId(paciente.getId());
        cuenta.setFechaApertura(LocalDateTime.now());
        cuenta.setEstado("ABIERTA");

        var saved = cuentaRepository.save(cuenta);
        assertThat(saved.getId()).isNotNull();

        var found = cuentaRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEstado()).isEqualTo("ABIERTA");
    }

    @Test
    void shouldFindByPacienteId() {
        var result = cuentaRepository.findByPacienteId(paciente.getId());
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFindByEstado() {
        var result = cuentaRepository.findByEstado("ABIERTA");
        assertThat(result).hasSize(1);

        var empty = cuentaRepository.findByEstado("CERRADA");
        assertThat(empty).isEmpty();
    }

    @Test
    void shouldSupportEstadoTransitions() {
        var cuentas = cuentaRepository.findByEstado("ABIERTA");
        assertThat(cuentas).hasSize(1);

        var cuenta = cuentas.get(0);
        cuenta.setEstado("CERRADA");
        cuentaRepository.saveAndFlush(cuenta);

        var cerradas = cuentaRepository.findByEstado("CERRADA");
        assertThat(cerradas).hasSize(1);

        var abiertas = cuentaRepository.findByEstado("ABIERTA");
        assertThat(abiertas).isEmpty();
    }

    @Test
    void shouldFindAllByActivoTrue() {
        var result = cuentaRepository.findAllByActivoTrue();
        assertThat(result).hasSize(1);
    }
}

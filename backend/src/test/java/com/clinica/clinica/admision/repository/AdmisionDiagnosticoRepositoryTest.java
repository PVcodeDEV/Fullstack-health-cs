package com.clinica.clinica.admision.repository;

import com.clinica.clinica.admision.entity.AdmisionDiagnostico;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdmisionDiagnosticoRepositoryTest {

    @Autowired
    private AdmisionDiagnosticoRepository diagnosticoRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private Cuenta cuenta;

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
        paciente = personaRepository.saveAndFlush(paciente);

        cuenta = new Cuenta();
        cuenta.setPacienteId(paciente.getId());
        cuenta.setFechaApertura(LocalDateTime.now());
        cuenta.setEstado("ABIERTA");
        cuenta = cuentaRepository.saveAndFlush(cuenta);

        var diag = new AdmisionDiagnostico();
        diag.setCuentaId(cuenta.getId());
        diag.setCodigoCIE11("AA00.0");
        diag.setTipo("PRINCIPAL");
        diagnosticoRepository.saveAndFlush(diag);
    }

    @Test
    void shouldSaveAndFindById() {
        var diag = new AdmisionDiagnostico();
        diag.setCuentaId(cuenta.getId());
        diag.setCodigoCIE11("BB11.1");
        diag.setTipo("SECUNDARIO");

        var saved = diagnosticoRepository.save(diag);
        assertThat(saved.getId()).isNotNull();

        var found = diagnosticoRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCodigoCIE11()).isEqualTo("BB11.1");
    }

    @Test
    void shouldFindByCuentaId() {
        var result = diagnosticoRepository.findByCuentaId(cuenta.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodigoCIE11()).isEqualTo("AA00.0");
    }

    @Test
    void shouldFindByCuentaIdAndTipo() {
        var result = diagnosticoRepository.findByCuentaIdAndTipo(cuenta.getId(), "PRINCIPAL");
        assertThat(result).hasSize(1);

        var secundarios = diagnosticoRepository.findByCuentaIdAndTipo(cuenta.getId(), "SECUNDARIO");
        assertThat(secundarios).isEmpty();
    }

    @Test
    void shouldStoreCIECodeCorrectly() {
        var diag = new AdmisionDiagnostico();
        diag.setCuentaId(cuenta.getId());
        diag.setCodigoCIE11("ZZ99.9");
        diag.setTipo("PRINCIPAL");
        diagnosticoRepository.saveAndFlush(diag);

        var found = diagnosticoRepository.findByCuentaIdAndTipo(cuenta.getId(), "PRINCIPAL");
        var codes = found.stream().map(AdmisionDiagnostico::getCodigoCIE11).toList();
        assertThat(codes).contains("AA00.0", "ZZ99.9");
    }
}

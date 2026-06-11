package com.clinica.clinica.paciente.repository;

import com.clinica.clinica.paciente.entity.Paciente;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
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
class PacienteRepositoryTest {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private Persona persona;

    @BeforeEach
    void setUp() {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocumentoIdentidadRepository.saveAndFlush(tdi);

        persona = new Persona();
        persona.setTipoDocumentoIdentidad(tdi);
        persona.setNumeroDocumento("12345678");
        persona.setNombres("JUAN");
        persona.setApellidoPaterno("PEREZ");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        var paciente = new Paciente();
        paciente.setPersona(persona);
        paciente.setTipoPaciente("PARTICULAR");
        paciente.setNroHistoriaClinica("HC-2025-00001");
        paciente.setActivo(true);
        pacienteRepository.saveAndFlush(paciente);
    }

    @Test
    void shouldSaveAndFindById() {
        var newPersona = new Persona();
        newPersona.setTipoDocumentoIdentidad(persona.getTipoDocumentoIdentidad());
        newPersona.setNumeroDocumento("87654321");
        newPersona.setNombres("MARIA");
        newPersona.setApellidoPaterno("GARCIA");
        newPersona.setActivo(true);
        newPersona = personaRepository.saveAndFlush(newPersona);

        var paciente = new Paciente();
        paciente.setPersona(newPersona);
        paciente.setTipoPaciente("VIP");
        paciente.setActivo(true);

        var saved = pacienteRepository.save(paciente);
        assertThat(saved.getId()).isNotNull();

        var found = pacienteRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTipoPaciente()).isEqualTo("VIP");
    }

    @Test
    void shouldFindByPersonaId() {
        var result = pacienteRepository.findByPersonaId(persona.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getTipoPaciente()).isEqualTo("PARTICULAR");
    }

    @Test
    void shouldFindByNroHistoriaClinica() {
        var result = pacienteRepository.findByNroHistoriaClinica("HC-2025-00001");
        assertThat(result).isPresent();
        assertThat(result.get().getPersona().getNombres()).isEqualTo("JUAN");
    }

    @Test
    void shouldCheckExistsByPersonaId() {
        assertThat(pacienteRepository.existsByPersonaId(persona.getId())).isTrue();
        assertThat(pacienteRepository.existsByPersonaId(999L)).isFalse();
    }

    @Test
    void shouldRejectDuplicatePersona() {
        var paciente = new Paciente();
        paciente.setPersona(persona); // same persona
        paciente.setTipoPaciente("VIP");
        paciente.setActivo(true);

        assertThatThrownBy(() -> pacienteRepository.saveAndFlush(paciente))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindAllByActivoTrue() {
        // Add an inactive paciente
        var newPersona = new Persona();
        newPersona.setTipoDocumentoIdentidad(persona.getTipoDocumentoIdentidad());
        newPersona.setNumeroDocumento("55555555");
        newPersona.setNombres("INACTIVO");
        newPersona.setApellidoPaterno("TEST");
        newPersona.setActivo(true);
        newPersona = personaRepository.saveAndFlush(newPersona);

        var inactive = new Paciente();
        inactive.setPersona(newPersona);
        inactive.setTipoPaciente("VIP");
        inactive.setActivo(false);
        pacienteRepository.saveAndFlush(inactive);

        var result = pacienteRepository.findAllByActivoTrue();
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldPerformSoftDelete() {
        var newPersona = new Persona();
        newPersona.setTipoDocumentoIdentidad(persona.getTipoDocumentoIdentidad());
        newPersona.setNumeroDocumento("99999999");
        newPersona.setNombres("DELETE");
        newPersona.setApellidoPaterno("ME");
        newPersona.setActivo(true);
        newPersona = personaRepository.saveAndFlush(newPersona);

        var paciente = new Paciente();
        paciente.setPersona(newPersona);
        paciente.setTipoPaciente("PARTICULAR");
        paciente.setActivo(true);
        var saved = pacienteRepository.save(paciente);

        saved.markAsInactive();
        pacienteRepository.save(saved);

        var found = pacienteRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getActivo()).isFalse();
    }
}

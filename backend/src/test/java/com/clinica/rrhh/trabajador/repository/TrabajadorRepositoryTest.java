package com.clinica.rrhh.trabajador.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrabajadorRepositoryTest {

    @Autowired
    private TrabajadorRepository trabajadorRepository;

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

        var trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-001");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setActivo(true);
        trabajadorRepository.saveAndFlush(trabajador);
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

        var trabajador = new Trabajador();
        trabajador.setPersona(newPersona);
        trabajador.setCodigoTrabajador("TR-002");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setActivo(true);

        var saved = trabajadorRepository.save(trabajador);
        assertThat(saved.getId()).isNotNull();

        var found = trabajadorRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCodigoTrabajador()).isEqualTo("TR-002");
    }

    @Test
    void shouldFindByPersonaId() {
        var result = trabajadorRepository.findByPersonaId(persona.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getCodigoTrabajador()).isEqualTo("TR-001");
    }

    @Test
    void shouldFindByCodigoTrabajador() {
        var result = trabajadorRepository.findByCodigoTrabajador("TR-001");
        assertThat(result).isPresent();
        assertThat(result.get().getPersona().getNombres()).isEqualTo("JUAN");
    }

    @Test
    void shouldCheckExistsByPersonaId() {
        assertThat(trabajadorRepository.existsByPersonaId(persona.getId())).isTrue();
        assertThat(trabajadorRepository.existsByPersonaId(999L)).isFalse();
    }

    @Test
    void shouldCheckExistsByCodigoTrabajador() {
        assertThat(trabajadorRepository.existsByCodigoTrabajador("TR-001")).isTrue();
        assertThat(trabajadorRepository.existsByCodigoTrabajador("TR-XXX")).isFalse();
    }

    @Test
    void shouldRejectDuplicatePersona() {
        var trabajador = new Trabajador();
        trabajador.setPersona(persona); // same persona
        trabajador.setCodigoTrabajador("TR-002");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setActivo(true);

        assertThatThrownBy(() -> trabajadorRepository.saveAndFlush(trabajador))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectDuplicateCodigoTrabajador() {
        var newPersona = new Persona();
        newPersona.setTipoDocumentoIdentidad(persona.getTipoDocumentoIdentidad());
        newPersona.setNumeroDocumento("55555555");
        newPersona.setNombres("OTRA");
        newPersona.setApellidoPaterno("PERSONA");
        newPersona.setActivo(true);
        newPersona = personaRepository.saveAndFlush(newPersona);

        var trabajador = new Trabajador();
        trabajador.setPersona(newPersona);
        trabajador.setCodigoTrabajador("TR-001"); // duplicate code
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setActivo(true);

        assertThatThrownBy(() -> trabajadorRepository.saveAndFlush(trabajador))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindAllByActivoTrue() {
        var newPersona = new Persona();
        newPersona.setTipoDocumentoIdentidad(persona.getTipoDocumentoIdentidad());
        newPersona.setNumeroDocumento("99999999");
        newPersona.setNombres("INACTIVO");
        newPersona.setApellidoPaterno("TEST");
        newPersona.setActivo(true);
        newPersona = personaRepository.saveAndFlush(newPersona);

        var inactive = new Trabajador();
        inactive.setPersona(newPersona);
        inactive.setCodigoTrabajador("TR-999");
        inactive.setFechaIngreso(LocalDate.of(2025, 1, 1));
        inactive.setActivo(false);
        trabajadorRepository.saveAndFlush(inactive);

        var result = trabajadorRepository.findAllByActivoTrue();
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldPerformSoftDelete() {
        var newPersona = new Persona();
        newPersona.setTipoDocumentoIdentidad(persona.getTipoDocumentoIdentidad());
        newPersona.setNumeroDocumento("77777777");
        newPersona.setNombres("DELETE");
        newPersona.setApellidoPaterno("ME");
        newPersona.setActivo(true);
        newPersona = personaRepository.saveAndFlush(newPersona);

        var trabajador = new Trabajador();
        trabajador.setPersona(newPersona);
        trabajador.setCodigoTrabajador("TR-DEL");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setActivo(true);
        var saved = trabajadorRepository.save(trabajador);

        saved.markAsInactive();
        trabajadorRepository.save(saved);

        var found = trabajadorRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getActivo()).isFalse();
    }
}

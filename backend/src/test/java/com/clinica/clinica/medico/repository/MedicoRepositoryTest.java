package com.clinica.clinica.medico.repository;

import com.clinica.clinica.medico.entity.Medico;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
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
class MedicoRepositoryTest {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private Trabajador trabajador;

    @BeforeEach
    void setUp() {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocumentoIdentidadRepository.saveAndFlush(tdi);

        var persona = new Persona();
        persona.setTipoDocumentoIdentidad(tdi);
        persona.setNumeroDocumento("12345678");
        persona.setNombres("JUAN");
        persona.setApellidoPaterno("PEREZ");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-001");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);

        var medico = new Medico();
        medico.setPersona(persona);
        medico.setTrabajador(trabajador);
        medico.setCmp("12345");
        medico.setEsEspecialista(false);
        medico.setActivo(true);
        medicoRepository.saveAndFlush(medico);
    }

    @Test
    void shouldSaveAndFindById() {
        var newPersona = new Persona();
        newPersona.setTipoDocumentoIdentidad(trabajador.getPersona().getTipoDocumentoIdentidad());
        newPersona.setNumeroDocumento("87654321");
        newPersona.setNombres("MARIA");
        newPersona.setApellidoPaterno("GARCIA");
        newPersona.setActivo(true);
        newPersona = personaRepository.saveAndFlush(newPersona);

        var newTrabajador = new Trabajador();
        newTrabajador.setPersona(newPersona);
        newTrabajador.setCodigoTrabajador("TR-002");
        newTrabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        newTrabajador.setActivo(true);
        newTrabajador = trabajadorRepository.saveAndFlush(newTrabajador);

        var medico = new Medico();
        medico.setPersona(newPersona);
        medico.setTrabajador(newTrabajador);
        medico.setCmp("99999");
        medico.setEsEspecialista(true);
        medico.setActivo(true);

        var saved = medicoRepository.save(medico);
        assertThat(saved.getId()).isNotNull();

        var found = medicoRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCmp()).isEqualTo("99999");
        assertThat(found.get().getEsEspecialista()).isTrue();
    }

    @Test
    void shouldFindByTrabajadorId() {
        var result = medicoRepository.findByTrabajadorId(trabajador.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getCmp()).isEqualTo("12345");
    }

    @Test
    void shouldFindByCmp() {
        var result = medicoRepository.findByCmp("12345");
        assertThat(result).isPresent();
        assertThat(result.get().getTrabajador().getCodigoTrabajador()).isEqualTo("TR-001");
    }

    @Test
    void shouldCheckExistsByTrabajadorId() {
        assertThat(medicoRepository.existsByTrabajadorId(trabajador.getId())).isTrue();
        assertThat(medicoRepository.existsByTrabajadorId(999L)).isFalse();
    }

    @Test
    void shouldCheckExistsByCmp() {
        assertThat(medicoRepository.existsByCmp("12345")).isTrue();
        assertThat(medicoRepository.existsByCmp("XXXXX")).isFalse();
    }

    @Test
    void shouldRejectDuplicateTrabajador() {
        var medico = new Medico();
        medico.setPersona(trabajador.getPersona());
        medico.setTrabajador(trabajador); // same trabajador
        medico.setCmp("54321");
        medico.setEsEspecialista(false);
        medico.setActivo(true);

        assertThatThrownBy(() -> medicoRepository.saveAndFlush(medico))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectDuplicateCmp() {
        var newPersona = new Persona();
        newPersona.setTipoDocumentoIdentidad(trabajador.getPersona().getTipoDocumentoIdentidad());
        newPersona.setNumeroDocumento("55555555");
        newPersona.setNombres("OTRA");
        newPersona.setApellidoPaterno("PERSONA");
        newPersona.setActivo(true);
        newPersona = personaRepository.saveAndFlush(newPersona);

        var newTrabajador = new Trabajador();
        newTrabajador.setPersona(newPersona);
        newTrabajador.setCodigoTrabajador("TR-003");
        newTrabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        newTrabajador.setActivo(true);
        newTrabajador = trabajadorRepository.saveAndFlush(newTrabajador);

        var medico = new Medico();
        medico.setPersona(newPersona);
        medico.setTrabajador(newTrabajador);
        medico.setCmp("12345"); // duplicate CMP
        medico.setActivo(true);

        assertThatThrownBy(() -> medicoRepository.saveAndFlush(medico))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindAllByActivoTrue() {
        var newPersona = new Persona();
        newPersona.setTipoDocumentoIdentidad(trabajador.getPersona().getTipoDocumentoIdentidad());
        newPersona.setNumeroDocumento("99999999");
        newPersona.setNombres("INACTIVO");
        newPersona.setApellidoPaterno("TEST");
        newPersona.setActivo(true);
        newPersona = personaRepository.saveAndFlush(newPersona);

        var newTrabajador = new Trabajador();
        newTrabajador.setPersona(newPersona);
        newTrabajador.setCodigoTrabajador("TR-999");
        newTrabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        newTrabajador.setActivo(true);
        newTrabajador = trabajadorRepository.saveAndFlush(newTrabajador);

        var inactive = new Medico();
        inactive.setPersona(newPersona);
        inactive.setTrabajador(newTrabajador);
        inactive.setCmp("99999");
        inactive.setActivo(false);
        medicoRepository.saveAndFlush(inactive);

        var result = medicoRepository.findAllByActivoTrue();
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldPerformSoftDelete() {
        var newPersona = new Persona();
        newPersona.setTipoDocumentoIdentidad(trabajador.getPersona().getTipoDocumentoIdentidad());
        newPersona.setNumeroDocumento("77777777");
        newPersona.setNombres("DELETE");
        newPersona.setApellidoPaterno("ME");
        newPersona.setActivo(true);
        newPersona = personaRepository.saveAndFlush(newPersona);

        var newTrabajador = new Trabajador();
        newTrabajador.setPersona(newPersona);
        newTrabajador.setCodigoTrabajador("TR-DEL");
        newTrabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        newTrabajador.setActivo(true);
        newTrabajador = trabajadorRepository.saveAndFlush(newTrabajador);

        var medico = new Medico();
        medico.setPersona(newPersona);
        medico.setTrabajador(newTrabajador);
        medico.setCmp("DEL00");
        medico.setActivo(true);
        var saved = medicoRepository.save(medico);

        saved.markAsInactive();
        medicoRepository.save(saved);

        var found = medicoRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getActivo()).isFalse();
    }
}

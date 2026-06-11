package com.clinica.rrhh.derechohabiente.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.derechohabiente.entity.Derechohabiente;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.TipoRelacionDerechohabiente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DerechohabienteRepositoryTest {

    @Autowired
    private DerechohabienteRepository derechohabienteRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private Trabajador trabajador;
    private Persona personaDerechohabiente;

    @BeforeEach
    void setUp() {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocumentoIdentidadRepository.saveAndFlush(tdi);

        var personaTrabajador = new Persona();
        personaTrabajador.setTipoDocumentoIdentidad(tdi);
        personaTrabajador.setNumeroDocumento("33333333");
        personaTrabajador.setNombres("PEDRO");
        personaTrabajador.setApellidoPaterno("RAMIREZ");
        personaTrabajador.setActivo(true);
        personaTrabajador = personaRepository.saveAndFlush(personaTrabajador);

        trabajador = new Trabajador();
        trabajador.setPersona(personaTrabajador);
        trabajador.setCodigoTrabajador("TR-003");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);

        personaDerechohabiente = new Persona();
        personaDerechohabiente.setTipoDocumentoIdentidad(tdi);
        personaDerechohabiente.setNumeroDocumento("44444444");
        personaDerechohabiente.setNombres("HIJO");
        personaDerechohabiente.setApellidoPaterno("RAMIREZ");
        personaDerechohabiente.setActivo(true);
        personaDerechohabiente = personaRepository.saveAndFlush(personaDerechohabiente);
    }

    @Test
    void shouldSaveAndFindById() {
        var dh = new Derechohabiente();
        dh.setTrabajador(trabajador);
        dh.setPersona(personaDerechohabiente);
        dh.setRelacion(TipoRelacionDerechohabiente.HIJO);
        dh.setFechaInicio(LocalDate.of(2025, 1, 1));
        dh.setEstado("ACTIVO");

        var saved = derechohabienteRepository.save(dh);
        assertThat(saved.getId()).isNotNull();

        var found = derechohabienteRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRelacion()).isEqualTo(TipoRelacionDerechohabiente.HIJO);
        assertThat(found.get().getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    void shouldFindByTrabajadorIdAndEstado() {
        var dh = new Derechohabiente();
        dh.setTrabajador(trabajador);
        dh.setPersona(personaDerechohabiente);
        dh.setRelacion(TipoRelacionDerechohabiente.CONYUGE);
        dh.setFechaInicio(LocalDate.of(2025, 1, 1));
        dh.setEstado("ACTIVO");
        derechohabienteRepository.saveAndFlush(dh);

        var result = derechohabienteRepository.findByTrabajadorIdAndEstadoOrderByFechaInicioDesc(
                trabajador.getId(), "ACTIVO");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRelacion()).isEqualTo(TipoRelacionDerechohabiente.CONYUGE);

        var inactivos = derechohabienteRepository.findByTrabajadorIdAndEstadoOrderByFechaInicioDesc(
                trabajador.getId(), "INACTIVO");
        assertThat(inactivos).isEmpty();
    }

    @Test
    void shouldRejectNullRelacion() {
        var dh = new Derechohabiente();
        dh.setTrabajador(trabajador);
        dh.setPersona(personaDerechohabiente);
        dh.setFechaInicio(LocalDate.of(2025, 1, 1));
        dh.setEstado("ACTIVO");

        assertThatThrownBy(() -> derechohabienteRepository.saveAndFlush(dh))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindByEstado() {
        var dh = new Derechohabiente();
        dh.setTrabajador(trabajador);
        dh.setPersona(personaDerechohabiente);
        dh.setRelacion(TipoRelacionDerechohabiente.PADRE);
        dh.setFechaInicio(LocalDate.of(2025, 1, 1));
        dh.setEstado("ACTIVO");
        derechohabienteRepository.saveAndFlush(dh);

        var activos = derechohabienteRepository.findByEstado("ACTIVO");
        assertThat(activos).isNotEmpty();
    }
}

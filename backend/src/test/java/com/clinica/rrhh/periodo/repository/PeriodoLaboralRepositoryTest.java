package com.clinica.rrhh.periodo.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.periodo.entity.PeriodoLaboral;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
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
class PeriodoLaboralRepositoryTest {

    @Autowired
    private PeriodoLaboralRepository periodoLaboralRepository;

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
        persona.setNumeroDocumento("22222222");
        persona.setNombres("MARIA");
        persona.setApellidoPaterno("GARCIA");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-002");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);
    }

    @Test
    void shouldSavePeriodo() {
        var pl = new PeriodoLaboral();
        pl.setTrabajador(trabajador);
        pl.setFechaInicio(LocalDate.of(2025, 1, 1));
        pl.setEsReingreso(false);

        var saved = periodoLaboralRepository.save(pl);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFechaInicio()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(saved.getActivo()).isTrue();
    }

    @Test
    void shouldFindByTrabajadorIdAndActivoTrue() {
        var pl = new PeriodoLaboral();
        pl.setTrabajador(trabajador);
        pl.setFechaInicio(LocalDate.of(2025, 1, 1));
        periodoLaboralRepository.saveAndFlush(pl);

        var found = periodoLaboralRepository.findByTrabajadorIdAndActivoTrue(trabajador.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFechaInicio()).isEqualTo(LocalDate.of(2025, 1, 1));

        // Mark inactive
        found.get().setActivo(false);
        periodoLaboralRepository.saveAndFlush(found.get());

        var afterInactive = periodoLaboralRepository.findByTrabajadorIdAndActivoTrue(trabajador.getId());
        assertThat(afterInactive).isEmpty();
    }

    @Test
    void shouldRejectNullTrabajador() {
        var pl = new PeriodoLaboral();
        pl.setFechaInicio(LocalDate.of(2025, 1, 1));
        assertThatThrownBy(() -> periodoLaboralRepository.saveAndFlush(pl))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindByTrabajadorIdOrderByFechaInicioDesc() {
        var pl1 = new PeriodoLaboral();
        pl1.setTrabajador(trabajador);
        pl1.setFechaInicio(LocalDate.of(2024, 1, 1));
        periodoLaboralRepository.saveAndFlush(pl1);

        var pl2 = new PeriodoLaboral();
        pl2.setTrabajador(trabajador);
        pl2.setFechaInicio(LocalDate.of(2025, 6, 1));
        periodoLaboralRepository.saveAndFlush(pl2);

        var result = periodoLaboralRepository.findByTrabajadorIdOrderByFechaInicioDesc(trabajador.getId());
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFechaInicio()).isEqualTo(LocalDate.of(2025, 6, 1));
        assertThat(result.get(1).getFechaInicio()).isEqualTo(LocalDate.of(2024, 1, 1));
    }
}

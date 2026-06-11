package com.clinica.rrhh.planillaplame.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planillaplame.entity.TRegistroEvento;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TRegistroEventoRepositoryTest {

    @Autowired
    private TRegistroEventoRepository repository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private PeriodoPlanillaRepository periodoPlanillaRepository;

    private Trabajador trabajador;
    private PeriodoPlanilla periodoPlanilla;

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
        persona.setNumeroDocumento("66666666");
        persona.setNombres("TREGISTRO");
        persona.setApellidoPaterno("EVENTO");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-TRE-001");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setCantidadHijos(0);
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);

        periodoPlanilla = new PeriodoPlanilla();
        periodoPlanilla.setAnio(2026);
        periodoPlanilla.setMes(1);
        periodoPlanilla.setFechaInicio(LocalDate.of(2026, Month.JANUARY, 1));
        periodoPlanilla.setFechaFin(LocalDate.of(2026, Month.JANUARY, 31));
        periodoPlanilla.setEstado("CERRADO");
        periodoPlanilla = periodoPlanillaRepository.saveAndFlush(periodoPlanilla);
    }

    private TRegistroEvento createEvento(Trabajador t, PeriodoPlanilla pp, String tipo, LocalDate fecha) {
        var e = new TRegistroEvento();
        e.setTrabajador(t);
        e.setTipoEvento(tipo);
        e.setFechaEvento(fecha);
        e.setPeriodoPlanilla(pp);
        e.setEstado("PENDIENTE");
        return e;
    }

    @Test
    void shouldSaveAndFindById() {
        var evento = createEvento(trabajador, periodoPlanilla, "ALTA", LocalDate.of(2026, Month.JANUARY, 15));
        var saved = repository.save(evento);
        assertThat(saved.getId()).isNotNull();

        var found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTipoEvento()).isEqualTo("ALTA");
        assertThat(found.get().getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    void shouldFindByPeriodoPlanillaIdOrderByFechaEventoAsc() {
        var e1 = createEvento(trabajador, periodoPlanilla, "ALTA", LocalDate.of(2026, Month.JANUARY, 5));
        repository.saveAndFlush(e1);

        var e2 = createEvento(trabajador, periodoPlanilla, "BAJA", LocalDate.of(2026, Month.JANUARY, 20));
        repository.saveAndFlush(e2);

        List<TRegistroEvento> result = repository
                .findByPeriodoPlanillaIdOrderByFechaEventoAsc(periodoPlanilla.getId());
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFechaEvento()).isBefore(result.get(1).getFechaEvento());
        assertThat(result.get(0).getPeriodoPlanilla().getId()).isEqualTo(periodoPlanilla.getId());
    }

    @Test
    void shouldFindByTrabajadorIdOrderByFechaEventoDesc() {
        var e1 = createEvento(trabajador, periodoPlanilla, "ALTA", LocalDate.of(2026, Month.JANUARY, 5));
        repository.saveAndFlush(e1);

        var e2 = createEvento(trabajador, periodoPlanilla, "BAJA", LocalDate.of(2026, Month.JANUARY, 20));
        repository.saveAndFlush(e2);

        List<TRegistroEvento> result = repository
                .findByTrabajadorIdOrderByFechaEventoDesc(trabajador.getId());
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFechaEvento()).isAfter(result.get(1).getFechaEvento());
        assertThat(result.get(0).getTrabajador().getId()).isEqualTo(trabajador.getId());
    }

    @Test
    void shouldFindByTipoEvento() {
        var alta = createEvento(trabajador, periodoPlanilla, "ALTA", LocalDate.of(2026, Month.JANUARY, 5));
        repository.saveAndFlush(alta);

        var baja = createEvento(trabajador, periodoPlanilla, "BAJA", LocalDate.of(2026, Month.JANUARY, 20));
        repository.saveAndFlush(baja);

        var altas = repository.findByTipoEvento("ALTA");
        assertThat(altas).hasSize(1);
        assertThat(altas.get(0).getTipoEvento()).isEqualTo("ALTA");

        var bajas = repository.findByTipoEvento("BAJA");
        assertThat(bajas).hasSize(1);

        var suspensiones = repository.findByTipoEvento("SUSPENSION");
        assertThat(suspensiones).isEmpty();
    }

    @Test
    void shouldCheckExistsByPeriodoPlanillaId() {
        assertThat(repository.existsByPeriodoPlanillaId(periodoPlanilla.getId())).isFalse();

        var evento = createEvento(trabajador, periodoPlanilla, "ALTA", LocalDate.of(2026, Month.JANUARY, 15));
        repository.saveAndFlush(evento);

        assertThat(repository.existsByPeriodoPlanillaId(periodoPlanilla.getId())).isTrue();
    }
}

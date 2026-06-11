package com.clinica.rrhh.vacacion.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.vacacion.entity.VacacionGoce;
import com.clinica.rrhh.vacacion.entity.VacacionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VacacionGoceRepositoryTest {

    @Autowired
    private VacacionGoceRepository goceRepository;

    @Autowired
    private VacacionRecordRepository recordRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private VacacionRecord record;

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
        persona.setNumeroDocumento("55555555");
        persona.setNombres("VACACION");
        persona.setApellidoPaterno("GOCE");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        var trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-VGO-001");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setCantidadHijos(0);
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);

        record = new VacacionRecord();
        record.setTrabajador(trabajador);
        record.setFechaInicio(LocalDate.of(2025, Month.JANUARY, 1));
        record.setFechaFin(LocalDate.of(2025, Month.DECEMBER, 31));
        record.setDiasDerecho(15);
        record.setDiasReduccion(0);
        record.setDiasPendientes(BigDecimal.valueOf(15));
        record.setEstado("ACTIVO");
        record.setFechaExpiracion(LocalDate.of(2026, Month.DECEMBER, 31));
        record = recordRepository.saveAndFlush(record);
    }

    private VacacionGoce createGoce(VacacionRecord r, LocalDate inicio, int dias, String estado) {
        var g = new VacacionGoce();
        g.setRecord(r);
        g.setFechaInicio(inicio);
        g.setFechaFin(inicio.plusDays(dias - 1));
        g.setDias(dias);
        g.setRemuneracion(new BigDecimal("2613.00"));
        g.setEstado(estado);
        return g;
    }

    @Test
    void shouldSaveAndFindById() {
        var goce = createGoce(record, LocalDate.of(2026, Month.FEBRUARY, 1), 7, "PROGRAMADO");
        var saved = goceRepository.save(goce);
        assertThat(saved.getId()).isNotNull();

        var found = goceRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDias()).isEqualTo(7);
        assertThat(found.get().getEstado()).isEqualTo("PROGRAMADO");
    }

    @Test
    void shouldFindByRecordIdOrderByFechaInicioAsc() {
        var g1 = createGoce(record, LocalDate.of(2026, Month.FEBRUARY, 1), 8, "COMPLETADO");
        goceRepository.saveAndFlush(g1);

        var g2 = createGoce(record, LocalDate.of(2026, Month.MARCH, 1), 7, "PROGRAMADO");
        goceRepository.saveAndFlush(g2);

        List<VacacionGoce> result = goceRepository.findByRecordIdOrderByFechaInicioAsc(record.getId());
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDias()).isEqualTo(8);
        assertThat(result.get(1).getDias()).isEqualTo(7);
    }

    @Test
    void shouldFindByRecordIdAndEstado() {
        var g1 = createGoce(record, LocalDate.of(2026, Month.FEBRUARY, 1), 7, "PROGRAMADO");
        goceRepository.saveAndFlush(g1);

        var g2 = createGoce(record, LocalDate.of(2026, Month.MARCH, 1), 8, "COMPLETADO");
        goceRepository.saveAndFlush(g2);

        var programados = goceRepository.findByRecordIdAndEstado(record.getId(), "PROGRAMADO");
        assertThat(programados).hasSize(1);
        assertThat(programados.get(0).getEstado()).isEqualTo("PROGRAMADO");

        var completados = goceRepository.findByRecordIdAndEstado(record.getId(), "COMPLETADO");
        assertThat(completados).hasSize(1);
    }

    @Test
    void shouldFindByEstado() {
        var g1 = createGoce(record, LocalDate.of(2026, Month.FEBRUARY, 1), 7, "PROGRAMADO");
        goceRepository.saveAndFlush(g1);

        var g2 = createGoce(record, LocalDate.of(2026, Month.MARCH, 1), 8, "EN_CURSO");
        goceRepository.saveAndFlush(g2);

        var programados = goceRepository.findByEstado("PROGRAMADO");
        assertThat(programados).hasSize(1);

        var enCurso = goceRepository.findByEstado("EN_CURSO");
        assertThat(enCurso).hasSize(1);

        var completados = goceRepository.findByEstado("COMPLETADO");
        assertThat(completados).isEmpty();
    }

    @Test
    void shouldCountByRecordIdAndEstado() {
        var g1 = createGoce(record, LocalDate.of(2026, Month.FEBRUARY, 1), 7, "PROGRAMADO");
        goceRepository.saveAndFlush(g1);

        var g2 = createGoce(record, LocalDate.of(2026, Month.MARCH, 1), 8, "PROGRAMADO");
        goceRepository.saveAndFlush(g2);

        long count = goceRepository.countByRecordIdAndEstado(record.getId(), "PROGRAMADO");
        assertThat(count).isEqualTo(2);

        long completados = goceRepository.countByRecordIdAndEstado(record.getId(), "COMPLETADO");
        assertThat(completados).isZero();
    }
}

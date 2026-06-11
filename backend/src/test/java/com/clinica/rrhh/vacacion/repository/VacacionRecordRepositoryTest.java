package com.clinica.rrhh.vacacion.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
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
class VacacionRecordRepositoryTest {

    @Autowired
    private VacacionRecordRepository repository;

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
        persona.setNumeroDocumento("44444444");
        persona.setNombres("VACACION");
        persona.setApellidoPaterno("RECORD");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-VCR-001");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setCantidadHijos(0);
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);
    }

    private VacacionRecord createRecord(Trabajador t, LocalDate inicio, String estado) {
        var r = new VacacionRecord();
        r.setTrabajador(t);
        r.setFechaInicio(inicio);
        r.setFechaFin(inicio.plusYears(1).minusDays(1));
        r.setDiasDerecho(15);
        r.setDiasReduccion(0);
        r.setDiasPendientes(BigDecimal.valueOf(15));
        r.setEstado(estado);
        r.setFechaExpiracion(inicio.plusYears(2).minusDays(1));
        return r;
    }

    @Test
    void shouldSaveAndFindById() {
        var record = createRecord(trabajador, LocalDate.of(2025, Month.JANUARY, 1), "ACTIVO");
        var saved = repository.save(record);
        assertThat(saved.getId()).isNotNull();

        var found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDiasDerecho()).isEqualTo(15);
        assertThat(found.get().getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    void shouldFindByTrabajadorIdOrderByFechaInicioDesc() {
        var r1 = createRecord(trabajador, LocalDate.of(2025, Month.JANUARY, 1), "ACTIVO");
        repository.saveAndFlush(r1);

        var r2 = createRecord(trabajador, LocalDate.of(2026, Month.JANUARY, 1), "ACTIVO");
        repository.saveAndFlush(r2);

        List<VacacionRecord> result = repository.findByTrabajadorIdOrderByFechaInicioDesc(trabajador.getId());
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFechaInicio()).isAfter(result.get(1).getFechaInicio());
        assertThat(result.get(0).getTrabajador().getId()).isEqualTo(trabajador.getId());
    }

    @Test
    void shouldFindByTrabajadorIdAndFechaInicio() {
        var record = createRecord(trabajador, LocalDate.of(2025, Month.JANUARY, 1), "ACTIVO");
        repository.saveAndFlush(record);

        var found = repository.findByTrabajadorIdAndFechaInicio(
            trabajador.getId(), LocalDate.of(2025, Month.JANUARY, 1));
        assertThat(found).isPresent();
        assertThat(found.get().getEstado()).isEqualTo("ACTIVO");

        var notFound = repository.findByTrabajadorIdAndFechaInicio(
            trabajador.getId(), LocalDate.of(2024, Month.JANUARY, 1));
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldCheckExistsByTrabajadorIdAndFechaInicio() {
        assertThat(repository.existsByTrabajadorIdAndFechaInicio(
            trabajador.getId(), LocalDate.of(2025, Month.JANUARY, 1))).isFalse();

        var record = createRecord(trabajador, LocalDate.of(2025, Month.JANUARY, 1), "ACTIVO");
        repository.saveAndFlush(record);

        assertThat(repository.existsByTrabajadorIdAndFechaInicio(
            trabajador.getId(), LocalDate.of(2025, Month.JANUARY, 1))).isTrue();
    }

    @Test
    void shouldFindByEstado() {
        var activo = createRecord(trabajador, LocalDate.of(2025, Month.JANUARY, 1), "ACTIVO");
        repository.saveAndFlush(activo);

        var completado = createRecord(trabajador, LocalDate.of(2024, Month.JANUARY, 1), "COMPLETADO");
        repository.saveAndFlush(completado);

        var activos = repository.findByEstado("ACTIVO");
        assertThat(activos).hasSize(1);
        assertThat(activos.get(0).getEstado()).isEqualTo("ACTIVO");

        var completados = repository.findByEstado("COMPLETADO");
        assertThat(completados).hasSize(1);

        var perdidos = repository.findByEstado("PERDIDO");
        assertThat(perdidos).isEmpty();
    }
}

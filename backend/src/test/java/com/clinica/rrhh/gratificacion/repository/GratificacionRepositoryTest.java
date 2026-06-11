package com.clinica.rrhh.gratificacion.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.gratificacion.entity.Gratificacion;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.EstadoContrato;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GratificacionRepositoryTest {

    @Autowired
    private GratificacionRepository gratificacionRepository;

    @Autowired
    private PeriodoPlanillaRepository periodoPlanillaRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    private PeriodoPlanilla periodo;
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
        persona.setNombres("GRATIF");
        persona.setApellidoPaterno("TEST");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-GRA-001");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setCantidadHijos(0);
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);

        periodo = new PeriodoPlanilla();
        periodo.setAnio(2026);
        periodo.setMes(6);
        periodo.setFechaInicio(LocalDate.of(2026, 6, 1));
        periodo.setFechaFin(LocalDate.of(2026, 6, 30));
        periodo.setEstado("ABIERTO");
        periodo = periodoPlanillaRepository.saveAndFlush(periodo);
    }

    private Gratificacion createGratificacion(PeriodoPlanilla p, Trabajador t, String semestre) {
        var g = new Gratificacion();
        g.setPeriodoPlanilla(p);
        g.setTrabajador(t);
        g.setSemestre(semestre);
        g.setMesesComputables(6);
        g.setRemuneracionComputable(new BigDecimal("2000.00"));
        g.setGratificacion(new BigDecimal("1000.00"));
        g.setBonificacionExtraordinaria(new BigDecimal("90.00"));
        g.setTotal(new BigDecimal("1090.00"));
        g.setEstado("CALCULADO");
        return g;
    }

    @Test
    void shouldSaveAndFindById() {
        var gratificacion = createGratificacion(periodo, trabajador, "ENERO-JUNIO");
        var saved = gratificacionRepository.save(gratificacion);
        assertThat(saved.getId()).isNotNull();

        var found = gratificacionRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSemestre()).isEqualTo("ENERO-JUNIO");
        assertThat(found.get().getGratificacion()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void shouldFindByPeriodoPlanillaId() {
        var gratificacion = createGratificacion(periodo, trabajador, "ENERO-JUNIO");
        gratificacionRepository.saveAndFlush(gratificacion);

        List<Gratificacion> result = gratificacionRepository.findByPeriodoPlanillaId(periodo.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrabajador().getId()).isEqualTo(trabajador.getId());
    }

    @Test
    void shouldFindByPeriodoPlanillaIdAndTrabajadorId() {
        var gratificacion = createGratificacion(periodo, trabajador, "ENERO-JUNIO");
        gratificacionRepository.saveAndFlush(gratificacion);

        var found = gratificacionRepository.findByPeriodoPlanillaIdAndTrabajadorId(
            periodo.getId(), trabajador.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSemestre()).isEqualTo("ENERO-JUNIO");

        var notFound = gratificacionRepository.findByPeriodoPlanillaIdAndTrabajadorId(
            periodo.getId(), 999L);
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldCheckExistsByPeriodoPlanillaId() {
        assertThat(gratificacionRepository.existsByPeriodoPlanillaId(periodo.getId())).isFalse();

        var gratificacion = createGratificacion(periodo, trabajador, "ENERO-JUNIO");
        gratificacionRepository.saveAndFlush(gratificacion);

        assertThat(gratificacionRepository.existsByPeriodoPlanillaId(periodo.getId())).isTrue();
    }

    @Test
    void shouldFindByTrabajadorIdOrderByCreatedAtDesc() {
        var g1 = createGratificacion(periodo, trabajador, "ENERO-JUNIO");
        gratificacionRepository.saveAndFlush(g1);

        // Create another period for same trabajador
        var periodo2 = new PeriodoPlanilla();
        periodo2.setAnio(2026);
        periodo2.setMes(12);
        periodo2.setFechaInicio(LocalDate.of(2026, 12, 1));
        periodo2.setFechaFin(LocalDate.of(2026, 12, 31));
        periodo2.setEstado("ABIERTO");
        periodo2 = periodoPlanillaRepository.saveAndFlush(periodo2);

        var g2 = createGratificacion(periodo2, trabajador, "JULIO-DICIEMBRE");
        gratificacionRepository.saveAndFlush(g2);

        var result = gratificacionRepository.findByTrabajadorIdOrderByCreatedAtDesc(trabajador.getId());
        assertThat(result).hasSize(2);
        // Both records should be present
        var semestres = result.stream().map(g -> g.getSemestre()).toList();
        assertThat(semestres).containsExactlyInAnyOrder("ENERO-JUNIO", "JULIO-DICIEMBRE");
    }
}

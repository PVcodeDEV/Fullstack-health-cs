package com.clinica.rrhh.gratificacion.integration;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.TipoContrato;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.maestro.repository.rrhh.TipoContratoRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.gratificacion.service.GratificacionService;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.EstadoContrato;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.jwt.secret=test-secret-key-for-integration-test-min-32-chars-long!!",
    "app.jwt.expiration-ms=3600000"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class GratificacionFlowIntegrationTest {

    @Autowired
    private GratificacionService gratificacionService;

    @Autowired
    private PeriodoPlanillaRepository periodoPlanillaRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private TipoContratoRepository tipoContratoRepository;

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
        persona.setNumeroDocumento("99999999");
        persona.setNombres("GRATIF");
        persona.setApellidoPaterno("FLUJO");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-GRA-FLOW");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setCantidadHijos(0);
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);

        var tipoContrato = new TipoContrato();
        tipoContrato.setCodigo("INDETERMINADO");
        tipoContrato.setNombre("Indeterminado");
        tipoContrato = tipoContratoRepository.saveAndFlush(tipoContrato);

        var contrato = new Contrato();
        contrato.setTrabajador(trabajador);
        contrato.setTipoContrato(tipoContrato);
        contrato.setRemuneracion(new BigDecimal("2000"));
        contrato.setFechaInicio(LocalDate.of(2025, 1, 1));
        contrato.setEstado(EstadoContrato.ACTIVO);
        contratoRepository.saveAndFlush(contrato);

        periodo = new PeriodoPlanilla();
        periodo.setAnio(2026);
        periodo.setMes(6);
        periodo.setFechaInicio(LocalDate.of(2026, 6, 1));
        periodo.setFechaFin(LocalDate.of(2026, 6, 30));
        periodo.setEstado("ABIERTO");
        periodo = periodoPlanillaRepository.saveAndFlush(periodo);
    }

    @Test
    void fullGratificacionFlow() {
        // 1. Calculate gratificación for June 2026
        var resultados = gratificacionService.calcular(periodo.getId());

        assertThat(resultados).isNotNull();
        assertThat(resultados).hasSize(1);

        var r = resultados.get(0);
        assertThat(r.periodoPlanillaId()).isEqualTo(periodo.getId());
        assertThat(r.trabajadorId()).isEqualTo(trabajador.getId());
        assertThat(r.semestre()).isEqualTo("ENERO-JUNIO");
        assertThat(r.mesesComputables()).isEqualTo(6);
        assertThat(r.remuneracionComputable()).isEqualByComparingTo(new BigDecimal("2000.00"));
        // 6 months → ½ sueldo = 1000
        assertThat(r.gratificacion()).isEqualByComparingTo(new BigDecimal("1000.00"));
        // 9% bonus = 90
        assertThat(r.bonificacionExtraordinaria()).isEqualByComparingTo(new BigDecimal("90.00"));
        // Total = 1090
        assertThat(r.total()).isEqualByComparingTo(new BigDecimal("1090.00"));
        assertThat(r.estado()).isEqualTo("CALCULADO");

        // 2. Re-run (idempotent) — should update, not duplicate
        var resultados2 = gratificacionService.calcular(periodo.getId());
        assertThat(resultados2).hasSize(1);
        assertThat(resultados2.get(0).id()).isEqualTo(r.id()); // same ID = upsert

        // 3. Verify findAll returns the record
        var all = gratificacionService.findAll();
        assertThat(all).hasSize(1);

        // 4. Verify findById returns expected
        var byId = gratificacionService.findById(r.id());
        assertThat(byId.semestre()).isEqualTo("ENERO-JUNIO");
    }
}

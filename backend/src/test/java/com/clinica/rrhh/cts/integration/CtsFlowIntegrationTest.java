package com.clinica.rrhh.cts.integration;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.TipoContrato;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.maestro.repository.rrhh.TipoContratoRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.cts.service.CtsService;
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
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.jwt.secret=test-secret-key-for-integration-test-min-32-chars-long!!",
    "app.jwt.expiration-ms=3600000"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class CtsFlowIntegrationTest {

    @Autowired
    private CtsService ctsService;

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
        persona.setNumeroDocumento("88888888");
        persona.setNombres("CTS");
        persona.setApellidoPaterno("FLUJO");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-CTS-FLOW");
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
        periodo.setMes(5);
        periodo.setFechaInicio(LocalDate.of(2026, 5, 1));
        periodo.setFechaFin(LocalDate.of(2026, 5, 31));
        periodo.setEstado("ABIERTO");
        periodo = periodoPlanillaRepository.saveAndFlush(periodo);
    }

    @Test
    void fullCtsFlow() {
        // 1. Calculate CTS for May 2026
        var resultados = ctsService.calcular(periodo.getId());

        assertThat(resultados).isNotNull();
        assertThat(resultados).hasSize(1);

        var r = resultados.get(0);
        assertThat(r.periodoPlanillaId()).isEqualTo(periodo.getId());
        assertThat(r.trabajadorId()).isEqualTo(trabajador.getId());
        assertThat(r.semestre()).isEqualTo("MAYO-OCTUBRE");
        assertThat(r.diasComputables()).isEqualTo(180);
        assertThat(r.remuneracionComputable()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(r.promedioGratificacion()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(r.promedioBonificacion()).isEqualByComparingTo(BigDecimal.ZERO);

        // monto = (2000/360) × 180 = 1000.00
        BigDecimal montoEsperado = new BigDecimal("2000.00")
            .divide(BigDecimal.valueOf(360), 10, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(180))
            .setScale(2, RoundingMode.HALF_UP);
        assertThat(r.montoCts()).isEqualByComparingTo(montoEsperado);
        assertThat(r.estado()).isEqualTo("CALCULADO");

        // 2. Re-run (idempotent) — should update, not duplicate
        var resultados2 = ctsService.calcular(periodo.getId());
        assertThat(resultados2).hasSize(1);
        assertThat(resultados2.get(0).id()).isEqualTo(r.id()); // same ID = upsert

        // 3. Verify findAll returns the record
        var all = ctsService.findAll();
        assertThat(all).hasSize(1);

        // 4. Verify findById returns expected
        var byId = ctsService.findById(r.id());
        assertThat(byId.semestre()).isEqualTo("MAYO-OCTUBRE");
        assertThat(byId.diasComputables()).isEqualTo(180);
    }
}

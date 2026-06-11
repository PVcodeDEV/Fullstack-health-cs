package com.clinica.rrhh.planilla.integration;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.maestro.entity.rrhh.AfpTasaHistorica;
import com.clinica.maestro.entity.rrhh.TipoContrato;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.maestro.repository.rrhh.AfpRepository;
import com.clinica.maestro.repository.rrhh.TipoContratoRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.planilla.dto.PeriodoPlanillaRequest;
import com.clinica.rrhh.planilla.dto.PeriodoPlanillaResponse;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planilla.repository.PlanillaDetalleRepository;
import com.clinica.rrhh.planilla.repository.PlanillaRepository;
import com.clinica.rrhh.planilla.service.PeriodoPlanillaService;
import com.clinica.rrhh.planilla.service.PlanillaLiquidacionService;
import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import com.clinica.rrhh.pension.repository.InformacionPensionariaRepository;
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
class PlanillaFlowIntegrationTest {

    @Autowired
    private PeriodoPlanillaService periodoPlanillaService;

    @Autowired
    private PlanillaLiquidacionService planillaLiquidacionService;

    @Autowired
    private PeriodoPlanillaRepository periodoPlanillaRepository;

    @Autowired
    private PlanillaRepository planillaRepository;

    @Autowired
    private PlanillaDetalleRepository planillaDetalleRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private AfpRepository afpRepository;

    @Autowired
    private TipoContratoRepository tipoContratoRepository;

    @Autowired
    private InformacionPensionariaRepository pensionRepository;

    private PeriodoPlanillaResponse periodo;
    private Trabajador trabajador;
    private Afp afpOnp;
    private Contrato contrato;

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
        persona.setNombres("PLANILLA");
        persona.setApellidoPaterno("FLUJO");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-PLA-001");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setCantidadHijos(0);
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);

        var tipoContrato = new TipoContrato();
        tipoContrato.setCodigo("INDETERMINADO");
        tipoContrato.setNombre("Indeterminado");
        tipoContrato = tipoContratoRepository.saveAndFlush(tipoContrato);

        contrato = new Contrato();
        contrato.setTrabajador(trabajador);
        contrato.setTipoContrato(tipoContrato);
        contrato.setRemuneracion(new BigDecimal("2500"));
        contrato.setFechaInicio(LocalDate.of(2025, 1, 1));
        contrato.setEstado(EstadoContrato.ACTIVO);
        contrato = contratoRepository.saveAndFlush(contrato);

        afpOnp = new Afp();
        afpOnp.setCodigo("ONP");
        afpOnp.setNombre("ONP");
        afpOnp = afpRepository.saveAndFlush(afpOnp);

        // Create pension info with ONP
        var info = new InformacionPensionaria();
        info.setTrabajador(trabajador);
        info.setAfp(afpOnp);
        info.setCuspp(persona.getNumeroDocumento());
        info.setFechaAfiliacion(LocalDate.of(2025, 1, 1));
        info.setEstado("ACTIVO");
        pensionRepository.saveAndFlush(info);

        // Create period
        var request = new PeriodoPlanillaRequest(
            2026, 1,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31));
        periodo = periodoPlanillaService.create(request);
    }

    @Test
    void fullGenerateFlow() {
        // 1. Generate planilla
        var planillaResponse = planillaLiquidacionService.generar(periodo.id());

        assertThat(planillaResponse).isNotNull();
        assertThat(planillaResponse.periodoPlanillaId()).isEqualTo(periodo.id());
        assertThat(planillaResponse.cantidadTrabajadores()).isEqualTo(1);
        assertThat(planillaResponse.totalNeto())
            .isEqualByComparingTo(new BigDecimal("2175.00")); // 2500 - 325 (ONP 13%)

        // 2. Verify header persisted
        var planillaEntity = planillaRepository.findById(planillaResponse.id());
        assertThat(planillaEntity).isPresent();
        assertThat(planillaEntity.get().getEstado()).isEqualTo("LIQUIDADO");
        assertThat(planillaEntity.get().getTotalAportes())
            .isEqualByComparingTo(new BigDecimal("225.00")); // EsSalud 9%

        // 3. Verify detalle rows
        var detalles = planillaDetalleRepository.findByPlanillaId(planillaResponse.id());
        assertThat(detalles).hasSize(1);
        var detalle = detalles.getFirst();
        assertThat(detalle.getTrabajador().getId()).isEqualTo(trabajador.getId());
        assertThat(detalle.getSueldoBase()).isEqualByComparingTo(new BigDecimal("2500"));
        assertThat(detalle.getNeto()).isEqualByComparingTo(new BigDecimal("2175.00"));
        assertThat(detalle.getConceptosJson()).contains("BASICO");
        assertThat(detalle.getConceptosJson()).contains("ONP_DESCUENTO");
        assertThat(detalle.getConceptosJson()).contains("ESSALUD_APORTE");

        // 4. Verify duplicate generation is rejected
        try {
            planillaLiquidacionService.generar(periodo.id());
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Ya existe una planilla");
        }

        // 5. Close period
        var closedPeriodo = periodoPlanillaService.cerrar(periodo.id());
        assertThat(closedPeriodo.estado()).isEqualTo("CERRADO");

        // 6. Verify period is CERRADO
        var periodoEntity = periodoPlanillaRepository.findById(periodo.id());
        assertThat(periodoEntity).isPresent();
        assertThat(periodoEntity.get().getEstado()).isEqualTo("CERRADO");
    }
}

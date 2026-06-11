package com.clinica.rrhh.integration;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.TipoContrato;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.maestro.repository.rrhh.TipoContratoRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.contrato.dto.ContratoRequest;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.contrato.service.ContratoService;
import com.clinica.rrhh.derechohabiente.dto.DerechohabienteRequest;
import com.clinica.rrhh.derechohabiente.repository.DerechohabienteRepository;
import com.clinica.rrhh.derechohabiente.service.DerechohabienteService;
import com.clinica.rrhh.periodo.repository.PeriodoLaboralRepository;
import com.clinica.rrhh.periodo.service.PeriodoLaboralService;
import com.clinica.rrhh.trabajador.dto.TrabajadorRequest;
import com.clinica.rrhh.trabajador.service.TrabajadorService;
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
class RrhhFlowIntegrationTest {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private TipoContratoRepository tipoContratoRepository;

    @Autowired
    private TrabajadorService trabajadorService;

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private DerechohabienteService derechohabienteService;

    @Autowired
    private DerechohabienteRepository derechohabienteRepository;

    @Autowired
    private PeriodoLaboralService periodoLaboralService;

    @Autowired
    private PeriodoLaboralRepository periodoLaboralRepository;

    private TipoDocumentoIdentidad tdi;
    private Persona personaTrabajador;
    private Persona personaDerechohabiente;
    private TipoContrato tipoContrato;

    @BeforeEach
    void setUp() {
        tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocumentoIdentidadRepository.saveAndFlush(tdi);

        personaTrabajador = new Persona();
        personaTrabajador.setTipoDocumentoIdentidad(tdi);
        personaTrabajador.setNumeroDocumento("55555555");
        personaTrabajador.setNombres("TRABAJADOR");
        personaTrabajador.setApellidoPaterno("INTEGRACION");
        personaTrabajador.setActivo(true);
        personaTrabajador = personaRepository.saveAndFlush(personaTrabajador);

        personaDerechohabiente = new Persona();
        personaDerechohabiente.setTipoDocumentoIdentidad(tdi);
        personaDerechohabiente.setNumeroDocumento("66666666");
        personaDerechohabiente.setNombres("HIJO");
        personaDerechohabiente.setApellidoPaterno("INTEGRACION");
        personaDerechohabiente.setActivo(true);
        personaDerechohabiente = personaRepository.saveAndFlush(personaDerechohabiente);

        tipoContrato = new TipoContrato();
        tipoContrato.setCodigo("INDETERMINADO");
        tipoContrato.setNombre("Indeterminado");
        tipoContrato = tipoContratoRepository.saveAndFlush(tipoContrato);
    }

    @Test
    void fullRrhhFlow() {
        // 1. Create Trabajador via TrabajadorService
        var trabajadorRequest = new TrabajadorRequest(
                personaTrabajador.getId(),
                "TR-INT-001",
                LocalDate.of(2025, 1, 1),
                null,           // tipo — no health professional (no colegiatura needed)
                null,           // regimenLaboral
                "Analista",     // cargo
                null,           // areaFuncionalId
                null,           // banco
                null,           // cuentaSueldo
                null,           // cci
                null,           // contactoNombre
                null,           // contactoTelefono
                0,              // cantidadHijos
                null,           // nroColegiatura
                null,           // tipoColegiaturaId
                false,          // discapacidad
                false           // sindicalizado
        );
        var trabajadorResponse = trabajadorService.create(trabajadorRequest);
        assertThat(trabajadorResponse).isNotNull();
        assertThat(trabajadorResponse.id()).isNotNull();
        assertThat(trabajadorResponse.codigoTrabajador()).isEqualTo("TR-INT-001");
        Long trabajadorId = trabajadorResponse.id();

        // Verify initial PeriodoLaboral was auto-created
        var periodos = periodoLaboralRepository.findByTrabajadorIdOrderByFechaInicioDesc(trabajadorId);
        assertThat(periodos).hasSize(1);
        assertThat(periodos.get(0).getActivo()).isTrue();

        // 2. Create Contrato via ContratoService
        var contratoRequest = new ContratoRequest(
                trabajadorId,
                tipoContrato.getId(),
                LocalDate.of(2025, 1, 1),
                null,           // fechaFin
                null,           // periodoPruebaMeses
                new BigDecimal("3000.00"),
                null            // jornada → default REGULAR
        );
        var contratoResponse = contratoService.create(contratoRequest);
        assertThat(contratoResponse).isNotNull();
        assertThat(contratoResponse.estado()).isEqualTo("ACTIVO");
        Long contratoId = contratoResponse.id();

        // Verify single ACTIVE contrato
        var activeContrato = contratoRepository.findByTrabajadorIdAndEstado(trabajadorId, EstadoContrato.ACTIVO);
        assertThat(activeContrato).isPresent();
        assertThat(activeContrato.get().getId()).isEqualTo(contratoId);

        // 3. Create Derechohabiente (HIJO) via DerechohabienteService
        var dhRequest = new DerechohabienteRequest(
                trabajadorId,
                personaDerechohabiente.getId(),
                "HIJO",
                LocalDate.of(2025, 6, 1),
                null    // fechaFin null → auto-calculated for HIJO
        );
        var dhResponse = derechohabienteService.create(dhRequest);
        assertThat(dhResponse).isNotNull();
        assertThat(dhResponse.relacion()).isEqualTo("HIJO");
        assertThat(dhResponse.estado()).isEqualTo("ACTIVO");

        // Verify HIJO auto-fechaFin = fechaInicio + 18 years
        assertThat(dhResponse.fechaFin()).isEqualTo(LocalDate.of(2043, 6, 1));

        // 4. Resolve contrato — should cascade to inactivate derechohabientes
        var resolved = contratoService.resolver(contratoId, null);
        assertThat(resolved.estado()).isEqualTo("RESUELTO");

        // Verify derechohabiente → INACTIVO (cascade)
        var dhActivos = derechohabienteRepository
                .findByTrabajadorIdAndEstadoOrderByFechaInicioDesc(trabajadorId, "ACTIVO");
        assertThat(dhActivos).isEmpty();

        var dhTodos = derechohabienteRepository
                .findByTrabajadorIdOrderByFechaInicioDesc(trabajadorId);
        assertThat(dhTodos).hasSize(1);
        assertThat(dhTodos.get(0).getEstado()).isEqualTo("INACTIVO");

        // 5. Create PeriodoLaboral reingreso
        var reingreso = periodoLaboralService.registrarIngreso(trabajadorId, LocalDate.of(2025, 7, 1), true);
        assertThat(reingreso).isNotNull();
        assertThat(reingreso.esReingreso()).isTrue();
        assertThat(reingreso.activo()).isTrue();

        // Verify periodos count = 2 (initial + reingreso)
        var periodosAfter = periodoLaboralRepository.findByTrabajadorIdOrderByFechaInicioDesc(trabajadorId);
        assertThat(periodosAfter).hasSize(2);

        // 6. Create new contrato (after resolution, a new ACTIVE one can be created)
        var nuevoContratoRequest = new ContratoRequest(
                trabajadorId,
                tipoContrato.getId(),
                LocalDate.of(2025, 7, 1),
                null,
                null,
                new BigDecimal("3500.00"),
                null
        );
        var nuevoContrato = contratoService.create(nuevoContratoRequest);
        assertThat(nuevoContrato).isNotNull();
        assertThat(nuevoContrato.estado()).isEqualTo("ACTIVO");
        assertThat(nuevoContrato.remuneracion()).isEqualByComparingTo(new BigDecimal("3500.00"));

        // Verify only the new contrato is ACTIVE
        var activeAfter = contratoRepository.findByTrabajadorIdAndEstado(trabajadorId, EstadoContrato.ACTIVO);
        assertThat(activeAfter).isPresent();
        assertThat(activeAfter.get().getId()).isEqualTo(nuevoContrato.id());
    }
}

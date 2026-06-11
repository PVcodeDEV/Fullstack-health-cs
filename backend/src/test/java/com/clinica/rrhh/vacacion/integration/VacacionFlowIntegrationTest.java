package com.clinica.rrhh.vacacion.integration;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.TipoContrato;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.maestro.repository.rrhh.TipoContratoRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.EstadoContrato;
import com.clinica.rrhh.vacacion.dto.ProgramarRequest;
import com.clinica.rrhh.vacacion.dto.VacacionGoceResponse;
import com.clinica.rrhh.vacacion.dto.VacacionRecordResponse;
import com.clinica.rrhh.vacacion.service.VacacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.jwt.secret=test-secret-key-for-integration-test-min-32-chars-long!!",
    "app.jwt.expiration-ms=3600000"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class VacacionFlowIntegrationTest {

    @Autowired
    private VacacionService vacacionService;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private TipoContratoRepository tipoContratoRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    private Long trabajadorId;
    private Long contratoId;

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
        persona.setNombres("VACACION");
        persona.setApellidoPaterno("FLUJO");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        var trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-VAC-FLOW");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setCantidadHijos(0);
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);
        trabajadorId = trabajador.getId();

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
        contrato = contratoRepository.saveAndFlush(contrato);
        contratoId = contrato.getId();
    }

    @Test
    void fullVacacionFlow() {
        // 1. POST /api/v1/vacaciones/calcular → 201, verify record created with 15 días pendientes
        List<VacacionRecordResponse> records = vacacionService.calcular(0);

        assertThat(records).isNotEmpty();
        var record = records.get(0);
        assertThat(record.trabajadorId()).isEqualTo(trabajadorId);
        assertThat(record.diasDerecho()).isEqualTo(15);
        assertThat(record.diasPendientes()).isEqualByComparingTo(BigDecimal.valueOf(15));
        assertThat(record.estado()).isEqualTo("ACTIVO");
        assertThat(record.fechaFin()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(record.fechaExpiracion()).isEqualTo(LocalDate.of(2027, 1, 1));
        Long recordId = record.id();

        // 2. POST /api/v1/vacaciones/programar → 201, verify goce in PROGRAMADO
        var programarRequest = new ProgramarRequest(trabajadorId, LocalDate.of(2026, 6, 15), 15);
        VacacionGoceResponse goce = vacacionService.programar(programarRequest);

        assertThat(goce).isNotNull();
        assertThat(goce.recordId()).isEqualTo(recordId);
        assertThat(goce.estado()).isEqualTo("PROGRAMADO");
        assertThat(goce.dias()).isEqualTo(15);
        assertThat(goce.remuneracion()).isEqualByComparingTo(new BigDecimal("2000.00"));
        Long goceId = goce.id();

        // 3. POST /api/v1/vacaciones/goces/{id}/iniciar → 200, verify EN_CURSO
        VacacionGoceResponse enCurso = vacacionService.iniciar(goceId);
        assertThat(enCurso.estado()).isEqualTo("EN_CURSO");

        // 4. POST /api/v1/vacaciones/goces/{id}/completar → 200, verify COMPLETADO
        VacacionGoceResponse completado = vacacionService.completar(goceId);
        assertThat(completado.estado()).isEqualTo("COMPLETADO");

        // Verify record pending days = 0 after full 15-day goce
        VacacionRecordResponse updatedRecord = vacacionService.findRecordById(recordId);
        assertThat(updatedRecord.diasPendientes()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(updatedRecord.estado()).isEqualTo("COMPLETADO");

        // 5. GET /api/v1/vacaciones/records/{recordId}/goces → 200, verify list
        List<VacacionGoceResponse> goces = vacacionService.findGocesByRecord(recordId);
        assertThat(goces).hasSize(1);
        assertThat(goces.get(0).id()).isEqualTo(goceId);
        assertThat(goces.get(0).estado()).isEqualTo("COMPLETADO");

        // 6. Re-run calcular (idempotent) — returns same record, no duplicate
        List<VacacionRecordResponse> reRun = vacacionService.calcular(0);
        assertThat(reRun).hasSize(1);
        assertThat(reRun.get(0).id()).isEqualTo(recordId);

        // 7. Verify findRecordsByTrabajador returns the record
        List<VacacionRecordResponse> byTrabajador = vacacionService.findRecordsByTrabajador(trabajadorId);
        assertThat(byTrabajador).hasSize(1);
        assertThat(byTrabajador.get(0).id()).isEqualTo(recordId);

        // 8. Verify findRecordById returns the record
        VacacionRecordResponse byId = vacacionService.findRecordById(recordId);
        assertThat(byId.trabajadorId()).isEqualTo(trabajadorId);
    }

}

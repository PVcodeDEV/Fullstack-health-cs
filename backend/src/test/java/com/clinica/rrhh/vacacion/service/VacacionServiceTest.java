package com.clinica.rrhh.vacacion.service;

import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.EstadoContrato;
import com.clinica.rrhh.vacacion.dto.ProgramarRequest;
import com.clinica.rrhh.vacacion.dto.VacacionGoceResponse;
import com.clinica.rrhh.vacacion.dto.VacacionRecordResponse;
import com.clinica.rrhh.vacacion.entity.VacacionGoce;
import com.clinica.rrhh.vacacion.entity.VacacionRecord;
import com.clinica.rrhh.vacacion.repository.VacacionGoceRepository;
import com.clinica.rrhh.vacacion.repository.VacacionRecordRepository;
import com.clinica.rrhh.planilla.config.PlanillaProperties;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacacionServiceTest {

    @Mock
    private VacacionRecordRepository recordRepository;

    @Mock
    private VacacionGoceRepository goceRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private TrabajadorRepository trabajadorRepository;

    @Mock
    private PlanillaProperties properties;

    @Mock
    private Clock clock;

    @InjectMocks
    private VacacionService service;

    @Captor
    private ArgumentCaptor<VacacionRecord> recordCaptor;

    @Captor
    private ArgumentCaptor<VacacionGoce> goceCaptor;

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final LocalDate FIXED_DATE = LocalDate.of(2027, 1, 15);

    private Persona persona;
    private Trabajador trabajador;
    private Trabajador trabajadorConHijos;
    private Contrato contrato;

    @BeforeEach
    void setUp() {
        persona = new Persona();
        persona.setId(1L);
        persona.setNombres("JUAN");
        persona.setApellidoPaterno("PEREZ");

        trabajador = new Trabajador();
        trabajador.setId(1L);
        trabajador.setPersona(persona);
        trabajador.setCantidadHijos(0);

        var personaConHijos = new Persona();
        personaConHijos.setId(2L);
        personaConHijos.setNombres("PEDRO");
        personaConHijos.setApellidoPaterno("SUAREZ");

        trabajadorConHijos = new Trabajador();
        trabajadorConHijos.setId(2L);
        trabajadorConHijos.setPersona(personaConHijos);
        trabajadorConHijos.setCantidadHijos(2);

        contrato = new Contrato();
        contrato.setId(1L);
        contrato.setTrabajador(trabajador);
        contrato.setRemuneracion(new BigDecimal("2000.00"));
        contrato.setFechaInicio(LocalDate.of(2026, 1, 1));
        contrato.setEstado(EstadoContrato.ACTIVO);
    }

    // Helper to fix the clock for calcular tests
    private void givenFixedClock() {
        when(clock.instant()).thenReturn(Instant.from(FIXED_DATE.atStartOfDay(ZONE)));
        when(clock.getZone()).thenReturn(ZONE);
    }

    // --- calcular: creates record for worker with 12+ months ---

    @Test
    void calcular_CreatesRecordForWorkerWith12Months() {
        givenFixedClock();
        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(recordRepository.findByEstado("ACTIVO")).thenReturn(List.of());
        when(recordRepository.existsByTrabajadorIdAndFechaInicio(1L, LocalDate.of(2026, 1, 1)))
            .thenReturn(false);
        when(recordRepository.save(any())).thenAnswer(i -> {
            var r = (VacacionRecord) i.getArgument(0);
            if (r.getId() == null) r.setId(1L);
            return r;
        });

        List<VacacionRecordResponse> results = service.calcular(0);

        assertThat(results).hasSize(1);
        var r = results.get(0);
        assertThat(r.trabajadorId()).isEqualTo(1L);
        assertThat(r.trabajadorNombre()).isEqualTo("JUAN PEREZ");
        assertThat(r.fechaInicio()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(r.fechaFin()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(r.diasDerecho()).isEqualTo(15);
        assertThat(r.diasPendientes()).isEqualByComparingTo(BigDecimal.valueOf(15));
        assertThat(r.estado()).isEqualTo("ACTIVO");
        assertThat(r.fechaExpiracion()).isEqualTo(LocalDate.of(2028, 1, 1));

        verify(recordRepository).save(recordCaptor.capture());
        var saved = recordCaptor.getValue();
        assertThat(saved.getTrabajador().getId()).isEqualTo(1L);
        assertThat(saved.getContrato().getId()).isEqualTo(1L);
    }

    // --- calcular: skips if record exists (idempotent) ---

    @Test
    void calcular_SkipsExistingRecord_Idempotent() {
        givenFixedClock();
        var existingRecord = new VacacionRecord();
        existingRecord.setId(1L);
        existingRecord.setTrabajador(trabajador);
        existingRecord.setContrato(contrato);
        existingRecord.setFechaInicio(LocalDate.of(2026, 1, 1));
        existingRecord.setFechaFin(LocalDate.of(2027, 1, 1));
        existingRecord.setDiasDerecho(15);
        existingRecord.setDiasPendientes(BigDecimal.valueOf(15));
        existingRecord.setEstado("ACTIVO");
        existingRecord.setFechaExpiracion(LocalDate.of(2028, 1, 1));

        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(recordRepository.findByEstado("ACTIVO")).thenReturn(List.of());
        when(recordRepository.existsByTrabajadorIdAndFechaInicio(1L, LocalDate.of(2026, 1, 1)))
            .thenReturn(true);
        when(recordRepository.findByTrabajadorIdAndFechaInicio(1L, LocalDate.of(2026, 1, 1)))
            .thenReturn(Optional.of(existingRecord));

        List<VacacionRecordResponse> results = service.calcular(0);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(1L);
        verify(recordRepository, never()).save(any());
    }

    // --- calcular: marks expired records as PERDIDO ---

    @Test
    void calcular_MarksExpiredRecordsAsPerdido() {
        givenFixedClock();
        var expiredRecord = new VacacionRecord();
        expiredRecord.setId(1L);
        expiredRecord.setTrabajador(trabajador);
        expiredRecord.setContrato(contrato);
        expiredRecord.setFechaInicio(LocalDate.of(2024, 1, 1));
        expiredRecord.setFechaFin(LocalDate.of(2025, 1, 1));
        expiredRecord.setDiasDerecho(15);
        expiredRecord.setDiasPendientes(BigDecimal.valueOf(15));
        expiredRecord.setEstado("ACTIVO");
        // Expired: fechaExpiracion = 2026-01-01 < fixed date 2027-01-15
        expiredRecord.setFechaExpiracion(LocalDate.of(2026, 1, 1));

        when(contratoRepository.findAll()).thenReturn(List.of());
        when(recordRepository.findByEstado("ACTIVO")).thenReturn(List.of(expiredRecord));
        when(recordRepository.save(expiredRecord)).thenReturn(expiredRecord);

        List<VacacionRecordResponse> results = service.calcular(0);

        assertThat(results).isEmpty();
        verify(recordRepository).save(expiredRecord);
        assertThat(expiredRecord.getEstado()).isEqualTo("PERDIDO");
        assertThat(expiredRecord.getDiasPendientes()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- calcular: worker with <12 months is skipped ---

    @Test
    void calcular_SkipsWorkerWithLessThan12Months() {
        givenFixedClock();
        contrato.setFechaInicio(LocalDate.of(2026, 7, 1));

        when(contratoRepository.findAll()).thenReturn(List.of(contrato));
        when(recordRepository.findByEstado("ACTIVO")).thenReturn(List.of());

        List<VacacionRecordResponse> results = service.calcular(0);

        assertThat(results).isEmpty();
        verify(recordRepository, never()).save(any());
    }

    // --- programar: creates goce in PROGRAMADO ---

    @Test
    void programar_CreatesGoceInProgramado() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(recordRepository.findByTrabajadorIdOrderByFechaInicioDesc(1L))
            .thenReturn(List.of(record));
        when(goceRepository.save(any())).thenAnswer(i -> {
            var g = (VacacionGoce) i.getArgument(0);
            if (g.getId() == null) g.setId(1L);
            return g;
        });

        var request = new ProgramarRequest(1L, LocalDate.of(2027, 2, 1), 15);
        VacacionGoceResponse result = service.programar(request);

        assertThat(result.recordId()).isEqualTo(record.getId());
        assertThat(result.fechaInicio()).isEqualTo(LocalDate.of(2027, 2, 1));
        assertThat(result.fechaFin()).isEqualTo(LocalDate.of(2027, 2, 15));
        assertThat(result.dias()).isEqualTo(15);
        assertThat(result.estado()).isEqualTo("PROGRAMADO");

        verify(goceRepository).save(goceCaptor.capture());
        var saved = goceCaptor.getValue();
        assertThat(saved.getRecord().getId()).isEqualTo(record.getId());
        assertThat(saved.getEstado()).isEqualTo("PROGRAMADO");
    }

    // --- programar: captures remuneracion sin hijos ---

    @Test
    void programar_CapturesRemuneracionSinHijos() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(recordRepository.findByTrabajadorIdOrderByFechaInicioDesc(1L))
            .thenReturn(List.of(record));
        when(goceRepository.save(any())).thenAnswer(i -> {
            var g = (VacacionGoce) i.getArgument(0);
            if (g.getId() == null) g.setId(1L);
            return g;
        });

        var request = new ProgramarRequest(1L, LocalDate.of(2027, 2, 1), 7);
        VacacionGoceResponse result = service.programar(request);

        assertThat(result.remuneracion()).isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    // --- programar: captures remuneracion con asignación familiar ---

    @Test
    void programar_CapturesRemuneracionConAsignacionFamiliar() {
        var contratoHijos = new Contrato();
        contratoHijos.setId(2L);
        contratoHijos.setTrabajador(trabajadorConHijos);
        contratoHijos.setRemuneracion(new BigDecimal("2500.00"));
        contratoHijos.setFechaInicio(LocalDate.of(2026, 1, 1));
        contratoHijos.setEstado(EstadoContrato.ACTIVO);

        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        record.setTrabajador(trabajadorConHijos);
        record.setContrato(contratoHijos);

        when(trabajadorRepository.findById(2L)).thenReturn(Optional.of(trabajadorConHijos));
        when(recordRepository.findByTrabajadorIdOrderByFechaInicioDesc(2L))
            .thenReturn(List.of(record));
        when(properties.getRmv()).thenReturn(1130);
        when(goceRepository.save(any())).thenAnswer(i -> {
            var g = (VacacionGoce) i.getArgument(0);
            if (g.getId() == null) g.setId(1L);
            return g;
        });

        var request = new ProgramarRequest(2L, LocalDate.of(2027, 2, 1), 7);
        VacacionGoceResponse result = service.programar(request);

        assertThat(result.remuneracion()).isEqualByComparingTo(new BigDecimal("2613.00"));
    }

    // --- programar: rejects if exceeds pending days ---

    @Test
    void programar_RejectsIfExceedsPendingDays() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(10));

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(recordRepository.findByTrabajadorIdOrderByFechaInicioDesc(1L))
            .thenReturn(List.of(record));

        var request = new ProgramarRequest(1L, LocalDate.of(2027, 2, 1), 12);

        assertThatThrownBy(() -> service.programar(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("exceden")
            .hasMessageContaining("12")
            .hasMessageContaining("10");
    }

    // --- programar: throws when no active record ---

    @Test
    void programar_ThrowsWhenNoActiveRecord() {
        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(recordRepository.findByTrabajadorIdOrderByFechaInicioDesc(1L))
            .thenReturn(List.of());

        var request = new ProgramarRequest(1L, LocalDate.of(2027, 2, 1), 7);

        assertThatThrownBy(() -> service.programar(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no tiene registro vacacional activo");
    }

    // --- iniciar: transitions from PROGRAMADO to EN_CURSO ---

    @Test
    void iniciar_TransitionsFromProgramadoToEnCurso() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        var goce = buildGoce("PROGRAMADO");
        goce.setRecord(record);

        when(goceRepository.findById(1L)).thenReturn(Optional.of(goce));
        when(goceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        VacacionGoceResponse result = service.iniciar(1L);

        assertThat(result.estado()).isEqualTo("EN_CURSO");
        verify(goceRepository).save(goceCaptor.capture());
        assertThat(goceCaptor.getValue().getEstado()).isEqualTo("EN_CURSO");
    }

    // --- iniciar: rejects if not PROGRAMADO ---

    @Test
    void iniciar_RejectsInvalidState() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        var goce = buildGoce("COMPLETADO");
        goce.setRecord(record);

        when(goceRepository.findById(1L)).thenReturn(Optional.of(goce));

        assertThatThrownBy(() -> service.iniciar(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no se puede iniciar");
    }

    // --- completar: transitions from EN_CURSO to COMPLETADO ---

    @Test
    void completar_TransitionsFromEnCursoToCompletado() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        var goce = buildGoce("EN_CURSO");
        goce.setRecord(record);

        when(goceRepository.findById(1L)).thenReturn(Optional.of(goce));
        when(goceRepository.findByRecordIdAndEstado(record.getId(), "COMPLETADO"))
            .thenReturn(List.of(goce));
        when(goceRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(recordRepository.save(any())).thenReturn(record);

        VacacionGoceResponse result = service.completar(1L);

        assertThat(result.estado()).isEqualTo("COMPLETADO");
        verify(goceRepository).save(goceCaptor.capture());
        assertThat(goceCaptor.getValue().getEstado()).isEqualTo("COMPLETADO");
    }

    // --- completar: updates pending days ---

    @Test
    void completar_UpdatesPendingDays() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        var goce = buildGoce("EN_CURSO");
        goce.setRecord(record);
        goce.setDias(7);

        var previousGoce = new VacacionGoce();
        previousGoce.setId(2L);
        previousGoce.setRecord(record);
        previousGoce.setDias(5);
        previousGoce.setEstado("COMPLETADO");

        when(goceRepository.findById(1L)).thenReturn(Optional.of(goce));
        when(goceRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(goceRepository.findByRecordIdAndEstado(record.getId(), "COMPLETADO"))
            .thenReturn(List.of(previousGoce, goce));
        when(recordRepository.save(any())).thenReturn(record);

        service.completar(1L);

        verify(recordRepository).save(recordCaptor.capture());
        var savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getDiasPendientes()).isEqualByComparingTo(BigDecimal.valueOf(3));
    }

    // --- completar: marks record COMPLETADO when pending=0 ---

    @Test
    void completar_MarksRecordCompletadoWhenPendingZero() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        var goce = buildGoce("EN_CURSO");
        goce.setRecord(record);
        goce.setDias(15);

        when(goceRepository.findById(1L)).thenReturn(Optional.of(goce));
        when(goceRepository.findByRecordIdAndEstado(record.getId(), "COMPLETADO"))
            .thenReturn(List.of(goce));
        when(goceRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(recordRepository.save(any())).thenReturn(record);

        service.completar(1L);

        verify(recordRepository).save(recordCaptor.capture());
        var savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getDiasPendientes()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedRecord.getEstado()).isEqualTo("COMPLETADO");
    }

    // --- completar: rejects if not EN_CURSO ---

    @Test
    void completar_RejectsInvalidState() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        var goce = buildGoce("PROGRAMADO");
        goce.setRecord(record);

        when(goceRepository.findById(1L)).thenReturn(Optional.of(goce));

        assertThatThrownBy(() -> service.completar(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no se puede completar");
    }

    // --- cancelar: transitions from PROGRAMADO to CANCELADO ---

    @Test
    void cancelar_TransitionsFromProgramadoToCancelado() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        var goce = buildGoce("PROGRAMADO");
        goce.setRecord(record);

        when(goceRepository.findById(1L)).thenReturn(Optional.of(goce));
        when(goceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        VacacionGoceResponse result = service.cancelar(1L);

        assertThat(result.estado()).isEqualTo("CANCELADO");
        verify(goceRepository).save(goceCaptor.capture());
        assertThat(goceCaptor.getValue().getEstado()).isEqualTo("CANCELADO");
    }

    // --- cancelar: can cancel EN_CURSO ---

    @Test
    void cancelar_CanCancelEnCurso() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        var goce = buildGoce("EN_CURSO");
        goce.setRecord(record);

        when(goceRepository.findById(1L)).thenReturn(Optional.of(goce));
        when(goceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        VacacionGoceResponse result = service.cancelar(1L);

        assertThat(result.estado()).isEqualTo("CANCELADO");
    }

    // --- cancelar: rejects COMPLETADO state ---

    @Test
    void cancelar_RejectsCompletadoState() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        var goce = buildGoce("COMPLETADO");
        goce.setRecord(record);

        when(goceRepository.findById(1L)).thenReturn(Optional.of(goce));

        assertThatThrownBy(() -> service.cancelar(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no se puede cancelar");
    }

    // --- cancelar: rejects CANCELADO state ---

    @Test
    void cancelar_RejectsCanceladoState() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        var goce = buildGoce("CANCELADO");
        goce.setRecord(record);

        when(goceRepository.findById(1L)).thenReturn(Optional.of(goce));

        assertThatThrownBy(() -> service.cancelar(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no se puede cancelar");
    }

    // --- finders ---

    @Test
    void findRecordsByTrabajador_ReturnsList() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        when(recordRepository.findByTrabajadorIdOrderByFechaInicioDesc(1L))
            .thenReturn(List.of(record));

        var results = service.findRecordsByTrabajador(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).trabajadorId()).isEqualTo(1L);
    }

    @Test
    void findRecordsByTrabajador_ReturnsEmptyWhenNone() {
        when(recordRepository.findByTrabajadorIdOrderByFechaInicioDesc(99L))
            .thenReturn(List.of());

        var results = service.findRecordsByTrabajador(99L);

        assertThat(results).isEmpty();
    }

    @Test
    void findGocesByRecord_ReturnsList() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        var goce = buildGoce("PROGRAMADO");
        goce.setRecord(record);

        when(goceRepository.findByRecordIdOrderByFechaInicioAsc(1L))
            .thenReturn(List.of(goce));

        var results = service.findGocesByRecord(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).recordId()).isEqualTo(1L);
    }

    @Test
    void findGocesByRecord_ReturnsEmptyWhenNone() {
        when(goceRepository.findByRecordIdOrderByFechaInicioAsc(99L))
            .thenReturn(List.of());

        var results = service.findGocesByRecord(99L);

        assertThat(results).isEmpty();
    }

    @Test
    void findRecordById_ReturnsRecord() {
        var record = buildActiveRecord(15, BigDecimal.valueOf(15));
        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

        var result = service.findRecordById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.trabajadorId()).isEqualTo(1L);
    }

    @Test
    void findRecordById_ThrowsWhenNotFound() {
        when(recordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findRecordById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    // --- goce found errors ---

    @Test
    void iniciar_ThrowsWhenNotFound() {
        when(goceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.iniciar(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void completar_ThrowsWhenNotFound() {
        when(goceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completar(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void cancelar_ThrowsWhenNotFound() {
        when(goceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelar(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    // --- Helper: build active record ---

    private VacacionRecord buildActiveRecord(int diasDerecho, BigDecimal diasPendientes) {
        var record = new VacacionRecord();
        record.setId(1L);
        record.setTrabajador(trabajador);
        record.setContrato(contrato);
        record.setFechaInicio(LocalDate.of(2026, 1, 1));
        record.setFechaFin(LocalDate.of(2027, 1, 1));
        record.setDiasDerecho(diasDerecho);
        record.setDiasReduccion(0);
        record.setDiasPendientes(diasPendientes);
        record.setEstado("ACTIVO");
        record.setFechaExpiracion(LocalDate.of(2028, 1, 1));
        return record;
    }

    // --- Helper: build goce ---

    private VacacionGoce buildGoce(String estado) {
        var goce = new VacacionGoce();
        goce.setId(1L);
        goce.setFechaInicio(LocalDate.of(2027, 2, 1));
        goce.setFechaFin(LocalDate.of(2027, 2, 15));
        goce.setDias(15);
        goce.setRemuneracion(new BigDecimal("2000.00"));
        goce.setEstado(estado);
        return goce;
    }
}

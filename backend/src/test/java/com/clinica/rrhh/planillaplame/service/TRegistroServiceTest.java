package com.clinica.rrhh.planillaplame.service;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import com.clinica.rrhh.pension.repository.InformacionPensionariaRepository;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planillaplame.entity.ArchivoPlanilla;
import com.clinica.rrhh.planillaplame.repository.ArchivoPlanillaRepository;
import com.clinica.rrhh.planillaplame.repository.TRegistroEventoRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.type.EstadoContrato;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TRegistroServiceTest {

    @Mock private PeriodoPlanillaRepository periodoPlanillaRepository;
    @Mock private ContratoRepository contratoRepository;
    @Mock private InformacionPensionariaRepository pensionRepository;
    @Mock private TRegistroEventoRepository eventoRepository;
    @Mock private ArchivoPlanillaRepository archivoRepository;

    @InjectMocks
    private TRegistroService service;

    private PeriodoPlanilla createPeriodoCerrado() {
        var p = new PeriodoPlanilla();
        p.setId(1L);
        p.setAnio(2026);
        p.setMes(1);
        p.setFechaInicio(LocalDate.of(2026, 1, 1));
        p.setFechaFin(LocalDate.of(2026, 1, 31));
        p.setEstado("CERRADO");
        return p;
    }

    private Trabajador createTrabajador(Long id, String numDoc) {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");

        var persona = new Persona();
        persona.setId(id);
        persona.setTipoDocumentoIdentidad(tdi);
        persona.setNumeroDocumento(numDoc);

        var t = new Trabajador();
        t.setId(id);
        t.setPersona(persona);
        t.setCodigoTrabajador("TR-" + id);
        return t;
    }

    private Contrato createContrato(Long id, Trabajador t, LocalDate inicio, LocalDate fin) {
        var c = new Contrato();
        c.setId(id);
        c.setTrabajador(t);
        c.setFechaInicio(inicio);
        c.setFechaFin(fin);
        c.setEstado(EstadoContrato.ACTIVO);
        return c;
    }

    @Test
    void generar_WithAltaEvento_ShouldCreateTxt() {
        var periodo = createPeriodoCerrado();
        var trabajador = createTrabajador(1L, "12345678");
        var contrato = createContrato(1L, trabajador, LocalDate.of(2026, 1, 15), null);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(contratoRepository.findByFechaInicioBetween(any(), any())).thenReturn(List.of(contrato));
        when(contratoRepository.findByFechaFinBetween(any(), any())).thenReturn(List.of());
        when(contratoRepository.findByEstadoAndUpdatedAtBetween(any(), any(), any())).thenReturn(List.of());
        when(pensionRepository.findByUpdatedAtBetween(any(), any())).thenReturn(List.of());

        var captor = ArgumentCaptor.<com.clinica.rrhh.planillaplame.entity.TRegistroEvento>captor();
        when(eventoRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));
        when(archivoRepository.findByPeriodoPlanillaIdAndTipo(any(), any())).thenReturn(Optional.empty());
        when(archivoRepository.save(any())).thenAnswer(i -> {
            var a = (ArchivoPlanilla) i.getArgument(0);
            a.setId(10L);
            return a;
        });

        var result = service.generar(1L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.tipo()).isEqualTo("T_REGISTRO");

        var savedEvento = captor.getValue();
        assertThat(savedEvento.getTipoEvento()).isEqualTo("ALTA");
        assertThat(savedEvento.getFechaEvento()).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void generar_WithBajaEvento_ShouldIncludeMotivoCese() {
        var periodo = createPeriodoCerrado();
        var trabajador = createTrabajador(1L, "12345678");
        var contrato = createContrato(1L, trabajador, LocalDate.of(2025, 6, 1), LocalDate.of(2026, 1, 20));
        contrato.setMotivoCese("Renuncia voluntaria");

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(contratoRepository.findByFechaInicioBetween(any(), any())).thenReturn(List.of());
        when(contratoRepository.findByFechaFinBetween(any(), any())).thenReturn(List.of(contrato));
        when(contratoRepository.findByEstadoAndUpdatedAtBetween(any(), any(), any())).thenReturn(List.of());
        when(pensionRepository.findByUpdatedAtBetween(any(), any())).thenReturn(List.of());

        var captor = ArgumentCaptor.forClass(com.clinica.rrhh.planillaplame.entity.TRegistroEvento.class);
        when(eventoRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));
        when(archivoRepository.findByPeriodoPlanillaIdAndTipo(any(), any())).thenReturn(Optional.empty());
        when(archivoRepository.save(any())).thenAnswer(i -> {
            var a = (ArchivoPlanilla) i.getArgument(0);
            a.setId(10L);
            return a;
        });

        service.generar(1L);

        var evento = captor.getValue();
        assertThat(evento.getTipoEvento()).isEqualTo("BAJA");
        assertThat(evento.getDetalleJson()).contains("Renuncia voluntaria");
    }

    @Test
    void generar_WithVariacionPension_ShouldCreateVariacionEvento() {
        var periodo = createPeriodoCerrado();
        var trabajador = createTrabajador(1L, "12345678");

        var pension = new InformacionPensionaria();
        pension.setTrabajador(trabajador);
        ReflectionTestUtils.setField(pension, "updatedAt", LocalDateTime.of(2026, 1, 15, 10, 0));

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(contratoRepository.findByFechaInicioBetween(any(), any())).thenReturn(List.of());
        when(contratoRepository.findByFechaFinBetween(any(), any())).thenReturn(List.of());
        when(contratoRepository.findByEstadoAndUpdatedAtBetween(any(), any(), any())).thenReturn(List.of());
        when(pensionRepository.findByUpdatedAtBetween(any(), any())).thenReturn(List.of(pension));

        var captor = ArgumentCaptor.forClass(com.clinica.rrhh.planillaplame.entity.TRegistroEvento.class);
        when(eventoRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));
        when(archivoRepository.findByPeriodoPlanillaIdAndTipo(any(), any())).thenReturn(Optional.empty());
        when(archivoRepository.save(any())).thenAnswer(i -> {
            var a = (ArchivoPlanilla) i.getArgument(0);
            a.setId(10L);
            return a;
        });

        service.generar(1L);

        var evento = captor.getValue();
        assertThat(evento.getTipoEvento()).isEqualTo("VARIACION");
    }

    @Test
    void generar_EmptyPeriod_ShouldCreateEmptyTxt() {
        var periodo = createPeriodoCerrado();

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(contratoRepository.findByFechaInicioBetween(any(), any())).thenReturn(List.of());
        when(contratoRepository.findByFechaFinBetween(any(), any())).thenReturn(List.of());
        when(contratoRepository.findByEstadoAndUpdatedAtBetween(any(), any(), any())).thenReturn(List.of());
        when(pensionRepository.findByUpdatedAtBetween(any(), any())).thenReturn(List.of());
        when(archivoRepository.findByPeriodoPlanillaIdAndTipo(any(), any())).thenReturn(Optional.empty());
        when(archivoRepository.save(any())).thenAnswer(i -> {
            var a = (ArchivoPlanilla) i.getArgument(0);
            a.setId(10L);
            return a;
        });

        var result = service.generar(1L);

        assertThat(result.tipo()).isEqualTo("T_REGISTRO");
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void generar_AbiertoPeriod_ShouldThrow() {
        var periodo = new PeriodoPlanilla();
        periodo.setId(1L);
        periodo.setEstado("ABIERTO");

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));

        assertThatThrownBy(() -> service.generar(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CERRADO");
    }

    @Test
    void generar_PeriodoNotFound_ShouldThrow() {
        when(periodoPlanillaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generar(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void generar_TxtFormat_ShouldBePipeDelimited() {
        var periodo = createPeriodoCerrado();
        var trabajador = createTrabajador(1L, "12345678");
        var contrato = createContrato(1L, trabajador, LocalDate.of(2026, 1, 15), null);

        when(periodoPlanillaRepository.findById(1L)).thenReturn(Optional.of(periodo));
        when(contratoRepository.findByFechaInicioBetween(any(), any())).thenReturn(List.of(contrato));
        when(contratoRepository.findByFechaFinBetween(any(), any())).thenReturn(List.of());
        when(contratoRepository.findByEstadoAndUpdatedAtBetween(any(), any(), any())).thenReturn(List.of());
        when(pensionRepository.findByUpdatedAtBetween(any(), any())).thenReturn(List.of());

        when(eventoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var archivoCaptor = ArgumentCaptor.forClass(ArchivoPlanilla.class);
        when(archivoRepository.findByPeriodoPlanillaIdAndTipo(any(), any())).thenReturn(Optional.empty());
        when(archivoRepository.save(archivoCaptor.capture())).thenAnswer(i -> {
            var a = (ArchivoPlanilla) i.getArgument(0);
            a.setId(10L);
            return a;
        });

        service.generar(1L);

        String txt = archivoCaptor.getValue().getContenido();
        assertThat(txt).isNotEmpty();
        assertThat(txt).containsPattern("\\d\\|\\d+\\|ALTA\\|\\d{8}\\|");
        assertThat(txt.lines().count()).isEqualTo(1);
    }

    @Test
    void getEventos_ShouldDelegateToRepo() {
        when(eventoRepository.findByPeriodoPlanillaIdOrderByFechaEventoAsc(1L))
                .thenReturn(List.of());

        var result = service.getEventos(1L);
        assertThat(result).isEmpty();
    }

    @Test
    void getArchivoParaDescarga_NotFound_ShouldThrow() {
        when(archivoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getArchivoParaDescarga(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }
}

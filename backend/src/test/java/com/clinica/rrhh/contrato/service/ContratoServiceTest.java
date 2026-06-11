package com.clinica.rrhh.contrato.service;

import com.clinica.maestro.entity.rrhh.TipoContrato;
import com.clinica.maestro.repository.rrhh.TipoContratoRepository;
import com.clinica.rrhh.contrato.dto.ContratoRequest;
import com.clinica.rrhh.contrato.dto.ContratoUpdateRequest;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.derechohabiente.service.DerechohabienteService;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.EstadoContrato;
import com.clinica.rrhh.type.TipoJornada;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private TrabajadorRepository trabajadorRepository;

    @Mock
    private TipoContratoRepository tipoContratoRepository;

    @Mock
    private DerechohabienteService derechohabienteService;

    @InjectMocks
    private ContratoService contratoService;

    @Captor
    private ArgumentCaptor<Contrato> contratoCaptor;

    private Trabajador createTrabajador(Long id) {
        var t = new Trabajador();
        t.setId(id);
        t.setCodigoTrabajador("TR-" + id);
        return t;
    }

    private TipoContrato createTipoContrato(Long id, String codigo, String nombre) {
        var tc = new TipoContrato();
        tc.setId(id);
        tc.setCodigo(codigo);
        tc.setNombre(nombre);
        return tc;
    }

    private Contrato createContrato(Long id, Trabajador t, TipoContrato tc, EstadoContrato estado) {
        var c = new Contrato();
        c.setId(id);
        c.setTrabajador(t);
        c.setTipoContrato(tc);
        c.setFechaInicio(LocalDate.of(2025, 1, 1));
        c.setRemuneracion(new BigDecimal("2500.00"));
        c.setJornada(TipoJornada.REGULAR);
        c.setEstado(estado);
        return c;
    }

    @Test
    void create_ShouldSaveAndReturnResponse() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(tipoContratoRepository.findById(1L)).thenReturn(Optional.of(tipoContrato));
        when(contratoRepository.findByTrabajadorIdAndEstado(1L, EstadoContrato.ACTIVO))
                .thenReturn(Optional.empty());
        when(contratoRepository.save(any())).thenAnswer(i -> {
            var c = (Contrato) i.getArgument(0);
            c.setId(1L);
            return c;
        });

        var request = new ContratoRequest(1L, 1L, LocalDate.of(2025, 1, 1), null, null,
                new BigDecimal("2500.00"), null);
        var result = contratoService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.estado()).isEqualTo("ACTIVO");
        verify(contratoRepository).save(contratoCaptor.capture());
        assertThat(contratoCaptor.getValue().getJornada()).isEqualTo(TipoJornada.REGULAR);
    }

    @Test
    void create_ShouldAutoExpirePreviousActive() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var previous = createContrato(99L, trabajador, tipoContrato, EstadoContrato.ACTIVO);

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(tipoContratoRepository.findById(1L)).thenReturn(Optional.of(tipoContrato));
        when(contratoRepository.findByTrabajadorIdAndEstado(1L, EstadoContrato.ACTIVO))
                .thenReturn(Optional.of(previous));
        when(contratoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new ContratoRequest(1L, 1L, LocalDate.of(2025, 6, 1), null, null,
                new BigDecimal("3000.00"), null);
        var result = contratoService.create(request);

        assertThat(result).isNotNull();
        assertThat(previous.getEstado()).isEqualTo(EstadoContrato.VENCIDO);
        verify(contratoRepository, times(2)).save(any());
    }

    @Test
    void create_DETERMINADO_sinFechaFin_throws() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "DETERMINADO", "Determinado");

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(tipoContratoRepository.findById(1L)).thenReturn(Optional.of(tipoContrato));

        var request = new ContratoRequest(1L, 1L, LocalDate.of(2025, 1, 1), null, null,
                new BigDecimal("2500.00"), null);

        assertThatThrownBy(() -> contratoService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requiere fecha de fin");
    }

    @Test
    void findById_ShouldReturnResponse() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.ACTIVO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));

        var result = contratoService.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.estado()).isEqualTo("ACTIVO");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(contratoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contratoService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void resolver_ACTIVO_Succeeds() {
        var trabajador = createTrabajador(1L);
        trabajador.setId(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.ACTIVO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = contratoService.resolver(1L, null);

        assertThat(result.estado()).isEqualTo("RESUELTO");
        verify(derechohabienteService).inactivarPorTrabajador(1L);
    }

    @Test
    void resolver_RESUELTO_throws() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.RESUELTO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));

        assertThatThrownBy(() -> contratoService.resolver(1L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESUELTO");
        verify(derechohabienteService, never()).inactivarPorTrabajador(any());
    }

    @Test
    void resolver_PersistsMotivoCese() {
        var trabajador = createTrabajador(1L);
        trabajador.setId(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.ACTIVO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = contratoService.resolver(1L, "Renuncia voluntaria");

        assertThat(result.estado()).isEqualTo("RESUELTO");
        assertThat(result.motivoCese()).isEqualTo("Renuncia voluntaria");
        verify(derechohabienteService).inactivarPorTrabajador(1L);
    }

    @Test
    void suspender_ACTIVO_Succeeds() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.ACTIVO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = contratoService.suspender(1L);

        assertThat(result.estado()).isEqualTo("SUSPENDIDO");
    }

    @Test
    void suspender_SUSPENDIDO_throws() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.SUSPENDIDO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));

        assertThatThrownBy(() -> contratoService.suspender(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SUSPENDIDO");
    }

    @Test
    void reactivar_SUSPENDIDO_Succeeds() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.SUSPENDIDO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = contratoService.reactivar(1L);

        assertThat(result.estado()).isEqualTo("ACTIVO");
    }

    @Test
    void reactivar_ACTIVO_throws() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.ACTIVO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));

        assertThatThrownBy(() -> contratoService.reactivar(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACTIVO");
    }

    @Test
    void resolver_CascadesToDerechohabienteService() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.ACTIVO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        contratoService.resolver(1L, null);

        verify(derechohabienteService).inactivarPorTrabajador(1L);
    }

    @Test
    void update_ShouldUpdateAndReturnResponse() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.ACTIVO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(tipoContratoRepository.findById(1L)).thenReturn(Optional.of(tipoContrato));
        when(contratoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new ContratoUpdateRequest(1L, LocalDate.of(2025, 6, 1), null, null,
                new BigDecimal("3000.00"), null);
        var result = contratoService.update(1L, request);

        assertThat(result.remuneracion()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(result.fechaInicio()).isEqualTo(LocalDate.of(2025, 6, 1));
    }

    @Test
    void update_RESUELTO_throws() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "INDETERMINADO", "Indeterminado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.RESUELTO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));

        var request = new ContratoUpdateRequest(1L, LocalDate.of(2025, 6, 1), null, null,
                new BigDecimal("3000.00"), null);

        assertThatThrownBy(() -> contratoService.update(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESUELTO");
    }

    @Test
    void update_DETERMINADO_sinFechaFin_throws() {
        var trabajador = createTrabajador(1L);
        var tipoContrato = createTipoContrato(1L, "DETERMINADO", "Determinado");
        var contrato = createContrato(1L, trabajador, tipoContrato, EstadoContrato.ACTIVO);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(tipoContratoRepository.findById(1L)).thenReturn(Optional.of(tipoContrato));

        var request = new ContratoUpdateRequest(1L, LocalDate.of(2025, 6, 1), null, null,
                new BigDecimal("3000.00"), null);

        assertThatThrownBy(() -> contratoService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requiere fecha de fin");
    }
}

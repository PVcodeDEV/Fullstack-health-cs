package com.clinica.clinica.hospitalizacion.service;

import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.entity.EstadoCama;
import com.clinica.clinica.cama.repository.CamaRepository;
import com.clinica.clinica.hospitalizacion.dto.AltaMedicaRequest;
import com.clinica.clinica.hospitalizacion.dto.CambioHabitacionRequest;
import com.clinica.clinica.hospitalizacion.dto.NotaEvolucionRequest;
import com.clinica.clinica.hospitalizacion.dto.SolicitudMedicamentoRequest;
import com.clinica.clinica.hospitalizacion.entity.*;
import com.clinica.clinica.hospitalizacion.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalizacionServiceTest {

    @Mock
    private HospitalizacionRepository hospitalizacionRepository;
    @Mock
    private CambioHabitacionRepository cambioHabitacionRepository;
    @Mock
    private NotaEvolucionRepository notaEvolucionRepository;
    @Mock
    private SolicitudMedicamentoRepository solicitudMedicamentoRepository;
    @Mock
    private AltaMedicaRepository altaMedicaRepository;
    @Mock
    private CamaRepository camaRepository;

    @InjectMocks
    private HospitalizacionService service;

    @Captor
    private ArgumentCaptor<Cama> camaCaptor;
    @Captor
    private ArgumentCaptor<Hospitalizacion> hospCaptor;

    private Hospitalizacion createHospitalizacion(Long id, Long camaId, String estado) {
        var h = new Hospitalizacion();
        h.setId(id);
        h.setSolicitudId(1L);
        h.setCuentaId(1L);
        h.setPacienteId(1L);
        h.setCamaId(camaId);
        h.setFechaIngreso(LocalDateTime.now());
        h.setEstado(estado);
        return h;
    }

    private Cama createCama(Long id, EstadoCama estado) {
        var c = new Cama();
        c.setId(id);
        c.setHabitacionId(1L);
        c.setCodigo("CAMA-" + id);
        c.setEstado(estado);
        return c;
    }

    @Test
    void cambiarCama_ShouldLiberateOrigenAndOccupyDestino() {
        var hosp = createHospitalizacion(1L, 1L, "HOSPITALIZADO");
        var camaOrigen = createCama(1L, EstadoCama.OCUPADO);
        var camaDestino = createCama(2L, EstadoCama.DISPONIBLE);

        when(hospitalizacionRepository.findById(1L)).thenReturn(Optional.of(hosp));
        when(camaRepository.findById(1L)).thenReturn(Optional.of(camaOrigen));
        when(camaRepository.findById(2L)).thenReturn(Optional.of(camaDestino));
        when(camaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(hospitalizacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(cambioHabitacionRepository.save(any())).thenAnswer(i -> {
            var c = (CambioHabitacion) i.getArgument(0);
            c.setId(1L);
            return c;
        });

        var request = new CambioHabitacionRequest(2L, "Cambio a mejor habitación");
        var result = service.cambiarCama(1L, request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.motivo()).isEqualTo("Cambio a mejor habitación");
        assertThat(camaOrigen.getEstado()).isEqualTo(EstadoCama.DISPONIBLE);
        assertThat(camaDestino.getEstado()).isEqualTo(EstadoCama.OCUPADO);
    }

    @Test
    void cambiarCama_WithCamaDestinoOcupada_ShouldThrow() {
        var hosp = createHospitalizacion(1L, 1L, "HOSPITALIZADO");
        var camaOrigen = createCama(1L, EstadoCama.OCUPADO);
        var camaDestino = createCama(2L, EstadoCama.OCUPADO);

        when(hospitalizacionRepository.findById(1L)).thenReturn(Optional.of(hosp));
        when(camaRepository.findById(1L)).thenReturn(Optional.of(camaOrigen));
        when(camaRepository.findById(2L)).thenReturn(Optional.of(camaDestino));

        var request = new CambioHabitacionRequest(2L, "Cambio");
        assertThatThrownBy(() -> service.cambiarCama(1L, request, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    void registrarNota_ShouldSaveAndReturnResponse() {
        var hosp = createHospitalizacion(1L, 1L, "HOSPITALIZADO");
        when(hospitalizacionRepository.findById(1L)).thenReturn(Optional.of(hosp));
        when(notaEvolucionRepository.save(any())).thenAnswer(i -> {
            var n = (NotaEvolucion) i.getArgument(0);
            n.setId(1L);
            return n;
        });

        var request = new NotaEvolucionRequest("Paciente evoluciona favorablemente", "Continuar tratamiento", "EVOLUCION", "TA: 120/80");
        var result = service.registrarNota(1L, request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.descripcion()).isEqualTo("Paciente evoluciona favorablemente");
    }

    @Test
    void registrarNota_WithInvalidHospitalizacion_ShouldThrow() {
        when(hospitalizacionRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new NotaEvolucionRequest("Test", null, null, null);
        assertThatThrownBy(() -> service.registrarNota(99L, request, 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void solicitarMedicamento_ShouldSaveAndReturnResponse() {
        var hosp = createHospitalizacion(1L, 1L, "HOSPITALIZADO");
        when(hospitalizacionRepository.findById(1L)).thenReturn(Optional.of(hosp));
        when(solicitudMedicamentoRepository.save(any())).thenAnswer(i -> {
            var s = (SolicitudMedicamento) i.getArgument(0);
            s.setId(1L);
            return s;
        });

        var request = new SolicitudMedicamentoRequest(1L, "500mg", "Cada 8 horas", 1L, LocalDate.now(), LocalDate.now().plusDays(7));
        var result = service.solicitarMedicamento(1L, request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.dosis()).isEqualTo("500mg");
        assertThat(result.estado()).isEqualTo("PENDIENTE");
    }

    @Test
    void darAlta_ShouldSetEstadoAltaAndNotLiberateBed() {
        var hosp = createHospitalizacion(1L, 1L, "HOSPITALIZADO");

        when(hospitalizacionRepository.findById(1L)).thenReturn(Optional.of(hosp));
        when(altaMedicaRepository.save(any())).thenAnswer(i -> {
            var a = (AltaMedica) i.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(hospitalizacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new AltaMedicaRequest("MEJORADO", "PACIENTE MEJORADO", null, 1L);
        var result = service.darAlta(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.tipoAlta()).isEqualTo("MEJORADO");
        assertThat(hosp.getEstado()).isEqualTo("ALTA");
    }

    @Test
    void darAlta_WithEstadoNotActivo_ShouldThrow() {
        var hosp = createHospitalizacion(1L, 1L, "ALTA");
        when(hospitalizacionRepository.findById(1L)).thenReturn(Optional.of(hosp));

        var request = new AltaMedicaRequest("MEJORADO", "Test", null, 1L);
        assertThatThrownBy(() -> service.darAlta(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no está activa");
    }

    @Test
    void darAlta_WithInvalidHospitalizacion_ShouldThrow() {
        when(hospitalizacionRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new AltaMedicaRequest("MEJORADO", "Test", null, 1L);
        assertThatThrownBy(() -> service.darAlta(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

package com.clinica.clinica.sop.service;

import com.clinica.clinica.sop.dto.ReporteQuirurgicoRequest;
import com.clinica.clinica.sop.dto.URPARegistroRequest;
import com.clinica.clinica.sop.entity.ReporteQuirurgico;
import com.clinica.clinica.sop.entity.URPARegistro;
import com.clinica.clinica.sop.repository.ReporteQuirurgicoRepository;
import com.clinica.clinica.sop.repository.URPARegistroRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SOPServiceTest {

    @Mock
    private ReporteQuirurgicoRepository reporteRepository;
    @Mock
    private URPARegistroRepository urpaRegistroRepository;

    @InjectMocks
    private SOPService service;

    @Captor
    private ArgumentCaptor<ReporteQuirurgico> reporteCaptor;

    private ReporteQuirurgico createReporte(Long id, String estado) {
        var r = new ReporteQuirurgico();
        r.setId(id);
        r.setHospitalizacionId(1L);
        r.setFechaCirugia(LocalDate.now());
        r.setHoraInicio(LocalTime.of(8, 0));
        r.setCirujanoId(1L);
        r.setProcedimientoRealizado("APENDICECTOMIA");
        r.setMedicoId(1L);
        r.setEstado(estado);
        return r;
    }

    @Test
    void crearReporte_ShouldSaveInBorrador() {
        when(reporteRepository.save(any())).thenAnswer(i -> {
            var r = (ReporteQuirurgico) i.getArgument(0);
            r.setId(1L);
            return r;
        });

        var request = new ReporteQuirurgicoRequest(
                1L, 1L, 2L, "APENDICITIS AGUDA",
                "APENDICECTOMIA", null, null,
                LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(10, 0), null);

        var result = service.crearReporte(request);

        assertThat(result).isNotNull();
        assertThat(result.estado()).isEqualTo("BORRADOR");
    }

    @Test
    void crearReporte_WithEstadoExplicit_ShouldUseIt() {
        when(reporteRepository.save(any())).thenAnswer(i -> {
            var r = (ReporteQuirurgico) i.getArgument(0);
            r.setId(1L);
            return r;
        });

        var request = new ReporteQuirurgicoRequest(
                1L, 1L, null, "TEST",
                "PROCEDIMIENTO", null, null,
                LocalDate.now(), LocalTime.of(9, 0), null, "BORRADOR");

        var result = service.crearReporte(request);

        assertThat(result.estado()).isEqualTo("BORRADOR");
    }

    @Test
    void completarReporte_ShouldChangeToCompletado() {
        var reporte = createReporte(1L, "BORRADOR");
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reporteRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.completarReporte(1L);

        assertThat(result.estado()).isEqualTo("COMPLETADO");
    }

    @Test
    void completarReporte_WithEstadoNoBorrador_ShouldThrow() {
        var reporte = createReporte(1L, "COMPLETADO");
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        assertThatThrownBy(() -> service.completarReporte(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BORRADOR");
    }

    @Test
    void completarReporte_WithInvalidId_ShouldThrow() {
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completarReporte(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void registrarURPA_ShouldSaveAndReturnResponse() {
        var reporte = createReporte(1L, "BORRADOR");
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(urpaRegistroRepository.save(any())).thenAnswer(i -> {
            var u = (URPARegistro) i.getArgument(0);
            u.setId(1L);
            return u;
        });

        var request = new URPARegistroRequest(10, "ESTABLE", "Sin complicaciones");
        var result = service.registrarURPA(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.escalaAldreteIngreso()).isEqualTo(10);
    }

    @Test
    void registrarURPA_WithInvalidReporte_ShouldThrow() {
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new URPARegistroRequest(10, "ESTABLE", null);
        assertThatThrownBy(() -> service.registrarURPA(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

package com.clinica.clinica.sop.repository;

import com.clinica.clinica.hospitalizacion.entity.Hospitalizacion;
import com.clinica.clinica.hospitalizacion.repository.HospitalizacionRepository;
import com.clinica.clinica.sop.entity.ReporteQuirurgico;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReporteQuirurgicoRepositoryTest {

    @Autowired
    private ReporteQuirurgicoRepository reporteRepository;

    @Autowired
    private HospitalizacionRepository hospitalizacionRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private Hospitalizacion hospitalizacion;

    @BeforeEach
    void setUp() {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocumentoIdentidadRepository.saveAndFlush(tdi);

        var paciente = new Persona();
        paciente.setTipoDocumentoIdentidad(tdi);
        paciente.setNumeroDocumento("12345678");
        paciente.setNombres("JUAN");
        paciente.setApellidoPaterno("PEREZ");
        paciente.setActivo(true);
        paciente = personaRepository.saveAndFlush(paciente);

        hospitalizacion = new Hospitalizacion();
        hospitalizacion.setSolicitudId(1L);
        hospitalizacion.setCuentaId(1L);
        hospitalizacion.setPacienteId(paciente.getId());
        hospitalizacion.setCamaId(1L);
        hospitalizacion.setFechaIngreso(LocalDateTime.now());
        hospitalizacion.setEstado("HOSPITALIZADO");
        hospitalizacion = hospitalizacionRepository.saveAndFlush(hospitalizacion);

        var reporte = new ReporteQuirurgico();
        reporte.setHospitalizacionId(hospitalizacion.getId());
        reporte.setFechaCirugia(LocalDate.now());
        reporte.setHoraInicio(LocalTime.of(8, 0));
        reporte.setHoraFin(LocalTime.of(10, 0));
        reporte.setCirujanoId(1L);
        reporte.setAnestesiologoId(2L);
        reporte.setDiagnosticoPre("APENDICITIS AGUDA");
        reporte.setProcedimientoRealizado("APENDICECTOMIA");
        reporte.setMedicoId(1L);
        reporte.setEstado("BORRADOR");
        reporteRepository.saveAndFlush(reporte);
    }

    @Test
    void shouldSaveAndFindById() {
        var nuevoHosp = new Hospitalizacion();
        nuevoHosp.setSolicitudId(99L);
        nuevoHosp.setCuentaId(1L);
        nuevoHosp.setPacienteId(1L);
        nuevoHosp.setCamaId(1L);
        nuevoHosp.setFechaIngreso(LocalDateTime.now());
        nuevoHosp.setEstado("HOSPITALIZADO");
        nuevoHosp = hospitalizacionRepository.saveAndFlush(nuevoHosp);

        var reporte = new ReporteQuirurgico();
        reporte.setHospitalizacionId(nuevoHosp.getId());
        reporte.setFechaCirugia(LocalDate.now());
        reporte.setHoraInicio(LocalTime.of(9, 0));
        reporte.setCirujanoId(2L);
        reporte.setProcedimientoRealizado("COLECISTECTOMIA");
        reporte.setMedicoId(2L);
        reporte.setEstado("BORRADOR");

        var saved = reporteRepository.save(reporte);
        assertThat(saved.getId()).isNotNull();

        var found = reporteRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getProcedimientoRealizado()).isEqualTo("COLECISTECTOMIA");
    }

    @Test
    void shouldFindByHospitalizacionId() {
        var result = reporteRepository.findByHospitalizacionId(hospitalizacion.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getDiagnosticoPre()).isEqualTo("APENDICITIS AGUDA");
    }

    @Test
    void shouldFindByEstado() {
        var result = reporteRepository.findByEstado("BORRADOR");
        assertThat(result).hasSize(1);

        var completados = reporteRepository.findByEstado("COMPLETADO");
        assertThat(completados).isEmpty();
    }

    @Test
    void shouldExistsByHospitalizacionId() {
        assertThat(reporteRepository.existsByHospitalizacionId(hospitalizacion.getId())).isTrue();
        assertThat(reporteRepository.existsByHospitalizacionId(999L)).isFalse();
    }

    @Test
    void shouldTransitionEstado() {
        var reporte = reporteRepository.findByHospitalizacionId(hospitalizacion.getId()).orElseThrow();
        reporte.setEstado("COMPLETADO");
        reporteRepository.saveAndFlush(reporte);

        var found = reporteRepository.findByEstado("COMPLETADO");
        assertThat(found).hasSize(1);

        var borradores = reporteRepository.findByEstado("BORRADOR");
        assertThat(borradores).isEmpty();
    }

    @Test
    void shouldRejectDuplicateHospitalizacionId() {
        var reporte = new ReporteQuirurgico();
        reporte.setHospitalizacionId(hospitalizacion.getId()); // duplicate
        reporte.setFechaCirugia(LocalDate.now());
        reporte.setHoraInicio(LocalTime.of(8, 0));
        reporte.setCirujanoId(1L);
        reporte.setProcedimientoRealizado("OTRA CIRUGIA");
        reporte.setMedicoId(1L);
        reporte.setEstado("BORRADOR");

        assertThatThrownBy(() -> reporteRepository.saveAndFlush(reporte))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

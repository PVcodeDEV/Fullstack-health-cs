package com.clinica.clinica.hospitalizacion.repository;

import com.clinica.clinica.hospitalizacion.entity.AltaMedica;
import com.clinica.clinica.hospitalizacion.entity.Hospitalizacion;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AltaMedicaRepositoryTest {

    @Autowired
    private AltaMedicaRepository altaMedicaRepository;

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

        var alta = new AltaMedica();
        alta.setHospitalizacionId(hospitalizacion.getId());
        alta.setFechaAlta(LocalDateTime.now());
        alta.setTipoAlta("VOLUNTARIA");
        alta.setDiagnosticoFinal("PACIENTE MEJORADO");
        alta.setMedicoId(1L);
        altaMedicaRepository.saveAndFlush(alta);
    }

    @Test
    void shouldSaveAndFindById() {
        var alta = new AltaMedica();
        alta.setHospitalizacionId(hospitalizacion.getId());
        alta.setFechaAlta(LocalDateTime.now());
        alta.setTipoAlta("MEJORADO");
        alta.setDiagnosticoFinal("ALTA POR MEJORIA");
        alta.setMedicoId(1L);

        // Need unique constraint on hospitalizacionId, so create new hosp
        var nuevaHosp = new Hospitalizacion();
        nuevaHosp.setSolicitudId(99L);
        nuevaHosp.setCuentaId(1L);
        nuevaHosp.setPacienteId(1L);
        nuevaHosp.setCamaId(1L);
        nuevaHosp.setFechaIngreso(LocalDateTime.now());
        nuevaHosp.setEstado("HOSPITALIZADO");
        nuevaHosp = hospitalizacionRepository.saveAndFlush(nuevaHosp);
        alta.setHospitalizacionId(nuevaHosp.getId());

        var saved = altaMedicaRepository.save(alta);
        assertThat(saved.getId()).isNotNull();

        var found = altaMedicaRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTipoAlta()).isEqualTo("MEJORADO");
    }

    @Test
    void shouldFindByHospitalizacionId() {
        var result = altaMedicaRepository.findByHospitalizacionId(hospitalizacion.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getDiagnosticoFinal()).isEqualTo("PACIENTE MEJORADO");
    }

    @Test
    void shouldRejectDuplicateHospitalizacionId() {
        var alta = new AltaMedica();
        alta.setHospitalizacionId(hospitalizacion.getId()); // duplicate
        alta.setFechaAlta(LocalDateTime.now());
        alta.setTipoAlta("MEJORADO");
        alta.setDiagnosticoFinal("DUPLICADO");
        alta.setMedicoId(1L);

        assertThatThrownBy(() -> altaMedicaRepository.saveAndFlush(alta))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

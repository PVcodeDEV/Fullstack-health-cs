package com.clinica.clinica.admision.repository;

import com.clinica.clinica.admision.entity.Cuenta;
import com.clinica.clinica.admision.entity.SolicitudHospitalizacion;
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
class SolicitudHospitalizacionRepositoryTest {

    @Autowired
    private SolicitudHospitalizacionRepository solicitudRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private Cuenta cuenta;

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

        cuenta = new Cuenta();
        cuenta.setPacienteId(paciente.getId());
        cuenta.setFechaApertura(LocalDateTime.now());
        cuenta.setEstado("ABIERTA");
        cuenta = cuentaRepository.saveAndFlush(cuenta);

        var solicitud = new SolicitudHospitalizacion();
        solicitud.setCuentaId(cuenta.getId());
        solicitud.setTipoHabitacionId(1L);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitudRepository.saveAndFlush(solicitud);
    }

    @Test
    void shouldSaveAndFindById() {
        var solicitud = new SolicitudHospitalizacion();
        solicitud.setCuentaId(cuenta.getId());
        solicitud.setTipoHabitacionId(2L);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaSolicitud(LocalDateTime.now());

        var saved = solicitudRepository.save(solicitud);
        assertThat(saved.getId()).isNotNull();

        var found = solicitudRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    void shouldFindByCuentaId() {
        var result = solicitudRepository.findByCuentaId(cuenta.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    void shouldFindByEstado() {
        var result = solicitudRepository.findByEstado("PENDIENTE");
        assertThat(result).hasSize(1);

        var asignadas = solicitudRepository.findByEstado("ASIGNADA");
        assertThat(asignadas).isEmpty();
    }

    @Test
    void shouldFindByCuentaIdAndEstado() {
        var result = solicitudRepository.findByCuentaIdAndEstado(cuenta.getId(), "PENDIENTE");
        assertThat(result).isPresent();
    }

    @Test
    void shouldRejectNullCuentaId() {
        var solicitud = new SolicitudHospitalizacion();
        solicitud.setTipoHabitacionId(1L);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaSolicitud(LocalDateTime.now());

        assertThatThrownBy(() -> solicitudRepository.saveAndFlush(solicitud))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

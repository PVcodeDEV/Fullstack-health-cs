package com.clinica.clinica.hospitalizacion.repository;

import com.clinica.clinica.admision.entity.Cuenta;
import com.clinica.clinica.admision.repository.CuentaRepository;
import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.entity.EstadoCama;
import com.clinica.clinica.cama.entity.Habitacion;
import com.clinica.clinica.cama.repository.CamaRepository;
import com.clinica.clinica.cama.repository.HabitacionRepository;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HospitalizacionRepositoryTest {

    @Autowired
    private HospitalizacionRepository hospitalizacionRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private CamaRepository camaRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    private Cuenta cuenta;
    private Cama cama;
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

        cuenta = new Cuenta();
        cuenta.setPacienteId(paciente.getId());
        cuenta.setFechaApertura(LocalDateTime.now());
        cuenta.setEstado("ABIERTA");
        cuenta = cuentaRepository.saveAndFlush(cuenta);

        var habitacion = new Habitacion();
        habitacion.setCodigo("HAB-HOSP");
        habitacion.setNombre("Habitación Hosp");
        habitacion.setTipoHabitacionId(1L);
        habitacion.setCapacidad(2);
        habitacion = habitacionRepository.saveAndFlush(habitacion);

        cama = new Cama();
        cama.setHabitacionId(habitacion.getId());
        cama.setCodigo("CAMA-HOSP");
        cama.setEstado(EstadoCama.OCUPADO);
        cama = camaRepository.saveAndFlush(cama);

        hospitalizacion = new Hospitalizacion();
        hospitalizacion.setSolicitudId(1L);
        hospitalizacion.setCuentaId(cuenta.getId());
        hospitalizacion.setPacienteId(paciente.getId());
        hospitalizacion.setCamaId(cama.getId());
        hospitalizacion.setFechaIngreso(LocalDateTime.now());
        hospitalizacion.setEstado("HOSPITALIZADO");
        hospitalizacion = hospitalizacionRepository.saveAndFlush(hospitalizacion);
    }

    @Test
    void shouldSaveAndFindById() {
        var hosp = new Hospitalizacion();
        hosp.setSolicitudId(99L);
        hosp.setCuentaId(cuenta.getId());
        hosp.setPacienteId(1L);
        hosp.setCamaId(cama.getId());
        hosp.setFechaIngreso(LocalDateTime.now());
        hosp.setEstado("HOSPITALIZADO");

        var saved = hospitalizacionRepository.save(hosp);
        assertThat(saved.getId()).isNotNull();

        var found = hospitalizacionRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEstado()).isEqualTo("HOSPITALIZADO");
    }

    @Test
    void shouldFindBySolicitudId() {
        var result = hospitalizacionRepository.findBySolicitudId(1L);
        assertThat(result).isPresent();
    }

    @Test
    void shouldFindByCuentaId() {
        var result = hospitalizacionRepository.findByCuentaId(cuenta.getId());
        assertThat(result).isPresent();
    }

    @Test
    void shouldFindByEstado() {
        var result = hospitalizacionRepository.findByEstado("HOSPITALIZADO");
        assertThat(result).hasSize(1);

        var altas = hospitalizacionRepository.findByEstado("ALTA");
        assertThat(altas).isEmpty();
    }

    @Test
    void shouldTransitionEstado() {
        hospitalizacion.setEstado("ALTA");
        hospitalizacion.setFechaAlta(LocalDateTime.now());
        hospitalizacionRepository.saveAndFlush(hospitalizacion);

        var found = hospitalizacionRepository.findById(hospitalizacion.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEstado()).isEqualTo("ALTA");
        assertThat(found.get().getFechaAlta()).isNotNull();
    }

    @Test
    void shouldFindByPacienteId() {
        var result = hospitalizacionRepository.findByPacienteId(hospitalizacion.getPacienteId());
        assertThat(result).hasSize(1);
    }
}

package com.clinica.clinica.cama.repository;

import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.entity.EstadoCama;
import com.clinica.clinica.cama.entity.Habitacion;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CamaRepositoryTest {

    @Autowired
    private CamaRepository camaRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private PersonaRepository personaRepository;

    private Habitacion habitacion;
    private Cama camaDisponible;

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
        persona.setNumeroDocumento("11111111");
        persona.setNombres("TEST");
        persona.setApellidoPaterno("ROOM");
        persona.setActivo(true);
        personaRepository.saveAndFlush(persona);

        habitacion = new Habitacion();
        habitacion.setCodigo("HAB-001");
        habitacion.setNombre("Habitación 101");
        habitacion.setTipoHabitacionId(1L);
        habitacion.setCapacidad(2);
        habitacion = habitacionRepository.saveAndFlush(habitacion);

        camaDisponible = new Cama();
        camaDisponible.setHabitacionId(habitacion.getId());
        camaDisponible.setCodigo("CAMA-001");
        camaDisponible.setEstado(EstadoCama.DISPONIBLE);
        camaDisponible = camaRepository.saveAndFlush(camaDisponible);
    }

    @Test
    void shouldSaveAndFindById() {
        var cama = new Cama();
        cama.setHabitacionId(habitacion.getId());
        cama.setCodigo("CAMA-NEW");
        cama.setEstado(EstadoCama.DISPONIBLE);

        var saved = camaRepository.save(cama);
        assertThat(saved.getId()).isNotNull();

        var found = camaRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCodigo()).isEqualTo("CAMA-NEW");
    }

    @Test
    void shouldFindByCodigo() {
        var result = camaRepository.findByCodigo("CAMA-001");
        assertThat(result).isPresent();
        assertThat(result.get().getEstado()).isEqualTo(EstadoCama.DISPONIBLE);
    }

    @Test
    void shouldFindByHabitacionId() {
        var result = camaRepository.findByHabitacionId(habitacion.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodigo()).isEqualTo("CAMA-001");
    }

    @Test
    void shouldFindByEstado() {
        var result = camaRepository.findByEstado(EstadoCama.DISPONIBLE);
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getEstado()).isEqualTo(EstadoCama.DISPONIBLE);
    }

    @Test
    void shouldFindDisponibles() {
        var result = camaRepository.findByEstado(EstadoCama.DISPONIBLE);
        assertThat(result).hasSize(1);

        // Mark as OCUPADO
        camaDisponible.setEstado(EstadoCama.OCUPADO);
        camaRepository.saveAndFlush(camaDisponible);

        var afterOcupar = camaRepository.findByEstado(EstadoCama.DISPONIBLE);
        assertThat(afterOcupar).isEmpty();
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        var cama = new Cama();
        cama.setHabitacionId(habitacion.getId());
        cama.setCodigo("CAMA-001"); // duplicate
        cama.setEstado(EstadoCama.DISPONIBLE);

        assertThatThrownBy(() -> camaRepository.saveAndFlush(cama))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindByHabitacionIdAndEstado() {
        // Add another bed in MANTENIMIENTO
        var camaMant = new Cama();
        camaMant.setHabitacionId(habitacion.getId());
        camaMant.setCodigo("CAMA-MANT");
        camaMant.setEstado(EstadoCama.MANTENIMIENTO);
        camaRepository.saveAndFlush(camaMant);

        var disponibles = camaRepository.findByHabitacionIdAndEstado(habitacion.getId(), EstadoCama.DISPONIBLE);
        assertThat(disponibles).hasSize(1);

        var mantenimiento = camaRepository.findByHabitacionIdAndEstado(habitacion.getId(), EstadoCama.MANTENIMIENTO);
        assertThat(mantenimiento).hasSize(1);
    }

    @Test
    void shouldCountByHabitacionIdAndEstado() {
        var count = camaRepository.countByHabitacionIdAndEstado(habitacion.getId(), EstadoCama.DISPONIBLE);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldPerformSoftDelete() {
        camaDisponible.markAsInactive();
        camaRepository.save(camaDisponible);

        var found = camaRepository.findById(camaDisponible.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getActivo()).isFalse();
    }
}

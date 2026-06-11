package com.clinica.persona.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersonaRepositoryTest {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private TipoDocumentoIdentidad tdiDni;

    @BeforeEach
    void setUp() {
        tdiDni = new TipoDocumentoIdentidad();
        tdiDni.setCodigoSunat("01");
        tdiDni.setNombre("DNI");
        tdiDni.setLongitudMinima(8);
        tdiDni.setLongitudMaxima(8);
        tdiDni = tipoDocumentoIdentidadRepository.saveAndFlush(tdiDni);

        var persona = new Persona();
        persona.setTipoDocumentoIdentidad(tdiDni);
        persona.setNumeroDocumento("12345678");
        persona.setNombres("JUAN");
        persona.setApellidoPaterno("PEREZ");
        persona.setApellidoMaterno("LOPEZ");
        persona.setActivo(true);
        personaRepository.saveAndFlush(persona);
    }

    @Test
    void shouldSaveAndFindById() {
        var entity = new Persona();
        entity.setTipoDocumentoIdentidad(tdiDni);
        entity.setNumeroDocumento("87654321");
        entity.setNombres("MARIA");
        entity.setApellidoPaterno("GARCIA");
        entity.setActivo(true);

        var saved = personaRepository.save(entity);
        assertThat(saved.getId()).isNotNull();

        var found = personaRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getNumeroDocumento()).isEqualTo("87654321");
    }

    @Test
    void shouldFindByNumeroDocumento() {
        var result = personaRepository.findByNumeroDocumento("12345678");
        assertThat(result).isPresent();
        assertThat(result.get().getNombres()).isEqualTo("JUAN");
    }

    @Test
    void shouldCheckExistsByNumeroDocumento() {
        assertThat(personaRepository.existsByNumeroDocumento("12345678")).isTrue();
        assertThat(personaRepository.existsByNumeroDocumento("99999999")).isFalse();
    }

    @Test
    void shouldRejectDuplicateNumeroDocumento() {
        var entity = new Persona();
        entity.setTipoDocumentoIdentidad(tdiDni);
        entity.setNumeroDocumento("12345678"); // duplicate
        entity.setNombres("OTRO");
        entity.setApellidoPaterno("PEREZ");
        entity.setActivo(true);

        assertThatThrownBy(() -> personaRepository.saveAndFlush(entity))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindByNombresContainingIgnoreCase() {
        var result = personaRepository.findByNombresContainingIgnoreCase("juan");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApellidoPaterno()).isEqualTo("PEREZ");
    }

    @Test
    void shouldFindByApellidoPaternoContainingIgnoreCase() {
        var result = personaRepository.findByApellidoPaternoContainingIgnoreCase("perez");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombres()).isEqualTo("JUAN");
    }

    @Test
    void shouldFindAllByActivoTrue() {
        // Add an inactive persona
        var inactive = new Persona();
        inactive.setTipoDocumentoIdentidad(tdiDni);
        inactive.setNumeroDocumento("99999999");
        inactive.setNombres("INACTIVO");
        inactive.setApellidoPaterno("TEST");
        inactive.setActivo(false);
        personaRepository.saveAndFlush(inactive);

        var result = personaRepository.findAllByActivoTrue();
        assertThat(result).hasSize(1); // only the active one from setUp
        assertThat(result.get(0).getNumeroDocumento()).isEqualTo("12345678");
    }

    @Test
    void shouldPerformSoftDelete() {
        var entity = new Persona();
        entity.setTipoDocumentoIdentidad(tdiDni);
        entity.setNumeroDocumento("55555555");
        entity.setNombres("DELETE");
        entity.setApellidoPaterno("ME");
        entity.setActivo(true);
        var saved = personaRepository.save(entity);

        saved.markAsInactive();
        personaRepository.save(saved);

        var found = personaRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getActivo()).isFalse();
    }
}

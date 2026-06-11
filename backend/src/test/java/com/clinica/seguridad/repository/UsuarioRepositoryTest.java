package com.clinica.seguridad.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.persona.entity.Persona;
import com.clinica.seguridad.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {"spring.cache.type=none"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private com.clinica.persona.repository.PersonaRepository personaRepository;

    @Autowired
    private com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository tipoDocRepository;

    private Persona savedPersona;

    @BeforeEach
    void setUp() {
        TipoDocumentoIdentidad tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocRepository.save(tdi);

        Persona persona = new Persona();
        persona.setTipoDocumentoIdentidad(tdi);
        persona.setNumeroDocumento("12345678");
        persona.setNombres("Admin");
        persona.setApellidoPaterno("User");
        savedPersona = personaRepository.save(persona);
    }

    @Test
    void shouldSaveAndFindByUsername() {
        Usuario usuario = new Usuario();
        usuario.setPersona(savedPersona);
        usuario.setUsername("admin");
        usuario.setPasswordHash("$2a$10$hashedpassword");
        usuario = usuarioRepository.save(usuario);

        var found = usuarioRepository.findByUsername("admin");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(usuario.getId());
        assertThat(found.get().getUsername()).isEqualTo("admin");
        assertThat(found.get().getPasswordHash()).isEqualTo("$2a$10$hashedpassword");
        assertThat(found.get().getActivo()).isTrue();
    }

    @Test
    void shouldRejectDuplicateUsername() {
        Usuario usuario1 = new Usuario();
        usuario1.setPersona(savedPersona);
        usuario1.setUsername("admin");
        usuario1.setPasswordHash("pass1");
        usuarioRepository.save(usuario1);

        // Need a different persona for the second user
        TipoDocumentoIdentidad tdi = tipoDocRepository.findAll().get(0);
        Persona persona2 = new Persona();
        persona2.setTipoDocumentoIdentidad(tdi);
        persona2.setNumeroDocumento("87654321");
        persona2.setNombres("Second");
        persona2.setApellidoPaterno("User");
        persona2 = personaRepository.save(persona2);

        Usuario usuario2 = new Usuario();
        usuario2.setPersona(persona2);
        usuario2.setUsername("admin"); // Duplicate username
        usuario2.setPasswordHash("pass2");

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(usuario2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindByUsername_WhenNotFound_ShouldReturnEmpty() {
        var found = usuarioRepository.findByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckExistsByUsername() {
        Usuario usuario = new Usuario();
        usuario.setPersona(savedPersona);
        usuario.setUsername("exists-user");
        usuario.setPasswordHash("pass");
        usuarioRepository.save(usuario);

        assertThat(usuarioRepository.existsByUsername("exists-user")).isTrue();
        assertThat(usuarioRepository.existsByUsername("unknown")).isFalse();
    }

    @Test
    void shouldEnforceForeignKeyConstraint() {
        Usuario usuario = new Usuario();
        usuario.setPersona(savedPersona);
        usuario.setUsername("valid-user");
        usuario.setPasswordHash("pass");
        usuarioRepository.save(usuario);

        // Verify we can read it back with the persona relationship
        var found = usuarioRepository.findByUsername("valid-user");
        assertThat(found).isPresent();
        assertThat(found.get().getPersona()).isNotNull();
        assertThat(found.get().getPersona().getNombres()).isEqualTo("Admin");
    }
}

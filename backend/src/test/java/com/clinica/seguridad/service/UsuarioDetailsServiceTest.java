package com.clinica.seguridad.service;

import com.clinica.seguridad.entity.Permiso;
import com.clinica.seguridad.entity.Rol;
import com.clinica.seguridad.entity.RolPermiso;
import com.clinica.seguridad.entity.RolPermisoId;
import com.clinica.seguridad.entity.Usuario;
import com.clinica.seguridad.entity.UsuarioRol;
import com.clinica.seguridad.entity.UsuarioRolId;
import com.clinica.seguridad.repository.RolPermisoRepository;
import com.clinica.seguridad.repository.UsuarioRepository;
import com.clinica.seguridad.repository.UsuarioRolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioRolRepository usuarioRolRepository;

    @Mock
    private RolPermisoRepository rolPermisoRepository;

    @InjectMocks
    private UsuarioDetailsService service;

    @Test
    void loadByUsername_ShouldReturnUserDetailsWithRolesAndPermisos() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setPasswordHash("hashed-pass");
        usuario.setActivo(true);

        Rol rolAdmin = new Rol();
        rolAdmin.setId(10L);
        rolAdmin.setCodigo("ADMIN");

        Rol rolMedico = new Rol();
        rolMedico.setId(20L);
        rolMedico.setCodigo("MEDICO");

        Permiso permisoRead = new Permiso();
        permisoRead.setId(100L);
        permisoRead.setCodigo("maestro:read");

        Permiso permisoWrite = new Permiso();
        permisoWrite.setId(200L);
        permisoWrite.setCodigo("seguridad:write");

        UsuarioRol ur1 = new UsuarioRol(new UsuarioRolId(1L, 10L), usuario, rolAdmin);
        UsuarioRol ur2 = new UsuarioRol(new UsuarioRolId(1L, 20L), usuario, rolMedico);

        RolPermiso rp1 = new RolPermiso(new RolPermisoId(10L, 100L), rolAdmin, permisoRead);
        RolPermiso rp2 = new RolPermiso(new RolPermisoId(10L, 200L), rolAdmin, permisoWrite);
        RolPermiso rp3 = new RolPermiso(new RolPermisoId(20L, 100L), rolMedico, permisoRead);

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(usuarioRolRepository.findByUsuarioId(1L)).thenReturn(List.of(ur1, ur2));
        when(rolPermisoRepository.findByRolId(10L)).thenReturn(List.of(rp1, rp2));
        when(rolPermisoRepository.findByRolId(20L)).thenReturn(List.of(rp3));

        // When
        UserDetails userDetails = service.loadUserByUsername("admin");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getPassword()).isEqualTo("hashed-pass");
        assertThat(userDetails.isEnabled()).isTrue();

        // Authorities should include: ROLE_ADMIN, ROLE_MEDICO, maestro:read, seguridad:write
        // (maestro:read appears twice from both roles but Set dedupes)
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(authorities).containsExactlyInAnyOrder(
                "ROLE_ADMIN", "ROLE_MEDICO",
                "maestro:read", "seguridad:write");
    }

    @Test
    void loadByUsername_WhenUserNotFound_ShouldThrowException() {
        when(usuarioRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void loadByUsername_WithSingleRoleAndNoPermisos_ShouldReturnRoleOnly() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setUsername("viewer");
        usuario.setPasswordHash("pass");
        usuario.setActivo(true);

        Rol rolViewer = new Rol();
        rolViewer.setId(30L);
        rolViewer.setCodigo("VIEWER");

        UsuarioRol ur = new UsuarioRol(new UsuarioRolId(2L, 30L), usuario, rolViewer);

        when(usuarioRepository.findByUsername("viewer")).thenReturn(Optional.of(usuario));
        when(usuarioRolRepository.findByUsuarioId(2L)).thenReturn(List.of(ur));
        when(rolPermisoRepository.findByRolId(30L)).thenReturn(List.of());

        UserDetails userDetails = service.loadUserByUsername("viewer");

        assertThat(userDetails.getAuthorities())
                .hasSize(1)
                .anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER"));
    }

    @Test
    void loadByUsername_WhenUserIsInactive_ShouldReturnDisabled() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setUsername("disabled");
        usuario.setPasswordHash("pass");
        usuario.setActivo(false);

        when(usuarioRepository.findByUsername("disabled")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = service.loadUserByUsername("disabled");

        assertThat(userDetails.isEnabled()).isFalse();
    }
}

package com.clinica.seguridad.service;

import com.clinica.seguridad.dto.RolResponse;
import com.clinica.seguridad.entity.Permiso;
import com.clinica.seguridad.entity.Rol;
import com.clinica.seguridad.entity.RolPermiso;
import com.clinica.seguridad.entity.RolPermisoId;
import com.clinica.seguridad.repository.PermisoRepository;
import com.clinica.seguridad.repository.RolPermisoRepository;
import com.clinica.seguridad.repository.RolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private RolPermisoRepository rolPermisoRepository;

    @Mock
    private PermisoRepository permisoRepository;

    @InjectMocks
    private RolService rolService;

    private Rol createRol(Long id, String codigo, String nombre, String descripcion) {
        Rol rol = new Rol();
        rol.setId(id);
        rol.setCodigo(codigo);
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol.setActivo(true);
        return rol;
    }

    private Permiso createPermiso(Long id, String codigo, String nombre, String modulo) {
        Permiso permiso = new Permiso();
        permiso.setId(id);
        permiso.setCodigo(codigo);
        permiso.setNombre(nombre);
        permiso.setModulo(modulo);
        permiso.setActivo(true);
        return permiso;
    }

    @Test
    void findAll_ShouldReturnAllRoles() {
        Rol rol = createRol(1L, "ADMIN", "Administrador", "Full access");
        when(rolRepository.findAll()).thenReturn(List.of(rol));
        when(rolPermisoRepository.findByRolId(1L)).thenReturn(List.of());

        List<RolResponse> roles = rolService.findAll();

        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).codigo()).isEqualTo("ADMIN");
    }

    @Test
    void findById_ShouldReturnRole() {
        Rol rol = createRol(1L, "ADMIN", "Administrador", "Full access");
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(rolPermisoRepository.findByRolId(1L)).thenReturn(List.of());

        RolResponse response = rolService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.codigo()).isEqualTo("ADMIN");
        assertThat(response.nombre()).isEqualTo("Administrador");
    }

    @Test
    void findById_WhenNotFound_ShouldThrowException() {
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveAndReturnNewRole() {
        when(rolRepository.existsByCodigo("NUEVO")).thenReturn(false);
        Rol saved = createRol(1L, "NUEVO", "Nuevo Rol", "Desc");
        when(rolRepository.save(any(Rol.class))).thenReturn(saved);

        RolResponse response = rolService.create("NUEVO", "Nuevo Rol", "Desc");

        assertThat(response.codigo()).isEqualTo("NUEVO");
        assertThat(response.permisos()).isEmpty();
        verify(rolRepository).save(any(Rol.class));
    }

    @Test
    void create_WithDuplicateCodigo_ShouldThrowException() {
        when(rolRepository.existsByCodigo("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> rolService.create("ADMIN", "Admin", "Desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ADMIN");
        verify(rolRepository, never()).save(any());
    }

    @Test
    void update_ShouldModifyExistingRole() {
        Rol existing = createRol(1L, "OLD", "Old Name", "Old Desc");
        when(rolRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(rolRepository.existsByCodigo("NEW")).thenReturn(false);
        when(rolRepository.save(any(Rol.class))).thenAnswer(i -> i.getArgument(0));
        when(rolPermisoRepository.findByRolId(1L)).thenReturn(List.of());

        RolResponse response = rolService.update(1L, "NEW", "New Name", "New Desc");

        assertThat(response.codigo()).isEqualTo("NEW");
        assertThat(response.nombre()).isEqualTo("New Name");
    }

    @Test
    void update_WhenNotFound_ShouldThrowException() {
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolService.update(99L, "ANY", "Any", "Any"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_ShouldSoftDelete() {
        Rol existing = createRol(1L, "ADMIN", "Admin", "Desc");
        when(rolRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(rolRepository.save(any(Rol.class))).thenAnswer(i -> i.getArgument(0));

        rolService.delete(1L);

        assertThat(existing.getActivo()).isFalse();
        verify(rolRepository).save(existing);
    }

    @Test
    void assignPermisos_ShouldReplaceExistingAssignments() {
        Rol rol = createRol(1L, "ADMIN", "Admin", "Desc");
        Permiso permiso = createPermiso(100L, "maestro:read", "Leer", "maestro");

        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(rolPermisoRepository.findByRolId(1L))
                .thenReturn(List.of(new RolPermiso(new RolPermisoId(1L, 99L), rol, new Permiso())))
                .thenReturn(List.of(new RolPermiso(new RolPermisoId(1L, 100L), rol, permiso)));
        when(permisoRepository.findById(100L)).thenReturn(Optional.of(permiso));

        RolResponse response = rolService.assignPermisos(1L, List.of(100L));

        assertThat(response).isNotNull();
        verify(rolPermisoRepository).deleteAll(any());
        verify(rolPermisoRepository).save(any(RolPermiso.class));
    }

    @Test
    void assignPermisos_WithEmptyList_ShouldRemoveAllAndNotAdd() {
        Rol rol = createRol(1L, "ADMIN", "Admin", "Desc");
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(rolPermisoRepository.findByRolId(1L)).thenReturn(List.of());

        RolResponse response = rolService.assignPermisos(1L, List.of());

        assertThat(response).isNotNull();
        verify(rolPermisoRepository).deleteAll(any());
        verify(permisoRepository, never()).findById(any());
    }

    @Test
    void assignPermisos_WithNonExistentPermiso_ShouldThrowException() {
        Rol rol = createRol(1L, "ADMIN", "Admin", "Desc");
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(rolPermisoRepository.findByRolId(1L)).thenReturn(List.of());
        when(permisoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolService.assignPermisos(1L, List.of(999L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void assignPermisos_IsIdempotent_WhenAssigningSamePermisosTwice() {
        Rol rol = createRol(1L, "ADMIN", "Admin", "Desc");
        Permiso permiso = createPermiso(100L, "maestro:read", "Leer", "maestro");

        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(permisoRepository.findById(100L)).thenReturn(Optional.of(permiso));

        // First call — no existing assignments
        when(rolPermisoRepository.findByRolId(1L))
                .thenReturn(List.of())
                .thenReturn(List.of(new RolPermiso(new RolPermisoId(1L, 100L), rol, permiso)));

        RolResponse firstCall = rolService.assignPermisos(1L, List.of(100L));
        assertThat(firstCall).isNotNull();

        // Second call — one existing assignment
        when(rolPermisoRepository.findByRolId(1L))
                .thenReturn(List.of(new RolPermiso(new RolPermisoId(1L, 100L), rol, permiso)))
                .thenReturn(List.of(new RolPermiso(new RolPermisoId(1L, 100L), rol, permiso)));

        RolResponse secondCall = rolService.assignPermisos(1L, List.of(100L));
        assertThat(secondCall).isNotNull();

        verify(rolPermisoRepository, times(2)).deleteAll(any());
        verify(rolPermisoRepository, times(2)).save(any(RolPermiso.class));
    }
}

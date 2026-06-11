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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RolService {

    private static final Logger log = LoggerFactory.getLogger(RolService.class);

    private final RolRepository rolRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final PermisoRepository permisoRepository;

    public RolService(RolRepository rolRepository,
                      RolPermisoRepository rolPermisoRepository,
                      PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.rolPermisoRepository = rolPermisoRepository;
        this.permisoRepository = permisoRepository;
    }

    @Transactional(readOnly = true)
    public List<RolResponse> findAll() {
        return rolRepository.findAll().stream()
            .map(rol -> RolResponse.fromEntity(rol, getPermisoCodigos(rol.getId())))
            .toList();
    }

    @Transactional(readOnly = true)
    public RolResponse findById(Long id) {
        Rol rol = rolRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));
        return RolResponse.fromEntity(rol, getPermisoCodigos(rol.getId()));
    }

    public RolResponse create(String codigo, String nombre, String descripcion) {
        if (rolRepository.existsByCodigo(codigo)) {
            throw new IllegalArgumentException("Ya existe un rol con el código: " + codigo);
        }
        Rol rol = new Rol();
        rol.setCodigo(codigo);
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol = rolRepository.save(rol);
        log.debug("Rol created with id: {} and codigo: {}", rol.getId(), codigo);
        return RolResponse.fromEntity(rol, List.of());
    }

    public RolResponse update(Long id, String codigo, String nombre, String descripcion) {
        Rol rol = rolRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));

        if (!rol.getCodigo().equals(codigo) && rolRepository.existsByCodigo(codigo)) {
            throw new IllegalArgumentException("Ya existe un rol con el código: " + codigo);
        }
        rol.setCodigo(codigo);
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol = rolRepository.save(rol);
        log.debug("Rol updated with id: {}", rol.getId());
        return RolResponse.fromEntity(rol, getPermisoCodigos(rol.getId()));
    }

    public void delete(Long id) {
        Rol rol = rolRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));
        rol.markAsInactive();
        rolRepository.save(rol);
        log.debug("Rol soft-deleted with id: {}", id);
    }

    /**
     * Replaces all permission assignments for a role with the given permission IDs.
     */
    public RolResponse assignPermisos(Long rolId, List<Long> permisoIds) {
        Rol rol = rolRepository.findById(rolId)
            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + rolId));

        // Remove existing assignments
        List<RolPermiso> existing = rolPermisoRepository.findByRolId(rolId);
        rolPermisoRepository.deleteAll(existing);

        // Add new assignments
        if (permisoIds != null) {
            for (Long permisoId : permisoIds) {
                Permiso permiso = permisoRepository.findById(permisoId)
                    .orElseThrow(() -> new EntityNotFoundException("Permiso no encontrado con id: " + permisoId));
                RolPermiso rp = new RolPermiso(new RolPermisoId(rolId, permisoId), rol, permiso);
                rolPermisoRepository.save(rp);
            }
        }

        log.debug("Permisos assigned to rol {}: {} permisos", rolId,
            permisoIds != null ? permisoIds.size() : 0);
        return RolResponse.fromEntity(rol, getPermisoCodigos(rolId));
    }

    /**
     * Returns the list of permission codes assigned to a role.
     */
    private List<String> getPermisoCodigos(Long rolId) {
        return rolPermisoRepository.findByRolId(rolId).stream()
            .map(rp -> rp.getPermiso().getCodigo())
            .toList();
    }
}

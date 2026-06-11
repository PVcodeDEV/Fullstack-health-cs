package com.clinica.seguridad.service;

import com.clinica.seguridad.entity.Rol;
import com.clinica.seguridad.entity.RolPermiso;
import com.clinica.seguridad.entity.Usuario;
import com.clinica.seguridad.entity.UsuarioRol;
import com.clinica.seguridad.repository.RolPermisoRepository;
import com.clinica.seguridad.repository.UsuarioRepository;
import com.clinica.seguridad.repository.UsuarioRolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioDetailsService.class);

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolPermisoRepository rolPermisoRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository,
                                  UsuarioRolRepository usuarioRolRepository,
                                  RolPermisoRepository rolPermisoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.rolPermisoRepository = rolPermisoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Load roles for this user
        List<UsuarioRol> usuarioRoles = usuarioRolRepository.findByUsuarioId(usuario.getId());
        for (UsuarioRol ur : usuarioRoles) {
            Rol rol = ur.getRol();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getCodigo()));

            // Load permissions assigned to this role
            List<RolPermiso> rolPermisos = rolPermisoRepository.findByRolId(rol.getId());
            for (RolPermiso rp : rolPermisos) {
                authorities.add(new SimpleGrantedAuthority(rp.getPermiso().getCodigo()));
            }
        }

        log.debug("User '{}' loaded with {} authorities", username, authorities.size());
        return new UsuarioPrincipal(usuario, authorities);
    }
}

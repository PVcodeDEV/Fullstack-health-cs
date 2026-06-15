package com.clinica.seguridad.bootstrap;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.seguridad.entity.Permiso;
import com.clinica.seguridad.entity.Rol;
import com.clinica.seguridad.entity.RolPermiso;
import com.clinica.seguridad.entity.RolPermisoId;
import com.clinica.seguridad.entity.Usuario;
import com.clinica.seguridad.entity.UsuarioRol;
import com.clinica.seguridad.entity.UsuarioRolId;
import com.clinica.seguridad.repository.PermisoRepository;
import com.clinica.seguridad.repository.RolPermisoRepository;
import com.clinica.seguridad.repository.RolRepository;
import com.clinica.seguridad.repository.UsuarioRepository;
import com.clinica.seguridad.repository.UsuarioRolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String ADMIN_PERSONA_DOCUMENTO = "72852927";

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PersonaRepository personaRepository;
    private final TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME:}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public DataInitializer(RolRepository rolRepository,
                           PermisoRepository permisoRepository,
                           RolPermisoRepository rolPermisoRepository,
                           UsuarioRepository usuarioRepository,
                           UsuarioRolRepository usuarioRolRepository,
                           PersonaRepository personaRepository,
                           TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository,
                           PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.rolPermisoRepository = rolPermisoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.personaRepository = personaRepository;
        this.tipoDocumentoIdentidadRepository = tipoDocumentoIdentidadRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (rolRepository.count() > 0) {
            log.info("Roles already seeded — skipping data initialization");
            return;
        }

        log.info("Starting data initialization...");

        // 1. Seed roles
        seedRoles();

        // 2. Seed permisos
        seedPermisos();

        // 3. Assign all permisos to ADMIN role
        assignAllPermisosToAdmin();

        // 4. Assign caja and entidad permisos to CAJA role
        assignCajaPermisos();

        // 5. Assign GERENCIA permisos
        assignGerenciaPermisos();

        // 6. Assign RECEPCION permisos
        assignRecepcionPermisos();

        // 7. Assign MEDICO permisos
        assignMedicoPermisos();

        // 8. Create bootstrap admin user if env vars are set
        seedAdminUser();
    }

    private void seedRoles() {
        List<SeedRole> roles = List.of(
            new SeedRole("ADMIN", "Administrador", "Acceso total al sistema"),
            new SeedRole("GERENCIA", "Gerencia", "Acceso a reportes, aprobaciones y supervisión"),
            new SeedRole("MEDICO", "Médico", "Acceso a módulos clínicos y pacientes"),
            new SeedRole("ENFERMERIA", "Enfermería", "Acceso a módulos de enfermería y pacientes"),
            new SeedRole("RECEPCION", "Recepción", "Acceso a admisión y registro de pacientes"),
            new SeedRole("FARMACIA", "Farmacia", "Acceso al módulo de farmacia e inventario"),
            new SeedRole("CAJA", "Caja", "Acceso al módulo de caja y facturación"),
            new SeedRole("CONTABILIDAD", "Contabilidad", "Acceso al módulo de contabilidad")
        );

        for (SeedRole sr : roles) {
            if (!rolRepository.existsByCodigo(sr.codigo())) {
                Rol rol = new Rol();
                rol.setCodigo(sr.codigo());
                rol.setNombre(sr.nombre());
                rol.setDescripcion(sr.descripcion());
                rolRepository.save(rol);
                log.debug("Rol seeded: {}", sr.codigo());
            }
        }
        log.info("Roles seeded successfully");
    }

    private void seedPermisos() {
        List<SeedPermiso> permisos = List.of(
            new SeedPermiso("persona:*", "Acceso total a Personas", "persona",
                "Permiso total para el módulo Personas"),
            new SeedPermiso("paciente:*", "Acceso total a Pacientes", "paciente",
                "Permiso total para el módulo Pacientes"),
            new SeedPermiso("trabajador:*", "Acceso total a Trabajadores", "trabajador",
                "Permiso total para el módulo Trabajadores"),
            new SeedPermiso("medico:*", "Acceso total a Médicos", "medico",
                "Permiso total para el módulo Médicos"),
            new SeedPermiso("usuario:*", "Acceso total a Usuarios", "usuario",
                "Permiso total para el módulo Usuarios"),
            new SeedPermiso("rol:*", "Acceso total a Roles", "rol",
                "Permiso total para el módulo Roles"),
            new SeedPermiso("permiso:*", "Acceso total a Permisos", "permiso",
                "Permiso total para el módulo Permisos"),
            new SeedPermiso("configuracion:*", "Acceso total a Configuración", "configuracion",
                "Permiso total para el módulo Configuración"),

            // Cama
            new SeedPermiso("cama:editar", "Editar camas y habitaciones", "cama",
                "Edición de camas y habitaciones"),
            new SeedPermiso("cama:ver", "Ver camas y habitaciones", "cama",
                "Visualización de camas y habitaciones"),

            // Admisión
            new SeedPermiso("admision:editar", "Editar admisiones", "admision",
                "Edición de admisiones"),
            new SeedPermiso("admision:ver", "Ver admisiones", "admision",
                "Visualización de admisiones"),

            // Hospitalización
            new SeedPermiso("hospitalizacion:editar", "Editar hospitalizaciones", "hospitalizacion",
                "Edición de hospitalizaciones"),
            new SeedPermiso("hospitalizacion:ver", "Ver hospitalizaciones", "hospitalizacion",
                "Visualización de hospitalizaciones"),

            // SOP (Reportes Quirúrgicos)
            new SeedPermiso("sop:editar", "Editar reportes quirúrgicos", "sop",
                "Edición de reportes quirúrgicos"),
            new SeedPermiso("sop:ver", "Ver reportes quirúrgicos", "sop",
                "Visualización de reportes quirúrgicos"),

            // HCE (Historia Clínica Electrónica)
            new SeedPermiso("hce:editar", "Editar documentos clínicos", "hce",
                "Edición de documentos clínicos"),
            new SeedPermiso("hce:ver", "Ver documentos clínicos", "hce",
                "Visualización de documentos clínicos"),

            // Cuenta (cargos y cobros)
            new SeedPermiso("cuenta:editar", "Editar cargos en cuenta", "cuenta",
                "Edición de cargos"),
            new SeedPermiso("cuenta:ver", "Ver cuentas y cargos", "cuenta",
                "Visualización de cuentas y cargos"),

            // RRHH
            new SeedPermiso("rrhh:ver", "Ver RRHH", "rrhh",
                "Visualización de datos de RRHH"),
            new SeedPermiso("rrhh:editar", "Editar RRHH", "rrhh",
                "Creación y edición de datos de RRHH"),
            new SeedPermiso("rrhh:contrato:gestionar", "Gestionar contratos", "rrhh",
                "Suspender, reactivar y resolver contratos"),
            new SeedPermiso("rrhh:derechohabiente:gestionar", "Gestionar derechohabientes", "rrhh",
                "Gestión de derechohabientes"),

            // Entidad (empresas, SUNAT consult)
            new SeedPermiso("entidad:crear", "Crear empresas", "entidad",
                "Creación de empresas/entidades"),
            new SeedPermiso("entidad:ver", "Ver empresas", "entidad",
                "Visualización de empresas/entidades"),
            new SeedPermiso("entidad:editar", "Editar empresas", "entidad",
                "Edición de empresas/entidades"),
            new SeedPermiso("entidad:consultar-sunat", "Consultar SUNAT RUC", "entidad",
                "Consulta de RUC en SUNAT"),

            // Caja
            new SeedPermiso("caja:crear", "Crear en caja", "caja",
                "Creación de sesiones, tarifarios, comprobantes"),
            new SeedPermiso("caja:ver", "Ver caja", "caja",
                "Visualización de módulo caja"),
            new SeedPermiso("caja:editar", "Editar caja", "caja",
                "Edición de registros de caja"),
            new SeedPermiso("caja:aprobar", "Aprobar operaciones de caja", "caja",
                "Aprobación de descuentos y operaciones"),
            new SeedPermiso("caja:anular", "Anular comprobantes", "caja",
                "Anulación de comprobantes vía Nota de Crédito")
        );

        for (SeedPermiso sp : permisos) {
            if (permisoRepository.findByCodigo(sp.codigo()).isEmpty()) {
                Permiso permiso = new Permiso();
                permiso.setCodigo(sp.codigo());
                permiso.setNombre(sp.nombre());
                permiso.setModulo(sp.modulo());
                permiso.setDescripcion(sp.descripcion());
                permisoRepository.save(permiso);
                log.debug("Permiso seeded: {}", sp.codigo());
            }
        }
        log.info("Permisos seeded successfully");
    }

    private void assignAllPermisosToAdmin() {
        Rol admin = rolRepository.findByCodigo("ADMIN")
            .orElseThrow(() -> new IllegalStateException("ADMIN role not found after seeding"));

        List<Permiso> allPermisos = permisoRepository.findAll();
        for (Permiso permiso : allPermisos) {
            RolPermisoId id = new RolPermisoId(admin.getId(), permiso.getId());
            if (rolPermisoRepository.findById(id).isEmpty()) {
                rolPermisoRepository.save(new RolPermiso(id, admin, permiso));
            }
        }
        log.info("All {} permisos assigned to ADMIN role", allPermisos.size());
    }

    private void assignCajaPermisos() {
        Rol caja = rolRepository.findByCodigo("CAJA")
            .orElseThrow(() -> new IllegalStateException("CAJA role not found after seeding"));

        List<String> cajaPermisoCodigos = List.of(
            "caja:crear", "caja:ver", "caja:editar", "caja:aprobar", "caja:anular",
            "entidad:crear", "entidad:ver", "entidad:editar", "entidad:consultar-sunat"
        );

        int assigned = 0;
        for (String codigo : cajaPermisoCodigos) {
            var permisoOpt = permisoRepository.findByCodigo(codigo);
            if (permisoOpt.isPresent()) {
                Permiso permiso = permisoOpt.get();
                RolPermisoId id = new RolPermisoId(caja.getId(), permiso.getId());
                if (rolPermisoRepository.findById(id).isEmpty()) {
                    rolPermisoRepository.save(new RolPermiso(id, caja, permiso));
                    assigned++;
                }
            }
        }
        log.info("{} caja/entidad permisos assigned to CAJA role", assigned);
    }

    private void assignGerenciaPermisos() {
        Rol gerencia = rolRepository.findByCodigo("GERENCIA")
            .orElseThrow(() -> new IllegalStateException("GERENCIA role not found after seeding"));

        // GERENCIA: approve discounts, view caja operations, view entities
        List<String> gerenciaPermisoCodigos = List.of(
            "caja:aprobar", "caja:ver", "caja:editar",
            "entidad:ver", "entidad:editar",
            "cuenta:ver"
        );

        int assigned = 0;
        for (String codigo : gerenciaPermisoCodigos) {
            var permisoOpt = permisoRepository.findByCodigo(codigo);
            if (permisoOpt.isPresent()) {
                Permiso permiso = permisoOpt.get();
                RolPermisoId id = new RolPermisoId(gerencia.getId(), permiso.getId());
                if (rolPermisoRepository.findById(id).isEmpty()) {
                    rolPermisoRepository.save(new RolPermiso(id, gerencia, permiso));
                    assigned++;
                }
            }
        }
        log.info("{} permisos assigned to GERENCIA role", assigned);
    }

    private void assignRecepcionPermisos() {
        Rol recepcion = rolRepository.findByCodigo("RECEPCION")
            .orElseThrow(() -> new IllegalStateException("RECEPCION role not found after seeding"));

        // RECEPCION: read empresas, read sesion caja, read pre-liquidación
        List<String> recepcionPermisoCodigos = List.of(
            "entidad:ver",
            "caja:ver",
            "cuenta:ver",
            "admision:ver", "admision:editar"
        );

        int assigned = 0;
        for (String codigo : recepcionPermisoCodigos) {
            var permisoOpt = permisoRepository.findByCodigo(codigo);
            if (permisoOpt.isPresent()) {
                Permiso permiso = permisoOpt.get();
                RolPermisoId id = new RolPermisoId(recepcion.getId(), permiso.getId());
                if (rolPermisoRepository.findById(id).isEmpty()) {
                    rolPermisoRepository.save(new RolPermiso(id, recepcion, permiso));
                    assigned++;
                }
            }
        }
        log.info("{} permisos assigned to RECEPCION role", assigned);
    }

    private void assignMedicoPermisos() {
        Rol medico = rolRepository.findByCodigo("MEDICO")
            .orElseThrow(() -> new IllegalStateException("MEDICO role not found after seeding"));

        // MEDICO: read pre-liquidación (LIQ-007-1), clinical data access
        List<String> medicoPermisoCodigos = List.of(
            "caja:ver",  // for pre-liquidación preview
            "cuenta:ver",
            "admision:ver",
            "hospitalizacion:ver",
            "sop:ver",
            "hce:ver", "hce:editar"
        );

        int assigned = 0;
        for (String codigo : medicoPermisoCodigos) {
            var permisoOpt = permisoRepository.findByCodigo(codigo);
            if (permisoOpt.isPresent()) {
                Permiso permiso = permisoOpt.get();
                RolPermisoId id = new RolPermisoId(medico.getId(), permiso.getId());
                if (rolPermisoRepository.findById(id).isEmpty()) {
                    rolPermisoRepository.save(new RolPermiso(id, medico, permiso));
                    assigned++;
                }
            }
        }
        log.info("{} permisos assigned to MEDICO role", assigned);
    }

    private void seedAdminUser() {
        if (adminUsername == null || adminUsername.isBlank()
            || adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_USERNAME/ADMIN_PASSWORD not set — skipping admin user creation");
            return;
        }

        // Idempotent: if user already exists, skip
        if (usuarioRepository.findByUsername(adminUsername).isPresent()) {
            log.info("Admin user '{}' already exists — skipping", adminUsername);
            return;
        }

        // Find or create the admin Persona
        Persona adminPersona = personaRepository.findByNumeroDocumento(ADMIN_PERSONA_DOCUMENTO)
            .orElseGet(this::createAdminPersona);

        // Create the Usuario
        Usuario usuario = new Usuario();
        usuario.setPersona(adminPersona);
        usuario.setUsername(adminUsername);
        usuario.setPasswordHash(passwordEncoder.encode(adminPassword));
        usuario = usuarioRepository.save(usuario);
        log.debug("Admin user created: {}", adminUsername);

        // Assign ADMIN role
        Rol adminRole = rolRepository.findByCodigo("ADMIN")
            .orElseThrow(() -> new IllegalStateException("ADMIN role not found after seeding"));
        UsuarioRolId usuarioRolId = new UsuarioRolId(usuario.getId(), adminRole.getId());
        usuarioRolRepository.save(new UsuarioRol(usuarioRolId, usuario, adminRole));
        log.info("Admin user '{}' created with ADMIN role", adminUsername);
    }

    private Persona createAdminPersona() {
        TipoDocumentoIdentidad dni = tipoDocumentoIdentidadRepository.findByCodigoSunat("01")
            .orElseThrow(() -> new IllegalStateException(
                "TipoDocumentoIdentidad with codigoSunat '01' (DNI) not found. " +
                "Ensure V1 migration has been applied."));

        Persona persona = new Persona();
        persona.setTipoDocumentoIdentidad(dni);
        persona.setNumeroDocumento(ADMIN_PERSONA_DOCUMENTO);
        persona.setNombres("Administrador");
        persona.setApellidoPaterno("Sistema");
        persona.setEmail("admin@clinica.com");
        return personaRepository.save(persona);
    }

    private record SeedRole(String codigo, String nombre, String descripcion) {}
    private record SeedPermiso(String codigo, String nombre, String modulo, String descripcion) {}
}

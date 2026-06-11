# 🏥 ERP Clínico — Fullstack Health CS

Sistema de gestión clínica modular (ERP) para clínicas y centros de salud en Perú.
Desarrollado con arquitectura de monolito modular, diseñado para escalar por dominio.

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Java 25 + Spring Boot 4.0.0 + Maven |
| Frontend | Thymeleaf + HTMX + Tailwind CSS (vía CLI con Bun) |
| Base de datos | PostgreSQL 18 (prod) / H2 (dev) |
| Seguridad | JWT + Spring Security + roles y permisos |
| Migraciones | Flyway |
| Tests | JUnit 5 + Mockito + Spring Boot Test |

## Módulos

El proyecto está organizado como monolito modular con `package-per-module` bajo `com.clinica`:

| Módulo | Package | Propósito |
|--------|---------|-----------|
| `maestro` | `com.clinica.maestro` | Catálogos maestros (ubigeo, CIE-11, documentos, financieros, etc.) |
| `seguridad` | `com.clinica.seguridad` | Autenticación, usuarios, roles, permisos, JWT |
| `persona` | `com.clinica.persona` | Personas (pacientes, trabajadores, médicos) + validación de DNI/SUNAT/RENIEC |
| `clinica` | `com.clinica.clinica` | Admisión, hospitalización, HCE (historia clínica), SOP (quirófano), cuenta |
| `farmacia` | `com.clinica.farmacia` | POS de farmacia, stock, lotes, descuentos, transferencias, reposición |
| `caja` | `com.clinica.caja` | Caja, facturación, comprobantes |
| `rrhh` | `com.clinica.rrhh` | RRHH, planilla, CTS, gratificaciones, vacaciones, PLAME, contratos |

## Arquitectura

Cada módulo sigue la misma convención en capas:

```
com.clinica.<modulo>/
├── entity/       → JPA entities
├── repository/   → Spring Data repos
├── service/      → Lógica de negocio
├── dto/          → Request/Response
├── controller/   → REST endpoints
└── type/         → Enums compartidos
```

### Principios

- **Monolito modular**: Cada módulo es independiente pero se despliega como un solo artefacto.
- **OpenSpec / SDD**: Los cambios se planifican con Spec-Driven Development (ver `doc/openspec/`).
- **Modularidad por dominio**: No hay dependencias cíclicas entre módulos.
- **API REST + Thymeleaf**: HTMX para interactividad sin SPA.
- **RFC 9457**: Errores como Problem Detail.

## Requisitos

- Java 25+
- Maven 3.9+
- PostgreSQL 18 (o H2 para desarrollo)
- Bun (para build de Tailwind CSS)
- Git + SSH key (para GitHub)

## Inicio Rápido

### 1. Base de datos

```bash
# Crear base de datos PostgreSQL
createdb csuarezdb

# O con psql
psql -U postgres -c "CREATE DATABASE csuarezdb;"
```

### 2. Backend

```bash
cd backend

# Desarrollo con H2 (default)
mvn spring-boot:run

# Desarrollo con PostgreSQL
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Build
mvn clean package

# Tests
mvn test
```

Las migraciones Flyway se ejecutan automáticamente al iniciar la aplicación.

### 3. Frontend (Tailwind CSS)

```bash
cd frontend
bun install
bun run build   # Compila CSS a backend/src/main/resources/static/css/output.css
bun run watch   # Modo watch
```

### 4. Acceso

- App: http://localhost:8080
- Perfil `dev`: H2 console en http://localhost:8080/h2-console

## Configuración

Ver `backend/src/main/resources/application.yml`:

| Variable | Default | Descripción |
|----------|---------|-------------|
| `DB_PASSWORD` | `csosi` (dev) | Password PostgreSQL |
| `JWT_SECRET` | Requerido | Secreto para firmar JWT |
| `JWT_EXPIRATION_MS` | `3600000` | Expiración del token (1h) |
| `RENIEC_SECURE_TOKEN` | Opcional | Token para API segura RENIEC |

## Cumplimiento Legal

- **Ley 29733** — Ley de Protección de Datos Personales (Perú)
- Los campos PII están identificados en las entidades
- Datos de salud protegidos según normativa

## Convenciones

- Commits en formato conventional commits (`feat:`, `fix:`, `chore:`, etc.)
- Siempre ejecutar `mvn compile` antes de commitear
- Los cambios nuevos se inician con SDD: `/sdd-new` en el orquestador
- No usar npm — usar Bun para frontend

## Licencia

Uso interno — clínica privada.

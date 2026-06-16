# Design: Frontend Portales

## Change
frontend-portales

## Technical Approach

### 1. Template Directory Structure

```
backend/src/main/resources/templates/
├── login.html                              # Standalone, no portal layout
├── layouts/
│   └── base.html                           # Shared boilerplate (head, css, footer, scripts)
├── portal-caja/
│   ├── layouts/
│   │   └── portal.html                     # Extends base, defines caja fragments
│   ├── fragments/
│   │   ├── header.html
│   │   └── sidebar.html
│   └── dashboard.html
├── portal-asistencial/
│   ├── layouts/portal.html
│   ├── fragments/header.html, sidebar.html
│   └── dashboard.html
├── portal-farmacia/
│   ├── layouts/portal.html
│   ├── fragments/header.html, sidebar.html
│   └── dashboard.html
└── portal-administrativo/
    ├── layouts/portal.html
    ├── fragments/header.html, sidebar.html
    └── dashboard.html
```

### 2. Layout Inheritance

```
base.html
├── <head> (meta, title pattern, CSS link)
├── <body>
│   ├── header (th:replace="~{portal-xxx/fragments/header}")
│   ├── sidebar (th:replace="~{portal-xxx/fragments/sidebar}")
│   ├── content (layout:fragment="content")
│   └── footer (hardcoded in base)
└── scripts (layout:fragment="scripts")
```

**base.html** structure:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title layout:title-pattern="$CONTENT_TITLE - ERP Clínico">ERP Clínico</title>
    <link rel="stylesheet" th:href="@{/css/output.css}"/>
    <style>
        :root {
            --portal-primary: #3b82f6;  /* default fallback */
            --portal-secondary: #2563eb;
            --portal-bg: #f8fafc;
            --portal-text: #1e293b;
        }
    </style>
</head>
<body class="min-h-screen flex flex-col bg-[var(--portal-bg)] text-[var(--portal-text)]">
    <!-- Header: portal provides its own -->
    <div th:replace="~{${portalHeader} :: header}"></div>
    
    <div class="flex flex-1">
        <!-- Sidebar: portal provides its own -->
        <aside th:replace="~{${portalSidebar} :: sidebar}"></aside>
        
        <!-- Main content -->
        <main class="flex-1 p-6" layout:fragment="content">
        </main>
    </div>
    
    <footer class="bg-gray-800 text-gray-300 text-center py-4 text-sm">
        <p>© 2026 ERP Clínico. Todos los derechos reservados.</p>
    </footer>
    
    <script layout:fragment="scripts"></script>
</body>
</html>
```

Each portal's layout sets the template variables:
```html
<!-- portal-caja/layouts/portal.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layouts/base}">
<head>
    <style>
        :root {
            --portal-primary: #14b8a6;
            --portal-secondary: #0d9488;
            --portal-bg: #f0fdfa;
            --portal-text: #134e4a;
        }
    </style>
</head>
<body>
    <th:block layout:fragment="content">
        <div layout:fragment="content"></div>
    </th:block>
</body>
</html>
```

**Key decision**: Use `th:with` or template variables in the controller to pass the portal's fragment paths:
```java
model.addAttribute("portalHeader", "portal-caja/fragments/header");
model.addAttribute("portalSidebar", "portal-caja/fragments/sidebar");
```

### 3. Authentication Flow

```
[Browser] → GET /login → login.html (no layout, centered card)
          → POST /login → UsernamePasswordAuthenticationFilter
              → Success: AuthenticationSuccessHandler
                  → iterate GrantedAuthority[]
                  → ROLE_ADMIN found? → redirect /administrativo
                  → asistencial:ver? → redirect /asistencial
                  → farmacia:ver? → redirect /farmacia
                  → caja:ver? → redirect /caja
                  → administrativo:ver? → redirect /administrativo
                  → none? → redirect /administrativo (fallback)
              → Failure: AuthenticationFailureHandler
                  → redirect /login?error
```

```java
@Component
public class PortalAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Map<String, String> PRIORITY = Map.of(
        "asistencial:ver", "/asistencial",
        "farmacia:ver", "/farmacia",
        "caja:ver", "/caja",
        "administrativo:ver", "/administrativo"
    );

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // Check ROLE_ADMIN first
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            getRedirectStrategy().sendRedirect(request, response, "/administrativo");
            return;
        }

        // Check portal permissions in priority order
        for (var entry : PRIORITY.entrySet()) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(entry.getKey()))) {
                getRedirectStrategy().sendRedirect(request, response, entry.getValue());
                return;
            }
        }

        // Fallback
        getRedirectStrategy().sendRedirect(request, response, "/administrativo");
    }
}
```

### 4. Controller Design

```java
@Controller
public class CajaPortalController {

    @GetMapping("/caja")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'ROLE_ADMIN')")
    public String dashboard(Model model) {
        model.addAttribute("portalHeader", "portal-caja/fragments/header");
        model.addAttribute("portalSidebar", "portal-caja/fragments/sidebar");
        // Add dashboard data
        return "portal-caja/dashboard";
    }
}
```

Same pattern for:
- `AsistencialPortalController` → `/asistencial`
- `FarmaciaPortalController` → `/farmacia`
- `AdministrativoPortalController` → `/administrativo`

### 5. Security Configuration

```java
@Bean
public SecurityFilterChain browserFilterChain(HttpSecurity http,
                                              PortalAuthenticationSuccessHandler successHandler) throws Exception {
    http
        .securityMatcher("/**")
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/css/**", "/js/**", "/webjars/**").permitAll()
            .requestMatchers("/asistencial/**", "/farmacia/**", "/caja/**",
                           "/administrativo/**").authenticated()
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .successHandler(successHandler)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutSuccessUrl("/login?logout")
            .permitAll()
        );
    return http.build();
}
```

Remove the old `.defaultSuccessUrl("/dashboard")`.

### 6. Permission Seeds (DataInitializer)

Add to the existing seed method in `DataInitializer.java`:

```java
// Portal permissions
seedPermisoIfNotExists("asistencial:ver", "Ver Portal Asistencial", moduloSeguridad);
seedPermisoIfNotExists("farmacia:ver", "Ver Portal Farmacia", moduloSeguridad);
seedPermisoIfNotExists("administrativo:ver", "Ver Portal Administrativo", moduloSeguridad);
// caja:ver already exists from modulo-caja seeding
```

The ADMIN role auto-assigns all permisos, so no extra role assignment needed.

### 7. Phase Boundaries

#### Phase 1 — Auth Foundation (apply now)
**Files to create/modify**:
| File | Action |
|------|--------|
| `templates/login.html` | CREATE |
| `templates/layouts/base.html` | CREATE |
| `seguridad/handler/PortalAuthenticationSuccessHandler.java` | CREATE |
| `seguridad/config/SecurityConfig.java` | MODIFY |
| `seguridad/bootstrap/DataInitializer.java` | MODIFY (seed 3 portal permisos) |

#### Phase 2 — Portal Caja
| File | Action |
|------|--------|
| `caja/controller/CajaPortalController.java` | CREATE |
| `templates/portal-caja/layouts/portal.html` | CREATE |
| `templates/portal-caja/fragments/header.html` | CREATE |
| `templates/portal-caja/fragments/sidebar.html` | CREATE |
| `templates/portal-caja/dashboard.html` | CREATE |
| `templates/caja/**/*.html` | MODIFY (change layout:decorate) |
| `frontend/tailwind.config.js` | MODIFY (add portal paths) |

#### Phase 3 — Portal Asistencial
| File | Action |
|------|--------|
| CREATE `AsistencialPortalController`, `portal-asistencial/*` templates | CREATE |

#### Phase 4 — Portal Farmacia
| File | Action |
|------|--------|
| CREATE `FarmaciaPortalController`, `portal-farmacia/*` templates | CREATE |

#### Phase 5 — Portal Administrativo
| File | Action |
|------|--------|
| CREATE `AdministrativoPortalController`, `portal-administrativo/*` templates | CREATE |

### 8. Key Technical Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Theming | CSS variables per portal | No Tailwind config changes, no safelist needed, dynamic |
| Layout strategy | Base + per-portal overrides | Shared boilerplate without coupling portals |
| Fragment resolution | Portal controller passes fragment paths via model | Base layout stays generic, portals define their fragments |
| Admin cross-portal nav | Conditional via `sec:authorize="hasRole('ADMIN')"` | Simple, no extra controller logic |
| Permission check | `@PreAuthorize` on portal controller methods | Consistent with existing pattern |
| Login page | Standalone, no portal layout | Login is pre-auth, shouldn't show any portal chrome |
| Portal priority in handler | Enum-ordered map | Predictable, easy to modify |

### 9. Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| Login redirect loop if handler misconfigured | Test with mock users before applying |
| Admin cannot navigate between portals | Portal switcher in base layout shown only for ADMIN |
| CSS variables not picked up by Tailwind | Verify `bun run build` includes portal template paths |
| Existing templates break | Migrate one portal at a time (Phase 2-5), test each |

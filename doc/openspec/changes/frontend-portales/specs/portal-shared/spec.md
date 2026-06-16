# Spec: Portal Shared Infrastructure

## Change
frontend-portales / Phases 1-5

## Requirements

### SHR-001: Base Layout
**Phase**: 1
**Description**: A base layout (`layouts/base.html`) that all portal layouts extend.
- Contains `<head>` with meta, CSS imports, title pattern
- Imports Tailwind CSS (`output.css`)
- Defines `<body>` structure: header region, sidebar region, content region, footer region
- Each region uses Thymeleaf `layout:fragment` or `th:replace` for portals to override
- No hardcoded navigation in base layout

### SHR-002: Portal Fragment Overrides
**Phase**: 1 (per-portal in Phase 2-5)
**Description**: Each portal provides its own fragments.
- `fragments/header.html` — portal title, branding, user info, logout button
- `fragments/sidebar.html` — portal navigation menu
- `fragments/nav.html` — (optional) horizontal nav items
- Shared across all views within the portal

### SHR-003: CSS Variable Theming
**Phase**: 1
**Description**: Each portal layout defines CSS custom properties for theming.
```
--portal-primary: #<color>     /* Main brand color */
--portal-secondary: #<color>   /* Hover/active states */
--portal-accent: #<color>      /* Accent/CTA color */
--portal-bg: #<color>          /* Background */
--portal-text: #<color>        /* Text color */
```
- Variables defined in a `<style>` block or inline in each portal's layout
- Portal colors:
  - Asistencial: Blue (#3b82f6)
  - Farmacia: Green (#22c55e)
  - Caja: Teal (#14b8a6)
  - Administrativo: Slate (#64748b)
- All templates use `var(--portal-*)` for consistency

### SHR-004: Tailwind Content Paths
**Phase**: 1
**Description**: Update `frontend/tailwind.config.js` to scan all portal template directories.
- Add `'../backend/src/main/resources/templates/portal-*/**/*.html'` to `content` array

### SHR-005: Permission Seeds
**Phase**: 1
**Description**: Seed the following portal permissions via DataInitializer.
- `asistencial:ver` — Access Portal Asistencial
- `farmacia:ver` — Access Portal Farmacia
- `caja:ver` — Access Portal Caja
- `administrativo:ver` — Access Portal Administrativo
- Assign `administrativo:ver` to existing ADMIN role (others already have `*:ver` patterns?)

### SHR-006: Admin Portal Switcher
**Phase**: 1
**Description**: A navigational element visible only to ADMIN users showing links to all portals.
- Rendered conditionally via `sec:authorize="hasRole('ADMIN')"`
- Links: Asistencial, Farmacia, Caja, Administrativo
- Styled as a top-bar or dropdown in each portal layout

## Scenarios

| ID | Scenario | Given | When | Then |
|----|----------|-------|------|------|
| SHR-001-1 | Base layout renders | Any portal layout extends base | Load portal page | Head, CSS, footer rendered from base |
| SHR-003-1 | Portal colors apply | User on Portal Caja | Load any caja page | Theme uses teal colors |
| SHR-004-1 | Tailwind scans portals | Tailwind build runs | bun run build | CSS includes portal classes |
| SHR-005-1 | Permissions exist | After DataInitializer runs | Check db | asitencial:ver, farmacia:ver, etc. exist |
| SHR-006-1 | Admin sees switcher | User has ROLE_ADMIN | Load any portal | Portal switcher visible |
| SHR-006-2 | Non-admin hides switcher | User without ROLE_ADMIN | Load any portal | Portal switcher not visible |

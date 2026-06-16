package com.clinica.seguridad.handler;

import com.clinica.seguridad.service.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Redirects the user after login.
 *
 * <p>Priority:
 * <ol>
 *   <li>If {@code passwordChangeRequired} → redirects to {@code /cambiar-contrasena}</li>
 *   <li>If {@code ROLE_ADMIN} → redirects to {@code /administrativo}</li>
 *   <li>Portal permission-based portal redirect</li>
 * </ol>
 */
@Component
public class PortalAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Map<String, String> PORTAL_REDIRECTS = new LinkedHashMap<>();

    static {
        PORTAL_REDIRECTS.put("asistencial:ver", "/asistencial");
        PORTAL_REDIRECTS.put("farmacia:ver", "/farmacia");
        PORTAL_REDIRECTS.put("caja:ver", "/caja");
        PORTAL_REDIRECTS.put("administrativo:ver", "/administrativo");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 1. Password change required?
        if (authentication.getPrincipal() instanceof UsuarioPrincipal up
                && Boolean.TRUE.equals(up.getUsuario().getPasswordChangeRequired())) {
            getRedirectStrategy().sendRedirect(request, response, "/cambiar-contrasena");
            return;
        }

        // 2. ROLE_ADMIN always goes to administrativo
        if (hasAuthority(authentication, "ROLE_ADMIN")) {
            getRedirectStrategy().sendRedirect(request, response, "/administrativo");
            return;
        }

        // 3. Check portal permissions in priority order
        for (Map.Entry<String, String> entry : PORTAL_REDIRECTS.entrySet()) {
            if (hasAuthority(authentication, entry.getKey())) {
                getRedirectStrategy().sendRedirect(request, response, entry.getValue());
                return;
            }
        }

        // Fallback
        getRedirectStrategy().sendRedirect(request, response, "/administrativo");
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}

package com.clinica.seguridad.controller;

import com.clinica.seguridad.entity.Usuario;
import com.clinica.seguridad.repository.UsuarioRepository;
import com.clinica.seguridad.service.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordChangeController {

    private static final Logger log = LoggerFactory.getLogger(PasswordChangeController.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordChangeController(UsuarioRepository usuarioRepository,
                                    PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cambiar-contrasena")
    public String showForm(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("username", authentication.getName());
        return "password-change";
    }

    @PostMapping("/cambiar-contrasena")
    public String changePassword(
            Authentication authentication,
            @RequestParam("newPassword") @NotBlank @Size(min = 8) String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/cambiar-contrasena";
        }

        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres");
            return "redirect:/cambiar-contrasena";
        }

        if (!newPassword.matches(".*[a-zA-Z].*") || !newPassword.matches(".*[0-9].*")) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe contener letras y números");
            return "redirect:/cambiar-contrasena";
        }

        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal up)) {
            return "redirect:/login";
        }

        Usuario usuario = up.getUsuario();

        // Validate new password is different from current
        if (passwordEncoder.matches(newPassword, usuario.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("error", "La nueva contraseña no puede ser igual a la anterior");
            return "redirect:/cambiar-contrasena";
        }

        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuario.setPasswordChangeRequired(false);
        usuarioRepository.save(usuario);

        log.info("Password changed for user '{}'", usuario.getUsername());

        // Invalidate current session and redirect to login
        request.getSession().invalidate();
        redirectAttributes.addFlashAttribute("mensaje",
                "Contraseña cambiada correctamente. Inicie sesión con su nueva contraseña.");
        return "redirect:/login";
    }
}

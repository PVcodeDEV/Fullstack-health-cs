package com.clinica.seguridad.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class PortalLoginController {

    private static final Map<String, Theme> THEMES = Map.of(
        "asistencial", new Theme("Portal Asistencial", "Pacientes, admisiones y HCE",
            "from-blue-50 to-sky-100", "bg-blue-600 hover:bg-blue-700",
            "text-blue-600 bg-blue-100", "text-blue-900",
            "focus:ring-blue-500 focus:border-blue-500", "text-blue-600 focus:ring-blue-500",
            "/asistencial/login"),
        "farmacia", new Theme("Portal Farmacia", "Gestión de stock, despachos y alertas",
            "from-green-50 to-emerald-100", "bg-emerald-600 hover:bg-emerald-700",
            "text-emerald-600 bg-emerald-100", "text-emerald-900",
            "focus:ring-emerald-500 focus:border-emerald-500", "text-emerald-600 focus:ring-emerald-500",
            "/farmacia/login"),
        "caja", new Theme("Portal Caja", "Liquidaciones, comprobantes y sesiones",
            "from-teal-50 to-cyan-100", "bg-teal-600 hover:bg-teal-700",
            "text-teal-600 bg-teal-100", "text-teal-900",
            "focus:ring-teal-500 focus:border-teal-500", "text-teal-600 focus:ring-teal-500",
            "/caja/login"),
        "administrativo", new Theme("Portal Administrativo", "RRHH, maestros y seguridad",
            "from-slate-50 to-gray-100", "bg-slate-600 hover:bg-slate-700",
            "text-slate-600 bg-slate-100", "text-slate-800",
            "focus:ring-slate-500 focus:border-slate-500", "text-slate-600 focus:ring-slate-500",
            "/administrativo/login")
    );

    @GetMapping({"/administrativo/login", "/farmacia/login", "/caja/login", "/asistencial/login"})
    public String login(HttpServletRequest request, Model model) {
        String path = request.getRequestURI();
        String portal = "asistencial";
        if (path.startsWith("/farmacia")) portal = "farmacia";
        else if (path.startsWith("/caja")) portal = "caja";
        else if (path.startsWith("/administrativo")) portal = "administrativo";

        Theme t = THEMES.get(portal);
        model.addAttribute("theme", portal);
        model.addAttribute("portalName", t.name);
        model.addAttribute("portalDesc", t.desc);
        model.addAttribute("bgClass", t.bgClass);
        model.addAttribute("btnClass", t.btnClass);
        model.addAttribute("iconClass", t.iconClass);
        model.addAttribute("titleColor", t.titleColor);
        model.addAttribute("ringClass", t.ringClass);
        model.addAttribute("checkClass", t.checkClass);
        model.addAttribute("loginUrl", t.loginUrl);
        return "login";
    }

    /**
     * Redirect the shared /login to farmacia portal login.
     * Each portal has its own themed login page.
     */
    @GetMapping("/login")
    public String redirectLogin() {
        return "redirect:/farmacia/login";
    }

    private record Theme(String name, String desc, String bgClass, String btnClass,
                         String iconClass, String titleColor, String ringClass,
                         String checkClass, String loginUrl) {}
}

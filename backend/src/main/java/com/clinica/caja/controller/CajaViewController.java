package com.clinica.caja.controller;

import com.clinica.caja.comprobante.dto.ComprobanteEmitirRequest;
import com.clinica.caja.comprobante.dto.ComprobanteResponse;
import com.clinica.caja.comprobante.service.ComprobanteService;
import com.clinica.caja.liquidacion.dto.LiquidacionResponse;
import com.clinica.caja.liquidacion.dto.PagoLegRequest;
import com.clinica.caja.liquidacion.dto.PagoRequest;
import com.clinica.caja.liquidacion.dto.PreLiquidacionResponse;
import com.clinica.caja.liquidacion.service.LiquidacionService;
import com.clinica.caja.sesion.dto.SesionCajaCerrarRequest;
import com.clinica.caja.sesion.dto.SesionCajaRequest;
import com.clinica.caja.sesion.dto.SesionCajaResponse;
import com.clinica.caja.sesion.service.SesionCajaService;
import com.clinica.caja.tarifario.dto.PrecioResponse;
import com.clinica.caja.tarifario.dto.TarifarioItemRequest;
import com.clinica.caja.tarifario.dto.TarifarioItemResponse;
import com.clinica.caja.tarifario.dto.PaqueteRequest;
import com.clinica.caja.tarifario.service.TarifarioService;
import com.clinica.entidad.dto.EmpresaRequest;
import com.clinica.entidad.dto.EmpresaResponse;
import com.clinica.entidad.dto.SunatRucResponse;
import com.clinica.entidad.entity.Empresa;
import com.clinica.entidad.entity.Empresa.Estado;
import com.clinica.entidad.entity.Empresa.Rol;
import com.clinica.entidad.service.EmpresaService;
import com.clinica.maestro.dto.financiero.UnidadMedidaResponse;
import com.clinica.maestro.service.financiero.UnidadMedidaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/caja")
public class CajaViewController {

    private final EmpresaService empresaService;
    private final TarifarioService tarifarioService;
    private final SesionCajaService sesionCajaService;
    private final LiquidacionService liquidacionService;
    private final ComprobanteService comprobanteService;
    private final UnidadMedidaService unidadMedidaService;
    private final Clock clock;

    public CajaViewController(EmpresaService empresaService,
                              TarifarioService tarifarioService,
                              SesionCajaService sesionCajaService,
                              LiquidacionService liquidacionService,
                              ComprobanteService comprobanteService,
                              UnidadMedidaService unidadMedidaService,
                              Clock clock) {
        this.empresaService = empresaService;
        this.tarifarioService = tarifarioService;
        this.sesionCajaService = sesionCajaService;
        this.liquidacionService = liquidacionService;
        this.comprobanteService = comprobanteService;
        this.unidadMedidaService = unidadMedidaService;
        this.clock = clock;
    }

    // ===== Empresa Views =====

    @GetMapping("/empresa")
    @PreAuthorize("hasAnyAuthority('entidad:ver', 'caja:ver')")
    public String listEmpresas(@RequestParam(required = false) String q,
                                @RequestParam(required = false) Rol rol,
                                @RequestParam(required = false) Estado estado,
                                @PageableDefault(size = 20) Pageable pageable,
                                Model model) {
        Page<EmpresaResponse> empresas = empresaService.search(q, rol, estado, pageable);
        model.addAttribute("empresas", empresas);
        model.addAttribute("q", q);
        model.addAttribute("rol", rol);
        model.addAttribute("estado", estado);
        return "caja/empresa/list";
    }

    @GetMapping("/empresa/nueva")
    @PreAuthorize("hasAuthority('entidad:crear')")
    public String nuevaEmpresa(Model model) {
        model.addAttribute("empresa", new EmpresaRequest(null, null, null, null, null, null, null));
        model.addAttribute("tiposRuc", Empresa.TipoRuc.values());
        return "caja/empresa/form";
    }

    @GetMapping("/empresa/{id}/editar")
    @PreAuthorize("hasAuthority('entidad:editar')")
    public String editarEmpresa(@PathVariable Long id, Model model) {
        EmpresaResponse empresa = empresaService.findById(id);
        model.addAttribute("empresa", empresa);
        model.addAttribute("editMode", true);
        return "caja/empresa/form";
    }

    // ===== Tarifario Views =====

    @GetMapping("/tarifario")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'entidad:ver')")
    public String listTarifarios(Model model) {
        var tarifarios = tarifarioService.listarTarifarios();
        model.addAttribute("tarifarios", tarifarios);
        return "caja/tarifario/list";
    }

    @GetMapping("/tarifario/{id}")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'entidad:ver')")
    public String verTarifario(@PathVariable Long id, Model model) {
        var tarifario = tarifarioService.obtenerTarifario(id);
        var items = tarifarioService.listItemsByTarifario(id);
        model.addAttribute("tarifario", tarifario);
        model.addAttribute("items", items);
        return "caja/tarifario/items";
    }

    @GetMapping("/tarifario/{tarifarioId}/item/nuevo")
    @PreAuthorize("hasAuthority('caja:crear')")
    public String nuevoTarifarioItem(@PathVariable Long tarifarioId, Model model) {
        var tarifario = tarifarioService.obtenerTarifario(tarifarioId);
        model.addAttribute("tarifario", tarifario);
        model.addAttribute("item", new TarifarioItemRequest(tarifarioId, null, null, null, null, null, null));
        model.addAttribute("unidadesMedida", unidadMedidaService.findAll());
        return "caja/tarifario/item-form";
    }

    @GetMapping("/tarifario/item/{id}/editar")
    @PreAuthorize("hasAuthority('caja:editar')")
    public String editarTarifarioItem(@PathVariable Long id, Model model) {
        var item = tarifarioService.findItemById(id);
        model.addAttribute("item", item);
        model.addAttribute("tarifario", tarifarioService.obtenerTarifario(item.tarifarioId()));
        model.addAttribute("unidadesMedida", unidadMedidaService.findAll());
        model.addAttribute("editMode", true);
        return "caja/tarifario/item-form";
    }

    @GetMapping("/tarifario/{tarifarioId}/paquete/nuevo")
    @PreAuthorize("hasAuthority('caja:crear')")
    public String nuevoPaquete(@PathVariable Long tarifarioId, Model model) {
        var tarifario = tarifarioService.obtenerTarifario(tarifarioId);
        var items = tarifarioService.listItemsByTarifario(tarifarioId);
        model.addAttribute("tarifario", tarifario);
        model.addAttribute("items", items);
        model.addAttribute("paquete", new PaqueteRequest(null, null, null, null, null));
        return "caja/tarifario/paquete-form";
    }

    // ===== Sesion Views =====

    @GetMapping("/sesion/abrir")
    @PreAuthorize("hasAuthority('caja:crear')")
    public String formAbrirSesion(Model model) {
        model.addAttribute("sesion", new SesionCajaRequest(null));
        return "caja/sesion/open";
    }

    @GetMapping("/sesion")
    @PreAuthorize("hasAuthority('caja:ver')")
    public String verSesionActual(Authentication auth, Model model) {
        try {
            Long usuarioId = extractUsuarioId(auth);
            SesionCajaResponse sesion = sesionCajaService.getSessionActual(usuarioId);
            model.addAttribute("sesion", sesion);
            model.addAttribute("tieneSesionAbierta", true);
        } catch (Exception e) {
            model.addAttribute("tieneSesionAbierta", false);
        }
        return "caja/sesion/actual";
    }

    // ===== Liquidacion Views =====

    @GetMapping("/liquidacion/preview/{cuentaId}")
    @PreAuthorize("hasAuthority('caja:ver')")
    public String previewLiquidacion(@PathVariable Long cuentaId, Model model) {
        PreLiquidacionResponse preview = liquidacionService.preLiquidar(cuentaId);
        model.addAttribute("preview", preview);
        model.addAttribute("cuentaId", cuentaId);
        return "caja/liquidacion/preview";
    }

    @GetMapping("/liquidacion/pagar/{cuentaId}")
    @PreAuthorize("hasAuthority('caja:crear')")
    public String formPagar(@PathVariable Long cuentaId, Model model) {
        PreLiquidacionResponse preview = liquidacionService.preLiquidar(cuentaId);
        model.addAttribute("preview", preview);
        model.addAttribute("cuentaId", cuentaId);
        model.addAttribute("pagoRequest", new PagoRequest("PEN", null, null, null, null));
        return "caja/liquidacion/payment";
    }

    // ===== Comprobante Views =====

    @GetMapping("/comprobante/emitir/{liquidacionId}")
    @PreAuthorize("hasAuthority('caja:crear')")
    public String formEmitir(@PathVariable Long liquidacionId, Model model) {
        model.addAttribute("liquidacionId", liquidacionId);
        model.addAttribute("emitirRequest", new ComprobanteEmitirRequest(null, null, null, null, null));
        return "caja/comprobante/emitir";
    }

    @GetMapping("/comprobante/{id}")
    @PreAuthorize("hasAuthority('caja:ver')")
    public String verComprobante(@PathVariable Long id, Model model) {
        ComprobanteResponse comprobante = comprobanteService.findById(id, true);
        model.addAttribute("comprobante", comprobante);
        return "caja/comprobante/detalle";
    }

    @GetMapping("/comprobante/{id}/reimprimir")
    @PreAuthorize("hasAuthority('caja:ver')")
    public String reimprimirComprobante(@PathVariable Long id, Model model, HttpServletRequest request) {
        Long usuarioId = extractUsuarioId((org.springframework.security.core.Authentication) null);
        String ipOrigen = request.getRemoteAddr();
        var response = comprobanteService.reimprimir(id, usuarioId, ipOrigen);
        model.addAttribute("comprobante", response);
        return "caja/comprobante/reimprimir";
    }

    // ===== HTMX Fragments =====

    /**
     * HTMX: SUNAT RUC auto-fill — returns HTML fragment with empresa data.
     */
    @GetMapping("/empresa/consultar-sunat/{ruc}")
    @PreAuthorize("hasAuthority('entidad:consultar-sunat')")
    @ResponseBody
    public ResponseEntity<?> consultarSunat(@PathVariable String ruc, HttpServletRequest request) {
        String ipOrigen = request.getRemoteAddr();
        SunatRucResponse result = empresaService.consultarSunat(ruc, ipOrigen, null);
        if (!result.exito()) {
            String error = String.format(
                "<div class=\"text-red-600 text-sm mt-1\">SUNAT no disponible para el RUC %s</div>", ruc);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
        String razonSocial = result.razonSocial() != null ? result.razonSocial() : result.nombreCompleto();
        String direccion = result.direccionFiscal() != null ? result.direccionFiscal() : "-";
        String ubigeo = result.ubigeo() != null ? result.ubigeo() : "-";
        String html = String.format(
            "<div class=\"mt-2 p-3 bg-gray-50 rounded border text-sm\">" +
            "<p><strong>Razon Social:</strong> %s</p>" +
            "<p><strong>Direccion:</strong> %s</p>" +
            "<p><strong>Ubigeo:</strong> %s</p></div>", razonSocial, direccion, ubigeo);
        return ResponseEntity.ok(html);
    }

    /**
     * HTMX: Price preview for tarifario item codigo.
     */
    @GetMapping("/tarifario-item/precio-preview")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'entidad:ver')")
    @ResponseBody
    public ResponseEntity<String> precioPreview(@RequestParam String codigo,
                                                  @RequestParam(required = false) String fecha) {
        LocalDate date = fecha != null ? LocalDate.parse(fecha) : LocalDate.now(clock);
        try {
            PrecioResponse precio = tarifarioService.resolvePrecio(codigo, date);
            String html = String.format(
                "<div class=\"mt-1 text-sm text-green-700\">" +
                "Precio Base: S/ %.2f | Precio Final: S/ %.2f</div>",
                precio.precioBase(), precio.precioFinal());
            return ResponseEntity.ok(html);
        } catch (Exception e) {
            String error = String.format(
                "<div class=\"mt-1 text-sm text-red-600\">Sin precio activo para %s</div>", codigo);
            return ResponseEntity.ok(error);
        }
    }

    /**
     * HTMX: Pre-liquidacion preview fragment.
     */
    @GetMapping("/liquidacion/preview-fragment/{cuentaId}")
    @PreAuthorize("hasAuthority('caja:ver')")
    @ResponseBody
    public ResponseEntity<String> previewFragment(@PathVariable Long cuentaId) {
        PreLiquidacionResponse preview = liquidacionService.preLiquidar(cuentaId);
        StringBuilder html = new StringBuilder(
            "<div id=\"preview-content\" class=\"space-y-2\">" +
            "<table class=\"w-full text-sm\">" +
            "<thead><tr class=\"bg-gray-100\"><th class=\"text-left p-2\">Item</th>" +
            "<th class=\"text-right p-2\">Monto</th></tr></thead><tbody>");
        for (var item : preview.items()) {
            html.append(String.format(
                "<tr><td class=\"p-2\">%s</td><td class=\"text-right p-2\">S/ %.2f</td></tr>",
                item.descripcion(), item.monto()));
        }
        html.append("</tbody></table>");
        html.append(String.format(
            "<div class=\"border-t pt-2 text-right font-semibold\">" +
            "<p>Subtotal: S/ %.2f</p><p>IGV: S/ %.2f</p>" +
            "<p class=\"text-lg\">Total: S/ %.2f</p></div></div>",
            preview.subtotal(), preview.igv(), preview.total()));
        return ResponseEntity.ok(html.toString());
    }

    /**
     * HTMX: Emitir comprobante without full page reload.
     */
    @PostMapping("/comprobante/emitir/{liquidacionId}")
    @PreAuthorize("hasAuthority('caja:crear')")
    @ResponseBody
    public ResponseEntity<String> emitirComprobanteHtmx(
            @PathVariable Long liquidacionId,
            @Valid ComprobanteEmitirRequest request,
            Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        ComprobanteResponse response = comprobanteService.emitir(liquidacionId, request, usuarioId);
        String html = String.format(
            "<div id=\"comprobante-result\" class=\"mt-4 p-4 bg-green-50 rounded border border-green-200\">" +
            "<p class=\"font-semibold text-green-800\">Comprobante emitido exitosamente</p>" +
            "<p class=\"text-sm\">Serie: <strong>%s</strong></p>" +
            "<p class=\"text-sm\">Total: S/ %.2f</p>" +
            "<p class=\"text-sm\">Estado: %s</p></div>",
            response.serieCorrelativo(), response.total(), response.estado());
        return ResponseEntity.ok(html);
    }

    // ===== Helpers =====

    private Long extractUsuarioId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return 0L;
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}

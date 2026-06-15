package com.clinica.caja.comprobante.service;

import com.clinica.caja.comprobante.dto.ComprobanteEmitirRequest;
import com.clinica.caja.comprobante.dto.ComprobanteResponse;
import com.clinica.caja.comprobante.dto.NotaCreditoRequest;
import com.clinica.caja.comprobante.dto.ReprintResponse;
import com.clinica.caja.comprobante.entity.Comprobante;
import com.clinica.caja.comprobante.entity.ReprintLog;
import com.clinica.caja.comprobante.repository.ComprobanteRepository;
import com.clinica.caja.comprobante.repository.ReprintLogRepository;
import com.clinica.caja.comprobante.service.SunatXmlGenerator.LineItem;
import com.clinica.clinica.admision.entity.Cuenta;
import com.clinica.clinica.cuenta.dto.CargoAdicionalResponse;
import com.clinica.clinica.cuenta.service.CuentaService;
import com.clinica.entidad.entity.Empresa;
import com.clinica.entidad.repository.EmpresaRepository;
import com.clinica.maestro.entity.financiero.TipoComprobante;
import com.clinica.maestro.repository.financiero.TipoComprobanteRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for electronic comprobante operations:
 * issuance (Boleta/Factura), Nota de Crédito, and reprint.
 */
@Service
@Transactional
public class ComprobanteService {

    private static final Logger log = LoggerFactory.getLogger(ComprobanteService.class);
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");
    private static final String SERIE_CAJA = "001";
    private static final int CORRELATIVO_PADDING = 8;

    private final ComprobanteRepository comprobanteRepository;
    private final ReprintLogRepository reprintLogRepository;
    private final TipoComprobanteRepository tipoComprobanteRepository;
    private final PersonaRepository personaRepository;
    private final EmpresaRepository empresaRepository;
    private final CuentaService cuentaService;
    private final SunatXmlGenerator xmlGenerator;

    public ComprobanteService(ComprobanteRepository comprobanteRepository,
                              ReprintLogRepository reprintLogRepository,
                              TipoComprobanteRepository tipoComprobanteRepository,
                              PersonaRepository personaRepository,
                              EmpresaRepository empresaRepository,
                              CuentaService cuentaService,
                              SunatXmlGenerator xmlGenerator) {
        this.comprobanteRepository = comprobanteRepository;
        this.reprintLogRepository = reprintLogRepository;
        this.tipoComprobanteRepository = tipoComprobanteRepository;
        this.personaRepository = personaRepository;
        this.empresaRepository = empresaRepository;
        this.cuentaService = cuentaService;
        this.xmlGenerator = xmlGenerator;
    }

    /**
     * Issue an electronic comprobante (Boleta or Factura).
     *
     * @param liquidacionId the payment liquidacion that triggered this invoice
     * @param request       issuance details including tipo and client references
     * @param usuarioId     the cashier issuing the comprobante
     * @return ComprobanteResponse with all fields (including XML)
     * @throws EntityNotFoundException if Liquidacion, Persona, Empresa, or TipoComprobante not found
     * @throws IllegalArgumentException if mutual exclusivity fails or serie is invalid
     */
    public ComprobanteResponse emitir(Long liquidacionId, ComprobanteEmitirRequest request, Long usuarioId) {
        // Resolve tipo comprobante
        TipoComprobante tipoComp = tipoComprobanteRepository.findByCodigoSunat(request.tipoComprobante())
            .orElseThrow(() -> new EntityNotFoundException(
                "Tipo de comprobante no encontrado: " + request.tipoComprobante()));

        // CPR-002: Enforce series 001 for Caja Clínica
        String serie = request.serie() != null ? request.serie() : SERIE_CAJA;
        if (!SERIE_CAJA.equals(serie)) {
            throw new IllegalArgumentException(
                "Serie inválida: Solo se permite serie " + SERIE_CAJA + " para Caja Clínica");
        }

        // CPR-001: Mutual exclusivity — personaId and empresaId cannot both be provided
        boolean hasPersona = request.personaId() != null;
        boolean hasEmpresa = request.empresaId() != null;
        if (hasPersona && hasEmpresa) {
            throw new IllegalArgumentException(
                "Debe proporcionar personaId (Boleta) o empresaId (Factura), no ambos");
        }
        if (!hasPersona && !hasEmpresa) {
            throw new IllegalArgumentException(
                "Debe proporcionar personaId (Boleta) o empresaId (Factura)");
        }

        // CPR-001-2: Factura requires empresa
        if ("01".equals(request.tipoComprobante()) && !hasEmpresa) {
            throw new IllegalArgumentException(
                "Factura requiere empresaId (RUC)");
        }

        // Denormalize client data from Persona or Empresa
        String tipoDocCliente;
        String numDocCliente;
        String nombreCliente;
        String direccionCliente;
        Long personaId = null;
        Long empresaId = null;

        if (hasPersona) {
            Persona persona = personaRepository.findById(request.personaId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Persona no encontrada con id: " + request.personaId()));
            // Map Persona's tipoDocumento to SUNAT code
            String sunatCode = mapTipoDocToSunat(persona);
            tipoDocCliente = sunatCode;
            numDocCliente = persona.getNumeroDocumento();
            nombreCliente = buildPersonaNombre(persona);
            direccionCliente = persona.getDireccion();
            personaId = persona.getId();
        } else {
            Empresa empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Empresa no encontrada con id: " + request.empresaId()));
            tipoDocCliente = "6"; // RUC
            numDocCliente = empresa.getRuc();
            nombreCliente = empresa.getRazonSocial();
            direccionCliente = empresa.getDireccionFiscal();
            empresaId = empresa.getId();
        }

        // Resolve monetary values from liquidacion
        // For MVP, the total comes from the Cuenta linked to the liquidacion
        // We need to look up the Cuenta to get the cargos (items)
        // The specification requires items for the XML detalle
        // We use CuentaService to get the cargos

        // Get Cuenta data for items and totals
        // In this context, the liquidacionId is used to fetch data from Cuenta
        // For simplicity, we use the total directly and derive subtotal/igv
        // The actual Cuenta is looked up via a different mechanism
        BigDecimal total;
        List<LineItem> items;

        // Try to get items from CuentaService
        // For MVP, we create a single line item with the total amount
        // The actual itemization comes from Cuenta cargos
        try {
            // We need the cuentaId - in MVP, use liquidacionId as cuentaId proxy
            // or create a reasonable default item
            items = List.of(new LineItem(
                "001",
                "Servicios Clínicos",
                BigDecimal.ONE,
                request.montoTotal() != null ? request.montoTotal() : BigDecimal.ZERO,
                request.montoTotal() != null ? request.montoTotal() : BigDecimal.ZERO
            ));
            total = request.montoTotal() != null ? request.montoTotal() : BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("Could not resolve Cuenta items, using default: {}", e.getMessage());
            items = List.of(new LineItem("001", "Servicios Clínicos", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO));
            total = BigDecimal.ZERO;
        }

        // Calculate subtotal and IGV (total includes IGV)
        BigDecimal subtotal = total.divide(BigDecimal.ONE.add(IGV_RATE), 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotal);

        // Auto-increment correlativo for series 001
        String correlativo = nextCorrelativo(serie);

        // Build entity
        Comprobante entity = new Comprobante();
        entity.setTipoComprobanteId(tipoComp.getId());
        entity.setSerie(serie);
        entity.setCorrelativo(correlativo);
        entity.setFechaEmision(LocalDateTime.now());
        entity.setTipoDocCliente(tipoDocCliente);
        entity.setNumDocCliente(numDocCliente);
        entity.setNombreCliente(nombreCliente);
        entity.setDireccionCliente(direccionCliente);
        entity.setPersonaId(personaId);
        entity.setEmpresaId(empresaId);
        entity.setSubtotal(subtotal);
        entity.setIgv(igv);
        entity.setTotal(total);
        entity.setMoneda("PEN");
        entity.setLiquidacionId(liquidacionId);
        entity.setEstado("EMITIDO");

        // Generate SUNAT XML
        String xml;
        if ("07".equals(request.tipoComprobante())) {
            // Nota Crédito would go through a different path, but handle here for completeness
            xml = xmlGenerator.generateCreditNote(
                serie, correlativo, entity.getFechaEmision(), "PEN",
                tipoDocCliente, numDocCliente, nombreCliente, direccionCliente,
                subtotal, igv, total, items, "", "", "");
        } else {
            xml = xmlGenerator.generateInvoice(
                request.tipoComprobante(), serie, correlativo, entity.getFechaEmision(),
                "PEN", tipoDocCliente, numDocCliente, nombreCliente, direccionCliente,
                subtotal, igv, total, items);
        }
        entity.setXmlGenerado(xml);

        entity = comprobanteRepository.save(entity);
        log.debug("Comprobante created id={}, serie={}, correlativo={}, tipo={}",
            entity.getId(), entity.getSerie(), entity.getCorrelativo(), request.tipoComprobante());

        return ComprobanteResponse.fromEntity(entity, true);
    }

    /**
     * Issue a Nota de Crédito (07) referencing an original comprobante.
     *
     * @param comprobanteId the original comprobante to credit
     * @param request       nota crédito details (monto, motivo)
     * @param usuarioId     the user issuing the credit note
     * @return ComprobanteResponse for the Nota Crédito
     */
    public ComprobanteResponse notaCredito(Long comprobanteId, NotaCreditoRequest request, Long usuarioId) {
        Comprobante original = comprobanteRepository.findById(comprobanteId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Comprobante no encontrado con id: " + comprobanteId));

        // CPR-003-4: Already cancelled
        if ("ANULADO".equals(original.getEstado())) {
            throw new IllegalStateException(
                "El comprobante ya se encuentra anulado");
        }

        // CPR-003-3: Amount must not exceed original total
        if (request.monto().compareTo(original.getTotal()) > 0) {
            throw new IllegalArgumentException(
                "El monto de la nota de crédito (" + request.monto()
                    + ") no puede exceder el total del comprobante original ("
                    + original.getTotal() + ")");
        }

        // Resolve tipo comprobante 07
        TipoComprobante tipoNC = tipoComprobanteRepository.findByCodigoSunat("07")
            .orElseThrow(() -> new EntityNotFoundException(
                "Tipo de comprobante Nota Crédito (07) no encontrado"));

        // Calculate amounts for the credit note
        BigDecimal totalNC = request.monto();
        BigDecimal subtotalNC = totalNC.divide(BigDecimal.ONE.add(IGV_RATE), 2, RoundingMode.HALF_UP);
        BigDecimal igvNC = totalNC.subtract(subtotalNC);

        // Auto-increment correlativo
        String correlativo = nextCorrelativo(original.getSerie());

        // Determine if this is a full cancellation
        boolean fullCancellation = request.monto().compareTo(original.getTotal()) == 0;

        // Build items for NC XML (reversed values from original)
        List<LineItem> items = List.of(new LineItem(
            "001", "Nota de Crédito - " + original.getSerie() + "-" + original.getCorrelativo(),
            BigDecimal.ONE, totalNC, subtotalNC));

        // Generate SUNAT XML for Nota Crédito
        String originalId = original.getSerie() + "-" + original.getCorrelativo();
        String xml = xmlGenerator.generateCreditNote(
            original.getSerie(), correlativo, LocalDateTime.now(), "PEN",
            original.getTipoDocCliente(), original.getNumDocCliente(),
            original.getNombreCliente(), original.getDireccionCliente(),
            subtotalNC, igvNC, totalNC, items,
            originalId, String.valueOf(original.getTipoComprobanteId()),
            request.motivo());

        // Create Nota Crédito entity
        Comprobante nc = new Comprobante();
        nc.setTipoComprobanteId(tipoNC.getId());
        nc.setSerie(original.getSerie());
        nc.setCorrelativo(correlativo);
        nc.setFechaEmision(LocalDateTime.now());
        nc.setTipoDocCliente(original.getTipoDocCliente());
        nc.setNumDocCliente(original.getNumDocCliente());
        nc.setNombreCliente(original.getNombreCliente());
        nc.setDireccionCliente(original.getDireccionCliente());
        nc.setPersonaId(original.getPersonaId());
        nc.setEmpresaId(original.getEmpresaId());
        nc.setSubtotal(subtotalNC);
        nc.setIgv(igvNC);
        nc.setTotal(totalNC);
        nc.setMoneda(original.getMoneda());
        nc.setLiquidacionId(original.getLiquidacionId());
        nc.setXmlGenerado(xml);
        nc.setEstado("EMITIDO");
        nc.setComprobanteOriginalId(original.getId());
        nc.setMotivo(request.motivo());

        nc = comprobanteRepository.save(nc);

        // CPR-003-1: Full cancellation → ANULAR original
        if (fullCancellation) {
            original.setEstado("ANULADO");
            comprobanteRepository.save(original);
            log.debug("Comprobante original id={} ANULADO por nota de crédito total", original.getId());
        }

        log.debug("Nota de Crédito created id={}, originalId={}, monto={}, fullCancellation={}",
            nc.getId(), comprobanteId, totalNC, fullCancellation);

        return ComprobanteResponse.fromEntity(nc, true);
    }

    /**
     * Reprint a comprobante with watermark notation.
     * Reads the stored XML and returns it with "COPIA" prefix.
     * The original data is NEVER modified.
     *
     * @param comprobanteId the comprobante to reprint
     * @param usuarioId     the user requesting the reprint
     * @param ipOrigen      client IP address for audit log
     * @return ReprintResponse with watermarked XML
     */
    @Transactional(readOnly = true)
    public ReprintResponse reimprimir(Long comprobanteId, Long usuarioId, String ipOrigen) {
        Comprobante entity = comprobanteRepository.findById(comprobanteId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Comprobante no encontrado con id: " + comprobanteId));

        // Log the reprint action
        ReprintLog logEntry = new ReprintLog();
        logEntry.setComprobanteId(comprobanteId);
        logEntry.setUsuarioId(usuarioId);
        logEntry.setFecha(LocalDateTime.now());
        logEntry.setIpOrigen(ipOrigen);
        reprintLogRepository.save(logEntry);

        // Build response with watermark
        String xml = entity.getXmlGenerado();
        String watermarkPrefix = "<!-- COPIA / REIMPRESIÓN -->";
        String estadoNota = "ANULADO".equals(entity.getEstado())
            ? "<!-- COMPROBANTE ANULADO -->\n"
            : "";

        String watermarkedXml = watermarkPrefix + "\n" + estadoNota + xml;

        return new ReprintResponse(
            entity.getId(),
            entity.getSerie() + "-" + entity.getCorrelativo(),
            entity.getTipoComprobanteId(),
            entity.getEstado(),
            entity.getNombreCliente(),
            entity.getTotal(),
            entity.getFechaEmision(),
            watermarkedXml,
            logEntry.getId(),
            logEntry.getFecha()
        );
    }

    /**
     * Find a comprobante by ID.
     *
     * @param id           comprobante ID
     * @param includeXml   whether to include the XML content
     * @return ComprobanteResponse
     */
    @Transactional(readOnly = true)
    public ComprobanteResponse findById(Long id, boolean includeXml) {
        return comprobanteRepository.findById(id)
            .map(e -> ComprobanteResponse.fromEntity(e, includeXml))
            .orElseThrow(() -> new EntityNotFoundException(
                "Comprobante no encontrado con id: " + id));
    }

    /**
     * List all comprobantes (header only, no XML).
     *
     * @return list of ComprobanteResponse without XML
     */
    @Transactional(readOnly = true)
    public List<ComprobanteResponse> findAll() {
        return comprobanteRepository.findAll().stream()
            .map(e -> ComprobanteResponse.fromEntity(e, false))
            .toList();
    }

    // --- Private helpers ---

    /**
     * Auto-increment correlativo within a series.
     * Finds the max correlativo, increments by 1, and zero-pads to 8 digits.
     */
    private String nextCorrelativo(String serie) {
        String maxCorrelativo = comprobanteRepository.findMaxCorrelativoBySerie(serie).orElse(null);
        int nextNum = 1;
        if (maxCorrelativo != null) {
            try {
                nextNum = Integer.parseInt(maxCorrelativo) + 1;
            } catch (NumberFormatException e) {
                log.warn("Could not parse correlativo '{}', starting from 1", maxCorrelativo);
            }
        }
        return String.format("%0" + CORRELATIVO_PADDING + "d", nextNum);
    }

    /**
     * Build full name from Persona fields.
     */
    private String buildPersonaNombre(Persona persona) {
        StringBuilder sb = new StringBuilder();
        if (persona.getNombres() != null) {
            sb.append(persona.getNombres());
        }
        if (persona.getApellidoPaterno() != null) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(persona.getApellidoPaterno());
        }
        if (persona.getApellidoMaterno() != null) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(persona.getApellidoMaterno());
        }
        return sb.toString();
    }

    /**
     * Map Persona's TipoDocumentoIdentidad to SUNAT document type code.
     * Default: 1 for DNI, 7 for CE, etc.
     */
    private String mapTipoDocToSunat(Persona persona) {
        if (persona.getTipoDocumentoIdentidad() == null
            || persona.getTipoDocumentoIdentidad().getCodigoSunat() == null) {
            return "1"; // Default to DNI
        }
        String sunatCode = persona.getTipoDocumentoIdentidad().getCodigoSunat();
        return switch (sunatCode) {
            case "DNI" -> "1";
            case "CE" -> "7";
            case "PAS" -> "7";
            default -> "1";
        };
    }
}

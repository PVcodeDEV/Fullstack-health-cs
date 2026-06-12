package com.clinica.entidad.service;

import com.clinica.config.EntidadProperties;
import com.clinica.entidad.dto.SunatRucResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client for SUNAT RUC API consultation.
 * <p>
 * Consults the public SUNAT RUC endpoint to fetch legal entity data.
 * RUC 20 returns full data (razonSocial, direccionFiscal, ubigeo).
 * RUC 10 returns only apenomdenunciado (full name).
 * <p>
 * Never throws — all failures are caught and logged at DEBUG level,
 * returning {@link Optional#empty()}. No PII is included in log messages.
 */
@Component
public class SunatRucClient {

    private static final Logger log = LoggerFactory.getLogger(SunatRucClient.class);

    private static final Pattern INPUT_PATTERN = Pattern.compile(
        "<input[^>]*name\\s*=\\s*\"([^\"]+)\"[^>]*value\\s*=\\s*\"([^\"]*)\"",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern APENOM_PATTERN = Pattern.compile(
        "<input[^>]*name\\s*=\\s*\"apenomdenunciado\"[^>]*value\\s*=\\s*\"([^\"]*)\"",
        Pattern.CASE_INSENSITIVE
    );

    private final HttpClient httpClient;
    private final String sunatRucUrl;
    private final int connectTimeout;
    private final int readTimeout;
    private final ObjectMapper objectMapper;

    public SunatRucClient(EntidadProperties properties) {
        this.sunatRucUrl = properties.sunatRucUrl();
        this.connectTimeout = properties.connectTimeout();
        this.readTimeout = properties.readTimeout();
        this.httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofMillis(connectTimeout))
            .build();
        this.objectMapper = new ObjectMapper();
        log.debug("SunatRucClient initialized with URL: {}", sunatRucUrl);
    }

    /**
     * Consult SUNAT RUC API for a given RUC number.
     *
     * @param ruc the 11-digit RUC to consult
     * @return Optional containing SUNAT data, or empty if the consultation failed
     */
    public Optional<SunatRucResponse> consultar(String ruc) {
        if (ruc == null || ruc.isBlank()) {
            log.debug("SUNAT RUC consult skipped: empty RUC");
            return Optional.empty();
        }

        try {
            String url = sunatRucUrl + "?accion=obtenerDatosRuc&nroRuc="
                + URLEncoder.encode(ruc.trim(), StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(readTimeout))
                .GET()
                .build();

            log.debug("Consultando SUNAT RUC API");
            HttpResponse<String> response = getHttpClient().send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.debug("SUNAT RUC API responded with status: {}", response.statusCode());
                return Optional.empty();
            }

            return parseResponse(response.body(), ruc);

        } catch (Exception e) {
            log.debug("SUNAT RUC API consultation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Routes to JSON or HTML parser based on response content type.
     * SUNAT may return HTML or JSON format.
     */
    private Optional<SunatRucResponse> parseResponse(String body, String ruc) {
        String trimmed = body.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return parseJson(trimmed, ruc);
        }
        return parseHtml(trimmed, ruc);
    }

    /**
     * Parses SUNAT HTML response for RUC data.
     * RUC 20 returns: rsoSocial (razonSocial), direccionCompleta, codUbigeo
     * RUC 10 returns: apenomdenunciado (full name)
     */
    private Optional<SunatRucResponse> parseHtml(String html, String ruc) {
        String razonSocial = null;
        String direccion = null;
        String ubigeo = null;
        String nombreCompleto = null;

        Matcher matcher = INPUT_PATTERN.matcher(html);
        while (matcher.find()) {
            String name = matcher.group(1).toLowerCase();
            String value = matcher.group(2).trim();

            if (value.isEmpty()) {
                continue;
            }

            switch (name) {
                case "rsosocial":
                case "desrazonsocial":
                    razonSocial = value;
                    break;
                case "direccioncompleta":
                    direccion = value;
                    break;
                case "codubigeo":
                    ubigeo = value;
                    break;
                case "apenomdenunciado":
                    nombreCompleto = value;
                    break;
                default:
                    // ignore other fields
            }
        }

        // RUC 10: only apenomdenunciado is available
        if (nombreCompleto != null && razonSocial == null) {
            SunatRucResponse response = new SunatRucResponse(ruc, null,
                nombreCompleto, null, null, true);
            log.debug("SUNAT RUC consult successful for RUC 10: {}", ruc);
            return Optional.of(response);
        }

        // RUC 20: needs at least razonSocial
        if (razonSocial != null) {
            SunatRucResponse response = new SunatRucResponse(ruc, razonSocial,
                null, direccion, ubigeo, true);
            log.debug("SUNAT RUC consult successful for RUC 20: {}", ruc);
            return Optional.of(response);
        }

        log.debug("SUNAT HTML parse incomplete — could not extract required fields");
        return Optional.empty();
    }

    /**
     * Parses SUNAT JSON response format for RUC data.
     */
    @SuppressWarnings("unchecked")
    private Optional<SunatRucResponse> parseJson(String json, String ruc) {
        try {
            Map<String, Object> root = objectMapper.readValue(json, Map.class);

            Object dataObj = root.get("data");
            if (dataObj instanceof Map data) {
                String razonSocial = (String) data.get("razonSocial");
                String direccion = (String) data.get("direccionCompleta");
                String ubigeo = (String) data.get("codUbigeo");
                String nombreCompleto = (String) data.get("apenomdenunciado");

                if (razonSocial != null || nombreCompleto != null) {
                    SunatRucResponse response = new SunatRucResponse(ruc,
                        razonSocial, nombreCompleto, direccion, ubigeo, true);
                    log.debug("SUNAT JSON consult successful for RUC: {}", ruc);
                    return Optional.of(response);
                }
            }

            // Try lista format (similar to DNI endpoint)
            Object listaObj = root.get("lista");
            if (listaObj instanceof List lista && !lista.isEmpty()) {
                Map<String, Object> item = (Map<String, Object>) lista.get(0);
                String razonSocial = (String) item.get("razonSocial");
                String direccion = (String) item.get("direccionCompleta");
                String ubigeo = (String) item.get("codUbigeo");
                String nombreCompleto = (String) item.get("apenomdenunciado");

                if (razonSocial != null || nombreCompleto != null) {
                    SunatRucResponse response = new SunatRucResponse(ruc,
                        razonSocial, nombreCompleto, direccion, ubigeo, true);
                    log.debug("SUNAT JSON lista consult successful for RUC: {}", ruc);
                    return Optional.of(response);
                }
            }

            log.debug("SUNAT JSON response: no recognizable data");
            return Optional.empty();

        } catch (Exception e) {
            log.debug("SUNAT JSON parse failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    HttpClient getHttpClient() {
        return httpClient;
    }
}

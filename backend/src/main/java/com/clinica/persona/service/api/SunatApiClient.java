package com.clinica.persona.service.api;

import com.clinica.config.ReniecProperties;
import com.clinica.persona.service.ReniecClient;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Free SUNAT API client for DNI consultation.
 * <p>
 * Makes an unauthenticated HTTP GET to the legacy SUNAT endpoint and parses
 * the HTML response to extract nombres, apellidoPaterno, and apellidoMaterno.
 * Only name fields are populated — SUNAT's free API does not return address,
 * birthdate, or sex data.
 * <p>
 * Never throws — all failures are caught and logged at DEBUG level,
 * returning {@link Optional#empty()}. No PII is included in log messages.
 */
@Component
public class SunatApiClient implements ReniecClient {

    private static final Logger log = LoggerFactory.getLogger(SunatApiClient.class);

    private static final Pattern INPUT_PATTERN = Pattern.compile(
        "<input[^>]*name\\s*=\\s*\"([^\"]+)\"[^>]*value\\s*=\\s*\"([^\"]*)\"",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern NOMBRES_APELLIDOS_PATTERN = Pattern.compile(
        "^([^,]+),\\s*(.+)$"
    );

    private final HttpClient httpClient;
    private final String sunatUrl;
    private final ObjectMapper objectMapper;

    public SunatApiClient(ReniecProperties rencieProperties) {
        this.httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
        this.sunatUrl = rencieProperties.sunatUrl();
        this.objectMapper = new ObjectMapper();
        log.debug("SunatApiClient initialized with URL: {}", sunatUrl);
    }

    @Override
    public Optional<PersonaDatos> consultarPorDni(String dni) {
        if (dni == null || dni.isBlank()) {
            log.debug("SUNAT consult skipped: empty DNI");
            return Optional.empty();
        }

        try {
            String url = sunatUrl + "?accion=obtenerDatosDni&numDocumento="
                + URLEncoder.encode(dni.trim(), StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(10))
                .GET()
                .build();

            log.debug("Consultando SUNAT API");
            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.debug("SUNAT API responded with status: {}", response.statusCode());
                return Optional.empty();
            }

            return parseResponse(response.body());

        } catch (Exception e) {
            log.debug("SUNAT API consultation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Routes to JSON or HTML parser based on response content type.
     * SUNAT changed from HTML to JSON format — handle both.
     */
    private Optional<PersonaDatos> parseResponse(String body) {
        String trimmed = body.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return parseJson(trimmed);
        }
        return parseHtml(trimmed);
    }

    /**
     * Parses SUNAT JSON response format.
     * <p>
     * Current endpoint returns:
     * {@code {"message":"success","lista":[{"nombresapellidos":"APELLIDOS,NOMBRES"}]}}
     * <p>
     * The nombresapellidos field is in "APELLIDO_PATERNO APELLIDO_MATERNO,NOMBRES" format.
     */
    @SuppressWarnings("unchecked")
    private Optional<PersonaDatos> parseJson(String json) {
        try {
            Map<String, Object> root = objectMapper.readValue(json, Map.class);
            if (!"success".equals(root.get("message"))) {
                log.debug("SUNAT JSON response: message not 'success'");
                return Optional.empty();
            }

            Object listaObj = root.get("lista");
            if (!(listaObj instanceof List)) {
                log.debug("SUNAT JSON response: no 'lista' array");
                return Optional.empty();
            }

            List<Map<String, Object>> lista = (List<Map<String, Object>>) listaObj;
            if (lista.isEmpty()) {
                log.debug("SUNAT JSON response: empty 'lista'");
                return Optional.empty();
            }

            Object nombresApellidosObj = lista.get(0).get("nombresapellidos");
            if (nombresApellidosObj == null) {
                log.debug("SUNAT JSON response: missing 'nombresapellidos'");
                return Optional.empty();
            }

            String nombresApellidos = nombresApellidosObj.toString().trim();
            Matcher matcher = NOMBRES_APELLIDOS_PATTERN.matcher(nombresApellidos);
            if (!matcher.matches()) {
                log.debug("SUNAT JSON response: could not parse nombresapellidos format");
                return Optional.empty();
            }

            String apellidos = matcher.group(1).trim();  // "AVILA CALDERON"
            String nombres = matcher.group(2).trim();     // "SONIA MARIA"

            // Split apellidos into paterno + materno (last space separates them)
            String apellidoPaterno;
            String apellidoMaterno;
            int lastSpace = apellidos.lastIndexOf(' ');
            if (lastSpace > 0) {
                apellidoPaterno = apellidos.substring(0, lastSpace).trim();
                apellidoMaterno = apellidos.substring(lastSpace + 1).trim();
            } else {
                apellidoPaterno = apellidos;
                apellidoMaterno = "";
            }

            PersonaDatos datos = new PersonaDatos(
                nombres, apellidoPaterno, apellidoMaterno,
                null, null, null, null
            );

            log.debug("SUNAT JSON consult successful for persona");
            return Optional.of(datos);

        } catch (Exception e) {
            log.debug("SUNAT JSON parse failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Parses SUNAT HTML response for name input fields.
     * <p>
     * The legacy SUNAT page returns HTML with pre-filled input fields:
     * {@code <input name="nombres" value="JUAN">}
     * {@code <input name="apellidoPaterno" value="PEREZ">}
     * {@code <input name="apellidoMaterno" value="LOPEZ">}
     */
    private Optional<PersonaDatos> parseHtml(String html) {
        String nombres = null;
        String apellidoPaterno = null;
        String apellidoMaterno = null;

        Matcher matcher = INPUT_PATTERN.matcher(html);
        while (matcher.find()) {
            String name = matcher.group(1).toLowerCase();
            String value = matcher.group(2).trim();

            if (value.isEmpty()) {
                continue;
            }

            switch (name) {
                case "nombres" -> nombres = value;
                case "apellidopaterno" -> apellidoPaterno = value;
                case "apellidomaterno" -> apellidoMaterno = value;
                default -> { /* ignore other fields */ }
            }
        }

        if (nombres == null || apellidoPaterno == null) {
            log.debug("SUNAT HTML parse incomplete — could not extract required fields");
            return Optional.empty();
        }

        PersonaDatos datos = new PersonaDatos(
            nombres, apellidoPaterno, apellidoMaterno,
            null, null, null, null
        );

        log.debug("SUNAT consult successful for persona");
        return Optional.of(datos);
    }
}

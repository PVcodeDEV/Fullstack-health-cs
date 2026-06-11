package com.clinica.persona.service.api;

import com.clinica.config.ReniecProperties;
import com.clinica.persona.service.ReniecClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Secure (paid) API client for full DNI consultation.
 * <p>
 * Requires a configured secure base URL and Bearer token. Returns the
 * complete {@link PersonaDatos} record (names, birthdate, sex, address, ubigeo).
 * <p>
 * Only registered as a Spring bean when {@code app.reniec.secure-enabled} is {@code true}.
 * When disabled, the fallback chain automatically skips this provider.
 * <p>
 * Never throws — all failures are caught and logged at DEBUG level,
 * returning {@link Optional#empty()}. No PII is included in log messages.
 */
@Component
@ConditionalOnProperty(name = "app.reniec.secure-enabled", havingValue = "true")
public class SecureApiClient implements ReniecClient {

    private static final Logger log = LoggerFactory.getLogger(SecureApiClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String secureUrl;
    private final String secureToken;

    public SecureApiClient(ReniecProperties rencieProperties, ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.objectMapper = objectMapper;
        this.secureUrl = rencieProperties.secureUrl();
        this.secureToken = rencieProperties.secureToken();
        log.debug("SecureApiClient initialized");
    }

    @Override
    public Optional<PersonaDatos> consultarPorDni(String dni) {
        if (dni == null || dni.isBlank()) {
            log.debug("Secure API consult skipped: empty DNI");
            return Optional.empty();
        }

        if (secureToken == null || secureToken.isBlank()) {
            log.debug("Secure API consult skipped: no token configured");
            return Optional.empty();
        }

        try {
            String url = secureUrl + (secureUrl.endsWith("/") ? "" : "/") + dni.trim();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + secureToken)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

            log.debug("Consultando Secure API");
            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.debug("Secure API responded with status: {}", response.statusCode());
                return Optional.empty();
            }

            return parseJson(response.body());

        } catch (Exception e) {
            log.debug("Secure API consultation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Parses the JSON response from the Secure API into a {@link PersonaDatos} record.
     * <p>
     * Expects a flat JSON object with snake_case keys matching {@link PersonaDatos} fields.
     *
     * @param json the raw JSON response body
     * @return populated {@link PersonaDatos} or empty if parsing fails
     */
    @SuppressWarnings("unchecked")
    private Optional<PersonaDatos> parseJson(String json) {
        try {
            Map<String, Object> data = objectMapper.readValue(json, Map.class);

            String nombres = stringValue(data, "nombres");
            String apellidoPaterno = stringValue(data, "apellido_paterno");
            String apellidoMaterno = stringValue(data, "apellido_materno");
            String direccion = stringValue(data, "direccion");
            String ubigeoDistrito = stringValue(data, "ubigeo_distrito");
            LocalDate fechaNacimiento = parseDate(data, "fecha_nacimiento");
            String sexo = stringValue(data, "sexo");

            if (nombres == null || apellidoPaterno == null) {
                log.debug("Secure API JSON parse incomplete — missing required fields");
                return Optional.empty();
            }

            PersonaDatos datos = new PersonaDatos(
                nombres, apellidoPaterno, apellidoMaterno,
                direccion, ubigeoDistrito, fechaNacimiento, sexo
            );

            log.debug("Secure API consult successful for persona");
            return Optional.of(datos);

        } catch (Exception e) {
            log.debug("Secure API JSON parse failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static String stringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString().trim() : null;
    }

    private static LocalDate parseDate(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        try {
            return LocalDate.parse(value.toString().trim());
        } catch (Exception e) {
            log.debug("Could not parse field '{}' as LocalDate: {}", key, value);
            return null;
        }
    }
}

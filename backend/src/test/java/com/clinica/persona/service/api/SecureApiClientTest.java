package com.clinica.persona.service.api;

import com.clinica.config.ReniecProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecureApiClientTest {

    private SecureApiClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        var properties = new ReniecProperties(
            "https://ww1.sunat.gob.pe/ol-ti-itfisdenreg/itfisdenreg.htm",
            "https://api.reniec.gob.pe/secure/consulta",
            "test-secure-token",
            true
        );
        client = new SecureApiClient(properties, objectMapper);
    }

    @Test
    void consultarPorDni_NullDni_ShouldReturnEmpty() {
        var result = client.consultarPorDni(null);
        assertThat(result).isEmpty();
    }

    @Test
    void consultarPorDni_EmptyDni_ShouldReturnEmpty() {
        var result = client.consultarPorDni("");
        assertThat(result).isEmpty();
    }

    @Test
    void parseJson_WithValidResponse_ShouldReturnPersonaDatos() throws Exception {
        var method = SecureApiClient.class.getDeclaredMethod("parseJson", String.class);
        method.setAccessible(true);

        String validJson = """
            {
                "nombres": "JUAN",
                "apellido_paterno": "PEREZ",
                "apellido_materno": "LOPEZ",
                "direccion": "Av. Principal 123",
                "ubigeo_distrito": "150101",
                "fecha_nacimiento": "1990-01-15",
                "sexo": "M"
            }
            """;

        @SuppressWarnings("unchecked")
        var result = (java.util.Optional<PersonaDatos>) method.invoke(client, validJson);

        assertThat(result).isPresent();
        assertThat(result.get().nombres()).isEqualTo("JUAN");
        assertThat(result.get().apellidoPaterno()).isEqualTo("PEREZ");
        assertThat(result.get().apellidoMaterno()).isEqualTo("LOPEZ");
        assertThat(result.get().direccion()).isEqualTo("Av. Principal 123");
        assertThat(result.get().ubigeoDistrito()).isEqualTo("150101");
        assertThat(result.get().fechaNacimiento()).isNotNull();
        assertThat(result.get().fechaNacimiento().toString()).isEqualTo("1990-01-15");
        assertThat(result.get().sexo()).isEqualTo("M");
    }

    @Test
    void parseJson_WithMissingRequiredFields_ShouldReturnEmpty() throws Exception {
        var method = SecureApiClient.class.getDeclaredMethod("parseJson", String.class);
        method.setAccessible(true);

        String incompleteJson = """
            {
                "nombres": "JUAN",
                "direccion": "Av. Principal 123"
            }
            """;

        @SuppressWarnings("unchecked")
        var result = (java.util.Optional<PersonaDatos>) method.invoke(client, incompleteJson);

        assertThat(result).isEmpty();
    }

    @Test
    void parseJson_WithMalformedJson_ShouldReturnEmpty() throws Exception {
        var method = SecureApiClient.class.getDeclaredMethod("parseJson", String.class);
        method.setAccessible(true);

        String malformedJson = "this is not json";

        @SuppressWarnings("unchecked")
        var result = (java.util.Optional<PersonaDatos>) method.invoke(client, malformedJson);

        assertThat(result).isEmpty();
    }

    @Test
    void parseJson_WithNullFields_ShouldHandleGracefully() throws Exception {
        var method = SecureApiClient.class.getDeclaredMethod("parseJson", String.class);
        method.setAccessible(true);

        String jsonWithNulls = """
            {
                "nombres": "JUAN",
                "apellido_paterno": "PEREZ",
                "apellido_materno": null,
                "direccion": null,
                "ubigeo_distrito": null,
                "fecha_nacimiento": null,
                "sexo": null
            }
            """;

        @SuppressWarnings("unchecked")
        var result = (java.util.Optional<PersonaDatos>) method.invoke(client, jsonWithNulls);

        assertThat(result).isPresent();
        assertThat(result.get().nombres()).isEqualTo("JUAN");
        assertThat(result.get().apellidoPaterno()).isEqualTo("PEREZ");
        assertThat(result.get().apellidoMaterno()).isNull();
    }

    @Test
    void parseJson_WithInvalidDate_ShouldReturnNullForDate() throws Exception {
        var method = SecureApiClient.class.getDeclaredMethod("parseJson", String.class);
        method.setAccessible(true);

        String jsonWithInvalidDate = """
            {
                "nombres": "MARIA",
                "apellido_paterno": "GARCIA",
                "apellido_materno": "RODRIGUEZ",
                "fecha_nacimiento": "not-a-date"
            }
            """;

        @SuppressWarnings("unchecked")
        var result = (java.util.Optional<PersonaDatos>) method.invoke(client, jsonWithInvalidDate);

        assertThat(result).isPresent();
        assertThat(result.get().fechaNacimiento()).isNull();
    }

    @Test
    void consultarPorDni_WithEmptyToken_ShouldReturnEmpty() {
        var properties = new ReniecProperties(
            "https://ww1.sunat.gob.pe/ol-ti-itfisdenreg/itfisdenreg.htm",
            "https://api.reniec.gob.pe/secure/consulta",
            "",
            true
        );
        var clientWithoutToken = new SecureApiClient(properties, objectMapper);

        var result = clientWithoutToken.consultarPorDni("12345678");
        assertThat(result).isEmpty();
    }
}

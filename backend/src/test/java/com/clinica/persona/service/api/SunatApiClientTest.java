package com.clinica.persona.service.api;

import com.clinica.config.ReniecProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SunatApiClientTest {

    private SunatApiClient client;

    @BeforeEach
    void setUp() {
        var properties = new ReniecProperties(
            "https://ww1.sunat.gob.pe/ol-ti-itfisdenreg/itfisdenreg.htm",
            "https://api.reniec.gob.pe/secure/consulta",
            "test-token",
            false
        );
        client = new SunatApiClient(properties);
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
    void consultarPorDni_BlankDni_ShouldReturnEmpty() {
        var result = client.consultarPorDni("   ");
        assertThat(result).isEmpty();
    }

    @Test
    void parseHtml_WithValidInput_ShouldReturnPersonaDatos() throws Exception {
        // Directly invoke the parseHtml method via reflection to test the parsing logic
        var method = SunatApiClient.class.getDeclaredMethod("parseHtml", String.class);
        method.setAccessible(true);

        String validHtml = """
            <html>
            <input name="nombres" value="JUAN">
            <input name="apellidoPaterno" value="PEREZ">
            <input name="apellidoMaterno" value="LOPEZ">
            </html>
            """;

        @SuppressWarnings("unchecked")
        var result = (java.util.Optional<PersonaDatos>) method.invoke(client, validHtml);

        assertThat(result).isPresent();
        assertThat(result.get().nombres()).isEqualTo("JUAN");
        assertThat(result.get().apellidoPaterno()).isEqualTo("PEREZ");
        assertThat(result.get().apellidoMaterno()).isEqualTo("LOPEZ");
    }

    @Test
    void parseHtml_WithEmptyValues_ShouldSkipEmptyFields() throws Exception {
        var method = SunatApiClient.class.getDeclaredMethod("parseHtml", String.class);
        method.setAccessible(true);

        String htmlWithEmpty = """
            <html>
            <input name="nombres" value="">
            <input name="apellidoPaterno" value="PEREZ">
            <input name="apellidoMaterno" value="">
            </html>
            """;

        @SuppressWarnings("unchecked")
        var result = (java.util.Optional<PersonaDatos>) method.invoke(client, htmlWithEmpty);

        // nombres is empty, so it should return empty
        assertThat(result).isEmpty();
    }

    @Test
    void parseHtml_WithMalformedHtml_ShouldReturnEmpty() throws Exception {
        var method = SunatApiClient.class.getDeclaredMethod("parseHtml", String.class);
        method.setAccessible(true);

        String malformedHtml = """
            <html>
            <div>No input fields here</div>
            <p>Some random content</p>
            </html>
            """;

        @SuppressWarnings("unchecked")
        var result = (java.util.Optional<PersonaDatos>) method.invoke(client, malformedHtml);

        assertThat(result).isEmpty();
    }

    @Test
    void parseHtml_WithMissingNames_ShouldReturnEmpty() throws Exception {
        var method = SunatApiClient.class.getDeclaredMethod("parseHtml", String.class);
        method.setAccessible(true);

        String htmlWithoutNames = """
            <html>
            <input name="apellidoPaterno" value="PEREZ">
            <input name="apellidoMaterno" value="LOPEZ">
            </html>
            """;

        @SuppressWarnings("unchecked")
        var result = (java.util.Optional<PersonaDatos>) method.invoke(client, htmlWithoutNames);

        assertThat(result).isEmpty();
    }

    @Test
    void parseHtml_WithExtraFields_ShouldExtractOnlyNames() throws Exception {
        var method = SunatApiClient.class.getDeclaredMethod("parseHtml", String.class);
        method.setAccessible(true);

        String htmlWithExtra = """
            <html>
            <input name="nombres" value="MARIA">
            <input name="apellidoPaterno" value="GARCIA">
            <input name="apellidoMaterno" value="RODRIGUEZ">
            <input name="direccion" value="Av. Siempre Viva 123">
            <input name="sexo" value="F">
            </html>
            """;

        @SuppressWarnings("unchecked")
        var result = (java.util.Optional<PersonaDatos>) method.invoke(client, htmlWithExtra);

        assertThat(result).isPresent();
        assertThat(result.get().nombres()).isEqualTo("MARIA");
        assertThat(result.get().apellidoPaterno()).isEqualTo("GARCIA");
        assertThat(result.get().apellidoMaterno()).isEqualTo("RODRIGUEZ");
        // Extra fields should be null since SUNAT only returns names
        assertThat(result.get().direccion()).isNull();
        assertThat(result.get().sexo()).isNull();
    }

    @Test
    void consultarPorDni_WithInvalidUrl_ShouldReturnEmpty() {
        // This will try to connect to a non-existent URL and should return empty
        var properties = new ReniecProperties(
            "https://nonexistent.example.invalid/api",
            "https://api.reniec.gob.pe/secure/consulta",
            "test-token",
            false
        );
        var testClient = new SunatApiClient(properties);

        var result = testClient.consultarPorDni("12345678");

        assertThat(result).isEmpty();
    }
}

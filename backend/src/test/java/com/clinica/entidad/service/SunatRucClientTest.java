package com.clinica.entidad.service;

import com.clinica.config.EntidadProperties;
import com.clinica.entidad.dto.SunatRucResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SunatRucClientTest {

    private EntidadProperties properties;
    private SunatRucClient client;

    @BeforeEach
    void setUp() {
        properties = new EntidadProperties(
            "https://sunat.example.com/ruc", 5000, 5000);
        client = new SunatRucClient(properties);
    }

    @Test
    void consultarRuc20_ShouldReturnRazonSocial() {
        // This tests with the actual HTTP client against a mock
        // For unit testing, we use a mocked HttpClient
        SunatRucClient clientWithMock = createClientWithMockHttpClient(
            """
            <html>
            <input name="rsoSocial" value="CLINICA EJEMPLO SAC">
            <input name="direccionCompleta" value="AV. PRINCIPAL 123, LIMA">
            <input name="codUbigeo" value="150101">
            </html>
            """,
            200
        );

        Optional<SunatRucResponse> result = clientWithMock.consultar("20123456789");

        assertThat(result).isPresent();
        assertThat(result.get().razonSocial()).isEqualTo("CLINICA EJEMPLO SAC");
        assertThat(result.get().direccionFiscal()).isEqualTo("AV. PRINCIPAL 123, LIMA");
        assertThat(result.get().ubigeo()).isEqualTo("150101");
        assertThat(result.get().exito()).isTrue();
    }

    @Test
    void consultarRuc10_ShouldReturnNombreCompleto() {
        SunatRucClient clientWithMock = createClientWithMockHttpClient(
            """
            <html>
            <input name="apenomdenunciado" value="JUAN PEREZ LOPEZ">
            </html>
            """,
            200
        );

        Optional<SunatRucResponse> result = clientWithMock.consultar("10123456789");

        assertThat(result).isPresent();
        assertThat(result.get().nombreCompleto()).isEqualTo("JUAN PEREZ LOPEZ");
        assertThat(result.get().razonSocial()).isNull();
        assertThat(result.get().direccionFiscal()).isNull();
        assertThat(result.get().exito()).isTrue();
    }

    @Test
    void consultar_WhenTimeout_ShouldReturnEmpty() {
        // Create a client that simulates timeout by throwing an exception
        SunatRucClient clientWithMock = createClientWithMockHttpClientThatFails();

        Optional<SunatRucResponse> result = clientWithMock.consultar("20123456789");

        assertThat(result).isEmpty();
    }

    @Test
    void consultar_WithNullRuc_ShouldReturnEmpty() {
        Optional<SunatRucResponse> result = client.consultar(null);
        assertThat(result).isEmpty();
    }

    @Test
    void consultar_WithBlankRuc_ShouldReturnEmpty() {
        Optional<SunatRucResponse> result = client.consultar("   ");
        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private SunatRucClient createClientWithMockHttpClient(String html, int statusCode) {
        EntidadProperties props = new EntidadProperties(
            "https://sunat.example.com/ruc", 5000, 5000);

        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        try {
            when(mockHttpClient.send(any(HttpRequest.class),
                    any(HttpResponse.BodyHandler.class)))
                .thenAnswer(invocation -> {
                    // Return the mock response
                    when(mockResponse.statusCode()).thenReturn(statusCode);
                    when(mockResponse.body()).thenReturn(html);
                    return mockResponse;
                });
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        SunatRucClient client = new SunatRucClient(props) {
            @Override
            HttpClient getHttpClient() {
                return mockHttpClient;
            }
        };
        return client;
    }

    @SuppressWarnings("unchecked")
    private SunatRucClient createClientWithMockHttpClientThatFails() {
        EntidadProperties props = new EntidadProperties(
            "https://sunat.example.com/ruc", 5000, 5000);

        HttpClient mockHttpClient = mock(HttpClient.class);

        try {
            when(mockHttpClient.send(any(HttpRequest.class),
                    any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.net.ConnectException("Connection timed out"));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        SunatRucClient client = new SunatRucClient(props) {
            @Override
            HttpClient getHttpClient() {
                return mockHttpClient;
            }
        };
        return client;
    }
}

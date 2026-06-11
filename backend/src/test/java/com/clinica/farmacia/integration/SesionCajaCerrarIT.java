package com.clinica.farmacia.integration;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.caja.dto.SesionCajaAbrirRequest;
import com.clinica.farmacia.caja.dto.SesionCajaCerrarRequest;
import com.clinica.farmacia.caja.dto.SesionCajaResponse;
import com.clinica.farmacia.caja.service.SesionCajaService;
import com.clinica.farmacia.caja.type.EstadoSesion;
import com.clinica.farmacia.testsupport.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cash session close: diferencia calculation + reject abrir when already open.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestIntegrationConfig.class)
class SesionCajaCerrarIT {

    @Autowired
    private SesionCajaService sesionCajaService;

    @Autowired
    private AlmacenRepository almacenRepository;

    private Almacen savedAlmacen;

    @BeforeEach
    void setUp() {
        savedAlmacen = almacenRepository.saveAndFlush(TestDataBuilder.almacenDefecto());
    }

    @Test
    void shouldCalculateDiferenciaCorrectly() {
        // Open with montoApertura = 500
        SesionCajaAbrirRequest abrirRequest = new SesionCajaAbrirRequest(
            savedAlmacen.getId(), new BigDecimal("500.00"), "Apertura caja test");
        SesionCajaResponse sesion = sesionCajaService.abrir(abrirRequest, 44L);

        // Simulate a sale by directly updating totalVentas (integration test shortcut)
        sesionCajaService.registrarVenta(sesion.id(), new BigDecimal("290.00"));

        // Close with montoCierreReal = 800
        // montoCierreEsperado = 500 + 290 = 790
        // diferencia = 800 - 790 = 10
        SesionCajaCerrarRequest cerrarRequest = new SesionCajaCerrarRequest(
            new BigDecimal("800.00"), "Cierre test");
        SesionCajaResponse closed = sesionCajaService.cerrar(sesion.id(), cerrarRequest);

        assertThat(closed.estado()).isEqualTo(EstadoSesion.CERRADA);
        assertThat(closed.montoCierreEsperado()).isEqualByComparingTo(new BigDecimal("790.00"));
        assertThat(closed.montoCierreReal()).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(closed.diferenciaCierre()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(closed.fechaCierre()).isNotNull();
    }

    @Test
    void shouldRejectAbrirWhenAlreadyOpen() {
        SesionCajaAbrirRequest abrirRequest = new SesionCajaAbrirRequest(
            savedAlmacen.getId(), new BigDecimal("500.00"), "Primera apertura");
        sesionCajaService.abrir(abrirRequest, 45L);

        assertThatThrownBy(() -> sesionCajaService.abrir(abrirRequest, 45L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ya tiene una sesión de caja abierta");
    }
}

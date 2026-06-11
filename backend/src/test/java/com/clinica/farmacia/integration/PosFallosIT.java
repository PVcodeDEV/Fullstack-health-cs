package com.clinica.farmacia.integration;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.caja.dto.SesionCajaAbrirRequest;
import com.clinica.farmacia.caja.dto.SesionCajaCerrarRequest;
import com.clinica.farmacia.caja.dto.SesionCajaResponse;
import com.clinica.farmacia.caja.service.SesionCajaService;
import com.clinica.farmacia.lote.dto.LoteRequest;
import com.clinica.farmacia.lote.service.LoteService;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import com.clinica.farmacia.testsupport.TestDataBuilder;
import com.clinica.farmacia.venta.dto.DetalleVentaRequest;
import com.clinica.farmacia.venta.dto.VentaRequest;
import com.clinica.farmacia.venta.service.VentaService;
import com.clinica.farmacia.venta.type.TipoLista;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * POS failure scenarios: stock insuficiente, sesión no abierta, descuento clamped.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestIntegrationConfig.class)
class PosFallosIT {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private LoteService loteService;

    @Autowired
    private SesionCajaService sesionCajaService;

    @Autowired
    private VentaService ventaService;

    private Producto savedProducto;
    private Almacen savedAlmacen;
    private Long loteId;
    private Long sesionId;

    @BeforeEach
    void setUp() {
        savedProducto = productoRepository.saveAndFlush(TestDataBuilder.productoValido());
        savedAlmacen = almacenRepository.saveAndFlush(TestDataBuilder.almacenDefecto());

        // Receive stock — only 2 units (to test stock insuficiente with request of 3)
        LoteRequest loteRequest = new LoteRequest(
            savedProducto.getId(),
            "LOTE-FALLOS-001",
            LocalDate.now().plusYears(2),
            2,
            new BigDecimal("5.0000"),
            savedAlmacen.getId(),
            42L,
            "Recepción test fallos"
        );
        loteId = loteService.recibir(loteRequest).id();

        // Open session
        SesionCajaAbrirRequest abrirRequest = new SesionCajaAbrirRequest(
            savedAlmacen.getId(), new BigDecimal("500.00"), "Apertura fallos");
        SesionCajaResponse sesion = sesionCajaService.abrir(abrirRequest, 43L);
        sesionId = sesion.id();
    }

    @AfterEach
    void tearDown() {
        if (sesionId != null) {
            try {
                sesionCajaService.cerrar(sesionId,
                    new SesionCajaCerrarRequest(BigDecimal.ZERO, "Cleanup"));
            } catch (Exception ignored) {
                // Session may already be closed by the test
            }
        }
    }

    @Test
    void shouldRejectStockInsuficiente() {
        // Only 2 units in stock, requesting 3 → error
        VentaRequest request = new VentaRequest(
            sesionId, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(loteId, 3, BigDecimal.ZERO))
        );

        assertThatThrownBy(() -> ventaService.completar(request, 42L))
            .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void shouldRejectSesionNoAbierta() {
        // Close the session first
        sesionCajaService.cerrar(sesionId,
            new com.clinica.farmacia.caja.dto.SesionCajaCerrarRequest(
                new BigDecimal("500.00"), "Cierre test"));

        // Now try to sell
        VentaRequest request = new VentaRequest(
            sesionId, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(loteId, 1, BigDecimal.ZERO))
        );

        assertThatThrownBy(() -> ventaService.completar(request, 42L))
            .hasMessageContaining("no está abierta");
    }

    @Test
    void shouldClampDiscountToCostoMasIgv() {
        // precioCosto = 5.00, IGV = 18% → costo+IGV = 5.90
        // precioVentaPublico = 12.00
        // Request large manual discount that exceeds cost+IGV floor
        VentaRequest request = new VentaRequest(
            sesionId, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(loteId, 1, new BigDecimal("8.00"))) // manual discount
        );

        var response = ventaService.completar(request, 42L);

        // maxDescuentoPct = 12.00 * 20% = 2.40
        // maxDescuentoFisico = 12.00 - (5.00 + 0.90) = 12.00 - 5.90 = 6.10
        // max = min(2.40, 6.10) = 2.40
        // descuentoTotal = min(8.00, 2.40) = 2.40
        // precioUnitarioFinal = 12.00 - 2.40 = 9.60
        assertThat(response.detalles().get(0).precioUnitario())
            .isEqualByComparingTo(new BigDecimal("9.60"));
        assertThat(response.detalles().get(0).descuentoAplicado())
            .isEqualByComparingTo(new BigDecimal("2.40"));
        assertThat(response.detalles().get(0).precioOriginal())
            .isEqualByComparingTo(new BigDecimal("12.00"));
        // subtotal = 9.60 * 1 = 9.60
        assertThat(response.detalles().get(0).subtotal())
            .isEqualByComparingTo(new BigDecimal("9.60"));
    }
}

package com.clinica.farmacia.integration;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.caja.dto.SesionCajaAbrirRequest;
import com.clinica.farmacia.caja.dto.SesionCajaCerrarRequest;
import com.clinica.farmacia.caja.dto.SesionCajaResponse;
import com.clinica.farmacia.caja.entity.SesionCaja;
import com.clinica.farmacia.caja.repository.SesionCajaRepository;
import com.clinica.farmacia.caja.service.SesionCajaService;
import com.clinica.farmacia.caja.type.EstadoSesion;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.lote.entity.MovimientoStock;
import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.lote.repository.MovimientoStockRepository;
import com.clinica.farmacia.lote.service.LoteService;
import com.clinica.farmacia.lote.type.TipoMovimiento;
import com.clinica.farmacia.lote.dto.LoteRequest;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.entity.Producto.TipoProducto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import com.clinica.farmacia.venta.dto.DetalleVentaRequest;
import com.clinica.farmacia.venta.dto.VentaRequest;
import com.clinica.farmacia.venta.dto.VentaResponse;
import com.clinica.farmacia.venta.repository.VentaRepository;
import com.clinica.farmacia.venta.service.VentaService;
import com.clinica.farmacia.venta.type.EstadoVenta;
import com.clinica.farmacia.venta.type.TipoLista;
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

/**
 * Full POS flow: open session → receive lote → sell → assert state.
 * This is the most important integration test in the farmacia suite.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestIntegrationConfig.class)
class PosFlujoCompletoIT {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimientoStockRepository movimientoStockRepository;

    @Autowired
    private SesionCajaRepository sesionCajaRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private SesionCajaService sesionCajaService;

    @Autowired
    private LoteService loteService;

    @Autowired
    private VentaService ventaService;

    private Producto savedProducto;
    private Almacen savedAlmacen;

    @BeforeEach
    void setUp() {
        // Create product
        savedProducto = new Producto();
        savedProducto.setCodigo("PROD-FLUJO-PARACETAMOL");
        savedProducto.setTipo(TipoProducto.MEDICAMENTO);
        savedProducto.setPrecioCosto(new BigDecimal("5.0000"));
        savedProducto.setUtilidadMedico(new BigDecimal("20.00"));
        savedProducto.setUtilidadPublico(new BigDecimal("20.00"));
        savedProducto.setPrecioVentaMedico(new BigDecimal("7.10"));
        savedProducto.setPrecioVentaPublico(new BigDecimal("12.00"));
        savedProducto.setStockMinimo(20);
        savedProducto.setStockCritico(5);
        savedProducto = productoRepository.saveAndFlush(savedProducto);

        // Create warehouse
        savedAlmacen = new Almacen();
        savedAlmacen.setCodigo("ALM-FLUJO");
        savedAlmacen.setNombre("Almacén Flujo");
        savedAlmacen.setDefaultWarehouse(true);
        savedAlmacen = almacenRepository.saveAndFlush(savedAlmacen);
    }

    @Test
    void shouldCompleteFullPosFlow() {
        // ===== STEP 1: Open cash session =====
        SesionCajaAbrirRequest abrirRequest = new SesionCajaAbrirRequest(
            savedAlmacen.getId(), new BigDecimal("500.00"), "Apertura test");
        SesionCajaResponse sesion = sesionCajaService.abrir(abrirRequest, 42L);

        assertThat(sesion).isNotNull();
        assertThat(sesion.estado()).isEqualTo(EstadoSesion.ABIERTA);
        assertThat(sesion.montoApertura()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(sesion.totalVentas()).isEqualByComparingTo(BigDecimal.ZERO);
        Long sesionId = sesion.id();

        // ===== STEP 2: Receive stock (lote with 100 units) =====
        LoteRequest loteRequest = new LoteRequest(
            savedProducto.getId(),
            "LOTE-FLUJO-001",
            LocalDate.now().plusYears(2),
            100,
            new BigDecimal("5.0000"),
            savedAlmacen.getId(),
            42L,
            "Recepción test"
        );
        var loteResponse = loteService.recibir(loteRequest);
        assertThat(loteResponse.stockActual()).isEqualTo(100);
        Long loteId = loteResponse.id();

        // ===== STEP 3: Sell 3 units at PUBLICO price =====
        VentaRequest ventaRequest = new VentaRequest(
            sesionId, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(loteId, 3, BigDecimal.ZERO))
        );
        VentaResponse venta = ventaService.completar(ventaRequest, 42L);

        // Assert correlativo = 1 (first sale in this session)
        assertThat(venta.correlativo()).isEqualTo(1);
        assertThat(venta.estado()).isEqualTo(EstadoVenta.COMPLETADA);
        assertThat(venta.detalles()).hasSize(1);
        assertThat(venta.detalles().get(0).cantidad()).isEqualTo(3);

        // precioUnitario = precioVentaPublico = 12.00, no discounts
        // subtotal = 3 * 12.00 = 36.00
        assertThat(venta.subtotal()).isEqualByComparingTo(new BigDecimal("36.00"));
        assertThat(venta.total()).isEqualByComparingTo(new BigDecimal("36.00"));

        // ===== STEP 4: Assert stock updated =====
        Lote updatedLote = loteRepository.findById(loteId).orElseThrow();
        // Stock started at 100, we sold 3, so stockActual = 97.
        // @Version is now on a separate lote_version field (see V35), not on stockActual.
        assertThat(updatedLote.getStockActual()).isEqualTo(97);

        // ===== STEP 5: Assert MovimientoStock logged =====
        List<MovimientoStock> movimientos = movimientoStockRepository.findAll();
        assertThat(movimientos).isNotEmpty();
        var salidaMov = movimientos.stream()
            .filter(m -> m.getLote().getId().equals(loteId)
                && m.getTipo() == TipoMovimiento.SALIDA)
            .findFirst();
        assertThat(salidaMov).isPresent();
        assertThat(salidaMov.get().getCantidad()).isEqualTo(3);

        // ===== STEP 6: Assert SesionCaja totalVentas updated =====
        SesionCaja updatedSesion = sesionCajaRepository.findById(sesionId).orElseThrow();
        assertThat(updatedSesion.getTotalVentas()).isEqualByComparingTo(new BigDecimal("36.00"));

        // ===== STEP 7: Close session with diferencia = 0 =====
        // montoCierreEsperado = 500 + 36 = 536
        SesionCajaCerrarRequest cerrarRequest = new SesionCajaCerrarRequest(
            new BigDecimal("536.00"), "Cierre test");
        SesionCajaResponse closedSession = sesionCajaService.cerrar(sesionId, cerrarRequest);

        assertThat(closedSession.estado()).isEqualTo(EstadoSesion.CERRADA);
        assertThat(closedSession.montoCierreReal()).isEqualByComparingTo(new BigDecimal("536.00"));
        assertThat(closedSession.diferenciaCierre()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}

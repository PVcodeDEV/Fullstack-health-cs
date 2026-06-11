package com.clinica.farmacia.venta.service;

import com.clinica.farmacia.caja.entity.SesionCaja;
import com.clinica.farmacia.caja.repository.SesionCajaRepository;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.lote.entity.MovimientoStock;
import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.lote.repository.MovimientoStockRepository;
import com.clinica.farmacia.lote.service.DescuentoService;
import com.clinica.farmacia.lote.type.TipoMovimiento;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.venta.dto.DetalleVentaRequest;
import com.clinica.farmacia.venta.dto.VentaRequest;
import com.clinica.farmacia.venta.dto.VentaResponse;
import com.clinica.farmacia.venta.entity.DetalleVenta;
import com.clinica.farmacia.venta.entity.Venta;
import com.clinica.farmacia.venta.repository.DetalleVentaRepository;
import com.clinica.farmacia.venta.repository.VentaRepository;
import com.clinica.farmacia.venta.type.EstadoVenta;
import com.clinica.farmacia.venta.type.TipoLista;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @Mock
    private SesionCajaRepository sesionCajaRepository;

    @Mock
    private DescuentoService descuentoService;

    @Captor
    private ArgumentCaptor<Venta> ventaCaptor;

    @Captor
    private ArgumentCaptor<MovimientoStock> movimientoCaptor;

    @Captor
    private ArgumentCaptor<Lote> loteCaptor;

    private VentaService ventaService;
    private Producto producto;
    private Lote lote;
    private SesionCaja sesion;

    private static final Long VENDEDOR_ID = 42L;

    @BeforeEach
    void setUp() {
        // Create VentaService with real TransactionTemplate backed by null PTM.
        // Tests call completarInternal() directly (package-private) to bypass
        // TransactionTemplate retry wrapper, keeping tests pure unit.
        ventaService = new VentaService(
            ventaRepository, detalleVentaRepository, loteRepository,
            movimientoStockRepository, sesionCajaRepository,
            descuentoService, null /* ptm */
        );

        // --- Fixtures ---
        producto = new Producto();
        producto.setId(1L);
        producto.setCodigo("PARACETAMOL-500");
        producto.setActivo(true);
        producto.setPrecioCosto(new BigDecimal("5.0000"));
        producto.setPrecioVentaMedico(new BigDecimal("10.00"));
        producto.setPrecioVentaPublico(new BigDecimal("12.00"));

        lote = new Lote();
        lote.setId(1L);
        lote.setProducto(producto);
        lote.setCodigoLote("LOTE-001");
        lote.setStockActual(100);
        lote.setActivo(true);
        lote.setPrecioCosto(new BigDecimal("5.0000"));

        sesion = new SesionCaja();
        sesion.setId(1L);
        sesion.setEstado("ABIERTA");
    }

    @Test
    void shouldCompletarVentaSuccessfully() {
        // SC-11: COMPLETAR → stock decrements, correlativo assigned
        VentaRequest request = new VentaRequest(
            1L, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(1L, 5, BigDecimal.ZERO))
        );

        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(ventaRepository.findMaxCorrelativoBySesionCajaId(1L)).thenReturn(0);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(descuentoService.calcularDescuento(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        VentaResponse response = ventaService.completarInternal(request, VENDEDOR_ID);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.correlativo()).isEqualTo(1);
        assertThat(response.estado()).isEqualTo(EstadoVenta.COMPLETADA);
        assertThat(response.vendedorUsuarioId()).isEqualTo(VENDEDOR_ID);
        assertThat(response.detalles()).hasSize(1);
        assertThat(response.detalles().get(0).cantidad()).isEqualTo(5);

        // Verify stock decremented
        verify(loteRepository).save(loteCaptor.capture());
        Lote savedLote = loteCaptor.getValue();
        assertThat(savedLote.getStockActual()).isEqualTo(95); // 100 - 5

        // Verify MovimientoStock logged
        verify(movimientoStockRepository).save(movimientoCaptor.capture());
        MovimientoStock mov = movimientoCaptor.getValue();
        assertThat(mov.getTipo()).isEqualTo(TipoMovimiento.SALIDA);
        assertThat(mov.getCantidad()).isEqualTo(5);
        assertThat(mov.getUsuarioId()).isEqualTo(VENDEDOR_ID);

        // Verify Venta saved with correct totals
        verify(ventaRepository).save(ventaCaptor.capture());
        Venta savedVenta = ventaCaptor.getValue();
        assertThat(savedVenta.getCorrelativo()).isEqualTo(1);
        assertThat(savedVenta.getVendedorUsuarioId()).isEqualTo(VENDEDOR_ID);
        assertThat(savedVenta.getTipoLista()).isEqualTo(TipoLista.PUBLICO);
        // subtotal = 5 * 12.00 = 60.00 → total with IGV = 60.00
        assertThat(savedVenta.getSubtotal()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(savedVenta.getTotal()).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void shouldRejectWhenStockInsuficiente() {
        lote.setStockActual(3); // Only 3 in stock
        VentaRequest request = new VentaRequest(
            1L, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(1L, 5, BigDecimal.ZERO))
        );

        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(ventaRepository.findMaxCorrelativoBySesionCajaId(1L)).thenReturn(0);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        // No descuentoService stub needed — stock validation fails first

        assertThatThrownBy(() -> ventaService.completarInternal(request, VENDEDOR_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Stock insuficiente")
            .hasMessageContaining("solicitado 5")
            .hasMessageContaining("disponible 3");
    }

    @Test
    void shouldRejectWhenLoteInactivo() {
        lote.setActivo(false);
        VentaRequest request = new VentaRequest(
            1L, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(1L, 5, BigDecimal.ZERO))
        );

        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(ventaRepository.findMaxCorrelativoBySesionCajaId(1L)).thenReturn(0);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));

        assertThatThrownBy(() -> ventaService.completarInternal(request, VENDEDOR_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("inactivo");
    }

    @Test
    void shouldAsignarCorrelativoIncremental() {
        // Two previous sales in session (maxCorrelativo = 2)
        VentaRequest request = new VentaRequest(
            1L, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(1L, 1, BigDecimal.ZERO))
        );

        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(ventaRepository.findMaxCorrelativoBySesionCajaId(1L)).thenReturn(2);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(descuentoService.calcularDescuento(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        VentaResponse response = ventaService.completarInternal(request, VENDEDOR_ID);

        assertThat(response.correlativo()).isEqualTo(3); // max(2) + 1 = 3

        verify(ventaRepository).save(ventaCaptor.capture());
        assertThat(ventaCaptor.getValue().getCorrelativo()).isEqualTo(3);
    }

    @Test
    void shouldAnularVentaRestoreStock() {
        // SC-15: Anulación restores stock + logs DEVOLUCION
        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(1L);
        detalle.setLote(lote);
        detalle.setCantidad(5);
        detalle.setPrecioUnitario(new BigDecimal("12.0000"));
        detalle.setPrecioOriginal(new BigDecimal("12.0000"));
        detalle.setDescuentoAplicado(BigDecimal.ZERO);
        detalle.setSubtotal(new BigDecimal("60.00"));

        Venta venta = new Venta();
        venta.setId(1L);
        venta.setSesionCaja(sesion);
        venta.setCorrelativo(1);
        venta.setTipoLista(TipoLista.PUBLICO);
        venta.setEstado(EstadoVenta.COMPLETADA);
        venta.setVendedorUsuarioId(VENDEDOR_ID);
        venta.addDetalle(detalle);
        venta.calcularTotales();

        lote.setStockActual(95); // After sale, stock = 95

        when(ventaRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(venta));
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        VentaResponse response = ventaService.anular(1L, VENDEDOR_ID);

        assertThat(response.estado()).isEqualTo(EstadoVenta.ANULADA);

        // Verify stock restored
        verify(loteRepository).save(loteCaptor.capture());
        assertThat(loteCaptor.getValue().getStockActual()).isEqualTo(100); // 95 + 5

        // Verify DEVOLUCION movimiento logged
        verify(movimientoStockRepository).save(movimientoCaptor.capture());
        assertThat(movimientoCaptor.getValue().getTipo()).isEqualTo(TipoMovimiento.DEVOLUCION);
        assertThat(movimientoCaptor.getValue().getCantidad()).isEqualTo(5);
        assertThat(movimientoCaptor.getValue().getVentaId()).isEqualTo(1L);
    }

    @Test
    void shouldApplyLifecycleDiscountAndManualDiscount() {
        // Both discounts apply, clamped to max 20%
        VentaRequest request = new VentaRequest(
            1L, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(1L, 2, new BigDecimal("0.50"))) // manual discount
        );

        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(ventaRepository.findMaxCorrelativoBySesionCajaId(1L)).thenReturn(0);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        // Lifecycle discount = 0.80
        when(descuentoService.calcularDescuento(any(), any(), any())).thenReturn(new BigDecimal("0.80"));
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        VentaResponse response = ventaService.completarInternal(request, VENDEDOR_ID);

        // precioOriginal = 12.00
        // lifecycle = 0.80, manual = 0.50, total = 1.30
        // maxDescuentoPct = 12.00 * 20% = 2.40
        // maxDescuentoFisico = 12.00 - (5.00 + 5.00*0.18) = 12.00 - 5.90 = 6.10
        // max = min(2.40, 6.10) = 2.40
        // descuentoTotal = min(1.30, 2.40) = 1.30
        // precioUnitarioFinal = 12.00 - 1.30 = 10.70 → roundPrecio(10.70) = 10.70
        // subtotalLinea = 10.70 * 2 = 21.40

        assertThat(response.detalles().get(0).precioUnitario()).isEqualByComparingTo(new BigDecimal("10.70"));
        assertThat(response.detalles().get(0).precioOriginal()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(response.detalles().get(0).descuentoAplicado()).isEqualByComparingTo(new BigDecimal("1.30"));
        assertThat(response.detalles().get(0).subtotal()).isEqualByComparingTo(new BigDecimal("21.40"));
    }

    @Test
    void shouldPickPrecioCorrectByTipoLista() {
        // MEDICO uses precioVentaMedico (10.00), PUBLICO uses precioVentaPublico (12.00)
        VentaRequest medicoRequest = new VentaRequest(
            1L, null, TipoLista.MEDICO, null,
            List.of(new DetalleVentaRequest(1L, 3, BigDecimal.ZERO))
        );

        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(ventaRepository.findMaxCorrelativoBySesionCajaId(1L)).thenReturn(0);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(descuentoService.calcularDescuento(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        VentaResponse medicoResponse = ventaService.completarInternal(medicoRequest, VENDEDOR_ID);

        // MEDICO: precioOriginal = 10.00, subtotal = 3 * 10.00 = 30.00
        assertThat(medicoResponse.detalles().get(0).precioUnitario()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(medicoResponse.detalles().get(0).precioOriginal()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(medicoResponse.detalles().get(0).subtotal()).isEqualByComparingTo(new BigDecimal("30.00"));

        // PUBLICO: precioOriginal = 12.00, subtotal = 3 * 12.00 = 36.00
        VentaRequest publicoRequest = new VentaRequest(
            1L, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(1L, 3, BigDecimal.ZERO))
        );

        // Reset mocks for second call
        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(ventaRepository.findMaxCorrelativoBySesionCajaId(1L)).thenReturn(0);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(descuentoService.calcularDescuento(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        VentaResponse publicoResponse = ventaService.completarInternal(publicoRequest, VENDEDOR_ID);

        assertThat(publicoResponse.detalles().get(0).precioUnitario()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(publicoResponse.detalles().get(0).precioOriginal()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(publicoResponse.detalles().get(0).subtotal()).isEqualByComparingTo(new BigDecimal("36.00"));
    }

    @Test
    void shouldRejectWhenSesionNotFound() {
        VentaRequest request = new VentaRequest(
            999L, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(1L, 1, BigDecimal.ZERO))
        );

        when(sesionCajaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.completarInternal(request, VENDEDOR_ID))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Sesión de caja no encontrada");
    }

    @Test
    void shouldRejectWhenSesionNotAbierta() {
        sesion.setEstado("CERRADA");
        VentaRequest request = new VentaRequest(
            1L, null, TipoLista.PUBLICO, null,
            List.of(new DetalleVentaRequest(1L, 1, BigDecimal.ZERO))
        );

        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(sesion));

        assertThatThrownBy(() -> ventaService.completarInternal(request, VENDEDOR_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no está abierta");
    }

    @Test
    void shouldRejectAnularWhenAlreadyAnulada() {
        Venta venta = new Venta();
        venta.setId(1L);
        venta.setEstado(EstadoVenta.ANULADA);

        when(ventaRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(venta));

        assertThatThrownBy(() -> ventaService.anular(1L, VENDEDOR_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ya está anulada");

        verify(loteRepository, never()).save(any());
        verify(movimientoStockRepository, never()).save(any());
    }
}

package com.clinica.farmacia.lote.service;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.lote.dto.LoteRequest;
import com.clinica.farmacia.lote.dto.LoteResponse;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.lote.entity.MovimientoStock;
import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.lote.repository.MovimientoStockRepository;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private AlmacenRepository almacenRepository;

    @Captor
    private ArgumentCaptor<Lote> loteCaptor;

    @Captor
    private ArgumentCaptor<MovimientoStock> movimientoCaptor;

    private LoteService loteService;
    private Producto producto;
    private Almacen almacen;

    @BeforeEach
    void setUp() {
        loteService = new LoteService(loteRepository, movimientoStockRepository, productoRepository, almacenRepository);

        producto = new Producto();
        producto.setId(1L);
        producto.setCodigo("PARACETAMOL-500");
        producto.setActivo(true);

        almacen = new Almacen();
        almacen.setId(1L);
        almacen.setCodigo("DEF");
        almacen.setNombre("Almacén Principal");
    }

    @Test
    void shouldRecibirStockAtomicamente() {
        // SC-07: Receive 100 units → stockActual=100
        LoteRequest request = new LoteRequest(
            1L, "LOTE-001", LocalDate.now().plusMonths(12),
            100, new BigDecimal("5.0000"),
            1L, 1L, "Recepción inicial"
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(almacen));

        Lote savedLote = new Lote();
        savedLote.setId(1L);
        savedLote.setProducto(producto);
        savedLote.setCodigoLote("LOTE-001");
        savedLote.setFechaVencimiento(request.fechaVencimiento());
        savedLote.setStockInicial(100);
        savedLote.setStockActual(100);
        savedLote.setPrecioCosto(new BigDecimal("5.0000"));
        savedLote.setAlmacen(almacen);

        when(loteRepository.save(any(Lote.class))).thenReturn(savedLote);

        LoteResponse response = loteService.recibir(request);

        assertThat(response).isNotNull();
        assertThat(response.stockActual()).isEqualTo(100);
        assertThat(response.stockInicial()).isEqualTo(100);

        // Verify MovimientoStock(ENTRADA) was created
        verify(movimientoStockRepository).save(movimientoCaptor.capture());
        MovimientoStock mov = movimientoCaptor.getValue();
        assertThat(mov.getTipo().name()).isEqualTo("ENTRADA");
        assertThat(mov.getCantidad()).isEqualTo(100);
    }

    @Test
    void shouldRejectInactiveProduct() {
        producto.setActivo(false);
        LoteRequest request = new LoteRequest(
            1L, "LOTE-002", LocalDate.now().plusMonths(6),
            50, new BigDecimal("10.0000"),
            1L, 1L, null
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(almacen));

        assertThatThrownBy(() -> loteService.recibir(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("producto inactivo");
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        LoteRequest request = new LoteRequest(
            999L, "LOTE-003", LocalDate.now().plusMonths(3),
            10, new BigDecimal("2.0000"),
            1L, 1L, null
        );

        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loteService.recibir(request))
            .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void shouldThrowWhenAlmacenNotFound() {
        LoteRequest request = new LoteRequest(
            1L, "LOTE-004", LocalDate.now().plusMonths(6),
            30, new BigDecimal("8.0000"),
            999L, 1L, null
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(almacenRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loteService.recibir(request))
            .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }
}

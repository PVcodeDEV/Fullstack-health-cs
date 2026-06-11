package com.clinica.farmacia.lote.service;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.lote.dto.TransferenciaRequest;
import com.clinica.farmacia.lote.dto.LoteResponse;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.lote.entity.MovimientoStock;
import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.lote.repository.MovimientoStockRepository;
import com.clinica.farmacia.lote.type.TipoMovimiento;
import com.clinica.farmacia.producto.entity.Producto;
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
class TransferenciaServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @Mock
    private AlmacenRepository almacenRepository;

    @Captor
    private ArgumentCaptor<Lote> loteCaptor;

    @Captor
    private ArgumentCaptor<MovimientoStock> movimientoCaptor;

    private TransferenciaService transferenciaService;
    private Almacen almacenOrigen;
    private Almacen almacenDestino;
    private Lote loteOrigen;
    private Producto producto;

    @BeforeEach
    void setUp() {
        transferenciaService = new TransferenciaService(loteRepository, movimientoStockRepository, almacenRepository);

        producto = new Producto();
        producto.setId(1L);
        producto.setCodigo("PARACETAMOL-500");

        almacenOrigen = new Almacen();
        almacenOrigen.setId(1L);
        almacenOrigen.setCodigo("ORIG");

        almacenDestino = new Almacen();
        almacenDestino.setId(2L);
        almacenDestino.setCodigo("DEST");

        loteOrigen = new Lote();
        loteOrigen.setId(1L);
        loteOrigen.setProducto(producto);
        loteOrigen.setCodigoLote("LOTE-TRF");
        loteOrigen.setFechaVencimiento(LocalDate.now().plusMonths(12));
        loteOrigen.setStockInicial(100);
        loteOrigen.setStockActual(100);
        loteOrigen.setPrecioCosto(new BigDecimal("5.0000"));
        loteOrigen.setAlmacen(almacenOrigen);
    }

    @Test
    void shouldTransferirStockAtomicamente() {
        // SC-22: Transfer 20 from A→B → source decrements 20, dest created with 20
        TransferenciaRequest request = new TransferenciaRequest(
            1L, 1L, 2L, 20, "Transferencia de prueba"
        );

        when(loteRepository.findById(1L)).thenReturn(Optional.of(loteOrigen));
        when(almacenRepository.findById(2L)).thenReturn(Optional.of(almacenDestino));

        // Mock save for source (decremented)
        Lote loteOrigenDecrementado = new Lote();
        loteOrigenDecrementado.setId(1L);
        loteOrigenDecrementado.setProducto(producto);
        loteOrigenDecrementado.setCodigoLote("LOTE-TRF");
        loteOrigenDecrementado.setFechaVencimiento(loteOrigen.getFechaVencimiento());
        loteOrigenDecrementado.setStockInicial(100);
        loteOrigenDecrementado.setStockActual(80); // 100 - 20
        loteOrigenDecrementado.setPrecioCosto(new BigDecimal("5.0000"));
        loteOrigenDecrementado.setAlmacen(almacenOrigen);

        // Mock save for destination (new)
        Lote loteDestino = new Lote();
        loteDestino.setId(2L);
        loteDestino.setProducto(producto);
        loteDestino.setCodigoLote("LOTE-TRF");
        loteDestino.setFechaVencimiento(loteOrigen.getFechaVencimiento());
        loteDestino.setStockInicial(20);
        loteDestino.setStockActual(20);
        loteDestino.setPrecioCosto(new BigDecimal("5.0000"));
        loteDestino.setAlmacen(almacenDestino);

        when(loteRepository.save(any(Lote.class)))
            .thenReturn(loteOrigenDecrementado)  // first save: source
            .thenReturn(loteDestino);             // second save: destination

        LoteResponse response = transferenciaService.transferir(request);

        assertThat(response).isNotNull();
        assertThat(response.stockActual()).isEqualTo(20);
        assertThat(response.almacenId()).isEqualTo(2L);

        // Verify MovimientoStock(TRANSFERENCIA) was created
        verify(movimientoStockRepository).save(movimientoCaptor.capture());
        MovimientoStock mov = movimientoCaptor.getValue();
        assertThat(mov.getTipo()).isEqualTo(TipoMovimiento.TRANSFERENCIA);
        assertThat(mov.getCantidad()).isEqualTo(20);
        assertThat(mov.getAlmacenOrigen().getId()).isEqualTo(1L);
        assertThat(mov.getAlmacenDestino().getId()).isEqualTo(2L);
    }

    @Test
    void shouldRejectWhenCantidadExceedsStock() {
        // SC-23: Transfer more than stock → 400
        TransferenciaRequest request = new TransferenciaRequest(
            1L, 1L, 2L, 150, "Excede stock"
        );

        when(loteRepository.findById(1L)).thenReturn(Optional.of(loteOrigen));

        assertThatThrownBy(() -> transferenciaService.transferir(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("excede el stock actual");
    }

    @Test
    void shouldRejectWhenSourceLoteNotFound() {
        TransferenciaRequest request = new TransferenciaRequest(
            1L, 999L, 2L, 10, "Lote inválido"
        );

        when(loteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferenciaService.transferir(request))
            .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void shouldRejectWhenDestinoAlmacenNotFound() {
        TransferenciaRequest request = new TransferenciaRequest(
            1L, 1L, 999L, 10, "Almacén inválido"
        );

        when(loteRepository.findById(1L)).thenReturn(Optional.of(loteOrigen));
        when(almacenRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferenciaService.transferir(request))
            .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void shouldRejectZeroCantidad() {
        TransferenciaRequest request = new TransferenciaRequest(
            1L, 1L, 2L, 0, "Cero"
        );

        assertThatThrownBy(() -> transferenciaService.transferir(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor a cero");
    }
}

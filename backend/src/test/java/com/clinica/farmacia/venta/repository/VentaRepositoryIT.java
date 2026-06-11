package com.clinica.farmacia.venta.repository;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.caja.entity.SesionCaja;
import com.clinica.farmacia.caja.repository.SesionCajaRepository;
import com.clinica.farmacia.caja.type.EstadoSesion;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.entity.Producto.TipoProducto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import com.clinica.farmacia.venta.entity.DetalleVenta;
import com.clinica.farmacia.venta.entity.Venta;
import com.clinica.farmacia.venta.type.EstadoVenta;
import com.clinica.farmacia.venta.type.TipoLista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VentaRepositoryIT {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private SesionCajaRepository sesionCajaRepository;

    private Producto savedProducto;
    private Almacen savedAlmacen;
    private Lote savedLote;
    private SesionCaja savedSesion;

    @BeforeEach
    void setUp() {
        savedProducto = new Producto();
        savedProducto.setCodigo("PROD-VTA-IT");
        savedProducto.setTipo(TipoProducto.MEDICAMENTO);
        savedProducto.setPrecioCosto(new BigDecimal("5.0000"));
        savedProducto.setUtilidadMedico(new BigDecimal("20.00"));
        savedProducto.setUtilidadPublico(new BigDecimal("20.00"));
        savedProducto.setPrecioVentaMedico(new BigDecimal("7.10"));
        savedProducto.setPrecioVentaPublico(new BigDecimal("12.00"));
        savedProducto.setStockMinimo(20);
        savedProducto.setStockCritico(5);
        savedProducto = productoRepository.saveAndFlush(savedProducto);

        savedAlmacen = new Almacen();
        savedAlmacen.setCodigo("ALM-VTA-IT");
        savedAlmacen.setNombre("Almacén Venta IT");
        savedAlmacen.setDefaultWarehouse(true);
        savedAlmacen = almacenRepository.saveAndFlush(savedAlmacen);

        savedLote = new Lote();
        savedLote.setProducto(savedProducto);
        savedLote.setCodigoLote("LOTE-VTA-IT");
        savedLote.setFechaVencimiento(LocalDate.now().plusYears(1));
        savedLote.setStockInicial(100);
        savedLote.setStockActual(100);
        savedLote.setPrecioCosto(new BigDecimal("5.0000"));
        savedLote.setAlmacen(savedAlmacen);
        savedLote = loteRepository.saveAndFlush(savedLote);

        savedSesion = new SesionCaja();
        savedSesion.setUsuarioId(42L);
        savedSesion.setAlmacenId(savedAlmacen.getId());
        savedSesion.setMontoApertura(new BigDecimal("500.00"));
        savedSesion.setTotalVentas(BigDecimal.ZERO);
        savedSesion.setEstado(EstadoSesion.ABIERTA);
        savedSesion.setFechaApertura(LocalDateTime.now());
        savedSesion = sesionCajaRepository.saveAndFlush(savedSesion);
    }

    @Test
    void shouldSaveAndFindVenta() {
        Venta venta = new Venta();
        venta.setSesionCaja(savedSesion);
        venta.setCorrelativo(1);
        venta.setClientePersonaId(null);
        venta.setTipoLista(TipoLista.PUBLICO);
        venta.setEstado(EstadoVenta.COMPLETADA);
        venta.setVendedorUsuarioId(42L);
        venta.setSubtotal(new BigDecimal("60.00"));
        venta.setTotal(new BigDecimal("60.00"));

        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setLote(savedLote);
        detalle.setCantidad(5);
        detalle.setPrecioUnitario(new BigDecimal("12.0000"));
        detalle.setPrecioOriginal(new BigDecimal("12.0000"));
        detalle.setDescuentoAplicado(BigDecimal.ZERO);
        detalle.setSubtotal(new BigDecimal("60.00"));
        detalle.setCreatedAt(LocalDateTime.now()); // @CreatedDate not active in @DataJpaTest
        venta.addDetalle(detalle);

        venta = ventaRepository.saveAndFlush(venta);
        assertThat(venta.getId()).isNotNull();

        var found = ventaRepository.findByIdWithDetalles(venta.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCorrelativo()).isEqualTo(1);
        assertThat(found.get().getDetalles()).hasSize(1);
        assertThat(found.get().getDetalles().getFirst().getCantidad()).isEqualTo(5);
    }

    @Test
    void shouldFindMaxCorrelativoBySesion() {
        // First sale in session → max = 0
        Integer max = ventaRepository.findMaxCorrelativoBySesionCajaId(savedSesion.getId());
        assertThat(max).isEqualTo(0);
    }

    @Test
    void shouldFindBySesionCajaId() {
        Venta venta = new Venta();
        venta.setSesionCaja(savedSesion);
        venta.setCorrelativo(1);
        venta.setTipoLista(TipoLista.PUBLICO);
        venta.setEstado(EstadoVenta.COMPLETADA);
        venta.setVendedorUsuarioId(42L);
        venta.setSubtotal(new BigDecimal("60.00"));
        venta.setTotal(new BigDecimal("60.00"));
        ventaRepository.saveAndFlush(venta);

        var results = ventaRepository.findBySesionCajaIdOrderByCorrelativoAsc(savedSesion.getId());
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCorrelativo()).isEqualTo(1);
    }

    @Test
    void shouldFindByClientePersonaId() {
        Venta venta = new Venta();
        venta.setSesionCaja(savedSesion);
        venta.setCorrelativo(1);
        venta.setClientePersonaId(123L);
        venta.setTipoLista(TipoLista.PUBLICO);
        venta.setEstado(EstadoVenta.COMPLETADA);
        venta.setVendedorUsuarioId(42L);
        venta.setSubtotal(new BigDecimal("60.00"));
        venta.setTotal(new BigDecimal("60.00"));
        ventaRepository.saveAndFlush(venta);

        var results = ventaRepository.findByClientePersonaIdOrderByCreatedAtDesc(123L);
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getClientePersonaId()).isEqualTo(123L);
    }
}

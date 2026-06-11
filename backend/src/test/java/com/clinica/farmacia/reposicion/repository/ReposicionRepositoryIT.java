package com.clinica.farmacia.reposicion.repository;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.entity.Producto.TipoProducto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import com.clinica.farmacia.reposicion.entity.Reposicion;
import com.clinica.farmacia.reposicion.entity.ReposicionDetalle;
import com.clinica.farmacia.reposicion.type.EstadoReposicion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReposicionRepositoryIT {

    @Autowired
    private ReposicionRepository reposicionRepository;

    @Autowired
    private ReposicionDetalleRepository detalleRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private LoteRepository loteRepository;

    private Producto savedProducto;
    private Almacen savedAlmacen;

    @BeforeEach
    void setUp() {
        savedProducto = new Producto();
        savedProducto.setCodigo("PROD-REP-IT");
        savedProducto.setTipo(TipoProducto.MEDICAMENTO);
        savedProducto.setPrecioCosto(new BigDecimal("5.0000"));
        savedProducto.setUtilidadMedico(new BigDecimal("20.00"));
        savedProducto.setUtilidadPublico(new BigDecimal("20.00"));
        savedProducto.setPrecioVentaMedico(new BigDecimal("7.10"));
        savedProducto.setPrecioVentaPublico(new BigDecimal("7.10"));
        savedProducto.setStockMinimo(20);
        savedProducto.setStockCritico(5);
        savedProducto = productoRepository.saveAndFlush(savedProducto);

        savedAlmacen = new Almacen();
        savedAlmacen.setCodigo("ALM-REP-IT");
        savedAlmacen.setNombre("Almacén Reposición IT");
        savedAlmacen.setDefaultWarehouse(true);
        savedAlmacen = almacenRepository.saveAndFlush(savedAlmacen);

        // Create a lote with stock below stockMinimo for reposicion tests
        Lote lote = new Lote();
        lote.setProducto(savedProducto);
        lote.setCodigoLote("LOTE-REP-IT");
        lote.setFechaVencimiento(LocalDate.now().plusYears(1));
        lote.setStockInicial(10);
        lote.setStockActual(10);  // stockMinimo=20 → below threshold
        lote.setPrecioCosto(new BigDecimal("5.0000"));
        lote.setAlmacen(savedAlmacen);
        loteRepository.saveAndFlush(lote);
    }

    @Test
    void shouldSaveReposicionWithDetalles() {
        // Create reposicion
        var r = new Reposicion();
        r.setGeneradaEn(LocalDateTime.now());
        r.setUsuarioId(42L);
        r.setAlmacenId(savedAlmacen.getId());
        r.setObservaciones("Test reposición");
        r.setEstado(EstadoReposicion.PENDIENTE);

        // Create detalle and add to reposicion's cascade-managed collection
        var d = new ReposicionDetalle();
        d.setProductoId(savedProducto.getId());
        d.setStockActual(10);
        d.setStockMinimo(20);
        d.setStockCritico(5);
        d.setCantidadSugerida(30);
        r.getDetalles().add(d);
        d.setReposicion(r);

        // Save reposicion (cascade persists detalle)
        r = reposicionRepository.saveAndFlush(r);

        var found = reposicionRepository.findById(r.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEstado()).isEqualTo(EstadoReposicion.PENDIENTE);
        assertThat(found.get().getDetalles()).hasSize(1);
    }

    @Test
    void shouldFindByEstado() {
        Reposicion r = new Reposicion();
        r.setGeneradaEn(LocalDateTime.now());
        r.setUsuarioId(42L);
        r.setAlmacenId(1L);
        r.setObservaciones("Test");
        r.setEstado(EstadoReposicion.PENDIENTE);
        reposicionRepository.saveAndFlush(r);

        var pendientes = reposicionRepository.findByEstadoOrderByGeneradaEnDesc(EstadoReposicion.PENDIENTE);
        assertThat(pendientes).hasSize(1);

        var procesadas = reposicionRepository.findByEstadoOrderByGeneradaEnDesc(EstadoReposicion.PROCESADA);
        assertThat(procesadas).isEmpty();
    }

    @Test
    void shouldCountByEstado() {
        long count = reposicionRepository.countByEstado(EstadoReposicion.PENDIENTE);
        assertThat(count).isEqualTo(0); // No reposiciones saved in setup
    }
}

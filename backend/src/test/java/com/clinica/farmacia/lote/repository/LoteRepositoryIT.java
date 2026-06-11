package com.clinica.farmacia.lote.repository;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.entity.Producto.TipoProducto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LoteRepositoryIT {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private PlatformTransactionManager ptm;

    private Producto savedProducto;
    private Almacen savedAlmacen;

    @BeforeEach
    void setUp() {
        Producto p = new Producto();
        p.setCodigo("PROD-LOTE-IT");
        p.setTipo(TipoProducto.MEDICAMENTO);
        p.setPrecioCosto(new BigDecimal("5.0000"));
        p.setUtilidadMedico(new BigDecimal("20.00"));
        p.setUtilidadPublico(new BigDecimal("20.00"));
        p.setPrecioVentaMedico(new BigDecimal("7.10"));
        p.setPrecioVentaPublico(new BigDecimal("7.10"));
        p.setStockMinimo(20);
        p.setStockCritico(5);
        savedProducto = productoRepository.saveAndFlush(p);

        Almacen a = new Almacen();
        a.setCodigo("ALM-LOTE-IT");
        a.setNombre("Almacén Lote IT");
        a.setDefaultWarehouse(true);
        savedAlmacen = almacenRepository.saveAndFlush(a);
    }

    @Test
    void shouldSaveAndFindLote() {
        Lote lote = new Lote();
        lote.setProducto(savedProducto);
        lote.setCodigoLote("LOTE-001");
        lote.setFechaVencimiento(LocalDate.now().plusYears(1));
        lote.setStockInicial(100);
        lote.setStockActual(100);
        lote.setPrecioCosto(new BigDecimal("5.0000"));
        lote.setAlmacen(savedAlmacen);
        lote = loteRepository.saveAndFlush(lote);

        var found = loteRepository.findById(lote.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStockActual()).isEqualTo(100);
        assertThat(found.get().getCodigoLote()).isEqualTo("LOTE-001");
        assertThat(found.get().getProducto().getId()).isEqualTo(savedProducto.getId());
    }

    @Test
    void shouldFindByProductoIdWithStock() {
        Lote lote = new Lote();
        lote.setProducto(savedProducto);
        lote.setCodigoLote("LOTE-002");
        lote.setFechaVencimiento(LocalDate.now().plusYears(1));
        lote.setStockInicial(50);
        lote.setStockActual(50);
        lote.setPrecioCosto(new BigDecimal("5.0000"));
        lote.setAlmacen(savedAlmacen);
        loteRepository.saveAndFlush(lote);

        var results = loteRepository.findByProductoIdAndStockActualGreaterThanAndActivoTrue(
            savedProducto.getId(), 0);
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getStockActual()).isEqualTo(50);
    }

    @Test
    void shouldAutoIncrementVersionOnUpdate() {
        // @Version is now on a separate lote_version field (see V35).
        // stockActual stores the correct business value; version auto-increments for locking.
        // Save a lote with stockActual=10 → version=0 (Hibernate default).
        // Update stockActual=5 → version increments to 1, stockActual stays 5.
        Lote lote = new Lote();
        lote.setProducto(savedProducto);
        lote.setCodigoLote("LOTE-VERSION");
        lote.setFechaVencimiento(LocalDate.now().plusYears(1));
        lote.setStockInicial(10);
        lote.setStockActual(10);
        lote.setPrecioCosto(new BigDecimal("5.0000"));
        lote.setAlmacen(savedAlmacen);
        lote = loteRepository.saveAndFlush(lote);
        assertThat(lote.getStockActual()).isEqualTo(10);
        assertThat(lote.getVersion()).isNotNull();

        // Update stock to 5 — @Version is now separate, so stockActual stays 5.
        lote.setStockActual(5);
        loteRepository.saveAndFlush(lote);

        // Reload from DB: stockActual=5 (correct), version=1 (incremented from 0).
        var reloaded = loteRepository.findById(lote.getId()).orElseThrow();
        assertThat(reloaded.getStockActual()).isEqualTo(5);
        assertThat(reloaded.getVersion()).isOne();
    }

    @Test
    void shouldFindByFechaVencimientoBefore() {
        Lote loteNearExpiry = new Lote();
        loteNearExpiry.setProducto(savedProducto);
        loteNearExpiry.setCodigoLote("LOTE-NEAR-EXP");
        loteNearExpiry.setFechaVencimiento(LocalDate.now().plusDays(30));
        loteNearExpiry.setStockInicial(10);
        loteNearExpiry.setStockActual(10);
        loteNearExpiry.setPrecioCosto(new BigDecimal("5.0000"));
        loteNearExpiry.setAlmacen(savedAlmacen);
        loteRepository.saveAndFlush(loteNearExpiry);

        var nearExpiry = loteRepository.findByFechaVencimientoBeforeAndStockActualGreaterThanAndActivoTrue(
            LocalDate.now().plusDays(60), 0);
        assertThat(nearExpiry).hasSize(1);

        var farFuture = loteRepository.findByFechaVencimientoBeforeAndStockActualGreaterThanAndActivoTrue(
            LocalDate.now().plusDays(10), 0);
        assertThat(farFuture).isEmpty();
    }

    /** Creates a TransactionTemplate with REQUIRES_NEW propagation. */
    private TransactionTemplate newTransactionTemplate() {
        var tt = new TransactionTemplate(ptm);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return tt;
    }
}

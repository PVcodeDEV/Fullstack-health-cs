package com.clinica.farmacia.integration;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.lote.dto.LoteRequest;
import com.clinica.farmacia.lote.dto.TransferenciaRequest;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.lote.service.LoteService;
import com.clinica.farmacia.lote.service.TransferenciaService;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import com.clinica.farmacia.testsupport.TestDataBuilder;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Transferencia entre almacenes: happy path + stock insuficiente.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestIntegrationConfig.class)
class TransferenciaIT {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private LoteService loteService;

    @Autowired
    private TransferenciaService transferenciaService;

    @Autowired
    private EntityManager entityManager;

    private Producto savedProducto;
    private Almacen origen;
    private Almacen destino;
    private Long loteOrigenId;

    @BeforeEach
    void setUp() {
        savedProducto = productoRepository.saveAndFlush(TestDataBuilder.productoValido());
        origen = almacenRepository.saveAndFlush(TestDataBuilder.almacenDefecto());
        destino = almacenRepository.saveAndFlush(TestDataBuilder.almacenSecundario());

        LoteRequest loteRequest = new LoteRequest(
            savedProducto.getId(),
            "LOTE-TRF-001",
            LocalDate.now().plusYears(2),
            100,
            new BigDecimal("5.0000"),
            origen.getId(),
            42L,
            "Recepción test"
        );
        loteOrigenId = loteService.recibir(loteRequest).id();
    }

    @Test
    @Transactional
    void shouldTransferHappyPath() {
        // Transfer 20 from origen to destino
        var response = transferenciaService.transferir(new TransferenciaRequest(
            savedProducto.getId(), loteOrigenId, destino.getId(), 20, "Transferencia test"));

        assertThat(response).isNotNull();
        assertThat(response.stockActual()).isEqualTo(20);
        assertThat(response.almacenId()).isEqualTo(destino.getId());
        assertThat(response.codigoLote()).isEqualTo("LOTE-TRF-001");

        entityManager.flush();
        entityManager.clear();

        // Source lot decremented
        Lote source = loteRepository.findById(loteOrigenId).orElseThrow();
        assertThat(source.getStockActual()).isEqualTo(80); // 100 - 20
        assertThat(source.getAlmacen().getId()).isEqualTo(origen.getId());

        // Destination lot created
        var destLotes = loteRepository.findByAlmacenIdAndProductoIdAndStockActualGreaterThanAndActivoTrue(
            destino.getId(), savedProducto.getId(), 0);
        assertThat(destLotes).hasSize(1);
        assertThat(destLotes.getFirst().getStockActual()).isEqualTo(20);
    }

    @Test
    void shouldRejectStockInsuficiente() {
        // Only 100 in stock, trying to transfer 200
        assertThatThrownBy(() -> transferenciaService.transferir(new TransferenciaRequest(
            savedProducto.getId(), loteOrigenId, destino.getId(), 200, "Excede stock")))
            .hasMessageContaining("excede el stock actual");
    }
}

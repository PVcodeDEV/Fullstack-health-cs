package com.clinica.farmacia.reposicion.service;

import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import com.clinica.farmacia.reposicion.dto.ProductoStockBajoProjection;
import com.clinica.farmacia.reposicion.dto.ReposicionGenerarRequest;
import com.clinica.farmacia.reposicion.dto.ReposicionResponse;
import com.clinica.farmacia.reposicion.entity.Reposicion;
import com.clinica.farmacia.reposicion.entity.ReposicionDetalle;
import com.clinica.farmacia.reposicion.repository.ReposicionDetalleRepository;
import com.clinica.farmacia.reposicion.repository.ReposicionRepository;
import com.clinica.farmacia.reposicion.type.EstadoReposicion;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReposicionServiceTest {

    @Mock
    private ReposicionRepository reposicionRepository;

    @Mock
    private ReposicionDetalleRepository detalleRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Captor
    private ArgumentCaptor<Reposicion> reposicionCaptor;

    private ReposicionService service;
    private static final Long USUARIO_ID = 42L;
    private static final Long ALMACEN_ID = 1L;

    @BeforeEach
    void setUp() {
        service = new ReposicionService(reposicionRepository, detalleRepository, productoRepository);
    }

    @Test
    void shouldGenerarReposicionConProductosBajoStock() {
        // SC-18: stockMinimo=20, stockActual=15 → included
        ReposicionGenerarRequest request = new ReposicionGenerarRequest(null, false, "Test");
        ProductoStockBajoProjection proj = new ProductoStockBajoProjection(
            1L, "PARACETAMOL", "Paracetamol 500mg", 20, 10, 15L);

        when(productoRepository.findProductosBajoStockMinimo()).thenReturn(List.of(proj));
        when(reposicionRepository.save(any(Reposicion.class))).thenAnswer(inv -> {
            Reposicion r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(detalleRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        ReposicionResponse response = service.generar(request, USUARIO_ID);

        assertThat(response).isNotNull();
        assertThat(response.estado()).isEqualTo(EstadoReposicion.PENDIENTE);
        assertThat(response.detalles()).hasSize(1);
        // cantidadSugerida = max(20*2 - 15, 0) = max(25, 0) = 25
        assertThat(response.detalles().get(0).cantidadSugerida()).isEqualTo(25);
        assertThat(response.detalles().get(0).productoId()).isEqualTo(1L);
        assertThat(response.detalles().get(0).stockActual()).isEqualTo(15);
    }

    @Test
    void shouldGenerarReposicionSoloCriticosSiFlag() {
        // SC-19: ?critico=true, stockCritico=5, stockActual=3 → included
        ReposicionGenerarRequest request = new ReposicionGenerarRequest(null, true, "Criticos");
        ProductoStockBajoProjection projCritico = new ProductoStockBajoProjection(
            1L, "IBUPROFENO", "Ibuprofeno 400mg", 20, 5, 3L);
        ProductoStockBajoProjection projNormal = new ProductoStockBajoProjection(
            2L, "VITAMINA-C", "Vitamina C 1000mg", 10, 5, 15L); // stock=15 > critico=5

        when(productoRepository.findProductosBajoStockCritico()).thenReturn(List.of(projCritico));
        when(reposicionRepository.save(any(Reposicion.class))).thenAnswer(inv -> {
            Reposicion r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });
        when(detalleRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        ReposicionResponse response = service.generar(request, USUARIO_ID);

        // Only the critico product should be included
        assertThat(response.detalles()).hasSize(1);
        assertThat(response.detalles().get(0).productoId()).isEqualTo(1L);
        assertThat(response.detalles().get(0).stockActual()).isEqualTo(3);
    }

    @Test
    void shouldMarcarProcesada() {
        Reposicion reposicion = new Reposicion();
        reposicion.setId(1L);
        reposicion.setEstado(EstadoReposicion.PENDIENTE);
        reposicion.setGeneradaEn(LocalDateTime.now());

        when(reposicionRepository.findById(1L)).thenReturn(Optional.of(reposicion));
        when(reposicionRepository.save(any(Reposicion.class))).thenAnswer(inv -> inv.getArgument(0));

        ReposicionResponse response = service.marcarProcesada(1L);

        assertThat(response.estado()).isEqualTo(EstadoReposicion.PROCESADA);
        assertThat(response.procesadaEn()).isNotNull();
    }

    @Test
    void shouldDescartarConMotivo() {
        Reposicion reposicion = new Reposicion();
        reposicion.setId(1L);
        reposicion.setEstado(EstadoReposicion.PENDIENTE);
        reposicion.setObservaciones("Generada automáticamente");
        reposicion.setGeneradaEn(LocalDateTime.now());

        when(reposicionRepository.findById(1L)).thenReturn(Optional.of(reposicion));
        when(reposicionRepository.save(any(Reposicion.class))).thenAnswer(inv -> inv.getArgument(0));

        ReposicionResponse response = service.descartar(1L, "Productos en tránsito");

        assertThat(response.estado()).isEqualTo(EstadoReposicion.DESCARTADA);
        assertThat(response.observaciones()).contains("DESCARTADO: Productos en tránsito");
    }

    @Test
    void shouldNotIncluirProductosSinLotes() {
        // Products with no lots have stockActual=0 via COALESCE in the query.
        // If stockMinimo=10 and stockActual=0 → included with cantidadSugerida = max(10*2-0, 0) = 20
        ReposicionGenerarRequest request = new ReposicionGenerarRequest(null, false, "Test");
        ProductoStockBajoProjection proj = new ProductoStockBajoProjection(
            1L, "SIN-STOCK", "Producto sin lotes", 10, 5, 0L);

        when(productoRepository.findProductosBajoStockMinimo()).thenReturn(List.of(proj));
        when(reposicionRepository.save(any(Reposicion.class))).thenAnswer(inv -> {
            Reposicion r = inv.getArgument(0);
            r.setId(3L);
            return r;
        });
        when(detalleRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        ReposicionResponse response = service.generar(request, USUARIO_ID);

        assertThat(response.detalles()).hasSize(1);
        assertThat(response.detalles().get(0).stockActual()).isEqualTo(0);
        // cantidadSugerida = max(10*2 - 0, 0) = 20
        assertThat(response.detalles().get(0).cantidadSugerida()).isEqualTo(20);
    }
}

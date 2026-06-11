package com.clinica.farmacia.producto.service;

import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.producto.dto.ActualizarUtilidadRequest;
import com.clinica.farmacia.producto.dto.ProductoResponse;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import com.clinica.farmacia.producto.entity.Producto.TipoProducto;
import com.clinica.seguridad.dto.ConfiguracionApiResponse;
import com.clinica.seguridad.service.ConfiguracionApiService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PricingService pricingService;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private ConfiguracionApiService configuracionApiService;

    @Mock
    private com.clinica.maestro.repository.organizacion.CategoriaInsumoRepository categoriaInsumoRepository;

    @Mock
    private com.clinica.maestro.repository.financiero.UnidadMedidaRepository unidadMedidaRepository;

    @Mock
    private com.clinica.maestro.repository.farmacia.TipoMedicamentoRepository tipoMedicamentoRepository;

    @Mock
    private com.clinica.maestro.repository.clinico.FormaFarmaceuticaRepository formaFarmaceuticaRepository;

    @Mock
    private com.clinica.maestro.repository.farmacia.FormaPresentacionRepository formaPresentacionRepository;

    @Mock
    private com.clinica.maestro.repository.farmacia.GrupoFarmacologicoRepository grupoFarmacologicoRepository;

    @Mock
    private com.clinica.maestro.repository.farmacia.MarcaRepository marcaRepository;

    private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(
            productoRepository, pricingService, loteRepository, configuracionApiService,
            categoriaInsumoRepository, unidadMedidaRepository, tipoMedicamentoRepository,
            formaFarmaceuticaRepository, formaPresentacionRepository,
            grupoFarmacologicoRepository, marcaRepository
        );

        producto = new Producto();
        producto.setId(1L);
        producto.setCodigo("PROD-001");
        producto.setTipo(TipoProducto.MEDICAMENTO);
        producto.setPrecioCosto(new BigDecimal("5.0000"));
        producto.setUtilidadMedico(new BigDecimal("20"));
        producto.setUtilidadPublico(new BigDecimal("20"));
        producto.setPrecioVentaMedico(new BigDecimal("7.10"));
        producto.setPrecioVentaPublico(new BigDecimal("7.10"));
        producto.setActivo(true);
    }

    @Test
    void shouldActualizarUtilidadSuccessfully() {
        // Given
        ActualizarUtilidadRequest request = new ActualizarUtilidadRequest(
            new BigDecimal("25"), new BigDecimal("30")
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(configuracionApiService.findByModuloAndClave("farmacia", "umbral_costo"))
            .thenReturn(new ConfiguracionApiResponse(1L, "farmacia", "umbral_costo", "90", "decimal", true));
        when(configuracionApiService.findByModuloAndClave("farmacia", "utilidad_base"))
            .thenReturn(new ConfiguracionApiResponse(2L, "farmacia", "utilidad_base", "20", "decimal", true));
        when(pricingService.calcularPrecioVenta(new BigDecimal("5.0000"), new BigDecimal("25")))
            .thenReturn(new BigDecimal("8.80"));
        when(pricingService.calcularPrecioVenta(new BigDecimal("5.0000"), new BigDecimal("30")))
            .thenReturn(new BigDecimal("9.20"));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ProductoResponse response = productoService.actualizarUtilidad(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.utilidadMedico()).isEqualByComparingTo(new BigDecimal("25"));
        assertThat(response.utilidadPublico()).isEqualByComparingTo(new BigDecimal("30"));
        assertThat(response.precioVentaMedico()).isEqualByComparingTo(new BigDecimal("8.80"));
        assertThat(response.precioVentaPublico()).isEqualByComparingTo(new BigDecimal("9.20"));
    }

    @Test
    void shouldRejectActualizarUtilidadWhenMedicoBelowMinimum() {
        // costo=5.00 <= umbral(90) → min utilidad = utilidad_base = 20
        ActualizarUtilidadRequest request = new ActualizarUtilidadRequest(
            new BigDecimal("15"), new BigDecimal("25")
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(configuracionApiService.findByModuloAndClave("farmacia", "umbral_costo"))
            .thenReturn(new ConfiguracionApiResponse(1L, "farmacia", "umbral_costo", "90", "decimal", true));
        when(configuracionApiService.findByModuloAndClave("farmacia", "utilidad_base"))
            .thenReturn(new ConfiguracionApiResponse(2L, "farmacia", "utilidad_base", "20", "decimal", true));

        assertThatThrownBy(() -> productoService.actualizarUtilidad(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("utilidad médico")
            .hasMessageContaining("20");
    }

    @Test
    void shouldRejectActualizarUtilidadWhenPublicoBelowMinimum() {
        ActualizarUtilidadRequest request = new ActualizarUtilidadRequest(
            new BigDecimal("25"), new BigDecimal("15")
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(configuracionApiService.findByModuloAndClave("farmacia", "umbral_costo"))
            .thenReturn(new ConfiguracionApiResponse(1L, "farmacia", "umbral_costo", "90", "decimal", true));
        when(configuracionApiService.findByModuloAndClave("farmacia", "utilidad_base"))
            .thenReturn(new ConfiguracionApiResponse(2L, "farmacia", "utilidad_base", "20", "decimal", true));

        assertThatThrownBy(() -> productoService.actualizarUtilidad(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("utilidad público")
            .hasMessageContaining("20");
    }

    @Test
    void shouldUseUtilidadAltaMinForHighCostProduct() {
        // costo > umbral(90) → min = utilidad_alta_min = 10
        producto.setPrecioCosto(new BigDecimal("100.0000"));

        ActualizarUtilidadRequest request = new ActualizarUtilidadRequest(
            new BigDecimal("12"), new BigDecimal("12")
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(configuracionApiService.findByModuloAndClave("farmacia", "umbral_costo"))
            .thenReturn(new ConfiguracionApiResponse(1L, "farmacia", "umbral_costo", "90", "decimal", true));
        when(configuracionApiService.findByModuloAndClave("farmacia", "utilidad_alta_min"))
            .thenReturn(new ConfiguracionApiResponse(3L, "farmacia", "utilidad_alta_min", "10", "decimal", true));
        when(pricingService.calcularPrecioVenta(new BigDecimal("100.0000"), new BigDecimal("12")))
            .thenReturn(new BigDecimal("132.00"));
        when(pricingService.calcularPrecioVenta(new BigDecimal("100.0000"), new BigDecimal("12")))
            .thenReturn(new BigDecimal("132.00"));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse response = productoService.actualizarUtilidad(1L, request);

        assertThat(response.utilidadMedico()).isEqualByComparingTo(new BigDecimal("12"));
        assertThat(response.utilidadPublico()).isEqualByComparingTo(new BigDecimal("12"));
    }

    @Test
    void shouldThrowWhenProductoNotFound() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        ActualizarUtilidadRequest request = new ActualizarUtilidadRequest(
            new BigDecimal("25"), new BigDecimal("25")
        );

        assertThatThrownBy(() -> productoService.actualizarUtilidad(999L, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Producto no encontrado");
    }

    @Test
    void shouldRejectSoftDeleteWithActiveStock() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(loteRepository.existsByProductoIdAndStockActualGreaterThan(1L, 0)).thenReturn(true);

        assertThatThrownBy(() -> productoService.softDelete(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("No se puede eliminar el producto")
            .hasMessageContaining("stock activo");
    }

    @Test
    void shouldSoftDeleteWhenNoActiveStock() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(loteRepository.existsByProductoIdAndStockActualGreaterThan(1L, 0)).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse response = productoService.softDelete(1L);

        assertThat(response).isNotNull();
        assertThat(response.activo()).isFalse();
    }
}

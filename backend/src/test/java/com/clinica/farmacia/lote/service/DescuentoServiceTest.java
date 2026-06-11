package com.clinica.farmacia.lote.service;

import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.seguridad.dto.ConfiguracionApiResponse;
import com.clinica.seguridad.service.ConfiguracionApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DescuentoServiceTest {

    @Mock
    private ConfiguracionApiService configService;

    private DescuentoService descuentoService;
    private Lote loteFresco;
    private Lote loteProximoVencer;

    @BeforeEach
    void setUp() {
        descuentoService = new DescuentoService(configService);

        Producto producto = new Producto();
        producto.setId(1L);

        // Lot that expires far in the future (>90 days)
        loteFresco = new Lote();
        loteFresco.setId(1L);
        loteFresco.setCodigoLote("FRESCO-001");
        loteFresco.setFechaVencimiento(LocalDate.now().plusMonths(6));
        loteFresco.setProducto(producto);

        // Lot expiring within the threshold (45 days)
        loteProximoVencer = new Lote();
        loteProximoVencer.setId(2L);
        loteProximoVencer.setCodigoLote("VENCE-001");
        loteProximoVencer.setFechaVencimiento(LocalDate.now().plusDays(45));
        loteProximoVencer.setProducto(producto);

        // Default config mocks
        lenient().when(configService.findByModuloAndClave(eq("farmacia"), eq("descuento_vencimiento_dias")))
            .thenReturn(new ConfiguracionApiResponse(1L, "farmacia", "descuento_vencimiento_dias", "90", "integer", true));
        lenient().when(configService.findByModuloAndClave(eq("farmacia"), eq("descuento_vencimiento_max_pct")))
            .thenReturn(new ConfiguracionApiResponse(2L, "farmacia", "descuento_vencimiento_max_pct", "20", "decimal", true));
    }

    @Test
    void shouldReturnZeroDiscountForFreshLot() {
        // Lot expiring > 90 days → no discount
        BigDecimal descuento = descuentoService.calcularDescuento(loteFresco, new BigDecimal("10.00"), new BigDecimal("6.00"));
        assertThat(descuento).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldCalculateDiscountWithinThreshold() {
        // SC-16: Lot expires 45d, precio=10, costo+IGV=6
        // descMax = 10 × 20% = 2.00
        // descFisico = 10 - 6 = 4.00
        // min(2.00, 4.00) = 2.00
        BigDecimal descuento = descuentoService.calcularDescuento(loteProximoVencer, new BigDecimal("10.00"), new BigDecimal("6.00"));
        assertThat(descuento).isEqualByComparingTo(new BigDecimal("2.00"));
    }

    @Test
    void shouldUseFisicoWhenLessThanMax() {
        // SC-17: costo+IGV=9.50, precio=10
        // descMax = 10 × 20% = 2.00
        // descFisico = 10 - 9.50 = 0.50
        // min(2.00, 0.50) = 0.50
        BigDecimal descuento = descuentoService.calcularDescuento(loteProximoVencer, new BigDecimal("10.00"), new BigDecimal("9.50"));
        assertThat(descuento).isEqualByComparingTo(new BigDecimal("0.50"));
    }

    @Test
    void shouldReturnZeroWhenCostoIgvExceedsPrice() {
        // costo+IGV > precio → descuentoFisico would be negative → floored to 0
        BigDecimal descuento = descuentoService.calcularDescuento(loteProximoVencer, new BigDecimal("8.00"), new BigDecimal("9.50"));
        assertThat(descuento).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldHandleNullParameters() {
        assertThat(descuentoService.calcularDescuento(null, new BigDecimal("10"), new BigDecimal("6")))
            .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(descuentoService.calcularDescuento(loteProximoVencer, null, new BigDecimal("6")))
            .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(descuentoService.calcularDescuento(loteProximoVencer, new BigDecimal("10"), null))
            .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldUseDefaultsWhenConfigNotFound() {
        when(configService.findByModuloAndClave(eq("farmacia"), anyString()))
            .thenThrow(new jakarta.persistence.EntityNotFoundException());

        BigDecimal descuento = descuentoService.calcularDescuento(loteProximoVencer, new BigDecimal("10.00"), new BigDecimal("6.00"));
        assertThat(descuento).isEqualByComparingTo(new BigDecimal("2.00"));
    }

    @Test
    void shouldUseCustomConfigWhenAvailable() {
        when(configService.findByModuloAndClave(eq("farmacia"), eq("descuento_vencimiento_dias")))
            .thenReturn(new ConfiguracionApiResponse(1L, "farmacia", "descuento_vencimiento_dias", "30", "integer", true));

        // Lot expires in 45 days > 30 day threshold → no discount
        BigDecimal descuento = descuentoService.calcularDescuento(loteProximoVencer, new BigDecimal("10.00"), new BigDecimal("6.00"));
        assertThat(descuento).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldUseCustomMaxPct() {
        when(configService.findByModuloAndClave(eq("farmacia"), eq("descuento_vencimiento_max_pct")))
            .thenReturn(new ConfiguracionApiResponse(2L, "farmacia", "descuento_vencimiento_max_pct", "10", "decimal", true));

        // descMax = 10 × 10% = 1.00
        // descFisico = 10 - 6 = 4.00
        // min(1.00, 4.00) = 1.00
        BigDecimal descuento = descuentoService.calcularDescuento(loteProximoVencer, new BigDecimal("10.00"), new BigDecimal("6.00"));
        assertThat(descuento).isEqualByComparingTo(new BigDecimal("1.00"));
    }
}

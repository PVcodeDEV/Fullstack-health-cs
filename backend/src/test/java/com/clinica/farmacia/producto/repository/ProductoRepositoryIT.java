package com.clinica.farmacia.producto.repository;

import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.entity.Producto.TipoProducto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductoRepositoryIT {

    @Autowired
    private ProductoRepository repository;

    @BeforeEach
    void setUp() {
        Producto p = new Producto();
        p.setCodigo("PARACETAMOL-500");
        p.setTipo(TipoProducto.MEDICAMENTO);
        p.setPrecioCosto(new BigDecimal("5.0000"));
        p.setUtilidadMedico(new BigDecimal("20.00"));
        p.setUtilidadPublico(new BigDecimal("20.00"));
        p.setPrecioVentaMedico(new BigDecimal("7.10"));
        p.setPrecioVentaPublico(new BigDecimal("7.10"));
        p.setStockMinimo(20);
        p.setStockCritico(5);
        repository.saveAndFlush(p);
    }

    @Test
    void shouldFindByCodigo() {
        var result = repository.findByCodigo("PARACETAMOL-500");
        assertThat(result).isPresent();
        assertThat(result.get().getTipo()).isEqualTo(TipoProducto.MEDICAMENTO);
    }

    @Test
    void shouldRejectDuplicateCodigo() {
        Producto p = new Producto();
        p.setCodigo("PARACETAMOL-500");
        p.setTipo(TipoProducto.INSUMO);
        p.setPrecioCosto(new BigDecimal("3.0000"));
        assertThatThrownBy(() -> repository.saveAndFlush(p))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindAllActive() {
        var all = repository.findAllByActivoTrueOrderByCodigo();
        assertThat(all).hasSize(1);

        var entity = all.getFirst();
        entity.markAsInactive();
        repository.saveAndFlush(entity);

        var afterDelete = repository.findAllByActivoTrueOrderByCodigo();
        assertThat(afterDelete).isEmpty();
    }

    @Test
    void shouldPersistInsumo() {
        Producto p = new Producto();
        p.setCodigo("GASA-ESTERIL-10");
        p.setTipo(TipoProducto.INSUMO);
        p.setPrecioCosto(new BigDecimal("2.5000"));
        p.setUtilidadMedico(new BigDecimal("20.00"));
        p.setUtilidadPublico(new BigDecimal("20.00"));
        p.setPrecioVentaMedico(new BigDecimal("3.60"));
        p.setPrecioVentaPublico(new BigDecimal("3.60"));
        p.setStockMinimo(50);
        p.setStockCritico(10);
        repository.saveAndFlush(p);

        var result = repository.findByCodigo("GASA-ESTERIL-10");
        assertThat(result).isPresent();
        assertThat(result.get().getTipo()).isEqualTo(TipoProducto.INSUMO);
    }

    @Test
    void shouldFindByTipo() {
        var medicamentos = repository.findByTipoAndActivoTrue(TipoProducto.MEDICAMENTO);
        assertThat(medicamentos).hasSize(1);

        var insumos = repository.findByTipoAndActivoTrue(TipoProducto.INSUMO);
        assertThat(insumos).isEmpty();
    }

    @Test
    void shouldReturnEmptyForUnknownCodigo() {
        var result = repository.findByCodigo("NONEXISTENT");
        assertThat(result).isEmpty();
    }
}

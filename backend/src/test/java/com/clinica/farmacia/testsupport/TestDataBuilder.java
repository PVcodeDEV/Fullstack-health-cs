package com.clinica.farmacia.testsupport;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.caja.entity.SesionCaja;
import com.clinica.farmacia.caja.type.EstadoSesion;
import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.entity.Producto.TipoProducto;
import com.clinica.farmacia.venta.dto.DetalleVentaRequest;
import com.clinica.farmacia.venta.dto.VentaRequest;
import com.clinica.farmacia.venta.type.TipoLista;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Static factory methods for creating test domain objects with unique
 * identifiers to avoid unique-constraint collisions across tests.
 */
public final class TestDataBuilder {

    private TestDataBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ===== Producto =====

    public static Producto productoValido() {
        Producto p = new Producto();
        p.setCodigo("PROD-" + uniqueCode());
        p.setTipo(TipoProducto.MEDICAMENTO);
        p.setPrecioCosto(new BigDecimal("5.0000"));
        p.setUtilidadMedico(new BigDecimal("20.00"));
        p.setUtilidadPublico(new BigDecimal("20.00"));
        p.setPrecioVentaMedico(new BigDecimal("7.10"));
        p.setPrecioVentaPublico(new BigDecimal("12.00"));
        p.setStockMinimo(20);
        p.setStockCritico(5);
        return p;
    }

    public static Producto productoInsumo() {
        Producto p = new Producto();
        p.setCodigo("INS-" + uniqueCode());
        p.setTipo(TipoProducto.INSUMO);
        p.setPrecioCosto(new BigDecimal("3.0000"));
        p.setUtilidadMedico(new BigDecimal("20.00"));
        p.setUtilidadPublico(new BigDecimal("20.00"));
        p.setPrecioVentaMedico(new BigDecimal("4.30"));
        p.setPrecioVentaPublico(new BigDecimal("4.30"));
        p.setStockMinimo(50);
        p.setStockCritico(10);
        return p;
    }

    // ===== Almacen =====

    public static Almacen almacenDefecto() {
        Almacen a = new Almacen();
        a.setCodigo("ALM-" + uniqueCode());
        a.setNombre("Almacén Test");
        a.setUbicacion("Ubicación Test");
        a.setDefaultWarehouse(true);
        return a;
    }

    public static Almacen almacenSecundario() {
        Almacen a = new Almacen();
        a.setCodigo("ALM2-" + uniqueCode());
        a.setNombre("Almacén Secundario Test");
        a.setUbicacion("Ubicación Secundaria");
        a.setDefaultWarehouse(false);
        return a;
    }

    // ===== Lote =====

    public static Lote loteConStock(Producto producto, Almacen almacen, int stock) {
        Lote l = new Lote();
        l.setProducto(producto);
        l.setCodigoLote("LOTE-" + uniqueCode());
        l.setFechaVencimiento(LocalDate.now().plusYears(2));
        l.setStockInicial(stock);
        l.setStockActual(stock);
        l.setPrecioCosto(producto.getPrecioCosto());
        l.setAlmacen(almacen);
        return l;
    }

    // ===== SesionCaja =====

    public static SesionCaja sesionAbierta(Long usuarioId, Almacen almacen) {
        SesionCaja s = new SesionCaja();
        s.setUsuarioId(usuarioId);
        s.setAlmacenId(almacen.getId());
        s.setMontoApertura(new BigDecimal("500.00"));
        s.setTotalVentas(BigDecimal.ZERO);
        s.setEstado(EstadoSesion.ABIERTA);
        s.setFechaApertura(LocalDateTime.now());
        return s;
    }

    public static SesionCaja sesionCerrada(Long usuarioId, Almacen almacen) {
        SesionCaja s = new SesionCaja();
        s.setUsuarioId(usuarioId);
        s.setAlmacenId(almacen.getId());
        s.setMontoApertura(BigDecimal.ZERO);
        s.setTotalVentas(BigDecimal.ZERO);
        s.setFechaApertura(LocalDateTime.now());
        s.abrir(usuarioId, almacen.getId(), BigDecimal.ZERO, null);
        s.cerrar(BigDecimal.ZERO, null);
        return s;
    }

    // ===== Venta DTOs =====

    public static VentaRequest ventaRequest(Long sesionCajaId, List<DetalleVentaRequest> detalles) {
        return new VentaRequest(sesionCajaId, null, TipoLista.PUBLICO, null, detalles);
    }

    public static DetalleVentaRequest detalleRequest(Long loteId, int cantidad) {
        return new DetalleVentaRequest(loteId, cantidad, BigDecimal.ZERO);
    }
}

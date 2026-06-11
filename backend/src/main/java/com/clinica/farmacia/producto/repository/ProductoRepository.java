package com.clinica.farmacia.producto.repository;

import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.entity.Producto.TipoProducto;
import com.clinica.farmacia.reposicion.dto.ProductoStockBajoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findAllByActivoTrueOrderByCodigo();

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Producto> findByTipoAndActivoTrue(TipoProducto tipo);

    /**
     * Find products where current stock (sum of lotes) <= stockMinimo.
     * Products with no lots are included with stockActual=0.
     */
    @Query("""
        SELECT new com.clinica.farmacia.reposicion.dto.ProductoStockBajoProjection(
            p.id, p.codigo, COALESCE(p.descripcion, ''), p.stockMinimo, p.stockCritico,
            COALESCE(SUM(l.stockActual), 0)
        )
        FROM Producto p
        LEFT JOIN Lote l ON l.producto = p AND l.activo = true
        WHERE p.activo = true
        GROUP BY p.id, p.codigo, p.descripcion, p.stockMinimo, p.stockCritico
        HAVING COALESCE(SUM(l.stockActual), 0) <= p.stockMinimo
        ORDER BY p.codigo
    """)
    List<ProductoStockBajoProjection> findProductosBajoStockMinimo();

    /**
     * Find products where current stock (sum of lotes) <= stockCritico.
     * Only includes products with stockCritico > 0.
     */
    @Query("""
        SELECT new com.clinica.farmacia.reposicion.dto.ProductoStockBajoProjection(
            p.id, p.codigo, COALESCE(p.descripcion, ''), p.stockMinimo, p.stockCritico,
            COALESCE(SUM(l.stockActual), 0)
        )
        FROM Producto p
        LEFT JOIN Lote l ON l.producto = p AND l.activo = true
        WHERE p.activo = true AND p.stockCritico > 0
        GROUP BY p.id, p.codigo, p.descripcion, p.stockMinimo, p.stockCritico
        HAVING COALESCE(SUM(l.stockActual), 0) <= p.stockCritico
        ORDER BY p.codigo
    """)
    List<ProductoStockBajoProjection> findProductosBajoStockCritico();

    /**
     * Same as findProductosBajoStockMinimo but filtered by almacen.
     */
    @Query("""
        SELECT new com.clinica.farmacia.reposicion.dto.ProductoStockBajoProjection(
            p.id, p.codigo, COALESCE(p.descripcion, ''), p.stockMinimo, p.stockCritico,
            COALESCE(SUM(l.stockActual), 0)
        )
        FROM Producto p
        LEFT JOIN Lote l ON l.producto = p AND l.activo = true AND l.almacen.id = :almacenId
        WHERE p.activo = true
        GROUP BY p.id, p.codigo, p.descripcion, p.stockMinimo, p.stockCritico
        HAVING COALESCE(SUM(l.stockActual), 0) <= p.stockMinimo
        ORDER BY p.codigo
    """)
    List<ProductoStockBajoProjection> findProductosBajoStockMinimoPorAlmacen(Long almacenId);

    /**
     * Same as findProductosBajoStockCritico but filtered by almacen.
     */
    @Query("""
        SELECT new com.clinica.farmacia.reposicion.dto.ProductoStockBajoProjection(
            p.id, p.codigo, COALESCE(p.descripcion, ''), p.stockMinimo, p.stockCritico,
            COALESCE(SUM(l.stockActual), 0)
        )
        FROM Producto p
        LEFT JOIN Lote l ON l.producto = p AND l.activo = true AND l.almacen.id = :almacenId
        WHERE p.activo = true AND p.stockCritico > 0
        GROUP BY p.id, p.codigo, p.descripcion, p.stockMinimo, p.stockCritico
        HAVING COALESCE(SUM(l.stockActual), 0) <= p.stockCritico
        ORDER BY p.codigo
    """)
    List<ProductoStockBajoProjection> findProductosBajoStockCriticoPorAlmacen(Long almacenId);
}

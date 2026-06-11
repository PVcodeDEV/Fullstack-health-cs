package com.clinica.farmacia.almacen.repository;

import com.clinica.farmacia.almacen.entity.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {

    List<Almacen> findAllByActivoTrueOrderByNombre();

    Optional<Almacen> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    Optional<Almacen> findByDefaultWarehouseTrue();

    boolean existsByDefaultWarehouseTrue();

    long countByDefaultWarehouseTrue();
}

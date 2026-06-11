package com.clinica.maestro.repository.organizacion;

import com.clinica.maestro.entity.organizacion.CategoriaInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaInsumoRepository extends JpaRepository<CategoriaInsumo, Integer> {

    List<CategoriaInsumo> findAllByOrderByNombreAsc();

    Optional<CategoriaInsumo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<CategoriaInsumo> findByCategoriaPadreId(Integer categoriaPadreId);

    List<CategoriaInsumo> findByCategoriaPadreIsNull();
}

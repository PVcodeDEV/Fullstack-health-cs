package com.clinica.maestro.repository.ubigeo;

import com.clinica.maestro.entity.ubigeo.UbigeoDistrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UbigeoDistritoRepository extends JpaRepository<UbigeoDistrito, String> {

    List<UbigeoDistrito> findAllByOrderByNombreAsc();

    List<UbigeoDistrito> findByProvinciaCodigoOrderByNombreAsc(String provinciaCodigo);
}

package com.clinica.maestro.repository.identidad;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoDocumentoIdentidadRepository extends JpaRepository<TipoDocumentoIdentidad, Long> {

    List<TipoDocumentoIdentidad> findAllByOrderByNombreAsc();

    Optional<TipoDocumentoIdentidad> findByCodigoSunat(String codigoSunat);

    boolean existsByCodigoSunat(String codigoSunat);
}

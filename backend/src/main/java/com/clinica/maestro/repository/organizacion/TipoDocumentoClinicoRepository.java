package com.clinica.maestro.repository.organizacion;

import com.clinica.maestro.entity.organizacion.TipoDocumentoClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoDocumentoClinicoRepository extends JpaRepository<TipoDocumentoClinico, Integer> {

    List<TipoDocumentoClinico> findAllByOrderByNombreAsc();

    Optional<TipoDocumentoClinico> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<TipoDocumentoClinico> findByRequiereFirma(Boolean requiereFirma);
}

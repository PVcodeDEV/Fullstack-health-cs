package com.clinica.maestro.repository.clinico;

import com.clinica.maestro.entity.clinico.TipoAtencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoAtencionRepository extends JpaRepository<TipoAtencion, Long> {

    List<TipoAtencion> findAllByOrderByNombreAsc();

    Optional<TipoAtencion> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}

package com.clinica.maestro.repository.rrhh;

import com.clinica.maestro.entity.rrhh.TipoColegiatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoColegiaturaRepository extends JpaRepository<TipoColegiatura, Long> {
    Optional<TipoColegiatura> findByCodigo(String codigo);
}

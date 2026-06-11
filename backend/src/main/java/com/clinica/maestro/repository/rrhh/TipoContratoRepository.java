package com.clinica.maestro.repository.rrhh;

import com.clinica.maestro.entity.rrhh.TipoContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoContratoRepository extends JpaRepository<TipoContrato, Long> {
    Optional<TipoContrato> findByCodigo(String codigo);
}

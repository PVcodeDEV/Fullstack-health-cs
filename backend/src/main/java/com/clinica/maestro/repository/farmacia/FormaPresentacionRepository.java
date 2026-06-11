package com.clinica.maestro.repository.farmacia;

import com.clinica.maestro.entity.farmacia.FormaPresentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormaPresentacionRepository extends JpaRepository<FormaPresentacion, Long> {

    List<FormaPresentacion> findAllByActivoTrueOrderByCodigo();

    Optional<FormaPresentacion> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}

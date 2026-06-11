package com.clinica.clinica.cuenta.repository;

import com.clinica.clinica.cuenta.entity.CargoAdicional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoAdicionalRepository extends JpaRepository<CargoAdicional, Long> {

    List<CargoAdicional> findByCuentaId(Long cuentaId);

    List<CargoAdicional> findByCuentaIdAndTipo(Long cuentaId, String tipo);
}

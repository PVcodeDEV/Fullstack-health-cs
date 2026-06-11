package com.clinica.clinica.admision.repository;

import com.clinica.clinica.admision.entity.CuentaPaquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuentaPaqueteRepository extends JpaRepository<CuentaPaquete, Long> {

    List<CuentaPaquete> findByCuentaId(Long cuentaId);
}

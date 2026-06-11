package com.clinica.rrhh.cts.repository;

import com.clinica.rrhh.cts.entity.DepositoCts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositoCtsRepository extends JpaRepository<DepositoCts, Long> {
    List<DepositoCts> findByPeriodoPlanillaId(Long periodoPlanillaId);
    Optional<DepositoCts> findByPeriodoPlanillaIdAndTrabajadorId(Long periodoPlanillaId, Long trabajadorId);
    List<DepositoCts> findByTrabajadorIdOrderByCreatedAtDesc(Long trabajadorId);
    boolean existsByPeriodoPlanillaId(Long periodoPlanillaId);
}

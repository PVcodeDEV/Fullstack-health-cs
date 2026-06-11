package com.clinica.rrhh.pension.repository;

import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InformacionPensionariaRepository extends JpaRepository<InformacionPensionaria, Long> {
    Optional<InformacionPensionaria> findByTrabajadorId(Long trabajadorId);
    boolean existsByTrabajadorId(Long trabajadorId);
    List<InformacionPensionaria> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
}

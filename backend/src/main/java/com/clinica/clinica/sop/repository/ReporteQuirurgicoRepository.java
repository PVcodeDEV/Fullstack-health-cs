package com.clinica.clinica.sop.repository;

import com.clinica.clinica.sop.entity.ReporteQuirurgico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteQuirurgicoRepository extends JpaRepository<ReporteQuirurgico, Long> {

    Optional<ReporteQuirurgico> findByHospitalizacionId(Long hospitalizacionId);

    List<ReporteQuirurgico> findByEstado(String estado);

    boolean existsByHospitalizacionId(Long hospitalizacionId);
}

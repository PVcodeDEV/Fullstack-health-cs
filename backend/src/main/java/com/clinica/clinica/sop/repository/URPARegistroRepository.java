package com.clinica.clinica.sop.repository;

import com.clinica.clinica.sop.entity.URPARegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface URPARegistroRepository extends JpaRepository<URPARegistro, Long> {

    List<URPARegistro> findByReporteId(Long reporteId);
}

package com.clinica.clinica.admision.repository;

import com.clinica.clinica.admision.entity.AdmisionDiagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmisionDiagnosticoRepository extends JpaRepository<AdmisionDiagnostico, Long> {

    List<AdmisionDiagnostico> findByCuentaId(Long cuentaId);

    List<AdmisionDiagnostico> findByCuentaIdAndTipo(Long cuentaId, String tipo);
}

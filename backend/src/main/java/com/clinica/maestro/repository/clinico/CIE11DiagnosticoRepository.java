package com.clinica.maestro.repository.clinico;

import com.clinica.maestro.entity.clinico.CIE11Diagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CIE11DiagnosticoRepository extends JpaRepository<CIE11Diagnostico, Long> {

    List<CIE11Diagnostico> findAllByOrderByFrecuenciaUsoDescCodigoAsc();

    List<CIE11Diagnostico> findByCodigoStartingWithIgnoreCaseOrderByFrecuenciaUsoDesc(String codigo);

    List<CIE11Diagnostico> findByDescripcionContainingIgnoreCaseOrderByFrecuenciaUsoDesc(String descripcion);

    boolean existsByCodigo(String codigo);
}

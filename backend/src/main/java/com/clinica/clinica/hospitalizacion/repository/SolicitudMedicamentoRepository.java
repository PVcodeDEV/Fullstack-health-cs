package com.clinica.clinica.hospitalizacion.repository;

import com.clinica.clinica.hospitalizacion.entity.SolicitudMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudMedicamentoRepository extends JpaRepository<SolicitudMedicamento, Long> {

    List<SolicitudMedicamento> findByHospitalizacionId(Long hospitalizacionId);

    List<SolicitudMedicamento> findByEstado(String estado);
}

package com.clinica.clinica.admision.repository;

import com.clinica.clinica.admision.entity.SolicitudHospitalizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudHospitalizacionRepository extends JpaRepository<SolicitudHospitalizacion, Long> {

    List<SolicitudHospitalizacion> findByCuentaId(Long cuentaId);

    List<SolicitudHospitalizacion> findByEstado(String estado);

    Optional<SolicitudHospitalizacion> findByCuentaIdAndEstado(Long cuentaId, String estado);
}

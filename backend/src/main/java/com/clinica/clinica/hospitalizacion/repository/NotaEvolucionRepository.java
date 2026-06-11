package com.clinica.clinica.hospitalizacion.repository;

import com.clinica.clinica.hospitalizacion.entity.NotaEvolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaEvolucionRepository extends JpaRepository<NotaEvolucion, Long> {

    List<NotaEvolucion> findByHospitalizacionId(Long hospitalizacionId);

    List<NotaEvolucion> findByHospitalizacionIdAndTipo(Long hospitalizacionId, String tipo);
}

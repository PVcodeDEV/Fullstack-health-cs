package com.clinica.clinica.hospitalizacion.repository;

import com.clinica.clinica.hospitalizacion.entity.CambioHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CambioHabitacionRepository extends JpaRepository<CambioHabitacion, Long> {

    List<CambioHabitacion> findByHospitalizacionId(Long hospitalizacionId);
}

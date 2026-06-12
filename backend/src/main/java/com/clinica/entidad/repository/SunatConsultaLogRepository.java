package com.clinica.entidad.repository;

import com.clinica.entidad.entity.SunatConsultaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SunatConsultaLogRepository extends JpaRepository<SunatConsultaLog, Long> {

    List<SunatConsultaLog> findByRucOrderByFechaDesc(String ruc);
}

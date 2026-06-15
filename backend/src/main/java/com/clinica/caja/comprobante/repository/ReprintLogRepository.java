package com.clinica.caja.comprobante.repository;

import com.clinica.caja.comprobante.entity.ReprintLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReprintLogRepository extends JpaRepository<ReprintLog, Long> {

    List<ReprintLog> findByComprobanteIdOrderByFechaDesc(Long comprobanteId);
}

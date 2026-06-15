package com.clinica.caja.liquidacion.repository;

import com.clinica.caja.liquidacion.entity.PaymentLeg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentLegRepository extends JpaRepository<PaymentLeg, Long> {

    List<PaymentLeg> findByLiquidacionId(Long liquidacionId);
}

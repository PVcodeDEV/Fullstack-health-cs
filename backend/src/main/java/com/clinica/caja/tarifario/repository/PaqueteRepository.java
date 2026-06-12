package com.clinica.caja.tarifario.repository;

import com.clinica.caja.tarifario.entity.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

    Optional<Paquete> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Paquete> findAllByActivoTrue();
}

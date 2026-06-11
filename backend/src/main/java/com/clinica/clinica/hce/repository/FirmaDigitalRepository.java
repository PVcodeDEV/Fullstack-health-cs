package com.clinica.clinica.hce.repository;

import com.clinica.clinica.hce.entity.FirmaDigital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FirmaDigitalRepository extends JpaRepository<FirmaDigital, Long> {

    Optional<FirmaDigital> findByDocumentoId(Long documentoId);

    List<FirmaDigital> findByUsuarioId(Long usuarioId);
}

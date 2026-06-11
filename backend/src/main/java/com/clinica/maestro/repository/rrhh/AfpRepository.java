package com.clinica.maestro.repository.rrhh;

import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.maestro.entity.rrhh.AfpTasaHistorica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AfpRepository extends JpaRepository<Afp, Long> {
    List<Afp> findAllByActivoTrueOrderByCodigo();
    Optional<Afp> findByCodigo(String codigo);

    @Query("SELECT a FROM AfpTasaHistorica a WHERE a.afp.id = :afpId AND a.vigenciaHasta IS NULL")
    Optional<AfpTasaHistorica> findCurrentRateByAfpId(@Param("afpId") Long afpId);
}

package com.clinica.seguridad.repository;

import com.clinica.seguridad.entity.NumeracionControl;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NumeracionControlRepository extends JpaRepository<NumeracionControl, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NumeracionControl n WHERE n.entidad = :entidad AND n.serie = :serie AND n.anio = :anio")
    Optional<NumeracionControl> findByEntidadAndSerieAndAnioWithLock(
            @Param("entidad") String entidad,
            @Param("serie") String serie,
            @Param("anio") int anio);

    Optional<NumeracionControl> findByEntidadAndSerieAndAnio(
            String entidad, String serie, int anio);

    boolean existsByEntidadAndSerieAndAnio(String entidad, String serie, int anio);
}

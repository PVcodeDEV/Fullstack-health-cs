package com.clinica.maestro.repository.identidad;

import com.clinica.maestro.entity.identidad.EstadoCivil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoCivilRepository extends JpaRepository<EstadoCivil, Long> {

    List<EstadoCivil> findAllByOrderByNombreAsc();

    Optional<EstadoCivil> findByCodigoReniec(String codigoReniec);

    boolean existsByCodigoReniec(String codigoReniec);
}

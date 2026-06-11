package com.clinica.maestro.repository.ubigeo;

import com.clinica.maestro.entity.ubigeo.UbigeoDepartamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UbigeoDepartamentoRepository extends JpaRepository<UbigeoDepartamento, String> {

    List<UbigeoDepartamento> findAllByOrderByNombreAsc();
}

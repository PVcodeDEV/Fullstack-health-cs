package com.clinica.maestro.repository.ubigeo;

import com.clinica.maestro.entity.ubigeo.UbigeoProvincia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UbigeoProvinciaRepository extends JpaRepository<UbigeoProvincia, String> {

    List<UbigeoProvincia> findAllByOrderByNombreAsc();

    List<UbigeoProvincia> findByDepartamentoCodigoOrderByNombreAsc(String departamentoCodigo);
}

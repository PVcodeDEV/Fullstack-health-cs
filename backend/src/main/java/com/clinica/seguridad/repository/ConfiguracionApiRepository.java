package com.clinica.seguridad.repository;

import com.clinica.seguridad.entity.ConfiguracionApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionApiRepository extends JpaRepository<ConfiguracionApi, Long> {

    Optional<ConfiguracionApi> findByModuloAndClave(String modulo, String clave);

    List<ConfiguracionApi> findByModulo(String modulo);

    boolean existsByModuloAndClave(String modulo, String clave);
}

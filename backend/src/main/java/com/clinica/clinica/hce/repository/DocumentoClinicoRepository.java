package com.clinica.clinica.hce.repository;

import com.clinica.clinica.hce.entity.DocumentoClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoClinicoRepository extends JpaRepository<DocumentoClinico, Long> {

    List<DocumentoClinico> findByPacienteId(Long pacienteId);

    List<DocumentoClinico> findByPacienteIdAndTipoDocumento(Long pacienteId, String tipoDocumento);

    List<DocumentoClinico> findByHospitalizacionId(Long hospitalizacionId);

    List<DocumentoClinico> findByDocumentoOriginalId(Long documentoOriginalId);
}

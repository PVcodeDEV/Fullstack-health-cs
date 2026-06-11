package com.clinica.clinica.hce.repository;

import com.clinica.clinica.hce.entity.DocumentoClinico;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DocumentoClinicoRepositoryTest {

    @Autowired
    private DocumentoClinicoRepository documentoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private Persona paciente;

    @BeforeEach
    void setUp() {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocumentoIdentidadRepository.saveAndFlush(tdi);

        paciente = new Persona();
        paciente.setTipoDocumentoIdentidad(tdi);
        paciente.setNumeroDocumento("12345678");
        paciente.setNombres("JUAN");
        paciente.setApellidoPaterno("PEREZ");
        paciente.setActivo(true);
        paciente = personaRepository.saveAndFlush(paciente);

        var doc = new DocumentoClinico();
        doc.setPacienteId(paciente.getId());
        doc.setHospitalizacionId(1L);
        doc.setTipoDocumento("INFORME_MEDICO");
        doc.setContenido("Contenido del documento".getBytes(StandardCharsets.UTF_8));
        doc.setTamanoBytes(1024L);
        documentoRepository.saveAndFlush(doc);
    }

    @Test
    void shouldSaveAndFindById() {
        var doc = new DocumentoClinico();
        doc.setPacienteId(paciente.getId());
        doc.setHospitalizacionId(2L);
        doc.setTipoDocumento("RECETA");
        doc.setContenido("Receta médica".getBytes(StandardCharsets.UTF_8));
        doc.setTamanoBytes(512L);

        var saved = documentoRepository.save(doc);
        assertThat(saved.getId()).isNotNull();

        var found = documentoRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTipoDocumento()).isEqualTo("RECETA");
    }

    @Test
    void shouldFindByPacienteId() {
        var result = documentoRepository.findByPacienteId(paciente.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTipoDocumento()).isEqualTo("INFORME_MEDICO");
    }

    @Test
    void shouldFindByHospitalizacionId() {
        var result = documentoRepository.findByHospitalizacionId(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFindByPacienteIdAndTipoDocumento() {
        var result = documentoRepository.findByPacienteIdAndTipoDocumento(paciente.getId(), "INFORME_MEDICO");
        assertThat(result).hasSize(1);

        var empty = documentoRepository.findByPacienteIdAndTipoDocumento(paciente.getId(), "RECETA");
        assertThat(empty).isEmpty();
    }

    @Test
    void shouldStoreByteaContent() {
        byte[] content = "TEST BYTEA CONTENT".getBytes(StandardCharsets.UTF_8);
        var doc = new DocumentoClinico();
        doc.setPacienteId(paciente.getId());
        doc.setHospitalizacionId(3L);
        doc.setTipoDocumento("ANALISIS");
        doc.setContenido(content);
        doc.setTamanoBytes((long) content.length);
        documentoRepository.saveAndFlush(doc);

        var found = documentoRepository.findByHospitalizacionId(3L).get(0);
        assertThat(found.getContenido()).isEqualTo(content);
        assertThat(found.getTamanoBytes()).isEqualTo(content.length);
    }

    @Test
    void shouldFindByDocumentoOriginalId() {
        var doc = documentoRepository.findByHospitalizacionId(1L).get(0);
        var version = new DocumentoClinico();
        version.setPacienteId(paciente.getId());
        version.setHospitalizacionId(1L);
        version.setTipoDocumento("INFORME_MEDICO");
        version.setDocumentoOriginalId(doc.getId());
        version.setContenido("Version 2".getBytes(StandardCharsets.UTF_8));
        version.setTamanoBytes(128L);
        documentoRepository.saveAndFlush(version);

        var versions = documentoRepository.findByDocumentoOriginalId(doc.getId());
        assertThat(versions).hasSize(1);
    }
}

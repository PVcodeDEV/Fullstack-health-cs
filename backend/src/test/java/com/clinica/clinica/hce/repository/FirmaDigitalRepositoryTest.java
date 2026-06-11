package com.clinica.clinica.hce.repository;

import com.clinica.clinica.hce.entity.DocumentoClinico;
import com.clinica.clinica.hce.entity.FirmaDigital;
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
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FirmaDigitalRepositoryTest {

    @Autowired
    private FirmaDigitalRepository firmaRepository;

    @Autowired
    private DocumentoClinicoRepository documentoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private DocumentoClinico documento;
    private FirmaDigital firma;

    @BeforeEach
    void setUp() {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocumentoIdentidadRepository.saveAndFlush(tdi);

        var paciente = new Persona();
        paciente.setTipoDocumentoIdentidad(tdi);
        paciente.setNumeroDocumento("12345678");
        paciente.setNombres("JUAN");
        paciente.setApellidoPaterno("PEREZ");
        paciente.setActivo(true);
        paciente = personaRepository.saveAndFlush(paciente);

        documento = new DocumentoClinico();
        documento.setPacienteId(paciente.getId());
        documento.setHospitalizacionId(1L);
        documento.setTipoDocumento("INFORME");
        documento.setContenido("Contenido".getBytes(StandardCharsets.UTF_8));
        documento.setTamanoBytes(128L);
        documento = documentoRepository.saveAndFlush(documento);

        firma = new FirmaDigital();
        firma.setDocumentoId(documento.getId());
        firma.setUsuarioId(1L);
        firma.setFechaFirma(LocalDateTime.now());
        firma.setHashSha256("a".repeat(64)); // 64-char SHA-256 hex
        firma.setIpOrigen("192.168.1.1");
        firma = firmaRepository.saveAndFlush(firma);
    }

    @Test
    void shouldSaveAndFindById() {
        var newDoc = new DocumentoClinico();
        newDoc.setPacienteId(1L);
        newDoc.setHospitalizacionId(99L);
        newDoc.setTipoDocumento("RECETA");
        newDoc.setContenido("test".getBytes(StandardCharsets.UTF_8));
        newDoc.setTamanoBytes(4L);
        newDoc = documentoRepository.saveAndFlush(newDoc);

        var f = new FirmaDigital();
        f.setDocumentoId(newDoc.getId());
        f.setUsuarioId(2L);
        f.setFechaFirma(LocalDateTime.now());
        f.setHashSha256("b".repeat(64));
        f.setIpOrigen("10.0.0.1");

        var saved = firmaRepository.save(f);
        assertThat(saved.getId()).isNotNull();

        var found = firmaRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getHashSha256()).isEqualTo("b".repeat(64));
    }

    @Test
    void shouldFindByDocumentoId() {
        var result = firmaRepository.findByDocumentoId(documento.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getHashSha256()).hasSize(64);
    }

    @Test
    void shouldFindByUsuarioId() {
        var result = firmaRepository.findByUsuarioId(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldStoreSha256HashCorrectly() {
        var result = firmaRepository.findByDocumentoId(documento.getId());
        assertThat(result).isPresent();
        String hash = result.get().getHashSha256();
        assertThat(hash).matches("[0-9a-f]{64}");
    }
}

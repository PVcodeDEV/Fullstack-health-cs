package com.clinica.clinica.hce.service;

import com.clinica.clinica.hce.dto.DocumentoClinicoRequest;
import com.clinica.clinica.hce.entity.DocumentoClinico;
import com.clinica.clinica.hce.entity.FirmaDigital;
import com.clinica.clinica.hce.repository.DocumentoClinicoRepository;
import com.clinica.clinica.hce.repository.FirmaDigitalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HCEServiceTest {

    @Mock
    private DocumentoClinicoRepository documentoRepository;
    @Mock
    private FirmaDigitalRepository firmaRepository;

    @InjectMocks
    private HCEService service;

    @Captor
    private ArgumentCaptor<DocumentoClinico> docCaptor;
    @Captor
    private ArgumentCaptor<FirmaDigital> firmaCaptor;

    private byte[] getContentBytes(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    private String computeHash(byte[] content) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void crearDocumento_ShouldSaveWithSha256Hash() {
        var content = "Contenido del documento clínico".getBytes(StandardCharsets.UTF_8);
        var expectedHash = computeHash(content);

        var savedDoc = new DocumentoClinico();
        savedDoc.setId(1L);
        savedDoc.setHospitalizacionId(1L);
        savedDoc.setTipoDocumento("INFORME");
        savedDoc.setContenido(content);
        savedDoc.setTamanoBytes((long) content.length);
        when(documentoRepository.save(any())).thenReturn(savedDoc);
        when(firmaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new DocumentoClinicoRequest(1L, "INFORME", "Contenido del documento clínico", null);
        var result = service.crearDocumento(request, 1L, "192.168.1.1");

        assertThat(result).isNotNull();
        assertThat(result.firmaPresente()).isTrue();
        assertThat(result.hashSha256()).isEqualTo(expectedHash);
        assertThat(result.hashSha256()).matches("[0-9a-f]{64}");

        verify(firmaRepository).save(firmaCaptor.capture());
        assertThat(firmaCaptor.getValue().getHashSha256()).isEqualTo(expectedHash);
        assertThat(firmaCaptor.getValue().getIpOrigen()).isEqualTo("192.168.1.1");
        assertThat(firmaCaptor.getValue().getUsuarioId()).isEqualTo(1L);
    }

    @Test
    void listarDocumentos_ShouldReturnListWithFirmaStatus() {
        var doc = new DocumentoClinico();
        doc.setId(1L);
        doc.setHospitalizacionId(1L);
        doc.setTipoDocumento("INFORME");
        doc.setContenido("test".getBytes(StandardCharsets.UTF_8));
        doc.setTamanoBytes(4L);

        var firma = new FirmaDigital();
        firma.setDocumentoId(1L);
        firma.setHashSha256("a".repeat(64));

        when(documentoRepository.findByHospitalizacionId(1L)).thenReturn(List.of(doc));
        when(firmaRepository.findByDocumentoId(1L)).thenReturn(Optional.of(firma));

        var result = service.listarDocumentos(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).firmaPresente()).isTrue();
    }

    @Test
    void listarDocumentos_ShouldReturnListWithoutFirma() {
        var doc = new DocumentoClinico();
        doc.setId(1L);
        doc.setHospitalizacionId(1L);
        doc.setTipoDocumento("INFORME");
        doc.setContenido("test".getBytes(StandardCharsets.UTF_8));
        doc.setTamanoBytes(4L);

        when(documentoRepository.findByHospitalizacionId(1L)).thenReturn(List.of(doc));
        when(firmaRepository.findByDocumentoId(1L)).thenReturn(Optional.empty());

        var result = service.listarDocumentos(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).firmaPresente()).isFalse();
    }

    @Test
    void verificarFirma_WithValidHash_ShouldReturnTrue() {
        var content = "Contenido original".getBytes(StandardCharsets.UTF_8);
        var hash = computeHash(content);

        var doc = new DocumentoClinico();
        doc.setId(1L);
        doc.setContenido(content);

        var firma = new FirmaDigital();
        firma.setDocumentoId(1L);
        firma.setHashSha256(hash);

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(firmaRepository.findByDocumentoId(1L)).thenReturn(Optional.of(firma));

        var result = service.verificarFirma(1L);

        assertThat(result).isTrue();
    }

    @Test
    void verificarFirma_WithInvalidHash_ShouldReturnFalse() {
        var content = "Contenido original".getBytes(StandardCharsets.UTF_8);

        var doc = new DocumentoClinico();
        doc.setId(1L);
        doc.setContenido(content);

        var firma = new FirmaDigital();
        firma.setDocumentoId(1L);
        firma.setHashSha256("f".repeat(64)); // wrong hash

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(firmaRepository.findByDocumentoId(1L)).thenReturn(Optional.of(firma));

        var result = service.verificarFirma(1L);

        assertThat(result).isFalse();
    }

    @Test
    void verificarFirma_WithInvalidDocumento_ShouldThrow() {
        when(documentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verificarFirma(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void verificarFirma_WithNoFirma_ShouldThrow() {
        var doc = new DocumentoClinico();
        doc.setId(1L);
        doc.setContenido("test".getBytes(StandardCharsets.UTF_8));

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(firmaRepository.findByDocumentoId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verificarFirma(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no tiene firma");
    }
}

package com.clinica.clinica.hce.service;

import com.clinica.clinica.hce.dto.*;
import com.clinica.clinica.hce.entity.*;
import com.clinica.clinica.hce.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
@Transactional
public class HCEService {

    private static final Logger log = LoggerFactory.getLogger(HCEService.class);

    private final DocumentoClinicoRepository documentoRepository;
    private final FirmaDigitalRepository firmaRepository;

    public HCEService(DocumentoClinicoRepository documentoRepository,
                      FirmaDigitalRepository firmaRepository) {
        this.documentoRepository = documentoRepository;
        this.firmaRepository = firmaRepository;
    }

    public DocumentoClinicoResponse crearDocumento(DocumentoClinicoRequest request, Long usuarioId, String ipOrigen) {
        DocumentoClinico doc = new DocumentoClinico();
        doc.setHospitalizacionId(request.hospitalizacionId());
        doc.setTipoDocumento(request.tipoDocumento());
        // Convert String content to byte[] for entity storage
        byte[] contenidoBytes = request.contenido().getBytes(StandardCharsets.UTF_8);
        doc.setContenido(contenidoBytes);
        doc.setTamanoBytes((long) contenidoBytes.length);
        doc = documentoRepository.save(doc);

        // Generate SHA-256 hash from the content bytes
        String hash = generarHash(contenidoBytes);

        // Create digital signature
        FirmaDigital firma = new FirmaDigital();
        firma.setDocumentoId(doc.getId());
        firma.setUsuarioId(usuarioId);
        firma.setFechaFirma(LocalDateTime.now());
        firma.setHashSha256(hash);
        firma.setIpOrigen(ipOrigen);
        firmaRepository.save(firma);

        log.debug("Documento clínico creado id={}, hash={}", doc.getId(), hash);
        return toDocumentoResponse(doc, true, hash);
    }

    @Transactional(readOnly = true)
    public List<DocumentoClinicoResponse> listarDocumentos(Long hospitalizacionId) {
        return documentoRepository.findByHospitalizacionId(hospitalizacionId).stream()
            .map(doc -> {
                boolean firmado = firmaRepository.findByDocumentoId(doc.getId()).isPresent();
                String hash = firmaRepository.findByDocumentoId(doc.getId())
                    .map(FirmaDigital::getHashSha256).orElse(null);
                return toDocumentoResponse(doc, firmado, hash);
            })
            .toList();
    }

    public boolean verificarFirma(Long documentoId) {
        DocumentoClinico doc = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con id: " + documentoId));

        FirmaDigital firma = firmaRepository.findByDocumentoId(documentoId)
            .orElseThrow(() -> new EntityNotFoundException("El documento no tiene firma digital"));

        // Hash from stored byte[] content
        String hashActual = generarHash(doc.getContenido());
        boolean valida = hashActual.equals(firma.getHashSha256());
        log.debug("Verificación de firma para documentoId={}: {}", documentoId, valida ? "VÁLIDA" : "INVÁLIDA");
        return valida;
    }

    private String generarHash(byte[] contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(contenido);
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    private DocumentoClinicoResponse toDocumentoResponse(DocumentoClinico doc, boolean firmaPresente, String hash) {
        return new DocumentoClinicoResponse(
            doc.getId(), doc.getHospitalizacionId(), doc.getTipoDocumento(),
            null, null,
            String.valueOf(doc.getMedicoId() != null ? doc.getMedicoId() : ""),
            doc.getCreatedAt(), firmaPresente, hash
        );
    }
}

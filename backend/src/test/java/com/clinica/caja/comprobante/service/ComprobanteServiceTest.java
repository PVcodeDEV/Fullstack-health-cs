package com.clinica.caja.comprobante.service;

import com.clinica.caja.comprobante.dto.ComprobanteEmitirRequest;
import com.clinica.caja.comprobante.dto.ComprobanteResponse;
import com.clinica.caja.comprobante.dto.NotaCreditoRequest;
import com.clinica.caja.comprobante.dto.ReprintResponse;
import com.clinica.caja.comprobante.entity.Comprobante;
import com.clinica.caja.comprobante.entity.ReprintLog;
import com.clinica.caja.comprobante.repository.ComprobanteRepository;
import com.clinica.caja.comprobante.repository.ReprintLogRepository;
import com.clinica.caja.comprobante.service.SunatXmlGenerator.LineItem;
import com.clinica.clinica.cuenta.service.CuentaService;
import com.clinica.entidad.entity.Empresa;
import com.clinica.entidad.repository.EmpresaRepository;
import com.clinica.maestro.entity.financiero.TipoComprobante;
import com.clinica.maestro.repository.financiero.TipoComprobanteRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComprobanteServiceTest {

    @Mock private ComprobanteRepository comprobanteRepository;
    @Mock private ReprintLogRepository reprintLogRepository;
    @Mock private TipoComprobanteRepository tipoComprobanteRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private CuentaService cuentaService;
    @Mock private SunatXmlGenerator xmlGenerator;

    private ComprobanteService service;

    @Captor private ArgumentCaptor<Comprobante> comprobanteCaptor;

    private static final Long USUARIO_ID = 1L;
    private static final Long LIQUIDACION_ID = 100L;
    private static final Long PERSONA_ID = 10L;
    private static final Long EMPRESA_ID = 5L;
    private static final BigDecimal MONTO_TOTAL = new BigDecimal("500.00");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 13, 10, 0, 0);

    private TipoComprobante tipoBoleta;
    private TipoComprobante tipoFactura;
    private TipoComprobante tipoNotaCredito;
    private Persona persona;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        service = new ComprobanteService(
            comprobanteRepository, reprintLogRepository, tipoComprobanteRepository,
            personaRepository, empresaRepository, cuentaService, xmlGenerator);

        tipoBoleta = createTipoComprobante(1, "03", "BOLETA");
        tipoFactura = createTipoComprobante(2, "01", "FACTURA");
        tipoNotaCredito = createTipoComprobante(3, "07", "NOTA CRÉDITO");

        persona = new Persona();
        persona.setId(PERSONA_ID);
        persona.setNombres("Juan");
        persona.setApellidoPaterno("Pérez");
        persona.setNumeroDocumento("12345678");
        persona.setDireccion("Jr. Las Flores 123, Lima");

        empresa = new Empresa();
        empresa.setId(EMPRESA_ID);
        empresa.setRuc("20123456789");
        empresa.setRazonSocial("Clínica Ejemplo SAC");
        empresa.setDireccionFiscal("Av. Principal 456, Lima");
    }

    // ============================================================
    // CPR-001-1: Issue Boleta for consumer
    // ============================================================

    @Test
    void emitir_WithBoletaAndPersona_ShouldCreateComprobante() {
        // GIVEN a Boleta request with personaId
        when(tipoComprobanteRepository.findByCodigoSunat("03")).thenReturn(Optional.of(tipoBoleta));
        when(personaRepository.findById(PERSONA_ID)).thenReturn(Optional.of(persona));
        when(comprobanteRepository.findMaxCorrelativoBySerie("001")).thenReturn(Optional.empty());
        when(xmlGenerator.generateInvoice(anyString(), anyString(), anyString(), any(), anyString(),
            anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any()))
            .thenReturn("<xml>dummy-invoice</xml>");
        when(comprobanteRepository.save(any())).thenAnswer(invocation -> {
            Comprobante c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        var request = new ComprobanteEmitirRequest("03", null, PERSONA_ID, null, MONTO_TOTAL);

        // WHEN
        ComprobanteResponse response = service.emitir(LIQUIDACION_ID, request, USUARIO_ID);

        // THEN
        assertThat(response.serie()).isEqualTo("001");
        assertThat(response.correlativo()).isEqualTo("00000001");
        assertThat(response.estado()).isEqualTo("EMITIDO");
        assertThat(response.tipoDocCliente()).isEqualTo("1");
        assertThat(response.numDocCliente()).isEqualTo("12345678");
        assertThat(response.nombreCliente()).isEqualTo("Juan Pérez");
        assertThat(response.personaId()).isEqualTo(PERSONA_ID);
        assertThat(response.empresaId()).isNull();
        assertThat(response.liquidacionId()).isEqualTo(LIQUIDACION_ID);
        assertThat(response.xmlGenerado()).isNotNull();

        verify(comprobanteRepository).save(comprobanteCaptor.capture());
        Comprobante saved = comprobanteCaptor.getValue();
        assertThat(saved.getSerie()).isEqualTo("001");
        assertThat(saved.getEstado()).isEqualTo("EMITIDO");
    }

    // ============================================================
    // CPR-001-2: Issue Factura for business with Empresa
    // ============================================================

    @Test
    void emitir_WithFacturaAndEmpresa_ShouldCreateComprobante() {
        // GIVEN a Factura request with empresaId
        when(tipoComprobanteRepository.findByCodigoSunat("01")).thenReturn(Optional.of(tipoFactura));
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
        when(comprobanteRepository.findMaxCorrelativoBySerie("001")).thenReturn(Optional.of("00000005"));
        when(xmlGenerator.generateInvoice(anyString(), anyString(), anyString(), any(), anyString(),
            anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any()))
            .thenReturn("<xml>dummy-invoice</xml>");
        when(comprobanteRepository.save(any())).thenAnswer(invocation -> {
            Comprobante c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        var request = new ComprobanteEmitirRequest("01", null, null, EMPRESA_ID, new BigDecimal("5000.00"));

        // WHEN
        ComprobanteResponse response = service.emitir(LIQUIDACION_ID, request, USUARIO_ID);

        // THEN
        assertThat(response.tipoComprobanteId()).isEqualTo(2);
        assertThat(response.tipoDocCliente()).isEqualTo("6");
        assertThat(response.numDocCliente()).isEqualTo("20123456789");
        assertThat(response.nombreCliente()).isEqualTo("Clínica Ejemplo SAC");
        assertThat(response.direccionCliente()).isEqualTo("Av. Principal 456, Lima");
        assertThat(response.empresaId()).isEqualTo(EMPRESA_ID);
        assertThat(response.personaId()).isNull();
        assertThat(response.correlativo()).isEqualTo("00000006");
    }

    // ============================================================
    // CPR-001-3: Factura missing empresaId
    // ============================================================

    @Test
    void emitir_WithFacturaNoEmpresaId_ShouldThrow() {
        // GIVEN a Factura request without empresaId
        when(tipoComprobanteRepository.findByCodigoSunat("01")).thenReturn(Optional.of(tipoFactura));

        var request = new ComprobanteEmitirRequest("01", null, PERSONA_ID, null, MONTO_TOTAL);

        // WHEN / THEN
        assertThatThrownBy(() -> service.emitir(LIQUIDACION_ID, request, USUARIO_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Factura requiere empresaId");

        verify(comprobanteRepository, never()).save(any());
    }

    // ============================================================
    // CPR-001-4: Mutually exclusive client references
    // ============================================================

    @Test
    void emitir_WithBothPersonaAndEmpresa_ShouldThrow() {
        // GIVEN a request with both personaId and empresaId
        when(tipoComprobanteRepository.findByCodigoSunat("03")).thenReturn(Optional.of(tipoBoleta));

        var request = new ComprobanteEmitirRequest("03", null, PERSONA_ID, EMPRESA_ID, MONTO_TOTAL);

        // WHEN / THEN
        assertThatThrownBy(() -> service.emitir(LIQUIDACION_ID, request, USUARIO_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no ambos");

        verify(comprobanteRepository, never()).save(any());
    }

    @Test
    void emitir_WithNeitherPersonaNorEmpresa_ShouldThrow() {
        // GIVEN a request with neither personaId nor empresaId
        when(tipoComprobanteRepository.findByCodigoSunat("03")).thenReturn(Optional.of(tipoBoleta));

        var request = new ComprobanteEmitirRequest("03", null, null, null, MONTO_TOTAL);

        // WHEN / THEN
        assertThatThrownBy(() -> service.emitir(LIQUIDACION_ID, request, USUARIO_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Debe proporcionar personaId");

        verify(comprobanteRepository, never()).save(any());
    }

    // ============================================================
    // CPR-001-5: Auto-increment correlativo
    // ============================================================

    @Test
    void emitir_WithExistingCorrelativo_ShouldAutoIncrement() {
        // GIVEN existing correlativo = 00000005
        when(tipoComprobanteRepository.findByCodigoSunat("03")).thenReturn(Optional.of(tipoBoleta));
        when(personaRepository.findById(PERSONA_ID)).thenReturn(Optional.of(persona));
        when(comprobanteRepository.findMaxCorrelativoBySerie("001")).thenReturn(Optional.of("00000005"));
        when(xmlGenerator.generateInvoice(anyString(), anyString(), anyString(), any(), anyString(),
            anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any()))
            .thenReturn("<xml>dummy</xml>");
        when(comprobanteRepository.save(any())).thenAnswer(invocation -> {
            Comprobante c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });

        var request = new ComprobanteEmitirRequest("03", null, PERSONA_ID, null, MONTO_TOTAL);
        ComprobanteResponse response = service.emitir(LIQUIDACION_ID, request, USUARIO_ID);

        // THEN correlativo = 00000006
        assertThat(response.correlativo()).isEqualTo("00000006");
    }

    // ============================================================
    // CPR-002-1: Enforce series 001
    // ============================================================

    @Test
    void emitir_WithInvalidSerie_ShouldThrow() {
        // GIVEN a request with serie "002"
        when(tipoComprobanteRepository.findByCodigoSunat("03")).thenReturn(Optional.of(tipoBoleta));

        var request = new ComprobanteEmitirRequest("03", "002", PERSONA_ID, null, MONTO_TOTAL);

        // WHEN / THEN
        assertThatThrownBy(() -> service.emitir(LIQUIDACION_ID, request, USUARIO_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Solo se permite serie 001");

        verify(comprobanteRepository, never()).save(any());
    }

    // ============================================================
    // CPR-003-1: Full cancellation via Nota Crédito
    // ============================================================

    @Test
    void notaCredito_WithFullAmount_ShouldAnularOriginal() {
        // GIVEN an emitted comprobante with total 500.00
        Comprobante original = createEmittedComprobante(1L, "001", "00000001", new BigDecimal("500.00"));
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(original));
        when(tipoComprobanteRepository.findByCodigoSunat("07")).thenReturn(Optional.of(tipoNotaCredito));
        when(comprobanteRepository.findMaxCorrelativoBySerie("001")).thenReturn(Optional.of("00000001"));
        when(xmlGenerator.generateCreditNote(anyString(), anyString(), any(), anyString(),
            anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any(),
            anyString(), anyString(), anyString())).thenReturn("<xml>nc</xml>");
        when(comprobanteRepository.save(any())).thenAnswer(invocation -> {
            Comprobante c = invocation.getArgument(0);
            if (c.getId() == null) c.setId(2L);
            return c;
        });

        // WHEN full cancellation
        var request = new NotaCreditoRequest(new BigDecimal("500.00"), "Cancelación total por error en datos");
        ComprobanteResponse response = service.notaCredito(1L, request, USUARIO_ID);

        // THEN Nota Crédito created in Series 001
        assertThat(response.serie()).isEqualTo("001");
        assertThat(response.correlativo()).isEqualTo("00000002");
        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.comprobanteOriginalId()).isEqualTo(1L);

        // AND original was ANULADO (2 saves: NC creation + original update)
        verify(comprobanteRepository, org.mockito.Mockito.times(2)).save(comprobanteCaptor.capture());
        List<Comprobante> saved = comprobanteCaptor.getAllValues();
        Comprobante updatedOriginal = saved.stream()
            .filter(c -> c.getId() != null && c.getId().equals(1L))
            .findFirst().orElse(null);
        assertThat(updatedOriginal).isNotNull();
        assertThat(updatedOriginal.getEstado()).isEqualTo("ANULADO");
    }

    // ============================================================
    // CPR-003-2: Partial adjustment via Nota Crédito
    // ============================================================

    @Test
    void notaCredito_WithPartialAmount_ShouldKeepOriginalEmitido() {
        // GIVEN an emitted comprobante with total 1000.00
        Comprobante original = createEmittedComprobante(1L, "001", "00000001", new BigDecimal("1000.00"));
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(original));
        when(tipoComprobanteRepository.findByCodigoSunat("07")).thenReturn(Optional.of(tipoNotaCredito));
        when(comprobanteRepository.findMaxCorrelativoBySerie("001")).thenReturn(Optional.of("00000001"));
        when(xmlGenerator.generateCreditNote(anyString(), anyString(), any(), anyString(),
            anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any(),
            anyString(), anyString(), anyString())).thenReturn("<xml>nc</xml>");
        when(comprobanteRepository.save(any())).thenAnswer(invocation -> {
            Comprobante c = invocation.getArgument(0);
            if (c.getId() == null) c.setId(2L);
            return c;
        });

        // WHEN partial adjustment of 200.00
        var request = new NotaCreditoRequest(new BigDecimal("200.00"), "Descuento no aplicado");
        ComprobanteResponse response = service.notaCredito(1L, request, USUARIO_ID);

        // THEN Nota Crédito created
        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("200.00"));

        // AND original remains EMITIDO
        assertThat(original.getEstado()).isEqualTo("EMITIDO");
    }

    // ============================================================
    // CPR-003-3 (spec scenario): Nota Crédito exceeds original total
    // ============================================================

    @Test
    void notaCredito_WithExceedingAmount_ShouldThrow() {
        // GIVEN original with total 500.00
        Comprobante original = createEmittedComprobante(1L, "001", "00000001", new BigDecimal("500.00"));
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(original));

        // WHEN nota with monto 600.00
        var request = new NotaCreditoRequest(new BigDecimal("600.00"), "Exceeds original");

        // THEN 422 equivalent
        assertThatThrownBy(() -> service.notaCredito(1L, request, USUARIO_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede exceder");
    }

    // ============================================================
    // CPR-003-4 (spec): Nota Crédito on already cancelled comprobante
    // ============================================================

    @Test
    void notaCredito_WithAnuladoComprobante_ShouldThrow() {
        // GIVEN comprobante with estado ANULADO
        Comprobante anulado = createEmittedComprobante(1L, "001", "00000001", new BigDecimal("500.00"));
        anulado.setEstado("ANULADO");
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(anulado));

        // WHEN
        var request = new NotaCreditoRequest(new BigDecimal("100.00"), "Already cancelled");

        // THEN
        assertThatThrownBy(() -> service.notaCredito(1L, request, USUARIO_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ya se encuentra anulado");
    }

    // ============================================================
    // CPR-004-1: Reprint with watermark
    // ============================================================

    @Test
    void reimprimir_WithEmitidoComprobante_ShouldReturnWatermarkedXml() {
        // GIVEN an emitted comprobante with stored XML
        Comprobante entity = createEmittedComprobante(1L, "001", "00000001", new BigDecimal("500.00"));
        entity.setXmlGenerado("<xml>original-content</xml>");
        entity.setNombreCliente("Juan Pérez");
        entity.setTotal(new BigDecimal("500.00"));

        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(reprintLogRepository.save(any())).thenAnswer(invocation -> {
            ReprintLog log = invocation.getArgument(0);
            log.setId(99L);
            log.setFecha(NOW);
            return log;
        });

        // WHEN
        ReprintResponse response = service.reimprimir(1L, USUARIO_ID, "192.168.1.1");

        // THEN response includes watermark prefix
        assertThat(response.xmlConCopia()).startsWith("<!-- COPIA / REIMPRESIÓN -->");
        assertThat(response.xmlConCopia()).contains("<xml>original-content</xml>");
        assertThat(response.reprintLogId()).isEqualTo(99L);

        // AND reprint log was saved
        verify(reprintLogRepository).save(any());
    }

    // ============================================================
    // CPR-004-2: Reprint of cancelled comprobante
    // ============================================================

    @Test
    void reimprimir_WithAnuladoComprobante_ShouldIncludeAnuladoNote() {
        // GIVEN an ANULADO comprobante
        Comprobante entity = createEmittedComprobante(1L, "001", "00000001", new BigDecimal("500.00"));
        entity.setXmlGenerado("<xml>original</xml>");
        entity.setEstado("ANULADO");

        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(reprintLogRepository.save(any())).thenAnswer(invocation -> {
            ReprintLog log = invocation.getArgument(0);
            log.setId(100L);
            log.setFecha(NOW);
            return log;
        });

        // WHEN
        ReprintResponse response = service.reimprimir(1L, USUARIO_ID, "10.0.0.1");

        // THEN watermark includes "COMPROBANTE ANULADO"
        assertThat(response.xmlConCopia()).contains("<!-- COPIA / REIMPRESIÓN -->");
        assertThat(response.xmlConCopia()).contains("<!-- COMPROBANTE ANULADO -->");
    }

    // ============================================================
    // CPR-005: XML generation validity (via service integration)
    // ============================================================

    @Test
    void emitir_ShouldGenerateXml() {
        // GIVEN
        when(tipoComprobanteRepository.findByCodigoSunat("03")).thenReturn(Optional.of(tipoBoleta));
        when(personaRepository.findById(PERSONA_ID)).thenReturn(Optional.of(persona));
        when(comprobanteRepository.findMaxCorrelativoBySerie("001")).thenReturn(Optional.empty());
        when(xmlGenerator.generateInvoice(anyString(), anyString(), anyString(), any(), anyString(),
            anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any()))
            .thenReturn("<xml>valid-ubl-21-invoice</xml>");
        when(comprobanteRepository.save(any())).thenAnswer(invocation -> {
            Comprobante c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        var request = new ComprobanteEmitirRequest("03", null, PERSONA_ID, null, MONTO_TOTAL);

        // WHEN
        ComprobanteResponse response = service.emitir(LIQUIDACION_ID, request, USUARIO_ID);

        // THEN xmlGenerado is populated
        assertThat(response.xmlGenerado()).isEqualTo("<xml>valid-ubl-21-invoice</xml>");
    }

    // ============================================================
    // Helpers
    // ============================================================

    private TipoComprobante createTipoComprobante(Integer id, String codigoSunat, String nombre) {
        TipoComprobante tc = new TipoComprobante();
        tc.setId(id);
        tc.setCodigoSunat(codigoSunat);
        tc.setNombre(nombre);
        return tc;
    }

    private Comprobante createEmittedComprobante(Long id, String serie, String correlativo, BigDecimal total) {
        Comprobante c = new Comprobante();
        c.setId(id);
        c.setTipoComprobanteId(1);
        c.setSerie(serie);
        c.setCorrelativo(correlativo);
        c.setFechaEmision(NOW);
        c.setTipoDocCliente("1");
        c.setNumDocCliente("12345678");
        c.setNombreCliente("Juan Pérez");
        c.setDireccionCliente("Jr. Las Flores 123");
        c.setPersonaId(PERSONA_ID);
        c.setSubtotal(total.divide(BigDecimal.valueOf(1.18), 2, java.math.RoundingMode.HALF_UP));
        c.setIgv(total.subtract(c.getSubtotal()));
        c.setTotal(total);
        c.setMoneda("PEN");
        c.setLiquidacionId(LIQUIDACION_ID);
        c.setEstado("EMITIDO");
        return c;
    }
}

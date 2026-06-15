package com.clinica.caja.comprobante.service;

import com.clinica.config.CajaComprobanteProperties;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

/**
 * UBL 2.1 SUNAT XML generator for electronic comprobantes.
 * Uses XMLStreamWriter (no JAXB dependency) to build invoices,
 * Boletas, and Notas de Crédito per SUNAT specifications.
 */
@Component
public class SunatXmlGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String UBL_INVOICE_NS = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
    private static final String UBL_CREDIT_NOTE_NS = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2";
    private static final String CAC_NS = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String CBC_NS = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    private static final String DS_NS = "http://www.w3.org/2000/09/xmldsig#";
    private static final String EXT_NS = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";

    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    private final CajaComprobanteProperties config;

    public SunatXmlGenerator(CajaComprobanteProperties config) {
        this.config = config;
    }

    /**
     * Generate UBL 2.1 XML for an invoice (Factura 01 or Boleta 03).
     */
    public String generateInvoice(String tipoComprobante, String serie, String correlativo,
                                  LocalDateTime fechaEmision, String moneda,
                                  String clienteTipoDoc, String clienteNumDoc,
                                  String clienteNombre, String clienteDireccion,
                                  BigDecimal subtotal, BigDecimal igv, BigDecimal total,
                                  List<LineItem> items) {
        try {
            StringWriter sw = new StringWriter();
            XMLStreamWriter xml = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
            xml.writeStartDocument("ISO-8859-1", "1.0");

            String rootNs = "01".equals(tipoComprobante) || "03".equals(tipoComprobante)
                ? UBL_INVOICE_NS : UBL_CREDIT_NOTE_NS;
            String rootLocal = "01".equals(tipoComprobante) || "03".equals(tipoComprobante)
                ? "Invoice" : "CreditNote";

            xml.writeStartElement(rootNs, rootLocal);
            xml.writeNamespace("", rootNs);
            xml.writeNamespace("cac", CAC_NS);
            xml.writeNamespace("cbc", CBC_NS);
            xml.writeNamespace("ds", DS_NS);
            xml.writeNamespace("ext", EXT_NS);

            writeUBLExtensions(xml);
            writeUBLVersionId(xml, "2.1");
            writeCustomizationId(xml, "2.0");

            // Comprobante ID: Serie-Correlativo
            writeElement(xml, CBC_NS, "ID", serie + "-" + correlativo);

            writeElement(xml, CBC_NS, "IssueDate", fechaEmision.format(DATE_FMT));
            writeElement(xml, CBC_NS, "IssueTime", fechaEmision.format(TIME_FMT));

            if ("01".equals(tipoComprobante) || "03".equals(tipoComprobante)) {
                writeElement(xml, CBC_NS, "InvoiceTypeCode", tipoComprobante);
            } else if ("07".equals(tipoComprobante)) {
                writeElement(xml, CBC_NS, "CreditNoteTypeCode", "07");
            }

            writeElement(xml, CBC_NS, "DocumentCurrencyCode", moneda);

            writeSignature(xml);
            writeAccountingSupplierParty(xml);
            writeAccountingCustomerParty(xml, clienteTipoDoc, clienteNumDoc,
                clienteNombre, clienteDireccion);
            writeTaxTotal(xml, subtotal, igv, moneda);
            writeLegalMonetaryTotal(xml, subtotal, total, moneda);
            writeInvoiceLines(xml, items, moneda);

            xml.writeEndElement(); // Invoice/CreditNote
            xml.writeEndDocument();
            xml.flush();
            xml.close();

            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating SUNAT XML", e);
        }
    }

    /**
     * Generate UBL 2.1 XML for a Nota de Crédito (07).
     * Includes reference to the original comprobante.
     */
    public String generateCreditNote(String serie, String correlativo,
                                     LocalDateTime fechaEmision, String moneda,
                                     String clienteTipoDoc, String clienteNumDoc,
                                     String clienteNombre, String clienteDireccion,
                                     BigDecimal subtotal, BigDecimal igv, BigDecimal total,
                                     List<LineItem> items,
                                     String originalSerieCorrelativo,
                                     String originalTipoComprobante,
                                     String motivo) {
        try {
            StringWriter sw = new StringWriter();
            XMLStreamWriter xml = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
            xml.writeStartDocument("ISO-8859-1", "1.0");

            xml.writeStartElement(UBL_CREDIT_NOTE_NS, "CreditNote");
            xml.writeNamespace("", UBL_CREDIT_NOTE_NS);
            xml.writeNamespace("cac", CAC_NS);
            xml.writeNamespace("cbc", CBC_NS);
            xml.writeNamespace("ds", DS_NS);
            xml.writeNamespace("ext", EXT_NS);

            writeUBLExtensions(xml);
            writeUBLVersionId(xml, "2.1");
            writeCustomizationId(xml, "2.0");

            writeElement(xml, CBC_NS, "ID", serie + "-" + correlativo);
            writeElement(xml, CBC_NS, "IssueDate", fechaEmision.format(DATE_FMT));
            writeElement(xml, CBC_NS, "IssueTime", fechaEmision.format(TIME_FMT));
            writeElement(xml, CBC_NS, "CreditNoteTypeCode", "07");
            writeElement(xml, CBC_NS, "DocumentCurrencyCode", moneda);

            // DiscrepancyResponse — references original document
            xml.writeStartElement(CAC_NS, "DiscrepancyResponse");
            writeElement(xml, CBC_NS, "ReferenceID", originalSerieCorrelativo);
            writeElement(xml, CBC_NS, "ResponseCode", "01"); // Anulación
            writeElement(xml, CBC_NS, "Description", motivo != null ? motivo : "");
            xml.writeEndElement();

            // BillingReference — original comprobante
            xml.writeStartElement(CAC_NS, "BillingReference");
            xml.writeStartElement(CAC_NS, "InvoiceDocumentReference");
            writeElement(xml, CBC_NS, "ID", originalSerieCorrelativo);
            writeElement(xml, CBC_NS, "DocumentTypeCode", originalTipoComprobante);
            xml.writeEndElement();
            xml.writeEndElement();

            writeSignature(xml);
            writeAccountingSupplierParty(xml);
            writeAccountingCustomerParty(xml, clienteTipoDoc, clienteNumDoc,
                clienteNombre, clienteDireccion);
            writeTaxTotal(xml, subtotal, igv, moneda);
            writeLegalMonetaryTotal(xml, subtotal, total, moneda);
            writeInvoiceLines(xml, items, moneda);

            xml.writeEndElement(); // CreditNote
            xml.writeEndDocument();
            xml.flush();
            xml.close();

            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating SUNAT CreditNote XML", e);
        }
    }

    // --- Private helpers ---

    private void writeUBLExtensions(XMLStreamWriter xml) throws Exception {
        xml.writeStartElement(EXT_NS, "UBLExtensions");
        xml.writeStartElement(EXT_NS, "UBLExtension");
        xml.writeStartElement(EXT_NS, "ExtensionContent");

        // Placeholder digital signature
        xml.writeStartElement(DS_NS, "Signature");
        xml.writeAttribute("Id", "signature");
        xml.writeStartElement(DS_NS, "SignedInfo");
        writeElement(xml, DS_NS, "CanonicalizationMethod", "");
        xml.writeAttribute("Algorithm", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
        writeElement(xml, DS_NS, "SignatureMethod", "");
        xml.writeAttribute("Algorithm", "http://www.w3.org/2000/09/xmldsig#rsa-sha1");
        xml.writeEndElement(); // SignedInfo

        // Hash value (placeholder — actual signature deferred per CPR-005)
        String hashValue = computeHash(config.emisorRuc());
        writeElement(xml, DS_NS, "SignatureValue", hashValue);

        xml.writeEndElement(); // Signature
        xml.writeEndElement(); // ExtensionContent
        xml.writeEndElement(); // UBLExtension
        xml.writeEndElement(); // UBLExtensions
    }

    private void writeUBLVersionId(XMLStreamWriter xml, String version) throws Exception {
        writeElement(xml, CBC_NS, "UBLVersionID", version);
    }

    private void writeCustomizationId(XMLStreamWriter xml, String id) throws Exception {
        writeElement(xml, CBC_NS, "CustomizationID", id);
    }

    private void writeSignature(XMLStreamWriter xml) throws Exception {
        xml.writeStartElement(CAC_NS, "Signature");
        writeElement(xml, CBC_NS, "ID", "IDSign");

        // SignatoryParty
        xml.writeStartElement(CAC_NS, "SignatoryParty");
        xml.writeStartElement(CAC_NS, "PartyIdentification");
        writeElement(xml, CBC_NS, "ID", config.emisorRuc());
        xml.writeEndElement();
        xml.writeStartElement(CAC_NS, "PartyName");
        writeElement(xml, CBC_NS, "Name", config.emisorRazonSocial());
        xml.writeEndElement();
        xml.writeEndElement();

        // DigitalSignatureAttachment
        xml.writeStartElement(CAC_NS, "DigitalSignatureAttachment");
        xml.writeStartElement(CAC_NS, "ExternalReference");
        writeElement(xml, CBC_NS, "URI", "#signature");
        xml.writeEndElement();
        xml.writeEndElement();

        xml.writeEndElement();
    }

    private void writeAccountingSupplierParty(XMLStreamWriter xml) throws Exception {
        xml.writeStartElement(CAC_NS, "AccountingSupplierParty");
        xml.writeStartElement(CAC_NS, "Party");

        xml.writeStartElement(CAC_NS, "PartyIdentification");
        String partyId = config.emisorRuc();
        if (partyId.length() == 11) {
            writeElementWithScheme(xml, CBC_NS, "ID", "6", partyId);
        } else {
            writeElement(xml, CBC_NS, "ID", partyId);
        }
        xml.writeEndElement();

        xml.writeStartElement(CAC_NS, "PartyName");
        writeElement(xml, CBC_NS, "Name", config.emisorRazonSocial());
        xml.writeEndElement();

        xml.writeStartElement(CAC_NS, "PartyLegalEntity");
        writeElement(xml, CBC_NS, "RegistrationName", config.emisorRazonSocial());

        xml.writeStartElement(CAC_NS, "RegistrationAddress");
        writeElement(xml, CBC_NS, "AddressTypeCode", config.emisorTipoVia());
        writeElement(xml, CBC_NS, "CitySubdivisionName", "-");
        writeElement(xml, CBC_NS, "CityName", config.emisorDepartamento());
        writeElement(xml, CBC_NS, "CountrySubentity", config.emisorProvincia());
        writeElement(xml, CBC_NS, "District", config.emisorDistrito());

        xml.writeStartElement(CAC_NS, "AddressLine");
        writeElement(xml, CBC_NS, "Line", config.emisorDireccion());
        xml.writeEndElement();

        xml.writeStartElement(CAC_NS, "Country");
        writeElement(xml, CBC_NS, "IdentificationCode", "PE");
        xml.writeEndElement();

        xml.writeEndElement(); // RegistrationAddress
        xml.writeEndElement(); // PartyLegalEntity

        xml.writeEndElement(); // Party
        xml.writeEndElement(); // AccountingSupplierParty
    }

    private void writeAccountingCustomerParty(XMLStreamWriter xml,
                                              String clienteTipoDoc, String clienteNumDoc,
                                              String clienteNombre, String clienteDireccion)
            throws Exception {
        xml.writeStartElement(CAC_NS, "AccountingCustomerParty");
        xml.writeStartElement(CAC_NS, "Party");

        xml.writeStartElement(CAC_NS, "PartyIdentification");
        writeElementWithScheme(xml, CBC_NS, "ID", clienteTipoDoc, clienteNumDoc);
        xml.writeEndElement();

        xml.writeStartElement(CAC_NS, "PartyLegalEntity");
        writeElement(xml, CBC_NS, "RegistrationName", clienteNombre);

        if (clienteDireccion != null && !clienteDireccion.isBlank()) {
            xml.writeStartElement(CAC_NS, "RegistrationAddress");
            xml.writeStartElement(CAC_NS, "AddressLine");
            writeElement(xml, CBC_NS, "Line", clienteDireccion);
            xml.writeEndElement();
            xml.writeStartElement(CAC_NS, "Country");
            writeElement(xml, CBC_NS, "IdentificationCode", "PE");
            xml.writeEndElement();
            xml.writeEndElement();
        }

        xml.writeEndElement(); // PartyLegalEntity
        xml.writeEndElement(); // Party
        xml.writeEndElement(); // AccountingCustomerParty
    }

    private void writeTaxTotal(XMLStreamWriter xml, BigDecimal subtotal,
                               BigDecimal igv, String moneda) throws Exception {
        xml.writeStartElement(CAC_NS, "TaxTotal");
        writeElementWithCurrency(xml, CBC_NS, "TaxAmount", igv, moneda);

        xml.writeStartElement(CAC_NS, "TaxSubtotal");
        writeElementWithCurrency(xml, CBC_NS, "TaxableAmount", subtotal, moneda);
        writeElementWithCurrency(xml, CBC_NS, "TaxAmount", igv, moneda);

        xml.writeStartElement(CAC_NS, "TaxCategory");
        xml.writeStartElement(CAC_NS, "TaxScheme");
        writeElement(xml, CBC_NS, "ID", "1000");
        writeElement(xml, CBC_NS, "Name", "IGV");
        writeElement(xml, CBC_NS, "TaxTypeCode", "VAT");
        xml.writeEndElement();
        xml.writeEndElement();

        xml.writeEndElement(); // TaxSubtotal
        xml.writeEndElement(); // TaxTotal
    }

    private void writeLegalMonetaryTotal(XMLStreamWriter xml, BigDecimal subtotal,
                                         BigDecimal total, String moneda) throws Exception {
        xml.writeStartElement(CAC_NS, "LegalMonetaryTotal");
        writeElementWithCurrency(xml, CBC_NS, "LineExtensionAmount", subtotal, moneda);
        writeElementWithCurrency(xml, CBC_NS, "TaxInclusiveAmount", total, moneda);
        writeElementWithCurrency(xml, CBC_NS, "PayableAmount", total, moneda);
        xml.writeEndElement();
    }

    private void writeInvoiceLines(XMLStreamWriter xml, List<LineItem> items,
                                   String moneda) throws Exception {
        int lineNum = 1;
        for (LineItem item : items) {
            xml.writeStartElement(CAC_NS, "InvoiceLine");
            writeElement(xml, CBC_NS, "ID", String.valueOf(lineNum));
            writeElementWithUnit(xml, CBC_NS, "InvoicedQuantity",
                item.cantidad(), "NIU");
            writeElementWithCurrency(xml, CBC_NS, "LineExtensionAmount",
                item.subtotal(), moneda);

            // PricingReference with alternative condition price (IGV included)
            xml.writeStartElement(CAC_NS, "PricingReference");
            xml.writeStartElement(CAC_NS, "AlternativeConditionPrice");
            writeElementWithCurrency(xml, CBC_NS, "PriceAmount",
                item.precioUnitario().multiply(item.cantidad()), moneda);
            writeElement(xml, CBC_NS, "PriceTypeCode", "01");
            xml.writeEndElement();
            xml.writeEndElement();

            // Item description
            xml.writeStartElement(CAC_NS, "Item");
            writeElement(xml, CBC_NS, "Description", item.descripcion());
            xml.writeStartElement(CAC_NS, "SellersItemIdentification");
            writeElement(xml, CBC_NS, "ID", item.codigo() != null ? item.codigo() : String.valueOf(lineNum));
            xml.writeEndElement();
            xml.writeEndElement();

            // Price
            xml.writeStartElement(CAC_NS, "Price");
            writeElementWithCurrency(xml, CBC_NS, "PriceAmount", item.precioUnitario(), moneda);
            xml.writeEndElement();

            xml.writeEndElement(); // InvoiceLine
            lineNum++;
        }
    }

    // --- XML writing helpers ---

    private void writeElement(XMLStreamWriter xml, String ns, String local, String value) throws Exception {
        xml.writeStartElement(ns, local);
        xml.writeCharacters(value != null ? value : "");
        xml.writeEndElement();
    }

    private void writeElementWithScheme(XMLStreamWriter xml, String ns, String local,
                                        String schemeId, String value) throws Exception {
        xml.writeStartElement(ns, local);
        xml.writeAttribute("schemeID", schemeId);
        xml.writeCharacters(value != null ? value : "");
        xml.writeEndElement();
    }

    private void writeElementWithCurrency(XMLStreamWriter xml, String ns, String local,
                                          BigDecimal value, String currency) throws Exception {
        xml.writeStartElement(ns, local);
        xml.writeAttribute("currencyID", currency);
        xml.writeCharacters(value != null ? value.setScale(2, RoundingMode.HALF_UP).toString() : "0.00");
        xml.writeEndElement();
    }

    private void writeElementWithUnit(XMLStreamWriter xml, String ns, String local,
                                      BigDecimal value, String unitCode) throws Exception {
        xml.writeStartElement(ns, local);
        xml.writeAttribute("unitCode", unitCode);
        xml.writeCharacters(value != null ? value.setScale(2, RoundingMode.HALF_UP).toString() : "0.00");
        xml.writeEndElement();
    }

    /**
     * Compute a placeholder hash value for the digital signature.
     * Actual SUNAT OSE submission will replace this with a proper signature.
     */
    private String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return "placeholder-hash-value";
        }
    }

    /**
     * Line item for the invoice XML.
     */
    public record LineItem(
        String codigo,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
    ) {}
}

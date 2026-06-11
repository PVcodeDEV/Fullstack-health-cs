package com.clinica.rrhh.pension.integration;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.maestro.repository.rrhh.AfpRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.pension.dto.InformacionPensionariaRequest;
import com.clinica.rrhh.pension.repository.InformacionPensionariaRepository;
import com.clinica.rrhh.pension.service.InformacionPensionariaService;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.jwt.secret=test-secret-key-for-integration-test-min-32-chars-long!!",
    "app.jwt.expiration-ms=3600000"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class RrhhPensionFlowIntegrationTest {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private AfpRepository afpRepository;

    @Autowired
    private InformacionPensionariaService informacionPensionariaService;

    @Autowired
    private InformacionPensionariaRepository informacionPensionariaRepository;

    private Trabajador trabajador;
    private Afp afpPrima;
    private Afp onp;

    @BeforeEach
    void setUp() {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocumentoIdentidadRepository.saveAndFlush(tdi);

        var persona = new Persona();
        persona.setTipoDocumentoIdentidad(tdi);
        persona.setNumeroDocumento("77777777");
        persona.setNombres("PENSION");
        persona.setApellidoPaterno("FLUJO");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-PEN-FLOW");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);

        afpPrima = new Afp();
        afpPrima.setCodigo("PRIMA");
        afpPrima.setNombre("Prima AFP");
        afpPrima = afpRepository.saveAndFlush(afpPrima);

        onp = new Afp();
        onp.setCodigo("ONP");
        onp.setNombre("ONP");
        onp = afpRepository.saveAndFlush(onp);
    }

    @Test
    void fullPensionFlow() {
        // 1. Create pension info for AFP Prima
        var request = new InformacionPensionariaRequest(
            afpPrima.getId(),
            "123456789012",
            "FLUJO",
            false,
            LocalDate.of(2025, 1, 1),
            null
        );
        var response = informacionPensionariaService.upsert(trabajador.getId(), request);
        assertThat(response).isNotNull();
        assertThat(response.trabajadorId()).isEqualTo(trabajador.getId());
        assertThat(response.afpId()).isEqualTo(afpPrima.getId());
        assertThat(response.cuspp()).isEqualTo("123456789012");
        assertThat(response.comisionTipo()).isEqualTo("FLUJO");
        assertThat(response.estado()).isEqualTo("ACTIVO");

        // 2. Verify GET returns same data
        var getResponse = informacionPensionariaService.getByTrabajadorId(trabajador.getId());
        assertThat(getResponse.cuspp()).isEqualTo("123456789012");
        assertThat(getResponse.afpCodigo()).isEqualTo("PRIMA");

        // 3. Second upsert updates in-place (switch to ONP)
        var onpRequest = new InformacionPensionariaRequest(
            onp.getId(),
            null,      // cuspp null for ONP
            null,      // comisionTipo null for ONP
            null,      // sctr null for ONP
            LocalDate.of(2025, 6, 1),
            "EXP-001"
        );
        var updated = informacionPensionariaService.upsert(trabajador.getId(), onpRequest);
        assertThat(updated.afpId()).isEqualTo(onp.getId());
        assertThat(updated.afpCodigo()).isEqualTo("ONP");
        assertThat(updated.comisionTipo()).isNull();
        assertThat(updated.cuspp()).isEqualTo("77777777"); // auto-populated from DNI
        assertThat(updated.documentoReferencia()).isEqualTo("EXP-001");

        // 4. Verify only one record per trabajador
        var all = informacionPensionariaRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getId()).isEqualTo(response.id());

        // 5. Verify toString excludes cuspp
        assertThat(updated.toString()).doesNotContain("77777777");
    }
}

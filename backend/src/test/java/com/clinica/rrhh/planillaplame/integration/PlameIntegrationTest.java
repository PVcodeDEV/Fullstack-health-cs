package com.clinica.rrhh.planillaplame.integration;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.maestro.entity.rrhh.TipoContrato;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.maestro.repository.rrhh.AfpRepository;
import com.clinica.maestro.repository.rrhh.TipoContratoRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.contrato.repository.ContratoRepository;
import com.clinica.rrhh.planilla.dto.PeriodoPlanillaRequest;
import com.clinica.rrhh.planilla.service.PeriodoPlanillaService;
import com.clinica.rrhh.planilla.service.PlanillaLiquidacionService;
import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import com.clinica.rrhh.pension.repository.InformacionPensionariaRepository;
import com.clinica.rrhh.planillaplame.repository.ArchivoPlanillaRepository;
import com.clinica.rrhh.planillaplame.repository.TRegistroEventoRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.EstadoContrato;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test for T-Registro and PLAME generation flows.
 * Tests the full stack: controller → service → repository → database.
 *
 * <p>Uses {@code .with(jwt())} for OAuth2 resource server auth matching
 * the app's JWT-based security. The {@code permisos} claim maps to
 * Spring Security authorities via {@code JwtAuthConverter}.</p>
 */
@SpringBootTest(properties = {
    "app.jwt.secret=test-secret-key-for-integration-test-min-32-chars-long!!",
    "app.jwt.expiration-ms=3600000"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@Transactional
class PlameIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PeriodoPlanillaService periodoPlanillaService;

    @Autowired
    private PlanillaLiquidacionService planillaLiquidacionService;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private AfpRepository afpRepository;

    @Autowired
    private TipoContratoRepository tipoContratoRepository;

    @Autowired
    private InformacionPensionariaRepository pensionRepository;

    @Autowired
    private TRegistroEventoRepository tRegistroEventoRepository;

    @Autowired
    private ArchivoPlanillaRepository archivoPlanillaRepository;

    private Long periodoPlanillaId;
    private String numeroDocumento;

    /**
     * Creates an authenticated request with JWT containing both
     * {@code rrhh:ver} and {@code rrhh:editar} permissions.
     */
    private static RequestPostProcessor jwtAuth() {
        return jwt().authorities(
                new SimpleGrantedAuthority("rrhh:ver"),
                new SimpleGrantedAuthority("rrhh:editar")
        );
    }

    @BeforeEach
    void setUp() {
        // 1. TipoDocumentoIdentidad (DNI, codigoSunat "01" → SUNAT "1")
        var tdi = new TipoDocumentoIdentidad();
        tdi.setCodigoSunat("01");
        tdi.setNombre("DNI");
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        tdi = tipoDocumentoIdentidadRepository.saveAndFlush(tdi);

        // 2. Persona
        numeroDocumento = "77777777";
        var persona = new Persona();
        persona.setTipoDocumentoIdentidad(tdi);
        persona.setNumeroDocumento(numeroDocumento);
        persona.setNombres("PLAME");
        persona.setApellidoPaterno("INTEGRACION");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        // 3. Trabajador
        var trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-PLA-INT");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setCantidadHijos(0);
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);

        // 4. TipoContrato
        var tipoContrato = new TipoContrato();
        tipoContrato.setCodigo("INDETERMINADO");
        tipoContrato.setNombre("Indeterminado");
        tipoContrato = tipoContratoRepository.saveAndFlush(tipoContrato);

        // 5. Contrato (fechaInicio at period start → detected as ALTA by T-Registro)
        //    remuneracion 2500 → ONP 13% = 325, EsSalud 9% = 225
        var contrato = new Contrato();
        contrato.setTrabajador(trabajador);
        contrato.setTipoContrato(tipoContrato);
        contrato.setRemuneracion(new BigDecimal("2500"));
        contrato.setFechaInicio(LocalDate.of(2026, 1, 1));
        contrato.setEstado(EstadoContrato.ACTIVO);
        contratoRepository.saveAndFlush(contrato);

        // 6. AFP/ONP
        var afpOnp = new Afp();
        afpOnp.setCodigo("ONP");
        afpOnp.setNombre("ONP");
        afpOnp = afpRepository.saveAndFlush(afpOnp);

        // 7. InformacionPensionaria (ONP)
        var info = new InformacionPensionaria();
        info.setTrabajador(trabajador);
        info.setAfp(afpOnp);
        info.setCuspp(persona.getNumeroDocumento());
        info.setFechaAfiliacion(LocalDate.of(2025, 1, 1));
        info.setEstado("ACTIVO");
        pensionRepository.saveAndFlush(info);

        // 8. Create PeriodoPlanilla via service (ABIERTO initially)
        var request = new PeriodoPlanillaRequest(
            2026, 1,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31));
        var periodo = periodoPlanillaService.create(request);
        periodoPlanillaId = periodo.id();

        // 9. Generate planilla (LIQUIDADO state)
        planillaLiquidacionService.generar(periodoPlanillaId);

        // 10. Close period (CERRADO)
        periodoPlanillaService.cerrar(periodoPlanillaId);
    }

    // =========================================================================
    // T-REGISTRO FLOW
    // =========================================================================

    @Test
    void tRegistro_Generar_ShouldCreateEventAndFile() throws Exception {
        // POST /api/v1/t-registro/generar → 201
        var tRegResult = mockMvc.perform(post("/api/v1/t-registro/generar")
                .with(jwtAuth())
                .param("periodoPlanillaId", periodoPlanillaId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("T_REGISTRO"))
                .andExpect(jsonPath("$.hash").isNotEmpty())
                .andReturn();

        // Verify TRegistroEvento records created: 1 ALTA from contrato
        var eventos = tRegistroEventoRepository.findByPeriodoPlanillaIdOrderByFechaEventoAsc(periodoPlanillaId);
        assertThat(eventos).as("Should create 1 T-Registro ALTA event").hasSize(1);
        var evento = eventos.get(0);
        assertThat(evento.getTipoEvento()).isEqualTo("ALTA");
        assertThat(evento.getFechaEvento()).isEqualTo(LocalDate.of(2026, 1, 1));

        // GET /api/v1/t-registro/archivos/{id}/descargar → 200 + TXT file
        var archivos = archivoPlanillaRepository.findByPeriodoPlanillaId(periodoPlanillaId);
        var tregArchivo = archivos.stream()
                .filter(a -> "T_REGISTRO".equals(a.getTipo()))
                .findFirst().orElseThrow();

        var downloadResult = mockMvc.perform(get("/api/v1/t-registro/archivos/{id}/descargar", tregArchivo.getId())
                .with(jwtAuth()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andReturn();

        // Verify TXT content format: TipoDoc|NroDoc|Evento|FechaEvento
        String txtContent = downloadResult.getResponse().getContentAsString();
        assertThat(txtContent)
                .as("T-Registro TXT should contain ALTA event with SUNAT format")
                .contains("1|" + numeroDocumento + "|ALTA|20260101");
    }

    @Test
    void tRegistro_AbiertoPeriod_ShouldReturn409() throws Exception {
        var periodoAbierto = periodoPlanillaService.create(
                new PeriodoPlanillaRequest(2026, 2,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 28)));

        mockMvc.perform(post("/api/v1/t-registro/generar")
                        .with(jwtAuth())
                        .param("periodoPlanillaId", periodoAbierto.id().toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void tRegistro_PeriodoNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(post("/api/v1/t-registro/generar")
                        .with(jwtAuth())
                        .param("periodoPlanillaId", "9999"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // PLAME FLOW
    // =========================================================================

    @Test
    void plame_Generar_ShouldCreate5Files() throws Exception {
        // POST /api/v1/plame/generar → 201
        var plameResult = mockMvc.perform(post("/api/v1/plame/generar")
                .with(jwtAuth())
                .param("periodoPlanillaId", periodoPlanillaId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].tipo").isString())
                .andReturn();

        // Verify 5 ArchivoPlanilla records created (REM, JOR, SNL, OR5, TOC)
        var archivos = archivoPlanillaRepository.findByPeriodoPlanillaId(periodoPlanillaId);
        var plameArchivos = archivos.stream()
                .filter(a -> !"T_REGISTRO".equals(a.getTipo()))
                .toList();
        assertThat(plameArchivos).as("Should create 5 PLAME file records").hasSize(5);

        var tipos = plameArchivos.stream().map(a -> a.getTipo()).toList();
        assertThat(tipos).containsExactlyInAnyOrder("REM", "JOR", "SNL", "OR5", "TOC");

        // Verify .rem content (ONP: sueldo 2500 → ONP 13%=325, EsSalud 9%=225)
        // Lines: 0121 sueldo, 0607 ONP, 0804 EsSalud
        var remArchivo = plameArchivos.stream().filter(a -> "REM".equals(a.getTipo())).findFirst().orElseThrow();
        var remLines = remArchivo.getContenido().lines().toList();
        assertThat(remLines).hasSize(3);
        assertThat(remLines.get(0)).contains("0121").contains("2500.00");
        assertThat(remLines.get(1)).contains("0607").contains("325.00");
        assertThat(remLines.get(2)).contains("0804").contains("225.00");

        // Verify .jor content (240 horas ordinarias)
        var jorArchivo = plameArchivos.stream().filter(a -> "JOR".equals(a.getTipo())).findFirst().orElseThrow();
        assertThat(jorArchivo.getContenido()).contains("1|" + numeroDocumento + "|240|0|0|0");

        // Verify .toc content (ONP → indicador 2)
        var tocArchivo = plameArchivos.stream().filter(a -> "TOC".equals(a.getTipo())).findFirst().orElseThrow();
        assertThat(tocArchivo.getContenido()).contains("1|" + numeroDocumento + "|2|0|0|1");

        // Verify .snl and .or5 exist (empty for v1, but files created)
        assertThat(plameArchivos).anyMatch(a -> "SNL".equals(a.getTipo()));
        assertThat(plameArchivos).anyMatch(a -> "OR5".equals(a.getTipo()));

        // Verify each file has a valid SHA-256 hash
        plameArchivos.forEach(a -> {
            assertThat(a.getHash()).as("File %s should have a hash", a.getTipo()).hasSize(64);
        });
    }

    @Test
    void plame_DescargarIndividual_ShouldReturnTxtFile() throws Exception {
        // Generate PLAME first
        mockMvc.perform(post("/api/v1/plame/generar")
                .with(jwtAuth())
                .param("periodoPlanillaId", periodoPlanillaId.toString()))
                .andExpect(status().isCreated());

        var archivos = archivoPlanillaRepository.findByPeriodoPlanillaId(periodoPlanillaId);
        var remArchivo = archivos.stream()
                .filter(a -> "REM".equals(a.getTipo()))
                .findFirst().orElseThrow();

        mockMvc.perform(get("/api/v1/plame/archivos/{id}/descargar", remArchivo.getId())
                        .with(jwtAuth()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"20123456789.rem\""))
                .andExpect(content().string(remArchivo.getContenido()));
    }

    @Test
    void plame_DescargarPorTipo_ShouldReturnCorrectFile() throws Exception {
        mockMvc.perform(post("/api/v1/plame/generar")
                .with(jwtAuth())
                .param("periodoPlanillaId", periodoPlanillaId.toString()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/plame/descargar")
                        .with(jwtAuth())
                        .param("periodoPlanillaId", periodoPlanillaId.toString())
                        .param("tipo", "TOC"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"20123456789.toc\""))
                .andExpect(content().string(containsString("2|0|0|1")));
    }

    @Test
    void plame_DescargarZip_ShouldContain5Files() throws Exception {
        // Generate PLAME
        mockMvc.perform(post("/api/v1/plame/generar")
                .with(jwtAuth())
                .param("periodoPlanillaId", periodoPlanillaId.toString()))
                .andExpect(status().isCreated());

        // GET descargar-zip → 200 + ZIP content
        var zipResult = mockMvc.perform(get("/api/v1/plame/descargar-zip")
                        .with(jwtAuth())
                        .param("periodoPlanillaId", periodoPlanillaId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"20123456789-202601.zip\""))
                .andReturn();

        byte[] zipBytes = zipResult.getResponse().getContentAsByteArray();
        assertThat(zipBytes).isNotEmpty();

        List<String> zipEntries = new ArrayList<>();
        try (var zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                zipEntries.add(entry.getName());
                zis.closeEntry();
            }
        }

        assertThat(zipEntries)
                .as("ZIP should contain exactly 5 PLAME files")
                .hasSize(5)
                .allMatch(name -> name.startsWith("20123456789."));
        assertThat(zipEntries).anyMatch(name -> name.endsWith(".rem"));
        assertThat(zipEntries).anyMatch(name -> name.endsWith(".jor"));
        assertThat(zipEntries).anyMatch(name -> name.endsWith(".snl"));
        assertThat(zipEntries).anyMatch(name -> name.endsWith(".or5"));
        assertThat(zipEntries).anyMatch(name -> name.endsWith(".toc"));
    }

    @Test
    void plame_AbiertoPeriod_ShouldReturn409() throws Exception {
        var periodoAbierto = periodoPlanillaService.create(
                new PeriodoPlanillaRequest(2026, 4,
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 4, 30)));

        mockMvc.perform(post("/api/v1/plame/generar")
                        .with(jwtAuth())
                        .param("periodoPlanillaId", periodoAbierto.id().toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void plame_PeriodoNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(post("/api/v1/plame/generar")
                        .with(jwtAuth())
                        .param("periodoPlanillaId", "9999"))
                .andExpect(status().isNotFound());
    }
}

package com.clinica.clinica.integration;

import com.clinica.clinica.admision.dto.AsignarCamaRequest;
import com.clinica.clinica.admision.dto.CuentaRequest;
import com.clinica.clinica.admision.dto.CuentaResponse;
import com.clinica.clinica.admision.repository.SolicitudHospitalizacionRepository;
import com.clinica.clinica.admision.service.AdmisionService;
import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.entity.EstadoCama;
import com.clinica.clinica.cama.entity.Habitacion;
import com.clinica.clinica.cama.repository.CamaRepository;
import com.clinica.clinica.cama.repository.HabitacionRepository;
import com.clinica.clinica.cuenta.entity.CargoAdicional;
import com.clinica.clinica.cuenta.repository.CargoAdicionalRepository;
import com.clinica.clinica.cuenta.service.CuentaService;
import com.clinica.clinica.hospitalizacion.dto.AltaMedicaRequest;
import com.clinica.clinica.hospitalizacion.dto.NotaEvolucionRequest;
import com.clinica.clinica.hospitalizacion.entity.Hospitalizacion;
import com.clinica.clinica.hospitalizacion.repository.HospitalizacionRepository;
import com.clinica.clinica.hospitalizacion.service.HospitalizacionService;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.jwt.secret=test-secret-key-for-integration-test-min-32-chars-long!!",
    "app.jwt.expiration-ms=3600000"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class AdmisionFlowIntegrationTest {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private AdmisionService admisionService;

    @Autowired
    private HospitalizacionService hospitalizacionService;

    @Autowired
    private CuentaService cuentaService;

    @Autowired
    private CargoAdicionalRepository cargoRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private CamaRepository camaRepository;

    @Autowired
    private HospitalizacionRepository hospitalizacionRepository;

    @Autowired
    private SolicitudHospitalizacionRepository solicitudRepository;

    private Persona paciente;
    private Habitacion habitacion;
    private Cama cama;

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

        habitacion = new Habitacion();
        habitacion.setCodigo("HAB-INT-01");
        habitacion.setNombre("Habitación Integración");
        habitacion.setTipoHabitacionId(1L);
        habitacion.setCapacidad(2);
        habitacion = habitacionRepository.saveAndFlush(habitacion);

        cama = new Cama();
        cama.setHabitacionId(habitacion.getId());
        cama.setCodigo("CAMA-INT-01");
        cama.setEstado(EstadoCama.DISPONIBLE);
        cama = camaRepository.saveAndFlush(cama);
    }

    @Test
    void fullAdmissionToDischargeFlow() {
        // 1. Create Cuenta via AdmisionService (with paqueteId to auto-generate solicitud)
        var cuentaRequest = new CuentaRequest(paciente.getId(), null, 1L, 1L, null);
        CuentaResponse cuentaResponse = admisionService.crearCuenta(cuentaRequest);
        assertThat(cuentaResponse).isNotNull();
        assertThat(cuentaResponse.estado()).isEqualTo("ABIERTA");
        Long cuentaId = cuentaResponse.id();
        assertThat(cuentaId).isNotNull();

        // Find the auto-generated solicitud
        var solicitudes = solicitudRepository.findByCuentaId(cuentaId);
        assertThat(solicitudes).hasSize(1);
        Long solicitudId = solicitudes.get(0).getId();

        // 2. Asignar cama via AdmisionService (with solicitud so cuentaId links correctly)
        var asignarRequest = new AsignarCamaRequest(cama.getId(), solicitudId);
        Hospitalizacion hospitalizacion = admisionService.asignarCama(asignarRequest);
        assertThat(hospitalizacion).isNotNull();
        assertThat(hospitalizacion.getEstado()).isEqualTo("HOSPITALIZADO");
        assertThat(hospitalizacion.getCamaId()).isEqualTo(cama.getId());
        Long hospitalizacionId = hospitalizacion.getId();

        // 3. Verify cama state = OCUPADO
        Cama camaActualizada = camaRepository.findById(cama.getId()).orElseThrow();
        assertThat(camaActualizada.getEstado()).isEqualTo(EstadoCama.OCUPADO);

        // 4. Registrar NotaEvolucion via HospitalizacionService
        var notaRequest = new NotaEvolucionRequest(
                "Paciente evoluciona favorablemente",
                "Continuar con tratamiento antibiótico",
                "EVOLUCION",
                "TA: 120/80, FC: 75"
        );
        var notaResponse = hospitalizacionService.registrarNota(hospitalizacionId, notaRequest, 1L);
        assertThat(notaResponse).isNotNull();
        assertThat(notaResponse.descripcion()).isEqualTo("Paciente evoluciona favorablemente");

        // 5. Dar alta via HospitalizacionService
        var altaRequest = new AltaMedicaRequest(
                "MEJORADO",
                "PACIENTE MEJORADO, APENDICITIS RESUELTA",
                "Control ambulatorio en 7 días",
                1L
        );
        var altaResponse = hospitalizacionService.darAlta(hospitalizacionId, altaRequest);
        assertThat(altaResponse).isNotNull();
        assertThat(altaResponse.tipoAlta()).isEqualTo("MEJORADO");

        // 6. Verify cama stays OCUPADO after alta (bed released via confirmarCobro)
        Cama camaPostAlta = camaRepository.findById(cama.getId()).orElseThrow();
        assertThat(camaPostAlta.getEstado()).isEqualTo(EstadoCama.OCUPADO);

        // 7. Verify hospitalizacion state = ALTA
        var hospPostAlta = hospitalizacionRepository.findById(hospitalizacionId).orElseThrow();
        assertThat(hospPostAlta.getEstado()).isEqualTo("ALTA");
        assertThat(hospPostAlta.getFechaAlta()).isNotNull();

        // 8. Create CargoAdicional via repository directly (service doesn't set usuarioId)
        var cargo = new CargoAdicional();
        cargo.setCuentaId(cuentaId);
        cargo.setTipo("CIRUGIA");
        cargo.setMonto(new BigDecimal("5000.00"));
        cargo.setDescripcion("Honorarios médicos - Cirugía");
        cargo.setFechaRegistro(LocalDateTime.now());
        cargo.setUsuarioId(1L);
        cargo = cargoRepository.saveAndFlush(cargo);
        assertThat(cargo.getId()).isNotNull();

        // 9. Verify cargos list via CuentaService
        var cargos = cuentaService.listarCargos(cuentaId);
        assertThat(cargos).hasSize(1);
        assertThat(cargos.get(0).descripcion()).isEqualTo("Honorarios médicos - Cirugía");
        assertThat(cargos.get(0).monto()).isEqualByComparingTo(new BigDecimal("5000.00"));

        // 10. Confirmar cobro — frees the bed and closes the cuenta
        cuentaService.confirmarCobro(cuentaId);

        Cama camaPostCobro = camaRepository.findById(cama.getId()).orElseThrow();
        assertThat(camaPostCobro.getEstado()).isEqualTo(EstadoCama.DISPONIBLE);

        var cuentaPostCobro = cuentaService.obtenerCuenta(cuentaId);
        assertThat(cuentaPostCobro.getEstado()).isEqualTo("CERRADA");
    }
}

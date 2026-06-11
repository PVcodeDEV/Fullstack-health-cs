package com.clinica.clinica.admision.service;

import com.clinica.clinica.admision.dto.AdmisionDiagnosticoRequest;
import com.clinica.clinica.admision.dto.AsignarCamaRequest;
import com.clinica.clinica.admision.dto.CuentaRequest;
import com.clinica.clinica.admision.entity.AdmisionDiagnostico;
import com.clinica.clinica.admision.entity.Cuenta;
import com.clinica.clinica.admision.entity.SolicitudHospitalizacion;
import com.clinica.clinica.admision.repository.AdmisionDiagnosticoRepository;
import com.clinica.clinica.admision.repository.CuentaPaqueteRepository;
import com.clinica.clinica.admision.repository.CuentaRepository;
import com.clinica.clinica.admision.repository.SolicitudHospitalizacionRepository;
import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.entity.EstadoCama;
import com.clinica.clinica.cama.repository.CamaRepository;
import com.clinica.clinica.hospitalizacion.entity.Hospitalizacion;
import com.clinica.clinica.hospitalizacion.repository.HospitalizacionRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmisionServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;
    @Mock
    private CuentaPaqueteRepository cuentaPaqueteRepository;
    @Mock
    private SolicitudHospitalizacionRepository solicitudRepository;
    @Mock
    private AdmisionDiagnosticoRepository diagnosticoRepository;
    @Mock
    private PersonaRepository personaRepository;
    @Mock
    private CamaRepository camaRepository;
    @Mock
    private HospitalizacionRepository hospitalizacionRepository;

    @InjectMocks
    private AdmisionService service;

    @Captor
    private ArgumentCaptor<Cuenta> cuentaCaptor;
    @Captor
    private ArgumentCaptor<SolicitudHospitalizacion> solicitudCaptor;
    @Captor
    private ArgumentCaptor<AdmisionDiagnostico> diagnosticoCaptor;
    @Captor
    private ArgumentCaptor<Cama> camaCaptor;
    @Captor
    private ArgumentCaptor<Hospitalizacion> hospitalizacionCaptor;

    private Persona createPersona(Long id) {
        var p = new Persona();
        p.setId(id);
        p.setNombres("JUAN");
        p.setApellidoPaterno("PEREZ");
        return p;
    }

    private Cama createCama(Long id, EstadoCama estado) {
        var c = new Cama();
        c.setId(id);
        c.setHabitacionId(1L);
        c.setCodigo("CAMA-" + id);
        c.setEstado(estado);
        return c;
    }

    @Test
    void buscarPaciente_ShouldSearchByDni() {
        var persona = createPersona(1L);
        when(personaRepository.findByNumeroDocumento("12345678")).thenReturn(Optional.of(persona));

        var result = service.buscarPaciente("12345678");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombres()).isEqualTo("JUAN");
    }

    @Test
    void buscarPaciente_WithBlankQuery_ShouldReturnEmpty() {
        var result = service.buscarPaciente("");
        assertThat(result).isEmpty();
    }

    @Test
    void crearCuenta_ShouldCreateAndReturnResponse() {
        var persona = createPersona(1L);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));

        var savedCuenta = new Cuenta();
        savedCuenta.setId(1L);
        savedCuenta.setPacienteId(1L);
        savedCuenta.setFechaApertura(LocalDateTime.now());
        savedCuenta.setEstado("ABIERTA");
        when(cuentaRepository.save(any())).thenReturn(savedCuenta);

        var request = new CuentaRequest(1L, null, 1L, null, null);
        var result = service.crearCuenta(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(cuentaRepository).save(cuentaCaptor.capture());
        assertThat(cuentaCaptor.getValue().getEstado()).isEqualTo("ABIERTA");
    }

    @Test
    void crearCuenta_WithPaquete_ShouldAutoGenerateSolicitud() {
        var persona = createPersona(1L);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));

        var savedCuenta = new Cuenta();
        savedCuenta.setId(1L);
        savedCuenta.setPacienteId(1L);
        savedCuenta.setFechaApertura(LocalDateTime.now());
        savedCuenta.setEstado("ABIERTA");
        when(cuentaRepository.save(any())).thenReturn(savedCuenta);

        when(solicitudRepository.save(any())).thenAnswer(i -> {
            var s = (SolicitudHospitalizacion) i.getArgument(0);
            s.setId(1L);
            return s;
        });

        var request = new CuentaRequest(1L, null, 1L, 5L, null);
        var result = service.crearCuenta(request);

        assertThat(result).isNotNull();
        verify(solicitudRepository).save(solicitudCaptor.capture());
        assertThat(solicitudCaptor.getValue().getCuentaId()).isEqualTo(1L);
        assertThat(solicitudCaptor.getValue().getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    void crearCuenta_ShouldThrowWhenPersonaNotFound() {
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new CuentaRequest(99L, null, 1L, null, null);
        assertThatThrownBy(() -> service.crearCuenta(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void asignarCama_ShouldCreateHospitalizacionAndOccupyBed() {
        var cama = createCama(1L, EstadoCama.DISPONIBLE);
        when(camaRepository.findById(1L)).thenReturn(Optional.of(cama));
        when(camaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var savedHosp = new Hospitalizacion();
        savedHosp.setId(1L);
        savedHosp.setCamaId(1L);
        savedHosp.setEstado("HOSPITALIZADO");
        when(hospitalizacionRepository.save(any())).thenReturn(savedHosp);

        var request = new AsignarCamaRequest(1L, null);
        var result = service.asignarCama(request);

        assertThat(result).isNotNull();
        assertThat(result.getEstado()).isEqualTo("HOSPITALIZADO");
        verify(camaRepository).save(camaCaptor.capture());
        assertThat(camaCaptor.getValue().getEstado()).isEqualTo(EstadoCama.OCUPADO);
    }

    @Test
    void asignarCama_WithSolicitud_ShouldUpdateSolicitudState() {
        var cama = createCama(1L, EstadoCama.DISPONIBLE);
        when(camaRepository.findById(1L)).thenReturn(Optional.of(cama));
        when(camaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var solicitud = new SolicitudHospitalizacion();
        solicitud.setId(10L);
        solicitud.setCuentaId(5L);
        solicitud.setEstado("PENDIENTE");
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        when(hospitalizacionRepository.save(any())).thenAnswer(i -> {
            var h = (Hospitalizacion) i.getArgument(0);
            h.setId(1L);
            return h;
        });

        var request = new AsignarCamaRequest(1L, 10L);
        var result = service.asignarCama(request);

        assertThat(result).isNotNull();
        assertThat(solicitud.getEstado()).isEqualTo("ASIGNADA");
    }

    @Test
    void asignarCama_WithCamaNoDisponible_ShouldThrow() {
        var cama = createCama(1L, EstadoCama.OCUPADO);
        when(camaRepository.findById(1L)).thenReturn(Optional.of(cama));

        var request = new AsignarCamaRequest(1L, null);
        assertThatThrownBy(() -> service.asignarCama(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    void asignarCama_WithInvalidCama_ShouldThrow() {
        when(camaRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new AsignarCamaRequest(99L, null);
        assertThatThrownBy(() -> service.asignarCama(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void registrarDiagnostico_ShouldSaveAndReturnResponse() {
        var cuenta = new Cuenta();
        cuenta.setId(1L);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(diagnosticoRepository.save(any())).thenAnswer(i -> {
            var d = (AdmisionDiagnostico) i.getArgument(0);
            d.setId(1L);
            return d;
        });

        var request = new AdmisionDiagnosticoRequest("AA00.0", "PRINCIPAL", "Diagnóstico de prueba");
        var result = service.registrarDiagnostico(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.codigoCie11()).isEqualTo("AA00.0");
        verify(diagnosticoRepository).save(diagnosticoCaptor.capture());
        assertThat(diagnosticoCaptor.getValue().getCodigoCIE11()).isEqualTo("AA00.0");
    }

    @Test
    void registrarDiagnostico_ShouldThrowWhenCuentaNotFound() {
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new AdmisionDiagnosticoRequest("AA00.0", "PRINCIPAL", "test");
        assertThatThrownBy(() -> service.registrarDiagnostico(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

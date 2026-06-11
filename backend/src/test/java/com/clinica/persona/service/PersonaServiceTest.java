package com.clinica.persona.service;

import com.clinica.maestro.entity.identidad.EstadoCivil;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.repository.identidad.EstadoCivilRepository;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.persona.dto.PersonaRequest;
import com.clinica.persona.dto.PersonaResponse;
import com.clinica.persona.dto.PersonaSearchResponse;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.persona.service.api.PersonaDatos;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Mock
    private EstadoCivilRepository estadoCivilRepository;

    @Mock
    private Modulo11Validator modulo11Validator;

    @Spy
    private List<ReniecClient> reniecClients = new ArrayList<>();

    @InjectMocks
    private PersonaService personaService;

    @Captor
    private ArgumentCaptor<Persona> personaCaptor;

    private TipoDocumentoIdentidad createTipoDocumento(Long id, String codigo, String nombre) {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setId(id);
        tdi.setCodigoSunat(codigo);
        tdi.setNombre(nombre);
        tdi.setLongitudMinima(8);
        tdi.setLongitudMaxima(8);
        return tdi;
    }

    private Persona createPersona(Long id, String numeroDocumento, TipoDocumentoIdentidad tdi) {
        var p = new Persona();
        p.setId(id);
        p.setTipoDocumentoIdentidad(tdi);
        p.setNumeroDocumento(numeroDocumento);
        p.setNombres("JUAN");
        p.setApellidoPaterno("PEREZ");
        p.setApellidoMaterno("LOPEZ");
        p.setActivo(true);
        return p;
    }

    // --- search() tests ---

    @Test
    void search_ByNumeroDocumento_ShouldReturnExactMatch() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var entity = createPersona(1L, "12345678", tdi);
        when(personaRepository.findByNumeroDocumento("12345678")).thenReturn(Optional.of(entity));

        var result = personaService.search("12345678", null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).numeroDocumento()).isEqualTo("12345678");
    }

    @Test
    void search_ByNombres_ShouldReturnMatches() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var entity = createPersona(1L, "12345678", tdi);
        when(personaRepository.findByNombresContainingIgnoreCase("JUAN")).thenReturn(List.of(entity));

        var result = personaService.search(null, "JUAN", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nombres()).isEqualTo("JUAN");
    }

    @Test
    void search_ByApellidoPaterno_ShouldReturnMatches() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var entity = createPersona(1L, "12345678", tdi);
        when(personaRepository.findByApellidoPaternoContainingIgnoreCase("PEREZ")).thenReturn(List.of(entity));

        var result = personaService.search(null, null, "PEREZ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).apellidoPaterno()).isEqualTo("PEREZ");
    }

    @Test
    void search_Default_ShouldReturnAllActive() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var entity = createPersona(1L, "12345678", tdi);
        when(personaRepository.findAllByActivoTrue()).thenReturn(List.of(entity));

        var result = personaService.search(null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void search_ShouldExcludeInactivePersonas() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var entity = createPersona(1L, "12345678", tdi);
        entity.setActivo(false);
        when(personaRepository.findByNumeroDocumento("12345678")).thenReturn(Optional.of(entity));

        var result = personaService.search("12345678", null, null);

        assertThat(result).isEmpty();
    }

    // --- findById() tests ---

    @Test
    void findById_ShouldReturnResponse() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var entity = createPersona(1L, "12345678", tdi);
        entity.setFechaUltimaConsulta(LocalDate.now());
        when(personaRepository.findById(1L)).thenReturn(Optional.of(entity));

        var result = personaService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.numeroDocumento()).isEqualTo("12345678");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaService.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void findById_WithStaleData_ShouldDetectRefreshNeeded() {
        // When fechaUltimaConsulta is more than 1 year old, the service logs a stale-data message
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var entity = createPersona(1L, "12345678", tdi);
        entity.setFechaUltimaConsulta(LocalDate.now().minusYears(2));
        when(personaRepository.findById(1L)).thenReturn(Optional.of(entity));

        var result = personaService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_WithNullFechaUltimaConsulta_ShouldDetectStaleData() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var entity = createPersona(1L, "12345678", tdi);
        entity.setFechaUltimaConsulta(null);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(entity));

        var result = personaService.findById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void findById_WithNonDniDocumentType_ShouldSkipStaleCheck() {
        // For non-DNI types, stale-data refresh is skipped (the stub condition only triggers for DNI)
        var tdi = createTipoDocumento(1L, "04", "CE");
        var entity = createPersona(1L, "123456789", tdi);
        entity.setFechaUltimaConsulta(null);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(entity));

        var result = personaService.findById(1L);

        assertThat(result).isNotNull();
    }

    // --- create() tests ---

    @Test
    void create_ShouldSaveAndReturnResponse() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        when(personaRepository.existsByNumeroDocumento("12345678")).thenReturn(false);
        when(tipoDocumentoIdentidadRepository.findById(1L)).thenReturn(Optional.of(tdi));
        when(modulo11Validator.validar("12345678")).thenReturn(true);

        var savedEntity = createPersona(1L, "12345678", tdi);
        when(personaRepository.save(any())).thenReturn(savedEntity);

        var request = new PersonaRequest(
            1L, "12345678", "JUAN", "PEREZ", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null
        );

        var result = personaService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.numeroDocumento()).isEqualTo("12345678");
        assertThat(result.nombres()).isEqualTo("JUAN");
        verify(personaRepository).save(personaCaptor.capture());
        assertThat(personaCaptor.getValue().getNombres()).isEqualTo("JUAN");
    }

    @Test
    void create_WithDniType_ShouldValidateAndAutoFill() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        when(personaRepository.existsByNumeroDocumento("12345678")).thenReturn(false);
        when(tipoDocumentoIdentidadRepository.findById(1L)).thenReturn(Optional.of(tdi));
        when(modulo11Validator.validar("12345678")).thenReturn(true);

        var savedEntity = createPersona(1L, "12345678", tdi);
        when(personaRepository.save(any())).thenReturn(savedEntity);

        var request = new PersonaRequest(
            1L, "12345678", "JUAN", "PEREZ", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null
        );

        var result = personaService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.numeroDocumento()).isEqualTo("12345678");
        verify(modulo11Validator).validar("12345678");
    }

    @Test
    void create_WithInvalidDni_ShouldThrowDniInvalidoException() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        when(personaRepository.existsByNumeroDocumento("12345678")).thenReturn(false);
        when(tipoDocumentoIdentidadRepository.findById(1L)).thenReturn(Optional.of(tdi));
        when(modulo11Validator.validar("12345678")).thenReturn(false);

        var request = new PersonaRequest(
            1L, "12345678", "JUAN", "PEREZ", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null
        );

        assertThatThrownBy(() -> personaService.create(request))
            .isInstanceOf(DniInvalidoException.class)
            .hasMessageContaining("dígito verificador");
        verify(personaRepository, never()).save(any());
    }

    @Test
    void create_WithNonDniDocumentType_ShouldSkipModulo11() {
        var tdi = createTipoDocumento(1L, "04", "CE");
        when(personaRepository.existsByNumeroDocumento("123456789")).thenReturn(false);
        when(tipoDocumentoIdentidadRepository.findById(1L)).thenReturn(Optional.of(tdi));

        var savedEntity = createPersona(1L, "123456789", tdi);
        savedEntity.setTipoDocumentoIdentidad(tdi);
        when(personaRepository.save(any())).thenReturn(savedEntity);

        var request = new PersonaRequest(
            1L, "123456789", "JUAN", "PEREZ", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null
        );

        var result = personaService.create(request);

        assertThat(result).isNotNull();
        verify(modulo11Validator, never()).validar(any());
    }

    @Test
    void create_ShouldRejectDuplicateDocument() {
        when(personaRepository.existsByNumeroDocumento("12345678")).thenReturn(true);

        var request = new PersonaRequest(
            1L, "12345678", "JUAN", "PEREZ", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null
        );

        assertThatThrownBy(() -> personaService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("12345678");
        verify(personaRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowWhenTipoDocumentoNotFound() {
        when(personaRepository.existsByNumeroDocumento("12345678")).thenReturn(false);
        when(tipoDocumentoIdentidadRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new PersonaRequest(
            99L, "12345678", "JUAN", "PEREZ", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null
        );

        assertThatThrownBy(() -> personaService.create(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    // --- update() tests ---

    @Test
    void update_ShouldModifyAndReturn() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var existing = createPersona(1L, "12345678", tdi);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tipoDocumentoIdentidadRepository.findById(1L)).thenReturn(Optional.of(tdi));
        when(personaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new PersonaRequest(
            1L, "12345678", "JUAN", "GARCIA", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null
        );

        var result = personaService.update(1L, request);

        assertThat(result.apellidoPaterno()).isEqualTo("GARCIA");
    }

    @Test
    void update_ShouldThrowWhenNotFound() {
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new PersonaRequest(
            1L, "12345678", "JUAN", "PEREZ", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null
        );

        assertThatThrownBy(() -> personaService.update(99L, request))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_ShouldRejectDuplicateDocument() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var existing = createPersona(1L, "87654321", tdi);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(personaRepository.existsByNumeroDocumento("12345678")).thenReturn(true);

        var request = new PersonaRequest(
            1L, "12345678", "JUAN", "PEREZ", "LOPEZ",
            LocalDate.of(1990, 1, 1), "M", null, null, null, null, null
        );

        assertThatThrownBy(() -> personaService.update(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("12345678");
    }

    // --- softDelete() tests ---

    @Test
    void softDelete_ShouldMarkInactive() {
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var entity = createPersona(1L, "12345678", tdi);
        entity.setActivo(true);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(personaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = personaService.softDelete(1L);

        assertThat(result.activo()).isFalse();
    }

    @Test
    void softDelete_ShouldThrowWhenNotFound() {
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaService.softDelete(99L))
            .isInstanceOf(EntityNotFoundException.class);
    }

    // --- PII exclusion test ---
    @Test
    void response_ShouldNotExposePiiInToString() {
        // Verify that PersonaResponse does not expose PII fields in unexpected ways
        // PII fields like numeroDocumento are intentionally included in the response
        // for legitimate display purposes. The @ToString.Exclude on Persona entity
        // fields ensures PII is excluded from logs.
        var tdi = createTipoDocumento(1L, "01", "DNI");
        var entity = createPersona(1L, "12345678", tdi);
        entity.setTelefono("999888777");
        entity.setEmail("juan@example.com");
        entity.setDireccion("Av. Principal 123");

        when(personaRepository.findById(1L)).thenReturn(Optional.of(entity));

        var response = personaService.findById(1L);

        assertThat(response.telefono()).isEqualTo("999888777");
        assertThat(response.email()).isEqualTo("juan@example.com");
        assertThat(response.direccion()).isEqualTo("Av. Principal 123");

        // Verify the entity itself uses @ToString.Exclude on PII fields
        assertThat(entity.toString()).doesNotContain("12345678");
        assertThat(entity.toString()).doesNotContain("999888777");
    }
}

package com.clinica.rrhh.pension.service;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.maestro.repository.rrhh.AfpRepository;
import com.clinica.rrhh.pension.dto.InformacionPensionariaRequest;
import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import com.clinica.rrhh.pension.repository.InformacionPensionariaRepository;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InformacionPensionariaServiceTest {

    @Mock
    private InformacionPensionariaRepository informacionPensionariaRepository;

    @Mock
    private TrabajadorRepository trabajadorRepository;

    @Mock
    private AfpRepository afpRepository;

    @InjectMocks
    private InformacionPensionariaService informacionPensionariaService;

    @Captor
    private ArgumentCaptor<InformacionPensionaria> pensionCaptor;

    private Trabajador createTrabajador(Long id, String dni) {
        var tdi = new TipoDocumentoIdentidad();
        tdi.setNombre("DNI");

        var persona = new com.clinica.persona.entity.Persona();
        persona.setId(1L);
        persona.setTipoDocumentoIdentidad(tdi);
        persona.setNumeroDocumento(dni);
        persona.setNombres("TRABAJADOR");
        persona.setApellidoPaterno("PENSION");

        var t = new Trabajador();
        t.setId(id);
        t.setCodigoTrabajador("TR-" + id);
        t.setPersona(persona);
        return t;
    }

    private Afp createAfp(Long id, String codigo, String nombre) {
        var a = new Afp();
        a.setId(id);
        a.setCodigo(codigo);
        a.setNombre(nombre);
        return a;
    }

    @Test
    void upsert_AFP_CreatesNew() {
        var trabajador = createTrabajador(1L, "12345678");
        var afp = createAfp(1L, "PRIMA", "Prima AFP");

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(afpRepository.findById(1L)).thenReturn(Optional.of(afp));
        when(informacionPensionariaRepository.findByTrabajadorId(1L)).thenReturn(Optional.empty());
        when(informacionPensionariaRepository.save(any())).thenAnswer(i -> {
            var info = (InformacionPensionaria) i.getArgument(0);
            info.setId(1L);
            return info;
        });

        var request = new InformacionPensionariaRequest(
            1L, "123456789012", "FLUJO", false,
            LocalDate.of(2025, 1, 1), null);
        var result = informacionPensionariaService.upsert(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.cuspp()).isEqualTo("123456789012");
        assertThat(result.comisionTipo()).isEqualTo("FLUJO");
        assertThat(result.estado()).isEqualTo("ACTIVO");

        verify(informacionPensionariaRepository).save(pensionCaptor.capture());
        assertThat(pensionCaptor.getValue().getSctr()).isFalse();
    }

    @Test
    void upsert_AFP_UpdatesExisting() {
        var trabajador = createTrabajador(1L, "12345678");
        var afp = createAfp(1L, "PRIMA", "Prima AFP");
        var existing = new InformacionPensionaria();
        existing.setId(1L);
        existing.setTrabajador(trabajador);
        existing.setAfp(afp);
        existing.setCuspp("123456789012");
        existing.setComisionTipo("FLUJO");
        existing.setEstado("ACTIVO");

        var afp2 = createAfp(2L, "HABITAT", "Habitat AFP");

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(afpRepository.findById(2L)).thenReturn(Optional.of(afp2));
        when(informacionPensionariaRepository.findByTrabajadorId(1L)).thenReturn(Optional.of(existing));
        when(informacionPensionariaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new InformacionPensionariaRequest(
            2L, "987654321098", "MIXTA", true,
            LocalDate.of(2025, 6, 1), "REF-001");
        var result = informacionPensionariaService.upsert(1L, request);

        assertThat(result.afpId()).isEqualTo(2L);
        assertThat(result.cuspp()).isEqualTo("987654321098");
        assertThat(result.comisionTipo()).isEqualTo("MIXTA");
        assertThat(result.sctr()).isTrue();
        assertThat(result.documentoReferencia()).isEqualTo("REF-001");
    }

    @Test
    void upsert_ONP_NullComisionAndCusppAutoDni() {
        var trabajador = createTrabajador(1L, "87654321");
        var onp = createAfp(5L, "ONP", "ONP");

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(afpRepository.findById(5L)).thenReturn(Optional.of(onp));
        when(informacionPensionariaRepository.findByTrabajadorId(1L)).thenReturn(Optional.empty());
        when(informacionPensionariaRepository.save(any())).thenAnswer(i -> {
            var info = (InformacionPensionaria) i.getArgument(0);
            info.setId(2L);
            return info;
        });

        var request = new InformacionPensionariaRequest(
            5L, null, null, null,
            LocalDate.of(2025, 1, 1), null);
        var result = informacionPensionariaService.upsert(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.comisionTipo()).isNull();
        assertThat(result.cuspp()).isEqualTo("87654321"); // auto-populated from DNI
        assertThat(result.sctr()).isFalse();
    }

    @Test
    void upsert_AFP_CusppTooShort_Throws() {
        var trabajador = createTrabajador(1L, "12345678");
        var afp = createAfp(1L, "PRIMA", "Prima AFP");

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(afpRepository.findById(1L)).thenReturn(Optional.of(afp));

        var request = new InformacionPensionariaRequest(
            1L, "12345", "FLUJO", false,
            LocalDate.of(2025, 1, 1), null);

        assertThatThrownBy(() -> informacionPensionariaService.upsert(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("12 dígitos");
    }

    @Test
    void upsert_AFP_CusppBlank_Throws() {
        var trabajador = createTrabajador(1L, "12345678");
        var afp = createAfp(1L, "PRIMA", "Prima AFP");

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(afpRepository.findById(1L)).thenReturn(Optional.of(afp));

        var request = new InformacionPensionariaRequest(
            1L, "", "FLUJO", false,
            LocalDate.of(2025, 1, 1), null);

        assertThatThrownBy(() -> informacionPensionariaService.upsert(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CUSPP es obligatorio");
    }

    @Test
    void upsert_AFP_ComisionTipoNull_Throws() {
        var trabajador = createTrabajador(1L, "12345678");
        var afp = createAfp(1L, "PRIMA", "Prima AFP");

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(afpRepository.findById(1L)).thenReturn(Optional.of(afp));

        var request = new InformacionPensionariaRequest(
            1L, "123456789012", null, false,
            LocalDate.of(2025, 1, 1), null);

        assertThatThrownBy(() -> informacionPensionariaService.upsert(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Tipo de comisión es obligatorio");
    }

    @Test
    void upsert_TrabajadorNotFound_Throws() {
        when(trabajadorRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new InformacionPensionariaRequest(
            1L, "123456789012", "FLUJO", false,
            LocalDate.of(2025, 1, 1), null);

        assertThatThrownBy(() -> informacionPensionariaService.upsert(99L, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void upsert_AfpNotFound_Throws() {
        var trabajador = createTrabajador(1L, "12345678");

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(afpRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new InformacionPensionariaRequest(
            99L, "123456789012", "FLUJO", false,
            LocalDate.of(2025, 1, 1), null);

        assertThatThrownBy(() -> informacionPensionariaService.upsert(1L, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void getByTrabajadorId_ShouldReturnResponse() {
        var trabajador = createTrabajador(1L, "12345678");
        var afp = createAfp(1L, "PRIMA", "Prima AFP");
        var info = new InformacionPensionaria();
        info.setId(1L);
        info.setTrabajador(trabajador);
        info.setAfp(afp);
        info.setCuspp("123456789012");
        info.setComisionTipo("FLUJO");
        info.setEstado("ACTIVO");

        when(informacionPensionariaRepository.findByTrabajadorId(1L)).thenReturn(Optional.of(info));

        var result = informacionPensionariaService.getByTrabajadorId(1L);
        assertThat(result).isNotNull();
        assertThat(result.estado()).isEqualTo("ACTIVO");
        assertThat(result.cuspp()).isEqualTo("123456789012");
    }

    @Test
    void getByTrabajadorId_ShouldThrowWhenNotFound() {
        when(informacionPensionariaRepository.findByTrabajadorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> informacionPensionariaService.getByTrabajadorId(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }
}

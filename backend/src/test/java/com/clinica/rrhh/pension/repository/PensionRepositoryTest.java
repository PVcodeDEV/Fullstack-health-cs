package com.clinica.rrhh.pension.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.maestro.repository.rrhh.AfpRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.pension.entity.InformacionPensionaria;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PensionRepositoryTest {

    @Autowired
    private AfpRepository afpRepository;

    @Autowired
    private InformacionPensionariaRepository informacionPensionariaRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    private Afp afp;
    private Trabajador trabajador;

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
        persona.setNumeroDocumento("99999999");
        persona.setNombres("PENSION");
        persona.setApellidoPaterno("TEST");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-PEN-001");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);

        afp = new Afp();
        afp.setCodigo("PRIMA");
        afp.setNombre("Prima AFP");
        afp = afpRepository.saveAndFlush(afp);
    }

    @Test
    void afp_findAllByActivoTrueOrderByNombre() {
        var onp = new Afp();
        onp.setCodigo("ONP");
        onp.setNombre("ONP");
        onp.setActivo(true);
        afpRepository.saveAndFlush(onp);

        var all = afpRepository.findAllByActivoTrueOrderByCodigo();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getCodigo()).isEqualTo("ONP"); // O comes before P
        assertThat(all.get(1).getCodigo()).isEqualTo("PRIMA");
    }

    @Test
    void afp_findByCodigo() {
        var found = afpRepository.findByCodigo("PRIMA");
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("Prima AFP");

        var notFound = afpRepository.findByCodigo("NONEXISTENT");
        assertThat(notFound).isEmpty();
    }

    @Test
    void pension_findByTrabajadorId() {
        var info = new InformacionPensionaria();
        info.setTrabajador(trabajador);
        info.setAfp(afp);
        info.setCuspp("123456789012");
        info.setFechaAfiliacion(LocalDate.of(2025, 1, 1));
        informacionPensionariaRepository.saveAndFlush(info);

        var found = informacionPensionariaRepository.findByTrabajadorId(trabajador.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCuspp()).isEqualTo("123456789012");
        assertThat(found.get().getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    void pension_existsByTrabajadorId() {
        assertThat(informacionPensionariaRepository.existsByTrabajadorId(trabajador.getId())).isFalse();

        var info = new InformacionPensionaria();
        info.setTrabajador(trabajador);
        info.setAfp(afp);
        info.setCuspp("123456789012");
        info.setFechaAfiliacion(LocalDate.of(2025, 1, 1));
        informacionPensionariaRepository.saveAndFlush(info);

        assertThat(informacionPensionariaRepository.existsByTrabajadorId(trabajador.getId())).isTrue();
    }

    @Test
    void pension_uniqueConstraint() {
        var info1 = new InformacionPensionaria();
        info1.setTrabajador(trabajador);
        info1.setAfp(afp);
        info1.setCuspp("123456789012");
        info1.setFechaAfiliacion(LocalDate.of(2025, 1, 1));
        informacionPensionariaRepository.saveAndFlush(info1);

        var info2 = new InformacionPensionaria();
        info2.setTrabajador(trabajador);  // same trabajador
        info2.setAfp(afp);
        info2.setCuspp("987654321098");
        info2.setFechaAfiliacion(LocalDate.of(2025, 6, 1));

        assertThatThrownBy(() -> informacionPensionariaRepository.saveAndFlush(info2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}

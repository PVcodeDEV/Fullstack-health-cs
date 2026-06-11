package com.clinica.rrhh.contrato.repository;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import com.clinica.maestro.entity.rrhh.TipoContrato;
import com.clinica.maestro.repository.identidad.TipoDocumentoIdentidadRepository;
import com.clinica.maestro.repository.rrhh.TipoContratoRepository;
import com.clinica.persona.entity.Persona;
import com.clinica.persona.repository.PersonaRepository;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.trabajador.repository.TrabajadorRepository;
import com.clinica.rrhh.type.EstadoContrato;
import com.clinica.rrhh.type.TipoJornada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ContratoRepositoryTest {

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @Autowired
    private TipoContratoRepository tipoContratoRepository;

    private Trabajador trabajador;
    private TipoContrato tipoContrato;

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
        persona.setNumeroDocumento("11111111");
        persona.setNombres("JUAN");
        persona.setApellidoPaterno("PEREZ");
        persona.setActivo(true);
        persona = personaRepository.saveAndFlush(persona);

        trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCodigoTrabajador("TR-001");
        trabajador.setFechaIngreso(LocalDate.of(2025, 1, 1));
        trabajador.setActivo(true);
        trabajador = trabajadorRepository.saveAndFlush(trabajador);

        tipoContrato = new TipoContrato();
        tipoContrato.setCodigo("INDETERMINADO");
        tipoContrato.setNombre("Indeterminado");
        tipoContrato.setDescripcion("Contrato a plazo indeterminado");
        tipoContrato = tipoContratoRepository.saveAndFlush(tipoContrato);
    }

    private Contrato createContrato(Trabajador t, TipoContrato tc, EstadoContrato estado) {
        var c = new Contrato();
        c.setTrabajador(t);
        c.setTipoContrato(tc);
        c.setFechaInicio(LocalDate.of(2025, 1, 1));
        c.setRemuneracion(new BigDecimal("2500.00"));
        c.setJornada(TipoJornada.REGULAR);
        c.setEstado(estado);
        return c;
    }

    @Test
    void shouldSaveAndFindById() {
        var contrato = createContrato(trabajador, tipoContrato, EstadoContrato.ACTIVO);
        var saved = contratoRepository.save(contrato);
        assertThat(saved.getId()).isNotNull();

        var found = contratoRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEstado()).isEqualTo(EstadoContrato.ACTIVO);
        assertThat(found.get().getRemuneracion()).isEqualByComparingTo(new BigDecimal("2500.00"));
    }

    @Test
    void shouldFindByTrabajadorIdAndEstado() {
        var contrato = createContrato(trabajador, tipoContrato, EstadoContrato.ACTIVO);
        contratoRepository.saveAndFlush(contrato);

        var found = contratoRepository.findByTrabajadorIdAndEstado(trabajador.getId(), EstadoContrato.ACTIVO);
        assertThat(found).isPresent();
        assertThat(found.get().getEstado()).isEqualTo(EstadoContrato.ACTIVO);

        var notFound = contratoRepository.findByTrabajadorIdAndEstado(trabajador.getId(), EstadoContrato.RESUELTO);
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldFindByEstado() {
        var activo = createContrato(trabajador, tipoContrato, EstadoContrato.ACTIVO);
        contratoRepository.saveAndFlush(activo);

        var result = contratoRepository.findByEstado(EstadoContrato.ACTIVO);
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getEstado()).isEqualTo(EstadoContrato.ACTIVO);

        var resultResuelto = contratoRepository.findByEstado(EstadoContrato.RESUELTO);
        assertThat(resultResuelto).isEmpty();
    }

    @Test
    void shouldRejectNullEstado() {
        var contrato = createContrato(trabajador, tipoContrato, null);
        assertThatThrownBy(() -> contratoRepository.saveAndFlush(contrato))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

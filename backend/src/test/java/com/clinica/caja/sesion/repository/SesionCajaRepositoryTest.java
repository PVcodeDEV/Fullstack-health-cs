package com.clinica.caja.sesion.repository;

import com.clinica.caja.sesion.entity.SesionCaja;
import com.clinica.caja.sesion.entity.SesionCaja.Estado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository test for clinical {@link ClinicaSesionCajaRepository}.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class SesionCajaRepositoryTest {

    @Autowired
    private ClinicaSesionCajaRepository sesionCajaRepository;

    private static final Long USUARIO_ID = 1L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 12, 10, 0, 0);

    @BeforeEach
    void setUp() {
        sesionCajaRepository.deleteAll();
    }

    private SesionCaja createSession(Long usuarioId, Estado estado, BigDecimal montoApertura) {
        SesionCaja session = new SesionCaja();
        session.setCodigo("SES-TEST-" + System.currentTimeMillis());
        session.setUsuarioAperturaId(usuarioId);
        session.setFechaApertura(NOW);
        session.setMontoApertura(montoApertura);
        session.setEstado(estado);
        session.setTotalVentas(BigDecimal.ZERO);
        return sesionCajaRepository.save(session);
    }

    @Test
    void shouldFindOpenSessionByUser() {
        // GIVEN an open session for user
        createSession(USUARIO_ID, Estado.ABIERTA, new BigDecimal("500.00"));

        // WHEN
        Optional<SesionCaja> found = sesionCajaRepository
            .findByUsuarioAperturaIdAndEstadoAndActivoTrue(USUARIO_ID, Estado.ABIERTA);

        // THEN
        assertThat(found).isPresent();
        assertThat(found.get().getEstado()).isEqualTo(Estado.ABIERTA);
        assertThat(found.get().getUsuarioAperturaId()).isEqualTo(USUARIO_ID);
    }

    @Test
    void shouldNotFindClosedSessionByUser() {
        // GIVEN a closed session for user
        SesionCaja session = createSession(USUARIO_ID, Estado.ABIERTA, new BigDecimal("500.00"));
        session.cerrar(new BigDecimal("500.00"), USUARIO_ID, NOW);
        sesionCajaRepository.save(session);

        // WHEN
        Optional<SesionCaja> found = sesionCajaRepository
            .findByUsuarioAperturaIdAndEstadoAndActivoTrue(USUARIO_ID, Estado.ABIERTA);

        // THEN
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDetectDoubleOpen() {
        // GIVEN an open session for user
        createSession(USUARIO_ID, Estado.ABIERTA, new BigDecimal("500.00"));

        // WHEN
        boolean exists = sesionCajaRepository
            .existsByUsuarioAperturaIdAndEstadoAndActivoTrue(USUARIO_ID, Estado.ABIERTA);

        // THEN
        assertThat(exists).isTrue();
    }

    @Test
    void shouldNotDetectDoubleOpenForDifferentUser() {
        // GIVEN an open session for user 1
        createSession(USUARIO_ID, Estado.ABIERTA, new BigDecimal("500.00"));

        // WHEN check for user 2
        boolean exists = sesionCajaRepository
            .existsByUsuarioAperturaIdAndEstadoAndActivoTrue(999L, Estado.ABIERTA);

        // THEN
        assertThat(exists).isFalse();
    }
}

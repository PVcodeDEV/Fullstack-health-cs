package com.clinica.farmacia.caja.repository;

import com.clinica.farmacia.caja.entity.SesionCaja;
import com.clinica.farmacia.caja.type.EstadoSesion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SesionCajaRepositoryIT {

    @Autowired
    private SesionCajaRepository repository;

    private SesionCaja savedSesion;

    @BeforeEach
    void setUp() {
        SesionCaja s = new SesionCaja();
        s.setUsuarioId(42L);
        s.setAlmacenId(1L);
        s.setMontoApertura(new BigDecimal("500.00"));
        s.setTotalVentas(BigDecimal.ZERO);
        s.setEstado(EstadoSesion.ABIERTA);
        s.setFechaApertura(LocalDateTime.now());
        savedSesion = repository.saveAndFlush(s);
    }

    @Test
    void shouldFindOpenSessionByUsuario() {
        var result = repository.findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(
            42L, EstadoSesion.ABIERTA);
        assertThat(result).isPresent();
        assertThat(result.get().getUsuarioId()).isEqualTo(42L);
        assertThat(result.get().getEstado()).isEqualTo(EstadoSesion.ABIERTA);
    }

    @Test
    void shouldDetectExistingOpenSession() {
        boolean exists = repository.existsByUsuarioIdAndEstado(42L, EstadoSesion.ABIERTA);
        assertThat(exists).isTrue();

        boolean notExists = repository.existsByUsuarioIdAndEstado(99L, EstadoSesion.ABIERTA);
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldCloseSessionAndCalculateDifference() {
        // Simulate adding sales
        savedSesion.setTotalVentas(new BigDecimal("290.00"));
        repository.saveAndFlush(savedSesion);

        savedSesion.cerrar(new BigDecimal("510.00"), "Cierre diario");
        repository.saveAndFlush(savedSesion);

        var closed = repository.findById(savedSesion.getId()).orElseThrow();
        assertThat(closed.getEstado()).isEqualTo(EstadoSesion.CERRADA);
        // montoCierreEsperado = 500 + 290 = 790
        assertThat(closed.getMontoCierreEsperado()).isEqualByComparingTo(new BigDecimal("790.00"));
        assertThat(closed.getDiferenciaCierre()).isEqualByComparingTo(new BigDecimal("-280.00"));
        assertThat(closed.getFechaCierre()).isNotNull();
    }
}

package com.clinica.rrhh.planillaplame.repository;

import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.planilla.repository.PeriodoPlanillaRepository;
import com.clinica.rrhh.planillaplame.entity.ArchivoPlanilla;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArchivoPlanillaRepositoryTest {

    @Autowired
    private ArchivoPlanillaRepository repository;

    @Autowired
    private PeriodoPlanillaRepository periodoPlanillaRepository;

    private PeriodoPlanilla periodoPlanilla;

    @BeforeEach
    void setUp() {
        periodoPlanilla = new PeriodoPlanilla();
        periodoPlanilla.setAnio(2026);
        periodoPlanilla.setMes(1);
        periodoPlanilla.setFechaInicio(LocalDate.of(2026, Month.JANUARY, 1));
        periodoPlanilla.setFechaFin(LocalDate.of(2026, Month.JANUARY, 31));
        periodoPlanilla.setEstado("CERRADO");
        periodoPlanilla = periodoPlanillaRepository.saveAndFlush(periodoPlanilla);
    }

    private ArchivoPlanilla createArchivo(PeriodoPlanilla pp, String tipo) {
        var a = new ArchivoPlanilla();
        a.setPeriodoPlanilla(pp);
        a.setTipo(tipo);
        a.setContenido("contenido de prueba para " + tipo);
        a.setHash("abc123def456hash");
        a.setGeneradoPor("test-user");
        return a;
    }

    @Test
    void shouldSaveAndFindById() {
        var archivo = createArchivo(periodoPlanilla, "PLAME");
        var saved = repository.save(archivo);
        assertThat(saved.getId()).isNotNull();

        var found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTipo()).isEqualTo("PLAME");
        assertThat(found.get().getHash()).isEqualTo("abc123def456hash");
    }

    @Test
    void shouldFindByPeriodoPlanillaId() {
        var plame = createArchivo(periodoPlanilla, "PLAME");
        repository.saveAndFlush(plame);

        var tRegistro = createArchivo(periodoPlanilla, "T_REGISTRO");
        repository.saveAndFlush(tRegistro);

        List<ArchivoPlanilla> result = repository.findByPeriodoPlanillaId(periodoPlanilla.getId());
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ArchivoPlanilla::getTipo)
                .containsExactlyInAnyOrder("PLAME", "T_REGISTRO");
    }

    @Test
    void shouldFindByPeriodoPlanillaIdAndTipo() {
        var plame = createArchivo(periodoPlanilla, "PLAME");
        repository.saveAndFlush(plame);

        var tRegistro = createArchivo(periodoPlanilla, "T_REGISTRO");
        repository.saveAndFlush(tRegistro);

        var found = repository.findByPeriodoPlanillaIdAndTipo(periodoPlanilla.getId(), "PLAME");
        assertThat(found).isPresent();
        assertThat(found.get().getTipo()).isEqualTo("PLAME");

        var notFound = repository.findByPeriodoPlanillaIdAndTipo(periodoPlanilla.getId(), "NO_EXISTE");
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldCheckExistsByPeriodoPlanillaIdAndTipo() {
        assertThat(repository.existsByPeriodoPlanillaIdAndTipo(periodoPlanilla.getId(), "PLAME")).isFalse();

        var archivo = createArchivo(periodoPlanilla, "PLAME");
        repository.saveAndFlush(archivo);

        assertThat(repository.existsByPeriodoPlanillaIdAndTipo(periodoPlanilla.getId(), "PLAME")).isTrue();
        assertThat(repository.existsByPeriodoPlanillaIdAndTipo(periodoPlanilla.getId(), "T_REGISTRO")).isFalse();
    }
}

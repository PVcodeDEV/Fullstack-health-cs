package com.clinica.clinica.cuenta.service;

import com.clinica.clinica.admision.entity.Cuenta;
import com.clinica.clinica.admision.repository.CuentaRepository;
import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.entity.EstadoCama;
import com.clinica.clinica.cama.repository.CamaRepository;
import com.clinica.clinica.cuenta.dto.CargoAdicionalRequest;
import com.clinica.clinica.cuenta.entity.CargoAdicional;
import com.clinica.clinica.cuenta.repository.CargoAdicionalRepository;
import com.clinica.clinica.hospitalizacion.entity.Hospitalizacion;
import com.clinica.clinica.hospitalizacion.repository.HospitalizacionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;
    @Mock
    private CargoAdicionalRepository cargoRepository;
    @Mock
    private HospitalizacionRepository hospitalizacionRepository;
    @Mock
    private CamaRepository camaRepository;

    @InjectMocks
    private CuentaService service;

    @Captor
    private ArgumentCaptor<CargoAdicional> cargoCaptor;

    @Test
    void agregarCargo_ShouldSaveAndReturnResponse() {
        when(cargoRepository.save(any())).thenAnswer(i -> {
            var c = (CargoAdicional) i.getArgument(0);
            c.setId(1L);
            return c;
        });

        var request = new CargoAdicionalRequest(1L, "Honorarios médicos", new BigDecimal("250.00"), "GENERAL");
        var result = service.agregarCargo(request);

        assertThat(result).isNotNull();
        assertThat(result.descripcion()).isEqualTo("Honorarios médicos");
        assertThat(result.monto()).isEqualByComparingTo(new BigDecimal("250.00"));

        verify(cargoRepository).save(cargoCaptor.capture());
        assertThat(cargoCaptor.getValue().getTipo()).isEqualTo("GENERAL");
    }

    @Test
    void agregarCargo_WithDefaultTipo() {
        when(cargoRepository.save(any())).thenAnswer(i -> {
            var c = (CargoAdicional) i.getArgument(0);
            c.setId(1L);
            return c;
        });

        var request = new CargoAdicionalRequest(1L, "Medicamentos", new BigDecimal("100.00"), null);
        var result = service.agregarCargo(request);

        assertThat(result).isNotNull();
        verify(cargoRepository).save(cargoCaptor.capture());
        assertThat(cargoCaptor.getValue().getTipo()).isEqualTo("GENERAL");
    }

    @Test
    void confirmarCobro_ShouldCompleteSuccessfully() {
        var cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setPacienteId(1L);
        cuenta.setEstado("ABIERTA");

        var hosp = new Hospitalizacion();
        hosp.setId(1L);
        hosp.setCamaId(1L);
        hosp.setCuentaId(1L);
        hosp.setEstado("HOSPITALIZADO");

        var cama = new Cama();
        cama.setId(1L);
        cama.setEstado(EstadoCama.OCUPADO);

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(hospitalizacionRepository.findByCuentaId(1L)).thenReturn(Optional.of(hosp));
        when(camaRepository.findById(1L)).thenReturn(Optional.of(cama));
        when(camaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(hospitalizacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(cuentaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.confirmarCobro(1L);

        assertThat(cuenta.getEstado()).isEqualTo("CERRADA");
        assertThat(hosp.getEstado()).isEqualTo("FINALIZADO");
        assertThat(cama.getEstado()).isEqualTo(EstadoCama.DISPONIBLE);
    }

    @Test
    void listarCargos_ShouldReturnList() {
        var cargo = new CargoAdicional();
        cargo.setId(1L);
        cargo.setCuentaId(1L);
        cargo.setTipo("GENERAL");
        cargo.setMonto(new BigDecimal("150.00"));
        cargo.setDescripcion("Consulta");

        when(cargoRepository.findByCuentaId(1L)).thenReturn(List.of(cargo));

        var result = service.listarCargos(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).descripcion()).isEqualTo("Consulta");
    }

    @Test
    void listarCargos_WithNoCargos_ShouldReturnEmpty() {
        when(cargoRepository.findByCuentaId(99L)).thenReturn(List.of());

        var result = service.listarCargos(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void obtenerCuenta_ShouldReturnEntity() {
        var cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setEstado("ABIERTA");
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        var result = service.obtenerCuenta(1L);

        assertThat(result).isNotNull();
        assertThat(result.getEstado()).isEqualTo("ABIERTA");
    }

    @Test
    void obtenerCuenta_ShouldThrowWhenNotFound() {
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerCuenta(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

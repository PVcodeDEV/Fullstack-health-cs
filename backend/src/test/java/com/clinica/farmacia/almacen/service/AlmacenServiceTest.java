package com.clinica.farmacia.almacen.service;

import com.clinica.farmacia.almacen.dto.AlmacenRequest;
import com.clinica.farmacia.almacen.dto.AlmacenResponse;
import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.almacen.repository.AlmacenRepository;
import com.clinica.farmacia.lote.repository.LoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlmacenServiceTest {

    @Mock
    private AlmacenRepository repository;

    @Mock
    private LoteRepository loteRepository;

    @Captor
    private ArgumentCaptor<Almacen> almacenCaptor;

    private AlmacenService service;

    @BeforeEach
    void setUp() {
        service = new AlmacenService(repository, loteRepository);
    }

    @Test
    void shouldCrearAlmacenAsDefault() {
        AlmacenRequest request = new AlmacenRequest("ALM-01", "Almacén Principal",
            "Sótano", true);

        when(repository.existsByCodigo("ALM-01")).thenReturn(false);
        when(repository.findByDefaultWarehouseTrue()).thenReturn(Optional.empty()); // no existing default
        when(repository.save(any(Almacen.class))).thenAnswer(inv -> {
            Almacen a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AlmacenResponse response = service.create(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.codigo()).isEqualTo("ALM-01");
        assertThat(response.defaultWarehouse()).isTrue();
    }

    @Test
    void shouldCrearAlmacenNoDefaultYAutoSetDefault() {
        // If no default exists and request is not default, auto-set it to default
        AlmacenRequest request = new AlmacenRequest("ALM-02", "Almacén Secundario",
            null, false);

        when(repository.existsByCodigo("ALM-02")).thenReturn(false);
        when(repository.existsByDefaultWarehouseTrue()).thenReturn(false); // no default exists
        when(repository.save(any(Almacen.class))).thenAnswer(inv -> {
            Almacen a = inv.getArgument(0);
            a.setId(2L);
            return a;
        });

        AlmacenResponse response = service.create(request);

        assertThat(response.defaultWarehouse()).isTrue(); // auto-set to default
    }

    @Test
    void shouldRechazarCrearMultiplesDefault() {
        // Second default clears the first
        Almacen existingDefault = new Almacen();
        existingDefault.setId(1L);
        existingDefault.setCodigo("DEF");
        existingDefault.setDefaultWarehouse(true);
        existingDefault.setNombre("Default");

        AlmacenRequest request = new AlmacenRequest("ALM-03", "Nuevo Principal",
            null, true);

        when(repository.existsByCodigo("ALM-03")).thenReturn(false);
        when(repository.findByDefaultWarehouseTrue()).thenReturn(Optional.of(existingDefault));
        when(repository.save(any(Almacen.class))).thenAnswer(inv -> {
            Almacen a = inv.getArgument(0);
            if (a.getId() == null) {
                a.setId(3L);
            }
            return a;
        });

        AlmacenResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.defaultWarehouse()).isTrue();

        // Verify the old default was cleared (first save) and new one created (second save)
        verify(repository, atLeast(2)).save(any(Almacen.class));

        // The old default should now be false
        assertThat(existingDefault.getDefaultWarehouse()).isFalse();
    }

    @Test
    void shouldRechazarEliminarAlmacenConLotesActivos() {
        Almacen entity = new Almacen();
        entity.setId(1L);
        entity.setCodigo("ALM-01");
        entity.setNombre("Almacén Principal");

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(loteRepository.existsByAlmacenIdAndStockActualGreaterThanAndActivoTrue(1L, 0))
            .thenReturn(true);

        assertThatThrownBy(() -> service.softDelete(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("tiene lotes con stock activo");

        verify(repository, never()).save(any());
    }
}

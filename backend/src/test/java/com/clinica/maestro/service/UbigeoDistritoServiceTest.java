package com.clinica.maestro.service;

import com.clinica.maestro.dto.ubigeo.UbigeoDistritoRequest;
import com.clinica.maestro.entity.ubigeo.UbigeoDistrito;
import com.clinica.maestro.entity.ubigeo.UbigeoProvincia;
import com.clinica.maestro.repository.ubigeo.UbigeoDistritoRepository;
import com.clinica.maestro.repository.ubigeo.UbigeoProvinciaRepository;
import com.clinica.maestro.service.ubigeo.UbigeoDistritoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UbigeoDistritoServiceTest {

    @Mock
    private UbigeoDistritoRepository repository;

    @Mock
    private UbigeoProvinciaRepository provinciaRepository;

    @InjectMocks
    private UbigeoDistritoService service;

    @Test
    void findAll_ShouldReturnList() {
        var prov = new UbigeoProvincia();
        prov.setCodigo("1501");
        var entity = new UbigeoDistrito();
        entity.setCodigo("150101");
        entity.setNombre("Lima");
        entity.setProvincia(prov);
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var prov = new UbigeoProvincia();
        prov.setCodigo("1501");
        var entity = new UbigeoDistrito();
        entity.setCodigo("150101");
        entity.setProvincia(prov);
        when(repository.findById("150101")).thenReturn(Optional.of(entity));
        assertThat(service.findById("150101").codigo()).isEqualTo("150101");
    }

    @Test
    void findByProvincia_ShouldReturnList() {
        var prov = new UbigeoProvincia();
        prov.setCodigo("1501");
        var dist = new UbigeoDistrito();
        dist.setProvincia(prov);
        when(repository.findByProvinciaCodigoOrderByNombreAsc("1501")).thenReturn(List.of(dist));
        assertThat(service.findByProvincia("1501")).hasSize(1);
    }

    @Test
    void create_ShouldSaveWithProvincia() {
        var prov = new UbigeoProvincia();
        prov.setCodigo("1501");
        var request = new UbigeoDistritoRequest("150101", "Lima", "1501");
        when(repository.existsById("150101")).thenReturn(false);
        when(provinciaRepository.findById("1501")).thenReturn(Optional.of(prov));
        var saved = new UbigeoDistrito();
        saved.setCodigo("150101");
        saved.setProvincia(prov);
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigo()).isEqualTo("150101");
    }

    @Test
    void create_ShouldThrowWhenProvinciaNotFound() {
        var request = new UbigeoDistritoRequest("150101", "Lima", "9999");
        when(repository.existsById("150101")).thenReturn(false);
        when(provinciaRepository.findById("9999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var prov = new UbigeoProvincia();
        prov.setCodigo("1501");
        var entity = new UbigeoDistrito();
        entity.setCodigo("150101");
        entity.setProvincia(prov);
        entity.setActivo(true);
        when(repository.findById("150101")).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete("150101").activo()).isFalse();
    }
}

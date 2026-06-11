package com.clinica.maestro.service;

import com.clinica.maestro.dto.ubigeo.UbigeoProvinciaRequest;
import com.clinica.maestro.entity.ubigeo.UbigeoDepartamento;
import com.clinica.maestro.entity.ubigeo.UbigeoProvincia;
import com.clinica.maestro.repository.ubigeo.UbigeoDepartamentoRepository;
import com.clinica.maestro.repository.ubigeo.UbigeoProvinciaRepository;
import com.clinica.maestro.service.ubigeo.UbigeoProvinciaService;
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
class UbigeoProvinciaServiceTest {

    @Mock
    private UbigeoProvinciaRepository repository;

    @Mock
    private UbigeoDepartamentoRepository departamentoRepository;

    @InjectMocks
    private UbigeoProvinciaService service;

    @Test
    void findAll_ShouldReturnList() {
        var dept = new UbigeoDepartamento();
        dept.setCodigo("15");
        var entity = new UbigeoProvincia();
        entity.setCodigo("1501");
        entity.setNombre("Lima");
        entity.setDepartamento(dept);
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var dept = new UbigeoDepartamento();
        dept.setCodigo("15");
        var entity = new UbigeoProvincia();
        entity.setCodigo("1501");
        entity.setDepartamento(dept);
        when(repository.findById("1501")).thenReturn(Optional.of(entity));
        assertThat(service.findById("1501").codigo()).isEqualTo("1501");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById("9999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById("9999")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findByDepartamento_ShouldReturnList() {
        var dept = new UbigeoDepartamento();
        dept.setCodigo("15");
        var prov = new UbigeoProvincia();
        prov.setDepartamento(dept);
        when(repository.findByDepartamentoCodigoOrderByNombreAsc("15")).thenReturn(List.of(prov));
        assertThat(service.findByDepartamento("15")).hasSize(1);
    }

    @Test
    void create_ShouldSaveWithDepartamento() {
        var dept = new UbigeoDepartamento();
        dept.setCodigo("15");
        var request = new UbigeoProvinciaRequest("1501", "Lima", "15");
        when(repository.existsById("1501")).thenReturn(false);
        when(departamentoRepository.findById("15")).thenReturn(Optional.of(dept));
        var saved = new UbigeoProvincia();
        saved.setCodigo("1501");
        saved.setDepartamento(dept);
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigo()).isEqualTo("1501");
    }

    @Test
    void create_ShouldThrowWhenDepartamentoNotFound() {
        var request = new UbigeoProvinciaRequest("1501", "Lima", "99");
        when(repository.existsById("1501")).thenReturn(false);
        when(departamentoRepository.findById("99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var dept = new UbigeoDepartamento();
        dept.setCodigo("15");
        var entity = new UbigeoProvincia();
        entity.setCodigo("1501");
        entity.setDepartamento(dept);
        entity.setActivo(true);
        when(repository.findById("1501")).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete("1501").activo()).isFalse();
    }
}

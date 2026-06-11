package com.clinica.maestro.service;

import com.clinica.maestro.dto.organizacion.CategoriaInsumoRequest;
import com.clinica.maestro.entity.organizacion.CategoriaInsumo;
import com.clinica.maestro.repository.organizacion.CategoriaInsumoRepository;
import com.clinica.maestro.service.organizacion.CategoriaInsumoService;
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
class CategoriaInsumoServiceTest {

    @Mock
    private CategoriaInsumoRepository repository;

    @InjectMocks
    private CategoriaInsumoService service;

    @Test
    void findAll_WithoutFilter_ShouldReturnAll() {
        var entity = new CategoriaInsumo();
        entity.setId(1);
        entity.setCodigo("MED");
        entity.setNombre("Medicamento");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll(null)).hasSize(1);
    }

    @Test
    void findAll_WithPadreId_ShouldFilter() {
        var entity = new CategoriaInsumo();
        entity.setId(2);
        entity.setCodigo("ANT");
        when(repository.findByCategoriaPadreId(1)).thenReturn(List.of(entity));
        assertThat(service.findAll(1)).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new CategoriaInsumo();
        entity.setId(1);
        entity.setCodigo("MED");
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1).codigo()).isEqualTo("MED");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveAndReturn() {
        var request = new CategoriaInsumoRequest("MED", "Medicamento", null);
        when(repository.existsByCodigo("MED")).thenReturn(false);
        var saved = new CategoriaInsumo();
        saved.setId(1);
        saved.setCodigo("MED");
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigo()).isEqualTo("MED");
    }

    @Test
    void create_WithCategoriaPadre_ShouldSetParent() {
        var padre = new CategoriaInsumo();
        padre.setId(1);
        var request = new CategoriaInsumoRequest("SUB", "Subcategoria", 1);
        when(repository.existsByCodigo("SUB")).thenReturn(false);
        when(repository.findById(1)).thenReturn(Optional.of(padre));
        var saved = new CategoriaInsumo();
        saved.setId(2);
        saved.setCodigo("SUB");
        saved.setCategoriaPadre(padre);
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigo()).isEqualTo("SUB");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new CategoriaInsumoRequest("MED", "Medicamento", null);
        when(repository.existsByCodigo("MED")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_ShouldModify() {
        var existing = new CategoriaInsumo();
        existing.setId(1);
        existing.setCodigo("MED");
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        var request = new CategoriaInsumoRequest("FARM", "Farmacia", null);
        when(repository.existsByCodigo("FARM")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.update(1, request).codigo()).isEqualTo("FARM");
    }

    @Test
    void softDelete_ShouldMarkInactive() {
        var entity = new CategoriaInsumo();
        entity.setId(1);
        entity.setActivo(true);
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.softDelete(1).activo()).isFalse();
    }
}

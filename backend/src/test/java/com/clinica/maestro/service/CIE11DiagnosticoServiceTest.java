package com.clinica.maestro.service;

import com.clinica.maestro.dto.clinico.CIE11DiagnosticoRequest;
import com.clinica.maestro.entity.clinico.CIE11Diagnostico;
import com.clinica.maestro.repository.clinico.CIE11DiagnosticoRepository;
import com.clinica.maestro.service.clinico.CIE11DiagnosticoService;
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
class CIE11DiagnosticoServiceTest {

    @Mock
    private CIE11DiagnosticoRepository repository;

    @InjectMocks
    private CIE11DiagnosticoService service;

    @Test
    void findAll_ShouldReturnList() {
        var entity = new CIE11Diagnostico();
        entity.setId(1L);
        entity.setCodigo("1A00");
        entity.setDescripcion("Cólera");
        when(repository.findAllByOrderByFrecuenciaUsoDescCodigoAsc()).thenReturn(List.of(entity));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse() {
        var entity = new CIE11Diagnostico();
        entity.setId(1L);
        entity.setCodigo("1A00");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1L).codigo()).isEqualTo("1A00");
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldSaveAndReturn() {
        var request = new CIE11DiagnosticoRequest("1A00", "Cólera", "INFECCIOSA", "AMBOS", 0, 120, "CIE-11");
        when(repository.existsByCodigo("1A00")).thenReturn(false);
        var saved = new CIE11Diagnostico();
        saved.setId(1L);
        saved.setCodigo("1A00");
        saved.setDescripcion("Cólera");
        when(repository.save(any())).thenReturn(saved);
        assertThat(service.create(request).codigo()).isEqualTo("1A00");
    }

    @Test
    void create_ShouldThrowWhenDuplicate() {
        var request = new CIE11DiagnosticoRequest("1A00", "Cólera", "INFECCIOSA", "AMBOS", 0, 120, "CIE-11");
        when(repository.existsByCodigo("1A00")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void search_WithBlankQuery_ShouldReturnAll() {
        when(repository.findAllByOrderByFrecuenciaUsoDescCodigoAsc()).thenReturn(List.of());
        assertThat(service.search("")).isEmpty();
    }

    @Test
    void search_WithQuery_ShouldMergeResults() {
        var byCode = new CIE11Diagnostico();
        byCode.setId(1L);
        byCode.setCodigo("1A00");
        var byDesc = new CIE11Diagnostico();
        byDesc.setId(2L);
        byDesc.setCodigo("1A01");
        when(repository.findByCodigoStartingWithIgnoreCaseOrderByFrecuenciaUsoDesc("1A"))
            .thenReturn(List.of(byCode));
        when(repository.findByDescripcionContainingIgnoreCaseOrderByFrecuenciaUsoDesc("1A"))
            .thenReturn(List.of(byDesc));

        var result = service.search("1A");

        assertThat(result).hasSize(2);
    }
}

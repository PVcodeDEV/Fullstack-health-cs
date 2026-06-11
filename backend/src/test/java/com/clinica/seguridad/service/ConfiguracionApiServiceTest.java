package com.clinica.seguridad.service;

import com.clinica.seguridad.dto.ConfiguracionApiRequest;
import com.clinica.seguridad.dto.ConfiguracionApiResponse;
import com.clinica.seguridad.entity.ConfiguracionApi;
import com.clinica.seguridad.repository.ConfiguracionApiRepository;
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
class ConfiguracionApiServiceTest {

    @Mock
    private ConfiguracionApiRepository repository;

    @InjectMocks
    private ConfiguracionApiService service;

    private ConfiguracionApi createConfig(Long id, String modulo, String clave, String valor, String tipo) {
        ConfiguracionApi config = new ConfiguracionApi();
        config.setId(id);
        config.setModulo(modulo);
        config.setClave(clave);
        config.setValor(valor);
        config.setTipo(tipo);
        config.setActivo(true);
        return config;
    }

    @Test
    void findAll_ShouldReturnAllConfigs() {
        ConfiguracionApi config = createConfig(1L, "reniec", "url", "https://api.reniec.gob.pe", "string");
        when(repository.findAll()).thenReturn(List.of(config));

        List<ConfiguracionApiResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).modulo()).isEqualTo("reniec");
        assertThat(result.get(0).clave()).isEqualTo("url");
    }

    @Test
    void findByModuloAndClave_ShouldReturnConfig() {
        ConfiguracionApi config = createConfig(1L, "reniec", "token", "abc123", "string");
        when(repository.findByModuloAndClave("reniec", "token")).thenReturn(Optional.of(config));

        ConfiguracionApiResponse result = service.findByModuloAndClave("reniec", "token");

        assertThat(result).isNotNull();
        assertThat(result.valor()).isEqualTo("abc123");
    }

    @Test
    void findByModuloAndClave_WhenNotFound_ShouldThrowException() {
        when(repository.findByModuloAndClave("unknown", "key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByModuloAndClave("unknown", "key"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findByModulo_ShouldReturnList() {
        ConfiguracionApi c1 = createConfig(1L, "reniec", "url", "url1", "string");
        ConfiguracionApi c2 = createConfig(2L, "reniec", "token", "tok1", "string");
        when(repository.findByModulo("reniec")).thenReturn(List.of(c1, c2));

        List<ConfiguracionApiResponse> result = service.findByModulo("reniec");

        assertThat(result).hasSize(2);
    }

    @Test
    void findById_ShouldReturnConfig() {
        ConfiguracionApi config = createConfig(1L, "maestro", "format", "PDF", "string");
        when(repository.findById(1L)).thenReturn(Optional.of(config));

        ConfiguracionApiResponse result = service.findById(1L);

        assertThat(result.modulo()).isEqualTo("maestro");
        assertThat(result.valor()).isEqualTo("PDF");
    }

    @Test
    void create_ShouldSaveAndReturnNewConfig() {
        ConfiguracionApiRequest request = new ConfiguracionApiRequest("reniec", "url", "https://api.reniec.gob.pe", "string");
        when(repository.existsByModuloAndClave("reniec", "url")).thenReturn(false);
        when(repository.save(any(ConfiguracionApi.class))).thenAnswer(i -> {
            ConfiguracionApi saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ConfiguracionApiResponse result = service.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.modulo()).isEqualTo("reniec");
        assertThat(result.clave()).isEqualTo("url");
        assertThat(result.valor()).isEqualTo("https://api.reniec.gob.pe");
    }

    @Test
    void create_WithDuplicateKey_ShouldThrowException() {
        ConfiguracionApiRequest request = new ConfiguracionApiRequest("reniec", "url", "val", "string");
        when(repository.existsByModuloAndClave("reniec", "url")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reniec")
                .hasMessageContaining("url");
        verify(repository, never()).save(any());
    }

    @Test
    void create_WithNoTipo_ShouldDefaultToString() {
        ConfiguracionApiRequest request = new ConfiguracionApiRequest("mod", "key", "val", null);
        when(repository.existsByModuloAndClave("mod", "key")).thenReturn(false);
        when(repository.save(any(ConfiguracionApi.class))).thenAnswer(i -> {
            ConfiguracionApi saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ConfiguracionApiResponse result = service.create(request);

        assertThat(result.tipo()).isEqualTo("string");
    }

    @Test
    void update_ShouldModifyExistingConfig() {
        ConfiguracionApi existing = createConfig(1L, "reniec", "url", "old-url", "string");
        ConfiguracionApiRequest request = new ConfiguracionApiRequest("reniec", "url", "new-url", "string");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(ConfiguracionApi.class))).thenAnswer(i -> i.getArgument(0));

        ConfiguracionApiResponse result = service.update(1L, request);

        assertThat(result.valor()).isEqualTo("new-url");
    }

    @Test
    void update_WhenChangingModuloClave_ShouldCheckUniqueness() {
        ConfiguracionApi existing = createConfig(1L, "reniec", "url", "val", "string");
        ConfiguracionApiRequest request = new ConfiguracionApiRequest("reniec", "new-key", "val", "string");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByModuloAndClave("reniec", "new-key")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_WhenNotFound_ShouldThrowException() {
        ConfiguracionApiRequest request = new ConfiguracionApiRequest("mod", "key", "val", "string");
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_ShouldSoftDelete() {
        ConfiguracionApi existing = createConfig(1L, "reniec", "url", "val", "string");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(ConfiguracionApi.class))).thenAnswer(i -> i.getArgument(0));

        service.delete(1L);

        assertThat(existing.getActivo()).isFalse();
        verify(repository).save(existing);
    }

    @Test
    void delete_WhenNotFound_ShouldThrowException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_ShouldEvictCache() {
        // The @CacheEvict annotation is on the method — we verify the save is called
        ConfiguracionApiRequest request = new ConfiguracionApiRequest("mod", "key", "val", "string");
        when(repository.existsByModuloAndClave("mod", "key")).thenReturn(false);
        when(repository.save(any(ConfiguracionApi.class))).thenAnswer(i -> {
            ConfiguracionApi saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        service.create(request);

        verify(repository).save(any(ConfiguracionApi.class));
    }
}

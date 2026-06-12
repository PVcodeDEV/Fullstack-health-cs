package com.clinica.entidad.repository;

import com.clinica.entidad.entity.Empresa;
import com.clinica.entidad.entity.Empresa.Estado;
import com.clinica.entidad.entity.Empresa.Rol;
import com.clinica.entidad.entity.Empresa.TipoRuc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class EmpresaRepositoryTest {

    @Autowired
    private EmpresaRepository empresaRepository;

    private Empresa createEmpresa(String ruc, TipoRuc tipoRuc, String razonSocial, String direccion) {
        Empresa e = new Empresa();
        e.setRuc(ruc);
        e.setTipoRuc(tipoRuc);
        e.setRazonSocial(razonSocial);
        e.setDireccionFiscal(direccion);
        e.setEstado(Estado.ACTIVO);
        e.setRol(Rol.CLIENTE);
        e.setActivo(true);
        return e;
    }

    private Long empresa1Id;

    @BeforeEach
    void setUp() {
        Empresa e1 = createEmpresa("20123456789", TipoRuc.RUC_20, "CLINICA EJEMPLO SAC", "AV. PRINCIPAL 123");
        e1 = empresaRepository.save(e1);
        empresa1Id = e1.getId();

        Empresa e2 = createEmpresa("10123456780", TipoRuc.RUC_10, null, null);
        empresaRepository.save(e2);
    }

    @Test
    void shouldSaveAndFindById() {
        var found = empresaRepository.findById(empresa1Id);
        assertThat(found).isPresent();
        assertThat(found.get().getRuc()).isEqualTo("20123456789");
    }

    @Test
    void shouldFindByRuc() {
        var found = empresaRepository.findByRuc("20123456789");
        assertThat(found).isPresent();
        assertThat(found.get().getRazonSocial()).isEqualTo("CLINICA EJEMPLO SAC");
    }

    @Test
    void shouldRejectDuplicateRuc() {
        Empresa dup = createEmpresa("20123456789", TipoRuc.RUC_20, "DUPLICADO", "OTRA DIR");
        assertThatThrownBy(() -> empresaRepository.saveAndFlush(dup))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void defaultRolShouldBeCliente() {
        Empresa e = createEmpresa("20234567890", TipoRuc.RUC_20, "OTRA EMPRESA SAC", "AV. OTRA 456");
        e = empresaRepository.save(e);
        assertThat(e.getRol()).isEqualTo(Rol.CLIENTE);
    }

    @Test
    void defaultEstadoShouldBeActivo() {
        Empresa e = createEmpresa("20345678901", TipoRuc.RUC_20, "TERCERA EMPRESA SAC", "AV. TERCERA 789");
        e = empresaRepository.save(e);
        assertThat(e.getEstado()).isEqualTo(Estado.ACTIVO);
    }

    @Test
    void shouldSearchByRuc() {
        Page<Empresa> result = empresaRepository.search("20123456789", PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRazonSocial()).isEqualTo("CLINICA EJEMPLO SAC");
    }

    @Test
    void shouldSearchByRazonSocial() {
        Page<Empresa> result = empresaRepository.search("CLINICA", PageRequest.of(0, 10));
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    void shouldFilterByRol() {
        Page<Empresa> result = empresaRepository.findByFilters(
            Rol.CLIENTE, null, null, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldSoftDelete() {
        Empresa entity = empresaRepository.findById(1L).orElseThrow();
        entity.setEstado(Estado.INACTIVO);
        entity.markAsInactive();
        empresaRepository.save(entity);

        var found = empresaRepository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getEstado()).isEqualTo(Estado.INACTIVO);
        assertThat(found.get().getActivo()).isFalse();
    }
}

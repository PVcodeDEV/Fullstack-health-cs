package com.clinica.farmacia.producto.service;

import com.clinica.farmacia.lote.repository.LoteRepository;
import com.clinica.farmacia.producto.dto.ActualizarUtilidadRequest;
import com.clinica.farmacia.producto.dto.ProductoRequest;
import com.clinica.farmacia.producto.dto.ProductoResponse;
import com.clinica.farmacia.producto.entity.Producto;
import com.clinica.farmacia.producto.entity.Producto.TipoProducto;
import com.clinica.farmacia.producto.repository.ProductoRepository;
import com.clinica.maestro.entity.clinico.FormaFarmaceutica;
import com.clinica.seguridad.service.ConfiguracionApiService;
import com.clinica.maestro.entity.farmacia.FormaPresentacion;
import com.clinica.maestro.entity.farmacia.GrupoFarmacologico;
import com.clinica.maestro.entity.farmacia.Marca;
import com.clinica.maestro.entity.farmacia.TipoMedicamento;
import com.clinica.maestro.entity.financiero.UnidadMedida;
import com.clinica.maestro.entity.organizacion.CategoriaInsumo;
import com.clinica.maestro.repository.clinico.FormaFarmaceuticaRepository;
import com.clinica.maestro.repository.farmacia.FormaPresentacionRepository;
import com.clinica.maestro.repository.farmacia.GrupoFarmacologicoRepository;
import com.clinica.maestro.repository.farmacia.MarcaRepository;
import com.clinica.maestro.repository.farmacia.TipoMedicamentoRepository;
import com.clinica.maestro.repository.financiero.UnidadMedidaRepository;
import com.clinica.maestro.repository.organizacion.CategoriaInsumoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;
    private final PricingService pricingService;
    private final LoteRepository loteRepository;
    private final ConfiguracionApiService configuracionApiService;
    private final CategoriaInsumoRepository categoriaInsumoRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    private final TipoMedicamentoRepository tipoMedicamentoRepository;
    private final FormaFarmaceuticaRepository formaFarmaceuticaRepository;
    private final FormaPresentacionRepository formaPresentacionRepository;
    private final GrupoFarmacologicoRepository grupoFarmacologicoRepository;
    private final MarcaRepository marcaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           PricingService pricingService,
                           LoteRepository loteRepository,
                           ConfiguracionApiService configuracionApiService,
                           CategoriaInsumoRepository categoriaInsumoRepository,
                           UnidadMedidaRepository unidadMedidaRepository,
                           TipoMedicamentoRepository tipoMedicamentoRepository,
                           FormaFarmaceuticaRepository formaFarmaceuticaRepository,
                           FormaPresentacionRepository formaPresentacionRepository,
                           GrupoFarmacologicoRepository grupoFarmacologicoRepository,
                           MarcaRepository marcaRepository) {
        this.productoRepository = productoRepository;
        this.pricingService = pricingService;
        this.loteRepository = loteRepository;
        this.configuracionApiService = configuracionApiService;
        this.categoriaInsumoRepository = categoriaInsumoRepository;
        this.unidadMedidaRepository = unidadMedidaRepository;
        this.tipoMedicamentoRepository = tipoMedicamentoRepository;
        this.formaFarmaceuticaRepository = formaFarmaceuticaRepository;
        this.formaPresentacionRepository = formaPresentacionRepository;
        this.grupoFarmacologicoRepository = grupoFarmacologicoRepository;
        this.marcaRepository = marcaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> findAll() {
        return productoRepository.findAllByActivoTrueOrderByCodigo()
            .stream()
            .map(ProductoResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse findById(Long id) {
        return productoRepository.findById(id)
            .map(ProductoResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));
    }

    public ProductoResponse create(ProductoRequest request) {
        // Validate unique codigo
        if (productoRepository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException("Ya existe un producto con el código: " + request.codigo());
        }

        // Validate type-specific fields
        request.validateTypeFields();

        // Resolve tipo enum
        TipoProducto tipo = TipoProducto.valueOf(request.tipo().toUpperCase());

        // Resolve utility percentages
        BigDecimal utilidadMedico = request.utilidadMedico();
        BigDecimal utilidadPublico = request.utilidadPublico();

        // If not provided, calculate defaults
        if (utilidadMedico == null) {
            utilidadMedico = pricingService.calcularUtilidadDefault(request.precioCosto());
        }
        if (utilidadPublico == null) {
            utilidadPublico = pricingService.calcularUtilidadDefault(request.precioCosto());
        }

        // Calculate prices
        BigDecimal precioVentaMedico = pricingService.calcularPrecioVenta(request.precioCosto(), utilidadMedico);
        BigDecimal precioVentaPublico = pricingService.calcularPrecioVenta(request.precioCosto(), utilidadPublico);

        // Validate floor: precioVenta >= costo + IGV
        if (!pricingService.validarPrecioMinimo(precioVentaMedico, request.precioCosto())) {
            throw new IllegalArgumentException(
                "El precio de venta para médico no puede ser menor al costo más IGV");
        }
        if (!pricingService.validarPrecioMinimo(precioVentaPublico, request.precioCosto())) {
            throw new IllegalArgumentException(
                "El precio de venta para público no puede ser menor al costo más IGV");
        }

        // Build entity
        Producto entity = new Producto();
        entity.setCodigo(request.codigo());
        entity.setTipo(tipo);
        entity.setPrecioCosto(request.precioCosto());
        entity.setUtilidadMedico(utilidadMedico);
        entity.setUtilidadPublico(utilidadPublico);
        entity.setPrecioVentaMedico(precioVentaMedico);
        entity.setPrecioVentaPublico(precioVentaPublico);
        entity.setStockMinimo(request.stockMinimo() != null ? request.stockMinimo() : 0);
        entity.setStockCritico(request.stockCritico() != null ? request.stockCritico() : 0);

        // Resolve optional FKs (IDs are Integer in existing catalogs)
        if (request.categoriaInsumoId() != null) {
            entity.setCategoriaInsumo(categoriaInsumoRepository.findById(request.categoriaInsumoId().intValue())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Categoría de insumo no encontrada con id: " + request.categoriaInsumoId())));
        }
        if (request.unidadMedidaId() != null) {
            entity.setUnidadMedida(unidadMedidaRepository.findById(request.unidadMedidaId().intValue())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Unidad de medida no encontrada con id: " + request.unidadMedidaId())));
        }

        // Type-specific fields
        if (tipo == TipoProducto.MEDICAMENTO) {
            entity.setGenerico(request.generico());
            entity.setDescripcion(request.descripcion());
            entity.setOrigen(request.origen() != null ? request.origen() : false);
            entity.setTipoMedicamento(resolveFK(request.tipoMedicamentoId(), tipoMedicamentoRepository, "TipoMedicamento"));
            entity.setFormaFarmaceutica(resolveFK(request.formaFarmaceuticaId(), formaFarmaceuticaRepository, "FormaFarmaceutica"));
            entity.setFormaPresentacion(resolveFK(request.formaPresentacionId(), formaPresentacionRepository, "FormaPresentacion"));
            entity.setGrupoFarmacologico(resolveFK(request.grupoFarmacologicoId(), grupoFarmacologicoRepository, "GrupoFarmacologico"));
        } else {
            entity.setDescripcion(request.descripcion());
            entity.setMarca(resolveFK(request.marcaId(), marcaRepository, "Marca"));
        }

        entity = productoRepository.save(entity);
        log.debug("Producto created with id: {}, tipo: {}", entity.getId(), tipo);
        return ProductoResponse.fromEntity(entity);
    }

    public ProductoResponse softDelete(Long id) {
        Producto entity = productoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));

        boolean hasActiveStock = loteRepository.existsByProductoIdAndStockActualGreaterThan(id, 0);
        if (hasActiveStock) {
            throw new IllegalStateException("No se puede eliminar el producto porque tiene lotes con stock activo");
        }

        entity.markAsInactive();
        entity = productoRepository.save(entity);
        log.debug("Producto soft-deleted with id: {}", entity.getId());
        return ProductoResponse.fromEntity(entity);
    }

    @Transactional
    public ProductoResponse actualizarUtilidad(Long id, ActualizarUtilidadRequest request) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));

        // Read config: umbral_costo, utilidad_base, utilidad_alta_min
        BigDecimal umbralCosto;
        BigDecimal utilidadBase;
        BigDecimal utilidadAltaMin;
        try {
            umbralCosto = new BigDecimal(configuracionApiService.findByModuloAndClave("farmacia", "umbral_costo").valor());
        } catch (Exception e) {
            umbralCosto = new BigDecimal("90");
        }
        try {
            utilidadBase = new BigDecimal(configuracionApiService.findByModuloAndClave("farmacia", "utilidad_base").valor());
        } catch (Exception e) {
            utilidadBase = new BigDecimal("20");
        }
        try {
            utilidadAltaMin = new BigDecimal(configuracionApiService.findByModuloAndClave("farmacia", "utilidad_alta_min").valor());
        } catch (Exception e) {
            utilidadAltaMin = new BigDecimal("10");
        }

        // Determine minimum utilidad based on cost threshold
        boolean isHighCost = producto.getPrecioCosto().compareTo(umbralCosto) > 0;
        BigDecimal minUtilidadMedico = isHighCost ? utilidadAltaMin : utilidadBase;
        BigDecimal minUtilidadPublico = isHighCost ? utilidadAltaMin : utilidadBase;

        if (request.utilidadMedico().compareTo(minUtilidadMedico) < 0) {
            throw new IllegalArgumentException(
                "La utilidad médico no puede ser menor al mínimo calculado (" + minUtilidadMedico + "%)");
        }
        if (request.utilidadPublico().compareTo(minUtilidadPublico) < 0) {
            throw new IllegalArgumentException(
                "La utilidad público no puede ser menor al mínimo calculado (" + minUtilidadPublico + "%)");
        }

        producto.setUtilidadMedico(request.utilidadMedico());
        producto.setUtilidadPublico(request.utilidadPublico());

        // Recompute prices
        producto.setPrecioVentaMedico(
            pricingService.calcularPrecioVenta(producto.getPrecioCosto(), request.utilidadMedico()));
        producto.setPrecioVentaPublico(
            pricingService.calcularPrecioVenta(producto.getPrecioCosto(), request.utilidadPublico()));

        producto = productoRepository.save(producto);
        log.debug("Producto utilidad updated: id={}, utilidadMedico={}, utilidadPublico={}",
            producto.getId(), request.utilidadMedico(), request.utilidadPublico());

        return ProductoResponse.fromEntity(producto);
    }

    private <T> T resolveFK(Long id, org.springframework.data.jpa.repository.JpaRepository<T, Long> repo, String entityName) {
        if (id == null) return null;
        return repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(entityName + " no encontrado con id: " + id));
    }
}

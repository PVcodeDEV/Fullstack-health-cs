package com.clinica.maestro.entity.rrhh;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_afp_tasas_historicas")
@AttributeOverride(name = "createdAt", column = @Column(name = "ath_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "ath_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "ath_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AfpTasaHistorica extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ath_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ath_afp_id", nullable = false)
    private Afp afp;

    @Column(name = "ath_tipo_comision", length = 20)
    private String tipoComision;

    @Column(name = "ath_tasa", nullable = false, precision = 5, scale = 4)
    private BigDecimal tasa;

    @Column(name = "ath_prima_seguro", nullable = false, precision = 5, scale = 4)
    private BigDecimal primaSeguro;

    @Column(name = "ath_rentabilidad", precision = 5, scale = 4)
    private BigDecimal rentabilidad;

    @Column(name = "ath_vigencia_desde", nullable = false)
    private LocalDate vigenciaDesde;

    @Column(name = "ath_vigencia_hasta")
    private LocalDate vigenciaHasta;
}

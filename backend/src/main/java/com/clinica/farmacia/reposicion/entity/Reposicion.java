package com.clinica.farmacia.reposicion.entity;

import com.clinica.farmacia.reposicion.type.EstadoReposicion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lista de reposición de productos — planning view.
 * <p>
 * Generada por el químico para visualizar qué productos están
 * por debajo de su stock mínimo y necesitan reposición.
 * No es una orden de compra formal.
 * </p>
 */
@Entity
@Table(name = "tb_reposicion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reposicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rep_id")
    private Long id;

    @Column(name = "rep_generada_en", nullable = false)
    private LocalDateTime generadaEn = LocalDateTime.now();

    @Column(name = "rep_usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "rep_almacen_id", nullable = false)
    private Long almacenId;

    @Column(name = "rep_observaciones", length = 500)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "rep_estado", nullable = false, length = 16)
    private EstadoReposicion estado = EstadoReposicion.PENDIENTE;

    @Column(name = "rep_procesada_en")
    private LocalDateTime procesadaEn;

    @OneToMany(mappedBy = "reposicion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReposicionDetalle> detalles = new ArrayList<>();
}

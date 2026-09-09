package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Table(name = "item_produto")
@Entity(name = "ItemProduto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItemProdutoEntity {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private ItemProdutoIdEntity id;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(name = "cd_item")
    private ItemEntity item;

    @ManyToOne
    @MapsId("produtoId")
    @JoinColumn(name = "cd_produto")
    private ProdutoEntity produto;

    @ManyToOne
    @JoinColumn(name = "cd_unidade_para")
    private UnidadeMedidaEntity unidadePara;

    @Column(name = "quantidade")
    private Double quantidade;

    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;
}


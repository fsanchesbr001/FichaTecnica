package com.fabriciosanches.fichatecnica.domains;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
@Table(name = "item_produto")
@Entity(name = "ItemProduto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItemProduto {
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long codigo;

    @ManyToOne
    @JoinColumn(name = "cd_item")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "cd_produto")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "cd_unidade_de")
    private UnidadeMedida unidadePara;

    private BigDecimal quantidade;

    private BigDecimal valor;

    public ItemProduto(Item item, Produto produto, UnidadeMedida unidadePara,
                       BigDecimal quantidade, BigDecimal valor) {
        this.item = item;
        this.produto = produto;
        this.unidadePara = unidadePara;
        this.quantidade = quantidade;
        this.valor = valor;
    }

}

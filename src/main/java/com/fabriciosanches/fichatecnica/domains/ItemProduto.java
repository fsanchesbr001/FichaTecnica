package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.dtos.ItemProdutoDTO;
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

    @EmbeddedId
    private ItemProdutoId id;

    @ManyToOne
    @JoinColumn(name = "cd_item" , insertable = false, updatable = false)
    @MapsId("itemId")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "cd_produto" , insertable = false, updatable = false)
    @MapsId("produtoId")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "cd_unidade_para")
    private UnidadeMedida unidadePara;

    private Integer quantidade;

    private BigDecimal valor;

    public ItemProduto(ItemProdutoDTO itemProdutoDTO) {
        this.id = new ItemProdutoId(itemProdutoDTO.cdProduto(), itemProdutoDTO.cdItem());
        this.unidadePara = new UnidadeMedida(itemProdutoDTO.cdUnidadeMedida(), null, null);
        this.quantidade = itemProdutoDTO.qtdItem();
        this.valor = itemProdutoDTO.vlrItem();
    }
}

package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.serializers.BigDecimalCurrencySerializer;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UnidadeMedidaEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    private UnidadeMedidaEntity unidadePara;

    private Double quantidade;

    @JsonSerialize(using = BigDecimalCurrencySerializer.class)
    private BigDecimal valor;
}

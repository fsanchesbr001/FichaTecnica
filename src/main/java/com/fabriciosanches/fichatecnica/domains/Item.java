package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.dtos.ItemDTO;
import com.fabriciosanches.fichatecnica.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private  Long codigo;

    private String nome;

    @OneToOne(optional = false)
    @JoinColumn(name = "cd_unidade_medida")
    private UnidadeMedida unidadeMedida;

    @OneToMany(mappedBy = "item")
    private List<ItemProduto> itemProdutoList;

    @JsonSerialize(using = BigDecimalCurrencySerializer.class)
    private BigDecimal valor;

    public Item(ItemDTO itemDTO) {
        this.codigo = itemDTO.codigo();
        this.nome = itemDTO.nome();
        this.unidadeMedida = itemDTO.unidadeMedida();
        this.valor = itemDTO.valor();

    }
}

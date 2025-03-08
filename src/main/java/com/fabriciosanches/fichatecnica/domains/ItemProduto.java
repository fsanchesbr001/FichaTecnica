package com.fabriciosanches.fichatecnica.domains;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity(name = "ItemProduto")
@Table(name = "item_produto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItemProduto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long codigo;


    @Column(name = "cd_item")
    private Long cdItem;

    private Integer quantidade;

    @Column(name = "cd_unidade_para")
    private Long cdUnidadePara;

    private BigDecimal valor;

    @ManyToOne
    @JoinColumn(name = "cd_produto", insertable = false, updatable = false)
    @JsonBackReference
    private Produto produto;
}

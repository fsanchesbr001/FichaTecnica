package com.fabriciosanches.fichatecnica.domains;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Embeddable
public class ItemProdutoId implements Serializable {

    @Column(name = "cd_produto")
    @EqualsAndHashCode.Include
    private Long produtoId;

    @Column(name = "cd_item")
    @EqualsAndHashCode.Include
    private Long itemId;

}

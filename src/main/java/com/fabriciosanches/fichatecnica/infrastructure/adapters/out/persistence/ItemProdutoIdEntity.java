package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Embeddable
public class ItemProdutoIdEntity implements Serializable {

    @Column(name = "cd_produto")
    @EqualsAndHashCode.Include
    private Long produtoId;

    @Column(name = "cd_item")
    @EqualsAndHashCode.Include
    private Long itemId;
}


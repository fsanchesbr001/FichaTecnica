package com.fabriciosanches.fichatecnica.domains;


import com.fabriciosanches.fichatecnica.dtos.ItensProdutoDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item_produto")
@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItensProduto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long codigo;

    private Long cdProduto;

    private Long cdItem;

    private Integer quantidade;
    private BigDecimal valor;
    @OneToOne
    @JoinColumn(name = "cd_unidade_para")
    private UnidadeMedida unidadeMedida;


    @ManyToOne()
    @JoinColumn(name = "cd_produto")
    @JsonBackReference
    private Produto produto;

    public ItensProduto(ItensProdutoDTO itensProdutoDTO) {
        this.codigo = itensProdutoDTO.codigo();
        this.quantidade = itensProdutoDTO.quantidade();
        this.cdProduto = itensProdutoDTO.codProduto();
        this.cdItem = itensProdutoDTO.codigoItem();
        this.valor = itensProdutoDTO.valor();
        this.unidadeMedida = itensProdutoDTO.unidadeMedida();
    }
}

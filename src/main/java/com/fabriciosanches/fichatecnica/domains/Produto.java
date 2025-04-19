package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fabriciosanches.fichatecnica.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;


@Table(name = "produto")
@Entity(name = "Produto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Produto {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Id
    private Long codigo;

    private String nome;
    private String descricao;
    private String imagem;

    @JsonSerialize(using = BigDecimalCurrencySerializer.class)
    private BigDecimal valorVenda;

    @JsonSerialize(using = BigDecimalCurrencySerializer.class)
    private BigDecimal valorItens;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemProduto> produtosList;

    public Produto(ProdutoDTO produtoDTO) {
        this.codigo = produtoDTO.codigo();
        this.nome = produtoDTO.nome();
        this.descricao = produtoDTO.descricao();
        this.imagem = produtoDTO.imagem();
        this.valorVenda = produtoDTO.valorVenda();
        this.valorItens = produtoDTO.valorItens();
    }
}

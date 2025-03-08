package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    private BigDecimal valorVenda;
    private BigDecimal valorItens;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemProduto> itensProduto;

    public Produto(ProdutoDTO produto) {
        this(produto.codigo(), produto.nome(), produto.descricao(), produto.imagem(), produto.valorVenda(),produto.valorItens(),produto.itensProduto());
    }

    /*public Produto(Long codigo, String nome, String descricao, String imagem, BigDecimal valorVenda, BigDecimal valorItens, List<ItemProduto> itensProduto) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.imagem = imagem;
        this.valorVenda = valorVenda;
        this.valorItens = valorItens;
        this.itensProduto = itensProduto;
    }*/
}

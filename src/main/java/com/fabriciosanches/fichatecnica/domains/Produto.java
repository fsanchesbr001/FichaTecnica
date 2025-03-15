package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
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
    private List<ItemProduto> itens;

    public Produto(ProdutoDTO produto) {
        this(produto.codigo(), produto.nome(), produto.descricao(), produto.imagem(), produto.valorVenda(),produto.valorItens(),produto.itensProduto());
    }
}

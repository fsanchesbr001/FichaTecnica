package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Table(name = "produto")
@Entity
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
    private BigDecimal valor;


    public Produto(ProdutoDTO produtoDTO) {
        this.codigo = produtoDTO.codigo();
        this.nome = produtoDTO.nome();
        this.descricao = produtoDTO.descricao();
        this.imagem = produtoDTO.imagem();
        this.valor = produtoDTO.valor();
    }
}

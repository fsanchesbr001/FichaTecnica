package com.fabriciosanches.fichatecnica.domain.produto;

import com.fabriciosanches.fichatecnica.domain.itensProduto.ItensProduto;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;


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

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItensProduto> itens;

    public Produto(DadosProduto dadosProduto) {
        this.codigo = dadosProduto.codigo();
        this.nome = dadosProduto.nome();
        this.descricao = dadosProduto.descricao();
        this.imagem = dadosProduto.imagem();
        this.valor = dadosProduto.valor();
    }
}

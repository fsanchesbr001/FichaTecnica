package com.fabriciosanches.fichatecnica.domain.itensProduto;


import com.fabriciosanches.fichatecnica.domain.medidas.UnidadeMedida;
import com.fabriciosanches.fichatecnica.domain.produto.Produto;
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
    private Long codigoProduto;
    private Long codigoItem;
    private Integer quantidade;
    private BigDecimal valor;
    @OneToOne
    @JoinColumn(name = "cd_unidade_para")
    private UnidadeMedida unidadeMedida;


    @ManyToOne()
    @JoinColumn(name = "cd_produto")
    @JsonBackReference
    private Produto produto;

    public ItensProduto(DadosItensProduto dadosItensProduto) {
        this.codigo = dadosItensProduto.codigo();
        this.quantidade = dadosItensProduto.quantidade();
        this.valor = dadosItensProduto.valor();
        this.produto = dadosItensProduto.produto();
        this.unidadeMedida = dadosItensProduto.unidadeMedida();
        this.codigoProduto = dadosItensProduto.codProduto();
    }
}

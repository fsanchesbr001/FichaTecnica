package com.fabriciosanches.fichatecnica.domain.produto;



import com.fabriciosanches.fichatecnica.domain.itensProduto.ItensProduto;

import java.math.BigDecimal;
import java.util.List;

public record DadosProduto(Long codigo, String nome, String descricao, String imagem, BigDecimal valor, List<ItensProduto> itens) {
    public DadosProduto(Produto produto) {
        this(produto.getCodigo(), produto.getNome(), produto.getDescricao(), produto.getImagem(), produto.getValor(), produto.getItens());
    }

    public static List<DadosProduto> from(List<Produto> lista) {
        return lista.stream().map(DadosProduto::new).toList();
    }
}

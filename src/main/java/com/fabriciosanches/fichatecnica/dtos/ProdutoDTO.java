package com.fabriciosanches.fichatecnica.dtos;



import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.domains.Produto;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoDTO(Long codigo, String nome, String descricao, String imagem, BigDecimal valorVenda, BigDecimal valorItens
        ,List<ItemProduto> itensProduto) {
    public ProdutoDTO(Produto produto) {
        this(produto.getCodigo(), produto.getNome(), produto.getDescricao(), produto.getImagem(), produto.getValorVenda(),produto.getValorItens(), produto.getItensProduto());
    }

    public static List<ProdutoDTO> from(List<Produto> lista) {
        return lista.stream().map(ProdutoDTO::new).toList();
    }
}

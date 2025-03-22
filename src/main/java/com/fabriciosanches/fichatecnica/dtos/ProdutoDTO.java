package com.fabriciosanches.fichatecnica.dtos;



import com.fabriciosanches.fichatecnica.domains.Produto;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoDTO(Long codigo, String nome, String descricao, String imagem, BigDecimal valorVenda,
                         BigDecimal valorItens) {
    public ProdutoDTO {
        if (valorItens == null) {
            valorItens = BigDecimal.ZERO;
        }
    }


    public static List<ProdutoDTO> from(List<Produto> lista) {
        return lista.stream().map(ls-> new ProdutoDTO(
                ls.getCodigo(), ls.getNome(), ls.getDescricao(), ls.getImagem(),
                ls.getValorVenda(), ls.getValorItens())).toList();

    }

    public static ProdutoDTO from(Produto produto) {
        return new ProdutoDTO(produto.getCodigo(), produto.getNome(),
                produto.getDescricao(), produto.getImagem(), produto.getValorVenda(),
                produto.getValorItens());
    }
}

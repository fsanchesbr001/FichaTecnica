package com.fabriciosanches.fichatecnica.dtos;



import com.fabriciosanches.fichatecnica.domains.ItensProduto;
import com.fabriciosanches.fichatecnica.domains.Produto;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoDTO(Long codigo, String nome, String descricao, String imagem, BigDecimal valor, List<ItensProduto> itens) {
    public ProdutoDTO(Produto produto) {
        this(produto.getCodigo(), produto.getNome(), produto.getDescricao(), produto.getImagem(), produto.getValor(), produto.getItens());
    }

    public static List<ProdutoDTO> from(List<Produto> lista) {
        return lista.stream().map(ProdutoDTO::new).toList();
    }
}

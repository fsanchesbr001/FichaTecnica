package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.domains.ItemProduto;

import java.util.ArrayList;
import java.util.List;

public record ItemProdutoDTO(Long codigo, Long cdItem, Integer quantidade, Long cdUnidadePara, Double valor,
                             ProdutoDTO produto) {


    public static List<ItemProdutoDTO> from(List<ItemProduto> itensProduto) {
        List<ItemProdutoDTO> lista = new ArrayList<>();
        for (ItemProduto itemProduto : itensProduto) {
            lista.add(new ItemProdutoDTO(itemProduto.getCodigo(), itemProduto.getCdItem(), itemProduto.getQuantidade(),
                    itemProduto.getCdUnidadePara(), itemProduto.getValor().doubleValue(), new ProdutoDTO(itemProduto.getProduto())));
        }
        return lista;
    }
}

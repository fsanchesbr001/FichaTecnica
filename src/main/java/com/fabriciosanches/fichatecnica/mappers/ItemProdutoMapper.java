package com.fabriciosanches.fichatecnica.mappers;

import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoResponseDTO;

public class ItemProdutoMapper {
    public static ItemProdutoResponseDTO toResponseDTO(ItemProduto itemProduto) {
        return new ItemProdutoResponseDTO(itemProduto.getCodigo(), itemProduto.getCdProduto(),
                itemProduto.getCdItem(), itemProduto.getQuantidade(), itemProduto.getCdUnidadePara(),
                itemProduto.getValor());
    }

    public static ItemProduto toEntity(ItemProdutoRequestDTO itemProdutoRequestDTO) {
        return new ItemProduto(null, itemProdutoRequestDTO.cdProduto(), itemProdutoRequestDTO.cdItem(),
                itemProdutoRequestDTO.quantidade(), itemProdutoRequestDTO.cdUnidadePara(),
                itemProdutoRequestDTO.valor(), itemProdutoRequestDTO.produto());
    }

}

package com.fabriciosanches.fichatecnica.mappers;

import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ItemProdutoMapper {
    ItemProdutoMapper INSTANCE = Mappers.getMapper(ItemProdutoMapper.class);

    ItemProdutoDTO toDTO(ItemProduto itemProduto);
    ItemProduto toEntity(ItemProdutoDTO itemProdutoDTO);
}

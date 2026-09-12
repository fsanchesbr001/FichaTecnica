package com.fabriciosanches.fichatecnica.core.ports.out;

import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.ItemProdutoId;

import java.util.List;
import java.util.Optional;

public interface ItemProdutoRepositoryPort {
    ItemProduto salvar(ItemProduto itemProduto);

    List<ItemProduto> buscarTodos();

    Optional<ItemProduto> buscarPorId(ItemProdutoId id);

    List<ItemProduto> buscarPorProdutoId(Long produtoId);

    List<ItemProduto> buscarPorItemId(Long itemId);

    Optional<ItemProduto> buscarPorProdutoIdEItemId(Long produtoId, Long itemId);

    void deletarPorProdutoIdEItemId(Long produtoId, Long itemId);
}


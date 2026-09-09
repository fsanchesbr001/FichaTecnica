package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;

import java.util.List;

public interface ListarItensDoProdutoPort {
    List<ItemProduto> listar(Long produtoId);
}


package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;

public interface AtualizarProdutoPort {
    ProdutoDTO atualizarProduto(Long id, ProdutoDTO produto);
}

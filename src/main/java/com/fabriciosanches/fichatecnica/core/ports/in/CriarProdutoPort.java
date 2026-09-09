package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;

public interface CriarProdutoPort {
    ProdutoDTO cadastrarProduto(ProdutoDTO produtoDTO);
}

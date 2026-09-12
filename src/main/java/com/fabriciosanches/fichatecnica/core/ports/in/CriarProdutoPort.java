package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutoDTO;

public interface CriarProdutoPort {
    ProdutoDTO cadastrarProduto(ProdutoDTO produtoDTO);
}


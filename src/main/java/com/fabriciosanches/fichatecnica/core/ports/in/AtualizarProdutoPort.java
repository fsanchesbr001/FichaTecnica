package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutoDTO;

public interface AtualizarProdutoPort {
    ProdutoDTO atualizarProduto(Long id, ProdutoDTO produto);
}


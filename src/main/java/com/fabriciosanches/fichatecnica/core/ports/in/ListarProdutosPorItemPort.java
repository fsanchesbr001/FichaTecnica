package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutosPorItemDTO;

import java.util.List;

public interface ListarProdutosPorItemPort {
    List<ProdutosPorItemDTO> listarPorItem(Long codigoItem);
}


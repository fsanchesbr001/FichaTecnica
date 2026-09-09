package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;

import java.util.List;

public interface BuscarProdutoPort {
    List<ProdutoDTO> listar();

    ProdutoDTO buscarPorId(Long id);
}

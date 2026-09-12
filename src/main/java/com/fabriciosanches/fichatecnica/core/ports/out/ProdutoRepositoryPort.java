package com.fabriciosanches.fichatecnica.core.ports.out;

import com.fabriciosanches.fichatecnica.core.domain.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepositoryPort {
    Produto salvar(Produto produto);

    List<Produto> buscarTodos();

    Optional<Produto> buscarPorId(Long id);

    long contarPorNome(String nome);

    void deletarPorId(Long id);
}



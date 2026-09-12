package com.fabriciosanches.fichatecnica.core.ports.in;

public interface AtualizarQuantidadeItemDoProdutoPort {
    void atualizarQuantidade(Long produtoId, Long itemId, Double novaQuantidade);
}



package com.fabriciosanches.fichatecnica.core.ports.out;

import com.fabriciosanches.fichatecnica.core.domain.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepositoryPort {
    Item salvar(Item item);

    List<Item> buscarTodos();

    Optional<Item> buscarPorId(Long id);

    long contarPorNome(String nome);

    void deletarPorId(Long id);
}


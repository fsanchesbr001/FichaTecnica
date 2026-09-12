package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BuscarItemUseCase implements BuscarItemPort {
    private final ItemRepositoryPort repositoryPort;

    public BuscarItemUseCase(ItemRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port nÃ£o pode ser nulo");
    }

    @Override
    public List<Item> listar() {
        return repositoryPort.buscarTodos().stream()
                .sorted(Comparator.comparing(Item::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public Item buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id nÃ£o pode ser nulo");
        }

        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new FichaTecnicaException("Item com ID " + id + " nÃ£o encontrado"));
    }
}



package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BuscarConversaoUseCase implements BuscarConversaoPort {
    private final ConversaoRepositoryPort repositoryPort;

    public BuscarConversaoUseCase(ConversaoRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
    }

    @Override
    public List<Conversao> buscarTodos() {
        return repositoryPort.buscarTodos().stream()
                .sorted(Comparator.comparing(Conversao::getUnidadeDe, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    @Override
    public Conversao buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id não pode ser nulo");
        }

        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Conversão com ID " + id + " não encontrada"));
    }
}

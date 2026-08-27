package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;
import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;

import java.util.List;
import java.util.Objects;

public class GerarRelatorioConversaoUseCase implements GerarRelatorioConversaoPort {
    private final ConversaoRepositoryPort repositoryPort;

    public GerarRelatorioConversaoUseCase(ConversaoRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
    }

    @Override
    public List<ConversaoRelatorioDTO> buscarTodosComNomes() {
        return repositoryPort.buscarTodosComNomes();
    }

    @Override
    public ConversaoRelatorioDTO buscarPorIdComNomes(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id não pode ser nulo");
        }

        return repositoryPort.buscarPorIdComNomes(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Conversão com ID " + id + " não encontrada"));
    }
}

package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.ports.in.DeletarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;

import java.util.Objects;

public class DeletarConversaoUseCase implements DeletarConversaoPort {
    private final ConversaoRepositoryPort repositoryPort;

    public DeletarConversaoUseCase(ConversaoRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
    }

    @Override
    public void deletar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id não pode ser nulo");
        }

        repositoryPort.deletar(id);
    }
}

package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;

import java.util.Objects;

public class AtualizarConversaoUseCase implements AtualizarConversaoPort {
    private final ConversaoRepositoryPort repositoryPort;

    public AtualizarConversaoUseCase(ConversaoRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
    }

    @Override
    public Conversao atualizar(Long id, Conversao conversao) {
        if (id == null) {
            throw new IllegalArgumentException("Id não pode ser nulo");
        }
        Objects.requireNonNull(conversao, "Conversão não pode ser nula");
        Objects.requireNonNull(conversao.getUnidadeDe(), "UnidadeDe não pode ser nulo");
        Objects.requireNonNull(conversao.getUnidadePara(), "UnidadePara não pode ser nulo");
        Objects.requireNonNull(conversao.getOperacao(), "Operação não pode ser nula");
        Objects.requireNonNull(conversao.getValor(), "Valor não pode ser nulo");

        repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Conversão com ID " + id + " não encontrada"));

        Conversao conversaoAtualizada = new Conversao(
                id,
                conversao.getUnidadeDe(),
                conversao.getUnidadePara(),
                conversao.getOperacao(),
                conversao.getValor()
        );
        return repositoryPort.salvar(conversaoAtualizada);
    }
}

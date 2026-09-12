package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;

import java.util.Objects;

public class AtualizarConversaoUseCase implements AtualizarConversaoPort {
    private final ConversaoRepositoryPort repositoryPort;

    public AtualizarConversaoUseCase(ConversaoRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port nÃ£o pode ser nulo");
    }

    @Override
    public Conversao atualizar(Long id, Conversao conversao) {
        if (id == null) {
            throw new IllegalArgumentException("Id nÃ£o pode ser nulo");
        }
        Objects.requireNonNull(conversao, "ConversÃ£o nÃ£o pode ser nula");
        Objects.requireNonNull(conversao.getUnidadeDe(), "UnidadeDe nÃ£o pode ser nulo");
        Objects.requireNonNull(conversao.getUnidadePara(), "UnidadePara nÃ£o pode ser nulo");
        Objects.requireNonNull(conversao.getOperacao(), "OperaÃ§Ã£o nÃ£o pode ser nula");
        Objects.requireNonNull(conversao.getValor(), "Valor nÃ£o pode ser nulo");

        repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("ConversÃ£o com ID " + id + " nÃ£o encontrada"));

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


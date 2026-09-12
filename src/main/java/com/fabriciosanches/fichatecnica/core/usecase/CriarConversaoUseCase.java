package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;

import java.util.Objects;

public class CriarConversaoUseCase implements CriarConversaoPort {
    private final ConversaoRepositoryPort repositoryPort;

    public CriarConversaoUseCase(ConversaoRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port nÃ£o pode ser nulo");
    }

    @Override
    public Conversao criar(Conversao conversao) {
        Objects.requireNonNull(conversao, "ConversÃ£o nÃ£o pode ser nula");
        Objects.requireNonNull(conversao.getUnidadeDe(), "UnidadeDe nÃ£o pode ser nulo");
        Objects.requireNonNull(conversao.getUnidadePara(), "UnidadePara nÃ£o pode ser nulo");
        Objects.requireNonNull(conversao.getOperacao(), "OperaÃ§Ã£o nÃ£o pode ser nula");
        Objects.requireNonNull(conversao.getValor(), "Valor nÃ£o pode ser nulo");

        if (repositoryPort.contarPorUnidadeDeEUnidadePara(conversao.getUnidadeDe(), conversao.getUnidadePara()) > 0) {
            throw new IllegalArgumentException("ConversÃ£o jÃ¡ cadastrada");
        }

        return repositoryPort.salvar(conversao);
    }
}


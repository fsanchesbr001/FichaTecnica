package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;

import java.util.Objects;

public class CriarConversaoUseCase implements CriarConversaoPort {
    private final ConversaoRepositoryPort repositoryPort;

    public CriarConversaoUseCase(ConversaoRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
    }

    @Override
    public Conversao criar(Conversao conversao) {
        Objects.requireNonNull(conversao, "Conversão não pode ser nula");
        Objects.requireNonNull(conversao.getUnidadeDe(), "UnidadeDe não pode ser nulo");
        Objects.requireNonNull(conversao.getUnidadePara(), "UnidadePara não pode ser nulo");
        Objects.requireNonNull(conversao.getOperacao(), "Operação não pode ser nula");
        Objects.requireNonNull(conversao.getValor(), "Valor não pode ser nulo");

        if (repositoryPort.contarPorUnidadeDeEUnidadePara(conversao.getUnidadeDe(), conversao.getUnidadePara()) > 0) {
            throw new IllegalArgumentException("Conversão já cadastrada");
        }

        return repositoryPort.salvar(conversao);
    }
}

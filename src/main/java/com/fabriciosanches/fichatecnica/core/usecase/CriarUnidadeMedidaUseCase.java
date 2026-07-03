package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;

import java.util.Objects;

public class CriarUnidadeMedidaUseCase implements CriarUnidadeMedidaPort {
    private final UnidadeMedidaRepositoryPort repositoryPort;

    public CriarUnidadeMedidaUseCase(UnidadeMedidaRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
    }

    @Override
    public UnidadeMedida criar(String nome, String sigla) {
        String nomeNormalizado = normalizar(nome, "Nome não pode ser vazio");
        String siglaNormalizada = normalizar(sigla, "Sigla não pode ser vazia").toUpperCase();

        repositoryPort.buscarPorSigla(siglaNormalizada)
                .ifPresent(unidadeExistente -> {
                    throw new IllegalArgumentException("Já existe uma unidade de medida com a sigla '" + siglaNormalizada + "'.");
                });

        UnidadeMedida novaUnidade = new UnidadeMedida(nomeNormalizado, siglaNormalizada);
        return repositoryPort.salvar(novaUnidade);
    }

    private String normalizar(String valor, String mensagemErro) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagemErro);
        }
        return valor.trim();
    }
}

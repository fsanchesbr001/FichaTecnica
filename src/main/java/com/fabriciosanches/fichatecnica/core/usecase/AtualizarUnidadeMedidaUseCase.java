package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;

import java.util.Objects;

public class AtualizarUnidadeMedidaUseCase implements AtualizarUnidadeMedidaPort {
    private final UnidadeMedidaRepositoryPort repositoryPort;

    public AtualizarUnidadeMedidaUseCase(UnidadeMedidaRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
    }

    @Override
    public UnidadeMedida atualizar(Long id, String nome, String sigla) {
        if (id == null) {
            throw new IllegalArgumentException("Id não pode ser nulo");
        }

        String nomeNormalizado = normalizar(nome, "Nome não pode ser vazio");
        String siglaNormalizada = normalizar(sigla, "Sigla não pode ser vazia").toUpperCase();

        repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Unidade de medida não encontrada"));

        repositoryPort.buscarPorSigla(siglaNormalizada)
                .filter(unidade -> !id.equals(unidade.getCodigo()))
                .ifPresent(unidadeExistente -> {
                    throw new IllegalArgumentException("Já existe uma unidade de medida com a sigla '" + siglaNormalizada + "'.");
                });

        UnidadeMedida unidadeAtualizada = new UnidadeMedida(id, nomeNormalizado, siglaNormalizada);
        return repositoryPort.salvar(unidadeAtualizada);
    }

    private String normalizar(String valor, String mensagemErro) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagemErro);
        }
        return valor.trim();
    }
}

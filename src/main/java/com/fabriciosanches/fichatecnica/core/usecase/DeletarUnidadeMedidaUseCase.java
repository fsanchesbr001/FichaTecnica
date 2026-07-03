package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.ports.in.DeletarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;

import java.util.Objects;

public class DeletarUnidadeMedidaUseCase implements DeletarUnidadeMedidaPort {
    private final UnidadeMedidaRepositoryPort repositoryPort;

    public DeletarUnidadeMedidaUseCase(UnidadeMedidaRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
    }

    @Override
    public void deletar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id não pode ser nulo");
        }

        repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Unidade de medida não encontrada"));

        repositoryPort.deletar(id);
    }
}
